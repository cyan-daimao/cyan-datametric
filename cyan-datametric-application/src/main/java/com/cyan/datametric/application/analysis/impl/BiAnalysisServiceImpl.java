package com.cyan.datametric.application.analysis.impl;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.analysis.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.adapter.analysis.http.dto.MetricBiChartDataDTO;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.infra.client.DatagatewayClient;
import com.cyan.datametric.infra.client.dto.SqlExecuteCmd;
import com.cyan.datametric.infra.client.dto.SqlExecuteResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标 BI 分析服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class BiAnalysisServiceImpl implements BiAnalysisService {

    private final MetricRepository metricRepository;
    private final DimensionRepository dimensionRepository;
    private final DatagatewayClient datagatewayClient;

    @Override
    public MetricBiChartDataDTO execute(MetricBiAnalysisCmd cmd) {
        long start = System.currentTimeMillis();
        try {
            String sql = buildSql(cmd);
            SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                    .setSql(sql)
                    .setDatabase(null)
                    .setTimeoutMs(60000L);
            Response<SqlExecuteResultDTO> response = datagatewayClient.execute(executeCmd);
            SqlExecuteResultDTO result = response.getData();
            long cost = System.currentTimeMillis() - start;

            if (result == null) {
                return new MetricBiChartDataDTO()
                        .setStatus("FAILED")
                        .setCostTimeMs(cost)
                        .setErrorMessage(response.getMessage() != null ? response.getMessage() : "执行失败")
                        .setSql(sql);
            }

            List<Map<String, Object>> rows = result.getData() != null ? result.getData() : List.of();
            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.getFirst().keySet());

            return new MetricBiChartDataDTO()
                    .setStatus(result.getStatus())
                    .setCostTimeMs(result.getCostTimeMs() != null ? result.getCostTimeMs() : cost)
                    .setColumns(columns)
                    .setRows(rows)
                    .setSql(sql)
                    .setErrorMessage(result.getErrorMessage());
        } catch (Exception e) {
            return new MetricBiChartDataDTO()
                    .setStatus("FAILED")
                    .setCostTimeMs(System.currentTimeMillis() - start)
                    .setErrorMessage(e.getMessage());
        }
    }

    @Override
    public String previewSql(MetricBiAnalysisCmd cmd) {
        return buildSql(cmd);
    }

    // ==================== SQL 组装 ====================

    private String buildSql(MetricBiAnalysisCmd cmd) {
        if (CollectionUtils.isEmpty(cmd.getMetrics())) {
            throw new BusinessException("请至少选择一个指标");
        }

        // 1. 解析指标
        List<MetricInfo> metricInfos = new ArrayList<>();
        for (MetricBiAnalysisCmd.MetricRef ref : cmd.getMetrics()) {
            metricInfos.add(resolveMetric(ref));
        }

        // 2. 校验：所有指标必须来自同一张表（Phase 1 限制）
        String tableRef = metricInfos.getFirst().tableRef;
        for (MetricInfo info : metricInfos) {
            if (!tableRef.equals(info.tableRef)) {
                throw new BusinessException("暂不支持跨表指标组合分析，指标 '" + info.alias + "' 与其他指标不在同一张表");
            }
        }

        // 3. 解析维度
        List<DimensionInfo> dimensionInfos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmd.getDimensions())) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                dimensionInfos.add(resolveDimension(ref));
            }
        }

        // 4. 构建 SELECT
        List<String> selectCols = new ArrayList<>();
        // 维度字段
        for (DimensionInfo dim : dimensionInfos) {
            selectCols.add(dim.columnName);
        }
        // 指标聚合表达式
        for (MetricInfo info : metricInfos) {
            selectCols.add(info.aggExpression + " AS `" + info.alias + "`");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectCols));
        sql.append(" FROM ").append(tableRef);

        // 5. 构建 WHERE（指标自带过滤 + 用户过滤）
        // 指标过滤条件（去重）
        Set<String> metricConditions = new LinkedHashSet<>();
        for (MetricInfo info : metricInfos) {
            if (info.filterConditions != null) {
                metricConditions.addAll(info.filterConditions);
            }
        }
        List<String> conditions = new ArrayList<>(metricConditions);

        // 用户过滤条件
        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                String condition = buildFilterCondition(filter, metricInfos, dimensionInfos);
                if (StringUtils.hasText(condition)) {
                    conditions.add(condition);
                }
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // 6. 构建 GROUP BY
        if (!dimensionInfos.isEmpty()) {
            String groupBy = dimensionInfos.stream()
                    .map(d -> d.columnName)
                    .collect(Collectors.joining(", "));
            sql.append(" GROUP BY ").append(groupBy);
        }

        // 7. 构建 ORDER BY
        if (!CollectionUtils.isEmpty(cmd.getOrders())) {
            List<String> orderParts = new ArrayList<>();
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                String orderCol = buildOrderColumn(order, metricInfos, dimensionInfos);
                if (StringUtils.hasText(orderCol)) {
                    orderParts.add(orderCol + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append(" ORDER BY ").append(String.join(", ", orderParts));
            }
        }

        // 8. 构建 LIMIT
        if (cmd.getLimitValue() != null && cmd.getLimitValue() > 0) {
            sql.append(" LIMIT ").append(cmd.getLimitValue());
        }

        return sql.toString();
    }

    // ==================== 指标解析 ====================

    private MetricInfo resolveMetric(MetricBiAnalysisCmd.MetricRef ref) {
        String metricCode = ref.getMetricCode();
        if (!StringUtils.hasText(metricCode)) {
            throw new BusinessException("指标编码不能为空");
        }
        Metric metric = metricRepository.findByMetricCode(metricCode);
        if (metric == null) {
            throw new BusinessException("指标 '" + metricCode + "' 不存在");
        }

        MetricInfo info = new MetricInfo();
        info.alias = StringUtils.hasText(ref.getAlias()) ? ref.getAlias() : metric.getMetricName();
        info.metricCode = metricCode;

        switch (metric.getMetricType()) {
            case ATOMIC -> {
                if (metric.getAtomicExt() == null) {
                    throw new BusinessException("原子指标 '" + metricCode + "' 扩展信息不存在");
                }
                MetricAtomicExt ext = metric.getAtomicExt();
                info.tableRef = ext.getDbName() + "." + ext.getTblName();
                info.aggExpression = buildAggExpression(ext.getStatFunc().getCode(), ext.getColName());
                info.filterConditions = buildFilterConditions(ext.getFilterCondition());
            }
            case DERIVED -> {
                if (metric.getDerivedExt() == null || !StringUtils.hasText(metric.getDerivedExt().getAtomicMetricId())) {
                    throw new BusinessException("派生指标 '" + metricCode + "' 原子指标信息不存在");
                }
                Metric atomic = metricRepository.findById(metric.getDerivedExt().getAtomicMetricId());
                if (atomic == null || atomic.getAtomicExt() == null) {
                    throw new BusinessException("派生指标 '" + metricCode + "' 关联的原子指标不存在");
                }
                MetricAtomicExt ext = atomic.getAtomicExt();
                info.tableRef = ext.getDbName() + "." + ext.getTblName();
                info.aggExpression = buildAggExpression(ext.getStatFunc().getCode(), ext.getColName());
                info.filterConditions = buildFilterConditions(ext.getFilterCondition());
            }
            case COMPOSITE -> throw new BusinessException("暂不支持复合指标 '" + metricCode + "' 的 BI 分析");
        }
        return info;
    }

    private String buildAggExpression(String func, String col) {
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT `" + col + "`)";
        }
        return func + "(`" + col + "`)";
    }

    private List<String> buildFilterConditions(List<MetricAtomicExt.FilterCondition> filters) {
        if (filters == null || filters.isEmpty()) {
            return List.of();
        }
        List<String> conditions = new ArrayList<>();
        for (MetricAtomicExt.FilterCondition f : filters) {
            conditions.add("`" + f.getField() + "` " + f.getOp() + " '" + f.getValue().replace("'", "''") + "'");
        }
        return conditions;
    }

    // ==================== 维度解析 ====================

    private DimensionInfo resolveDimension(MetricBiAnalysisCmd.DimensionRef ref) {
        String dimCode = ref.getDimCode();
        if (!StringUtils.hasText(dimCode)) {
            throw new BusinessException("维度编码不能为空");
        }
        Dimension dim = dimensionRepository.findByDimCode(dimCode);
        if (dim == null) {
            throw new BusinessException("维度 '" + dimCode + "' 不存在");
        }
        if (!StringUtils.hasText(dim.getColumnName())) {
            throw new BusinessException("维度 '" + dimCode + "' 未配置关联字段");
        }
        DimensionInfo info = new DimensionInfo();
        info.dimCode = dimCode;
        info.columnName = "`" + dim.getColumnName() + "`";
        info.alias = StringUtils.hasText(ref.getAlias()) ? ref.getAlias() : dim.getDimName();
        return info;
    }

    // ==================== 过滤条件解析 ====================

    private String buildFilterCondition(MetricBiAnalysisCmd.FilterRef filter,
                                        List<MetricInfo> metricInfos,
                                        List<DimensionInfo> dimensionInfos) {
        String operator = filter.getOperator();
        List<String> values = filter.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }

        String column = null;
        if (StringUtils.hasText(filter.getMetricCode())) {
            // 按指标过滤：找到指标对应的字段
            for (MetricInfo info : metricInfos) {
                if (filter.getMetricCode().equals(info.metricCode)) {
                    // 从聚合表达式中提取字段名（如 SUM(`amount`) → `amount`）
                    column = extractColumnFromAgg(info.aggExpression);
                    break;
                }
            }
        } else if (StringUtils.hasText(filter.getDimCode())) {
            for (DimensionInfo dim : dimensionInfos) {
                if (filter.getDimCode().equals(dim.dimCode)) {
                    column = dim.columnName;
                    break;
                }
            }
        }

        if (column == null) {
            return null;
        }

        String valueStr = values.stream()
                .map(v -> "'" + v.replace("'", "''") + "'")
                .collect(Collectors.joining(", "));

        return switch (operator.toUpperCase()) {
            case "EQ", "=" -> column + " = '" + values.getFirst().replace("'", "''") + "'";
            case "NE", "!=" -> column + " != '" + values.getFirst().replace("'", "''") + "'";
            case "GT", ">" -> column + " > '" + values.getFirst().replace("'", "''") + "'";
            case "GTE", ">=" -> column + " >= '" + values.getFirst().replace("'", "''") + "'";
            case "LT", "<" -> column + " < '" + values.getFirst().replace("'", "''") + "'";
            case "LTE", "<=" -> column + " <= '" + values.getFirst().replace("'", "''") + "'";
            case "IN" -> column + " IN (" + valueStr + ")";
            case "NOT_IN" -> column + " NOT IN (" + valueStr + ")";
            case "LIKE" -> column + " LIKE '%" + values.getFirst().replace("'", "''") + "%'";
            case "NOT_LIKE" -> column + " NOT LIKE '%" + values.getFirst().replace("'", "''") + "%'";
            case "IS_NULL" -> column + " IS NULL";
            case "IS_NOT_NULL" -> column + " IS NOT NULL";
            case "BETWEEN" -> {
                if (values.size() >= 2) {
                    yield column + " BETWEEN '" + values.get(0).replace("'", "''") + "' AND '" + values.get(1).replace("'", "''") + "'";
                }
                yield null;
            }
            default -> column + " = '" + values.get(0).replace("'", "''") + "'";
        };
    }

    private String extractColumnFromAgg(String aggExpression) {
        // SUM(`amount`) → `amount`
        // COUNT(DISTINCT `order_id`) → `order_id`
        int start = aggExpression.indexOf('`');
        int end = aggExpression.lastIndexOf('`');
        if (start >= 0 && end > start) {
            return aggExpression.substring(start, end + 1);
        }
        return aggExpression;
    }

    // ==================== 排序解析 ====================

    private String buildOrderColumn(MetricBiAnalysisCmd.OrderRef order,
                                    List<MetricInfo> metricInfos,
                                    List<DimensionInfo> dimensionInfos) {
        if (StringUtils.hasText(order.getMetricCode())) {
            for (MetricInfo info : metricInfos) {
                if (order.getMetricCode().equals(info.metricCode)) {
                    return "`" + info.alias + "`";
                }
            }
        } else if (StringUtils.hasText(order.getDimCode())) {
            for (DimensionInfo dim : dimensionInfos) {
                if (order.getDimCode().equals(dim.dimCode)) {
                    return dim.columnName;
                }
            }
        }
        return null;
    }

    // ==================== 内部数据结构 ====================

    private static class MetricInfo {
        String metricCode;
        String alias;
        String tableRef;
        String aggExpression;
        List<String> filterConditions;
    }

    private static class DimensionInfo {
        String dimCode;
        String columnName;
        String alias;
    }
}
