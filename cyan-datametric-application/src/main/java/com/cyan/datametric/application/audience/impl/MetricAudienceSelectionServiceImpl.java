package com.cyan.datametric.application.audience.impl;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.application.audience.MetricAudienceSelectionService;
import com.cyan.datametric.client.audience.dto.MetricAudienceEstimateDTO;
import com.cyan.datametric.client.audience.dto.MetricAudienceSelectionSqlDTO;
import com.cyan.datametric.client.audience.request.MetricAudienceSelectionCmd;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.infra.gateway.SqlGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 指标人群圈选服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MetricAudienceSelectionServiceImpl implements MetricAudienceSelectionService {

    private static final String DEFAULT_ENTITY_TYPE = "USER";
    private static final String DEFAULT_ENTITY_ID_DIM_CODE = "DIM_USER_ID";
    private static final String ENTITY_ID_ALIAS = "user_id";

    private final BiAnalysisService biAnalysisService;
    private final SqlGateway sqlGateway;

    @Override
    public MetricAudienceSelectionSqlDTO compile(MetricAudienceSelectionCmd cmd, String executor) {
        validate(cmd);
        MetricBiAnalysisCmd baseCmd = buildBaseCmd(cmd);
        String baseSql = stripTailLimit(biAnalysisService.previewSql(baseCmd));
        String metricFilterSql = buildMetricOuterFilter(cmd);
        String wrappedSql = "SELECT * FROM (" + baseSql + ") audience_base";
        if (StringUtils.hasText(metricFilterSql)) {
            wrappedSql += " WHERE " + metricFilterSql;
        }

        String memberSql = "SELECT DISTINCT `" + ENTITY_ID_ALIAS + "` AS `" + ENTITY_ID_ALIAS + "` FROM (" + wrappedSql + ") audience_members";
        String countSql = "SELECT COUNT(DISTINCT `" + ENTITY_ID_ALIAS + "`) AS `total` FROM (" + wrappedSql + ") audience_count";
        String queryHash = DigestUtils.md5DigestAsHex((memberSql + "|" + executor).getBytes(StandardCharsets.UTF_8));
        return new MetricAudienceSelectionSqlDTO()
                .setQueryHash(queryHash)
                .setCountSql(countSql)
                .setMemberSql(memberSql)
                .setEntityIdColumn(ENTITY_ID_ALIAS);
    }

    @Override
    public MetricAudienceEstimateDTO estimate(MetricAudienceSelectionCmd cmd, String executor) {
        MetricAudienceSelectionSqlDTO sqlDTO = compile(cmd, executor);
        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(sqlDTO.getCountSql())
                .setPassport(executor);
        Response<SqlExecuteResultDTO> response = sqlGateway.executeMetricSql(executeCmd);
        if (response == null || response.getData() == null || response.getData().getData() == null) {
            throw new BusinessException(response != null ? response.getMessage() : "人群预估执行失败");
        }
        Long total = response.getData().getData().stream()
                .findFirst()
                .map(row -> row.get("total"))
                .map(this::toLong)
                .orElse(0L);
        return new MetricAudienceEstimateDTO()
                .setQueryHash(sqlDTO.getQueryHash())
                .setEstimatedCount(total)
                .setCountSql(sqlDTO.getCountSql());
    }

    /**
     * 校验圈选命令
     */
    private void validate(MetricAudienceSelectionCmd cmd) {
        if (cmd == null) {
            throw new BusinessException("圈选条件不能为空");
        }
        String entityType = StringUtils.hasText(cmd.getEntityType()) ? cmd.getEntityType() : DEFAULT_ENTITY_TYPE;
        if (!DEFAULT_ENTITY_TYPE.equalsIgnoreCase(entityType)) {
            throw new BusinessException("首版仅支持 USER 圈选");
        }
        if (cmd.getMetrics() == null || cmd.getMetrics().isEmpty()) {
            throw new BusinessException("请至少选择一个指标");
        }
    }

    /**
     * 构建用于内层聚合的 BI DSL
     */
    private MetricBiAnalysisCmd buildBaseCmd(MetricAudienceSelectionCmd cmd) {
        MetricBiAnalysisCmd baseCmd = new MetricBiAnalysisCmd();
        baseCmd.setChartType("TABLE");
        baseCmd.setMetrics(cmd.getMetrics());
        baseCmd.setDimensions(buildDimensions(cmd));
        baseCmd.setFilters(cmd.getFilters() == null ? List.of() : cmd.getFilters().stream()
                .filter(filter -> !StringUtils.hasText(filter.getMetricCode()))
                .toList());
        baseCmd.setOrders(List.of());
        baseCmd.setLimitValue(null);
        return baseCmd;
    }

    /**
     * 构建包含实体ID维度的维度列表
     */
    private List<MetricBiAnalysisCmd.DimensionRef> buildDimensions(MetricAudienceSelectionCmd cmd) {
        String entityIdDimCode = StringUtils.hasText(cmd.getEntityIdDimCode())
                ? cmd.getEntityIdDimCode()
                : DEFAULT_ENTITY_ID_DIM_CODE;
        Map<String, MetricBiAnalysisCmd.DimensionRef> dimensionMap = new LinkedHashMap<>();
        MetricBiAnalysisCmd.DimensionRef entityIdRef = new MetricBiAnalysisCmd.DimensionRef()
                .setDimCode(entityIdDimCode)
                .setAlias(ENTITY_ID_ALIAS)
                .setDimName(ENTITY_ID_ALIAS);
        dimensionMap.put(entityIdDimCode, entityIdRef);
        if (cmd.getDimensions() != null) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                if (ref != null && StringUtils.hasText(ref.getDimCode()) && !dimensionMap.containsKey(ref.getDimCode())) {
                    dimensionMap.put(ref.getDimCode(), ref);
                }
            }
        }
        return new ArrayList<>(dimensionMap.values());
    }

    /**
     * 构建指标聚合后的外层过滤
     */
    private String buildMetricOuterFilter(MetricAudienceSelectionCmd cmd) {
        if (cmd.getFilters() == null || cmd.getFilters().isEmpty()) {
            return null;
        }
        Map<String, String> metricAliasMap = cmd.getMetrics().stream()
                .filter(Objects::nonNull)
                .filter(ref -> StringUtils.hasText(ref.getMetricCode()))
                .collect(Collectors.toMap(
                        MetricBiAnalysisCmd.MetricRef::getMetricCode,
                        ref -> StringUtils.hasText(ref.getAlias()) ? ref.getAlias() : ref.getMetricCode(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        List<String> conditions = new ArrayList<>();
        for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
            if (!StringUtils.hasText(filter.getMetricCode())) {
                continue;
            }
            String alias = metricAliasMap.get(filter.getMetricCode());
            if (!StringUtils.hasText(alias)) {
                throw new BusinessException("指标过滤引用的指标不存在: " + filter.getMetricCode());
            }
            String condition = buildFilterCondition("`" + escapeIdentifier(alias) + "`", filter.getOperator(), filter.getValues());
            if (StringUtils.hasText(condition)) {
                conditions.add(condition);
            }
        }
        return conditions.isEmpty() ? null : String.join(" AND ", conditions);
    }

    /**
     * 构建过滤条件
     */
    private String buildFilterCondition(String column, String operator, List<String> values) {
        String op = StringUtils.hasText(operator) ? operator.toUpperCase() : "EQ";
        if ("IS_NULL".equals(op)) {
            return column + " IS NULL";
        }
        if ("IS_NOT_NULL".equals(op)) {
            return column + " IS NOT NULL";
        }
        if (values == null || values.isEmpty()) {
            return null;
        }
        String first = escapeValue(values.get(0));
        String valueStr = values.stream()
                .map(this::escapeValue)
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(", "));
        return switch (op) {
            case "EQ", "=" -> column + " = '" + first + "'";
            case "NE", "!=" -> column + " != '" + first + "'";
            case "GT", ">" -> column + " > '" + first + "'";
            case "GTE", ">=" -> column + " >= '" + first + "'";
            case "LT", "<" -> column + " < '" + first + "'";
            case "LTE", "<=" -> column + " <= '" + first + "'";
            case "IN" -> column + " IN (" + valueStr + ")";
            case "NOT_IN" -> column + " NOT IN (" + valueStr + ")";
            case "LIKE" -> column + " LIKE '%" + first + "%'";
            case "NOT_LIKE" -> column + " NOT LIKE '%" + first + "%'";
            case "BETWEEN" -> values.size() >= 2
                    ? column + " BETWEEN '" + first + "' AND '" + escapeValue(values.get(1)) + "'"
                    : null;
            default -> throw new BusinessException("不支持的过滤操作符: " + operator);
        };
    }

    /**
     * 去掉末尾 LIMIT
     */
    private String stripTailLimit(String sql) {
        if (!StringUtils.hasText(sql)) {
            return sql;
        }
        return sql.replaceAll("(?is)\\s+LIMIT\\s+\\d+\\s*$", "");
    }

    /**
     * 转换 Long
     */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 转义SQL值
     */
    private String escapeValue(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    /**
     * 转义字段别名
     */
    private String escapeIdentifier(String value) {
        return value == null ? "" : value.replace("`", "``");
    }
}
