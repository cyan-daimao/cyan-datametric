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
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.infra.gateway.SqlGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String DEFAULT_ENTITY_ID_COLUMN = "user_id";
    private static final String ENTITY_ID_ALIAS = "entity_id";
    private static final String PHYSICAL_ENTITY_ID_DIMENSION = "__PHYSICAL_ENTITY_ID__";

    private final BiAnalysisService biAnalysisService;
    private final DimensionRepository dimensionRepository;
    private final SqlGateway sqlGateway;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    @Override
    public MetricAudienceSelectionSqlDTO compile(MetricAudienceSelectionCmd cmd, String executor) {
        validate(cmd);
        boolean metricSelection = hasMetrics(cmd);
        String baseSql = metricSelection ? buildMetricSelectionSql(cmd) : buildDimensionSelectionSql(cmd);
        String sourceEntityColumn = ENTITY_ID_ALIAS;
        String metricFilterSql = buildMetricOuterFilter(cmd);
        String wrappedSql = "SELECT * FROM (" + baseSql + ") audience_base";
        if (StringUtils.hasText(metricFilterSql)) {
            wrappedSql += " WHERE " + metricFilterSql;
        }

        String memberSql = "SELECT DISTINCT `" + escapeIdentifier(sourceEntityColumn) + "` AS `" + ENTITY_ID_ALIAS + "` FROM (" + wrappedSql + ") audience_members";
        String countSql = "SELECT COUNT(DISTINCT `" + escapeIdentifier(sourceEntityColumn) + "`) AS `total` FROM (" + wrappedSql + ") audience_count";
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
        if (!StringUtils.hasText(cmd.getEntityType())) {
            cmd.setEntityType(DEFAULT_ENTITY_TYPE);
        }
        if (!StringUtils.hasText(cmd.getEntityIdColumn())) {
            cmd.setEntityIdColumn(DEFAULT_ENTITY_ID_COLUMN);
        }
        validateIdentifier(cmd.getEntityIdColumn(), "实体ID字段");
        if (!hasMetrics(cmd)) {
            if (cmd.getFilters() == null || cmd.getFilters().stream().noneMatch(filter -> StringUtils.hasText(filter.getDimCode()))) {
                throw new BusinessException("无指标圈选至少需要一个维度过滤条件");
            }
            boolean hasMetricFilter = cmd.getFilters().stream().anyMatch(filter -> StringUtils.hasText(filter.getMetricCode()));
            if (hasMetricFilter) {
                throw new BusinessException("无指标圈选不支持指标过滤");
            }
        }
    }

    /**
     * 是否选择指标
     */
    private boolean hasMetrics(MetricAudienceSelectionCmd cmd) {
        return cmd.getMetrics() != null && !cmd.getMetrics().isEmpty();
    }

    /**
     * 构建指标聚合圈选SQL
     */
    private String buildMetricSelectionSql(MetricAudienceSelectionCmd cmd) {
        MetricBiAnalysisCmd baseCmd = buildBaseCmd(cmd);
        return stripTailLimit(biAnalysisService.previewSql(baseCmd));
    }

    /**
     * 构建无指标维度圈选SQL
     */
    private String buildDimensionSelectionSql(MetricAudienceSelectionCmd cmd) {
        Map<String, Dimension> dimensionMap = resolveDimensionMap(cmd);
        Dimension tableDimension = dimensionMap.values().stream()
                .filter(dimension -> StringUtils.hasText(dimension.getTableName()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("无指标圈选至少需要一个关联维表的维度"));
        String tableRef = buildDimensionTableRef(tableDimension.getSchemaName(), tableDimension.getTableName());
        List<String> conditions = new ArrayList<>();
        for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
            Dimension dimension = dimensionMap.get(filter.getDimCode());
            if (dimension == null) {
                throw new BusinessException("过滤维度不存在: " + filter.getDimCode());
            }
            String currentTableRef = buildDimensionTableRef(dimension.getSchemaName(), dimension.getTableName());
            if (!Objects.equals(tableRef, currentTableRef)) {
                throw new BusinessException("无指标圈选暂不支持跨维表过滤");
            }
            String condition = buildFilterCondition(resolveDimensionExpression(dimension), filter.getOperator(), filter.getValues());
            if (StringUtils.hasText(condition)) {
                conditions.add(condition);
            }
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT `")
                .append(escapeIdentifier(cmd.getEntityIdColumn()))
                .append("` AS `")
                .append(ENTITY_ID_ALIAS)
                .append("` FROM ")
                .append(tableRef);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        return sql.toString();
    }

    /**
     * 解析圈选维度
     */
    private Map<String, Dimension> resolveDimensionMap(MetricAudienceSelectionCmd cmd) {
        Map<String, Dimension> dimensionMap = new LinkedHashMap<>();
        if (cmd.getFilters() != null) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                putDimension(dimensionMap, filter.getDimCode());
            }
        }
        return dimensionMap;
    }

    /**
     * 添加维度
     */
    private void putDimension(Map<String, Dimension> dimensionMap, String dimCode) {
        if (!StringUtils.hasText(dimCode) || dimensionMap.containsKey(dimCode)) {
            return;
        }
        Dimension dimension = dimensionRepository.findByDimCode(dimCode);
        if (dimension == null) {
            throw new BusinessException("维度不存在: " + dimCode);
        }
        dimensionMap.put(dimCode, dimension);
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
        String entityIdColumn = StringUtils.hasText(cmd.getEntityIdColumn())
                ? cmd.getEntityIdColumn()
                : DEFAULT_ENTITY_ID_COLUMN;
        Map<String, MetricBiAnalysisCmd.DimensionRef> dimensionMap = new LinkedHashMap<>();
        MetricBiAnalysisCmd.DimensionRef entityIdRef = new MetricBiAnalysisCmd.DimensionRef()
                .setDimCode(entityIdColumn)
                .setAlias(ENTITY_ID_ALIAS)
                .setDimName(PHYSICAL_ENTITY_ID_DIMENSION);
        dimensionMap.put(entityIdColumn, entityIdRef);
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
        if (!hasMetrics(cmd)) {
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
     * 构建维表引用
     */
    private String buildDimensionTableRef(String schema, String tableName) {
        if (!StringUtils.hasText(tableName)) {
            throw new BusinessException("维度未配置关联维表");
        }
        if (tableName.contains(".")) {
            return normalizeTableRef(tableName);
        }
        if (StringUtils.hasText(schema)) {
            return normalizeTableRef(schema + "." + tableName);
        }
        return normalizeTableRef(tableName);
    }

    /**
     * 标准化表引用
     */
    private String normalizeTableRef(String tableRef) {
        String[] parts = tableRef.split("\\.");
        if (parts.length == 2) {
            return defaultCatalog + "." + tableRef;
        }
        if (parts.length == 1) {
            throw new BusinessException("表引用格式错误，期望 schema.table 或 catalog.schema.table，实际: " + tableRef);
        }
        return tableRef;
    }

    /**
     * 解析维度表达式
     */
    private String resolveDimensionExpression(Dimension dimension) {
        String sourceType = StringUtils.hasText(dimension.getSourceType()) ? dimension.getSourceType() : "COLUMN";
        return switch (sourceType) {
            case "JSON_PATH" -> "JSON_VALUE(properties, '" + escapeValue(resolveJsonPath(dimension)) + "')";
            case "EXPRESSION" -> dimension.getSourceExpr();
            default -> "`" + escapeIdentifier(dimension.getColumnName()) + "`";
        };
    }

    /**
     * 解析 JSON Path
     */
    private String resolveJsonPath(Dimension dimension) {
        if (StringUtils.hasText(dimension.getSourceExpr())) {
            return dimension.getSourceExpr();
        }
        return "$.properties." + dimension.getColumnName();
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

    /**
     * 校验物理字段名
     */
    private void validateIdentifier(String value, String name) {
        if (!value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new BusinessException(name + "只能是物理字段名");
        }
    }
}
