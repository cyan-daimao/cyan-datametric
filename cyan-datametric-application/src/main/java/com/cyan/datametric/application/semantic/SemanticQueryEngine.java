package com.cyan.datametric.application.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.datametric.application.semantic.JoinPathResolver.JoinEdge;
import com.cyan.datametric.application.semantic.QueryRouter.RouteDecision;
import com.cyan.datametric.application.semantic.SemanticSqlBuilder.DimensionRef;
import com.cyan.datametric.application.semantic.SemanticSqlBuilder.FilterRef;
import com.cyan.datametric.application.semantic.SemanticSqlBuilder.OrderRef;
import com.cyan.datametric.domain.semantic.LogicalTable;
import com.cyan.datametric.domain.semantic.MaterializedView;
import com.cyan.datametric.domain.semantic.QueryPlan;
import com.cyan.datametric.domain.semantic.SemanticMetric;
import com.cyan.datametric.domain.semantic.repository.*;
import com.cyan.datametric.enums.semantic.RouteType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 语义查询引擎（Phase 3 核心入口）
 * <p>
 * 职责：
 * 1. 接收指标编码列表 + 维度列表 + 过滤条件
 * 2. 解析语义指标和逻辑表
 * 3. 调用 QueryRouter 判断物化视图命中
 * 4. 未命中时，调用 JoinPathResolver 计算 JOIN 路径，SemanticSqlBuilder 生成实时 SQL
 * 5. 通过 SqlGatewayClient 下发执行
 * 6. 记录 QueryPlan 用于后续分析
 * <p>
 * 兼容 Phase 1/2：当语义模型未覆盖时，可降级到旧版 BiAnalysisServiceImpl。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticQueryEngine {

    private final SemanticMetricRepository semanticMetricRepository;
    private final LogicalTableRepository logicalTableRepository;
    private final TableRelationshipRepository tableRelationshipRepository;
    private final MaterializedViewRepository materializedViewRepository;
    private final QueryPlanRepository queryPlanRepository;
    private final JoinPathResolver joinPathResolver;
    private final SemanticSqlBuilder semanticSqlBuilder;
    private final QueryRouter queryRouter;
    private final MaterializedViewService materializedViewService;
    private final SqlGatewayClient sqlGatewayClient;

    /**
     * 执行语义查询
     *
     * @param cmd    查询命令
     * @param executor 执行人
     * @return 查询结果
     */
    public SemanticQueryResult execute(SemanticQueryCmd cmd, String executor) {
        long startTime = System.currentTimeMillis();
        String queryHash = computeQueryHash(cmd);

        // 1. 解析语义指标
        List<SemanticMetric> metrics = resolveMetrics(cmd.getMetricCodes());

        // 2. 解析维度
        List<DimensionRef> dimensions = cmd.getDimensions();

        // 3. 尝试物化视图路由
        List<MaterializedView> activeMvs = materializedViewService.listActive();
        List<String> dimKeys = dimensions.stream()
                .map(d -> d.getTableId() + "." + d.getColumnName())
                .toList();
        RouteDecision route = queryRouter.route(
                cmd.getMetricCodes(), dimKeys, activeMvs);

        String finalSql;
        RouteType routeType;
        String hitMvId = null;

        if (route.hit()) {
            // 命中物化视图
            finalSql = route.rewrittenSql();
            routeType = RouteType.MATERIALIZED;
            hitMvId = route.mvId();
            if (hitMvId != null) {
                MaterializedView mv = materializedViewRepository.findById(hitMvId);
                if (mv != null) {
                    mv.recordHit(materializedViewRepository);
                }
            }
        } else {
            // 4. 未命中物化视图：计算 JOIN 路径并生成实时 SQL
            finalSql = buildRealtimeSql(metrics, dimensions, cmd);
            routeType = RouteType.REALTIME;
        }

        // 5. 执行 SQL
        long execStart = System.currentTimeMillis();
        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(finalSql)
                .setPassport(executor);
        com.cyan.arch.common.api.Response<SqlExecuteResultDTO> response =
                sqlGatewayClient.executeStarRocksSql(executeCmd);
        long execCost = System.currentTimeMillis() - execStart;

        // 6. 记录查询计划
        QueryPlan plan = new QueryPlan();
        plan.setQueryHash(queryHash);
        plan.setQuerySql(finalSql);
        plan.setRouteType(routeType);
        plan.setMvId(hitMvId);
        plan.setCostTimeMs(execCost);
        plan.setHitCache(false);
        plan.setCreatedAt(LocalDateTime.now());
        queryPlanRepository.save(plan);

        // 7. 组装结果
        SemanticQueryResult result = new SemanticQueryResult();
        result.setSql(finalSql);
        result.setRouteType(routeType.getCode());
        result.setCostTimeMs(System.currentTimeMillis() - startTime);

        if (response == null || response.getCode() != 200 || response.getData() == null) {
            result.setStatus("FAILED");
            result.setErrorMessage(response != null ? response.getMessage() : "执行结果为空");
            return result;
        }

        SqlExecuteResultDTO data = response.getData();
        result.setStatus(data.getStatus());
        result.setErrorMessage(data.getErrorMessage());
        if (data.getData() != null && !data.getData().isEmpty()) {
            result.setRows(data.getData());
            result.setColumns(new ArrayList<>(data.getData().get(0).keySet()));
        } else {
            result.setColumns(new ArrayList<>());
            result.setRows(new ArrayList<>());
        }
        return result;
    }

    /**
     * SQL 预览（不执行）
     */
    public String previewSql(SemanticQueryCmd cmd) {
        List<SemanticMetric> metrics = resolveMetrics(cmd.getMetricCodes());
        return buildRealtimeSql(metrics, cmd.getDimensions(), cmd);
    }

    /**
     * 构建实时计算 SQL
     */
    private String buildRealtimeSql(List<SemanticMetric> metrics,
                                    List<DimensionRef> dimensions,
                                    SemanticQueryCmd cmd) {
        // 收集涉及的所有逻辑表
        Set<String> tableIds = new HashSet<>();
        for (SemanticMetric metric : metrics) {
            tableIds.add(metric.getSourceTableId());
        }
        for (DimensionRef dim : dimensions) {
            tableIds.add(dim.getTableId());
        }

        Map<String, LogicalTable> tableMap = tableIds.stream()
                .map(logicalTableRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LogicalTable::getId, t -> t));

        // 分离事实表和维度表
        List<LogicalTable> factTables = new ArrayList<>();
        List<LogicalTable> dimTables = new ArrayList<>();
        for (LogicalTable table : tableMap.values()) {
            if (table.isFactTable()) {
                factTables.add(table);
            } else {
                dimTables.add(table);
            }
        }

        // 加载全量关联关系（实际可优化为只加载涉及表的关联）
        List<com.cyan.datametric.domain.semantic.TableRelationship> relationships =
                tableRelationshipRepository.findAll();

        // 计算 JOIN 路径
        List<JoinEdge> joinEdges = joinPathResolver.resolve(factTables, dimTables, relationships);

        // 生成 SQL
        return semanticSqlBuilder.build(
                metrics,
                dimensions,
                joinEdges,
                tableMap,
                cmd.getFilters(),
                cmd.getOrders(),
                cmd.getLimit()
        );
    }

    /**
     * 解析语义指标列表
     */
    private List<SemanticMetric> resolveMetrics(List<String> metricCodes) {
        Assert.notEmpty(metricCodes, new BusinessException("指标编码列表不能为空"));
        List<SemanticMetric> metrics = semanticMetricRepository.findByMetricCodes(metricCodes);
        if (metrics.size() != metricCodes.size()) {
            Set<String> found = metrics.stream().map(SemanticMetric::getMetricCode).collect(Collectors.toSet());
            List<String> missing = metricCodes.stream().filter(c -> !found.contains(c)).toList();
            throw new BusinessException("以下指标未找到语义定义: " + String.join(", ", missing));
        }
        return metrics;
    }

    /**
     * 计算查询特征哈希
     */
    private String computeQueryHash(SemanticQueryCmd cmd) {
        String raw = String.join(",", cmd.getMetricCodes()) + "|" +
                cmd.getDimensions().stream()
                        .map(d -> d.getTableId() + "." + d.getColumnName())
                        .sorted()
                        .collect(Collectors.joining(","));
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(raw.hashCode());
        }
    }

    // ==================== 查询命令与结果 ====================

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class SemanticQueryCmd {
        private List<String> metricCodes;
        private List<DimensionRef> dimensions;
        private List<FilterRef> filters;
        private List<OrderRef> orders;
        private Integer limit;
    }

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class SemanticQueryResult {
        private String status;
        private String sql;
        private String routeType;
        private Long costTimeMs;
        private String errorMessage;
        private List<String> columns;
        private List<Map<String, Object>> rows;
    }
}
