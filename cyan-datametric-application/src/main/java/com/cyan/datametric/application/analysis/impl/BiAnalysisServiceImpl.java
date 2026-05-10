package com.cyan.datametric.application.analysis.impl;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.application.bi.convert.MetricBiAnalysisAppConvert;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.dataman.client.table.TableRelationClient;
import com.cyan.dataman.client.table.dto.TableRelationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final SqlGatewayClient sqlGatewayClient;
    private final TableRelationClient tableRelationClient;
    private final MetricBiAnalysisAppConvert metricBiAnalysisAppConvert;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    @Override
    public ChartDataBO execute(MetricBiAnalysisCmd cmd, String executor) {
        long start = System.currentTimeMillis();
        try {
            String sql = buildSql(cmd);
            SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                    .setSql(sql)
                    .setPassport(executor);
            Response<SqlExecuteResultDTO> response = sqlGatewayClient.executeStarRocksSql(executeCmd);
            SqlExecuteResultDTO result = response.getData();
            long cost = System.currentTimeMillis() - start;

            if (result == null) {
                return metricBiAnalysisAppConvert.toChartDataBO(
                        "FAILED", cost, new ArrayList<>(), new ArrayList<>(), sql,
                        response.getMessage() != null ? response.getMessage() : "执行失败");
            }

            List<Map<String, Object>> rows = result.getData() != null ? result.getData() : List.of();
            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.getFirst().keySet());

            return metricBiAnalysisAppConvert.toChartDataBO(
                    result.getStatus(),
                    result.getCostTimeMs() != null ? result.getCostTimeMs() : cost,
                    columns, rows, sql, result.getErrorMessage());
        } catch (Exception e) {
            return metricBiAnalysisAppConvert.toChartDataBO(
                    "FAILED",
                    System.currentTimeMillis() - start,
                    new ArrayList<>(), new ArrayList<>(), null,
                    e.getMessage());
        }
    }

    @Override
    public String previewSql(MetricBiAnalysisCmd cmd) {
        return buildSql(cmd);
    }

    // ==================== SQL 组装 ====================

    private boolean isFilterChart(String chartType) {
        return chartType != null && chartType.startsWith("FILTER_");
    }

    private String buildSql(MetricBiAnalysisCmd cmd) {
        boolean isFilterChart = isFilterChart(cmd.getChartType());

        if (!isFilterChart && CollectionUtils.isEmpty(cmd.getMetrics())) {
            throw new BusinessException("请至少选择一个指标");
        }

        if (isFilterChart && CollectionUtils.isEmpty(cmd.getDimensions())) {
            throw new BusinessException("筛选框图表至少需要选择一个维度");
        }

        List<MetricInfo> metricInfos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmd.getMetrics())) {
            for (MetricBiAnalysisCmd.MetricRef ref : cmd.getMetrics()) {
                metricInfos.add(resolveMetric(ref));
            }
        }

        List<DimensionInfo> dimensionInfos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmd.getDimensions())) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                dimensionInfos.add(resolveDimension(ref));
            }
        }

        if (isFilterChart) {
            return buildFilterSql(dimensionInfos, cmd);
        }

        Map<String, List<MetricInfo>> factGroups = metricInfos.stream()
                .collect(Collectors.groupingBy(m -> m.tableRef));

        if (factGroups.size() == 1) {
            String factTableRef = factGroups.keySet().iterator().next();
            Set<String> dimTableRefs = new HashSet<>();
            for (DimensionInfo dim : dimensionInfos) {
                if (StringUtils.hasText(dim.tableName) && !factTableRef.equals(dim.tableName)) {
                    dimTableRefs.add(dim.tableName);
                }
            }
            if (dimTableRefs.isEmpty()) {
                return buildSingleTableSql(metricInfos, dimensionInfos, cmd, factTableRef);
            }
            String[] factParts = factTableRef.split("\\.");
            List<String> dimTableRefList = new ArrayList<>(dimTableRefs);
            List<TableRelationDTO> joins = tableRelationClient.findJoinPaths(
                    factParts[0], factParts[1], factParts[2], dimTableRefList);
            if (joins == null || joins.isEmpty()) {
                DimensionInfo firstDim = dimensionInfos.stream()
                        .filter(d -> StringUtils.hasText(d.tableName) && !factTableRef.equals(d.tableName))
                        .findFirst()
                        .orElse(null);
                String dimName = firstDim != null ? firstDim.alias : "维度";
                throw new BusinessException("维度 '" + dimName + "' 所在表与指标事实表 '" + factTableRef
                        + "' 之间未配置关联关系，请在元数据平台的表详情页配置。");
            }
            return buildJoinSql(metricInfos, dimensionInfos, joins, cmd, factTableRef);
        } else {
            return buildMultiFactSql(factGroups, dimensionInfos, cmd);
        }
    }

    private String buildFilterSql(List<DimensionInfo> dimensionInfos, MetricBiAnalysisCmd cmd) {
        if (dimensionInfos.isEmpty()) {
            throw new BusinessException("筛选框图表至少需要选择一个维度");
        }

        String tableRef = dimensionInfos.get(0).tableName;
        if (!StringUtils.hasText(tableRef)) {
            throw new BusinessException("筛选框维度未配置关联维表");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT ");

        List<String> selectCols = new ArrayList<>();
        for (DimensionInfo dim : dimensionInfos) {
            selectCols.add(dim.columnName + " AS `" + dim.dimCode + "`");
        }
        sql.append(String.join(", ", selectCols));
        sql.append(" FROM ").append(tableRef);

        List<String> conditions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                String condition = buildFilterCondition(filter, List.of(), dimensionInfos, null, null);
                if (StringUtils.hasText(condition)) {
                    conditions.add(condition);
                }
            }
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (!CollectionUtils.isEmpty(cmd.getOrders())) {
            List<String> orderParts = new ArrayList<>();
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                String orderCol = buildOrderColumn(order, List.of(), dimensionInfos, null, null);
                if (StringUtils.hasText(orderCol)) {
                    orderParts.add(orderCol + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append(" ORDER BY ").append(String.join(", ", orderParts));
            }
        }

        Integer limit = cmd.getLimitValue();
        if (limit == null || limit <= 0) {
            limit = 1000;
        }
        sql.append(" LIMIT ").append(limit);

        return sql.toString();
    }

    private String resolveSelectColumn(DimensionInfo dim) {
        return StringUtils.hasText(dim.displayColumn) ? dim.displayColumn : dim.columnName;
    }

    private String qualifyColumnRef(String expression, String alias) {
        if (!StringUtils.hasText(expression) || !StringUtils.hasText(alias)) {
            return expression;
        }
        return expression.replaceFirst("`", alias + ".`");
    }

    private String buildSingleTableSql(List<MetricInfo> metricInfos, List<DimensionInfo> dimensionInfos,
                                       MetricBiAnalysisCmd cmd, String tableRef) {
        List<String> selectCols = new ArrayList<>();
        for (DimensionInfo dim : dimensionInfos) {
            selectCols.add(resolveSelectColumn(dim));
        }
        for (MetricInfo info : metricInfos) {
            selectCols.add(info.aggExpression + " AS `" + info.alias + "`");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectCols));
        sql.append(" FROM ").append(tableRef);

        Set<String> metricConditions = new LinkedHashSet<>();
        for (MetricInfo info : metricInfos) {
            if (info.filterConditions != null) {
                metricConditions.addAll(info.filterConditions);
            }
        }
        List<String> conditions = new ArrayList<>(metricConditions);

        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                String condition = buildFilterCondition(filter, metricInfos, dimensionInfos, null, null);
                if (StringUtils.hasText(condition)) {
                    conditions.add(condition);
                }
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (!dimensionInfos.isEmpty()) {
            String groupBy = dimensionInfos.stream()
                    .map(this::resolveSelectColumn)
                    .collect(Collectors.joining(", "));
            sql.append(" GROUP BY ").append(groupBy);
        }

        if (!CollectionUtils.isEmpty(cmd.getOrders())) {
            List<String> orderParts = new ArrayList<>();
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                String orderCol = buildOrderColumn(order, metricInfos, dimensionInfos, null, null);
                if (StringUtils.hasText(orderCol)) {
                    orderParts.add(orderCol + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append(" ORDER BY ").append(String.join(", ", orderParts));
            }
        }

        if (cmd.getLimitValue() != null && cmd.getLimitValue() > 0) {
            sql.append(" LIMIT ").append(cmd.getLimitValue());
        }

        return sql.toString();
    }

    private String buildJoinSql(List<MetricInfo> metrics, List<DimensionInfo> dimensions,
                                List<TableRelationDTO> joins, MetricBiAnalysisCmd cmd, String factTableRef) {
        StringBuilder sql = new StringBuilder();
        String factAlias = "t0";

        sql.append("SELECT ");
        List<String> selectItems = new ArrayList<>();
        for (DimensionInfo dim : dimensions) {
            String col = resolveSelectColumn(dim);
            if (StringUtils.hasText(dim.tableName) && !factTableRef.equals(dim.tableName)) {
                String alias = getTableAlias(dim.tableName, joins, factTableRef);
                selectItems.add(alias + "." + col + " AS `" + dim.alias + "`");
            } else {
                selectItems.add(factAlias + "." + col + " AS `" + dim.alias + "`");
            }
        }
        for (MetricInfo metric : metrics) {
            selectItems.add(qualifyColumnRef(metric.aggExpression, factAlias) + " AS `" + metric.alias + "`");
        }
        sql.append(String.join(", ", selectItems));

        sql.append(" FROM ").append(factTableRef).append(" ").append(factAlias);

        int aliasIndex = 1;
        Map<String, String> aliasMap = new HashMap<>();
        aliasMap.put(factTableRef, factAlias);

        for (TableRelationDTO join : joins) {
            String dimTable = join.getTargetCatalog() + "." + join.getTargetSchema() + "." + join.getTargetTable();
            if (aliasMap.containsKey(dimTable)) {
                continue;
            }
            String dimAlias = "t" + aliasIndex++;
            aliasMap.put(dimTable, dimAlias);

            sql.append(" ").append(join.getJoinType())
                    .append(" JOIN ").append(dimTable).append(" ").append(dimAlias)
                    .append(" ON ")
                    .append(factAlias).append(".").append("`").append(join.getSourceColumn()).append("`")
                    .append(" = ")
                    .append(dimAlias).append(".").append("`").append(join.getTargetColumn()).append("`");
        }

        Set<String> metricConditions = new LinkedHashSet<>();
        for (MetricInfo info : metrics) {
            if (info.filterConditions != null) {
                for (String condition : info.filterConditions) {
                    metricConditions.add(qualifyColumnRef(condition, factAlias));
                }
            }
        }
        List<String> conditions = new ArrayList<>(metricConditions);

        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                String condition = buildFilterCondition(filter, metrics, dimensions, factTableRef, aliasMap);
                if (StringUtils.hasText(condition)) {
                    conditions.add(condition);
                }
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (!dimensions.isEmpty()) {
            List<String> groupByCols = new ArrayList<>();
            for (DimensionInfo dim : dimensions) {
                String col = resolveSelectColumn(dim);
                if (StringUtils.hasText(dim.tableName) && !factTableRef.equals(dim.tableName)) {
                    String alias = aliasMap.get(dim.tableName);
                    if (StringUtils.hasText(alias)) {
                        groupByCols.add(alias + "." + col);
                    } else {
                        groupByCols.add(col);
                    }
                } else {
                    groupByCols.add(factAlias + "." + col);
                }
            }
            sql.append(" GROUP BY ").append(String.join(", ", groupByCols));
        }

        if (!CollectionUtils.isEmpty(cmd.getOrders())) {
            List<String> orderParts = new ArrayList<>();
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                String orderCol = buildOrderColumn(order, metrics, dimensions, factTableRef, aliasMap);
                if (StringUtils.hasText(orderCol)) {
                    orderParts.add(orderCol + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append(" ORDER BY ").append(String.join(", ", orderParts));
            }
        }

        if (cmd.getLimitValue() != null && cmd.getLimitValue() > 0) {
            sql.append(" LIMIT ").append(cmd.getLimitValue());
        }

        return sql.toString();
    }

    private String getTableAlias(String tableName, List<TableRelationDTO> joins, String factTableRef) {
        if (factTableRef.equals(tableName)) {
            return "t0";
        }
        int index = 1;
        Set<String> seen = new HashSet<>();
        for (TableRelationDTO join : joins) {
            String dimTable = join.getTargetCatalog() + "." + join.getTargetSchema() + "." + join.getTargetTable();
            if (seen.contains(dimTable)) {
                continue;
            }
            seen.add(dimTable);
            if (dimTable.equals(tableName)) {
                return "t" + index;
            }
            index++;
        }
        return "";
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
                info.tableRef = normalizeTableRef(ext.getDbName() + "." + ext.getTblName());
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
                info.tableRef = normalizeTableRef(ext.getDbName() + "." + ext.getTblName());
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
        if (StringUtils.hasText(dim.getDisplayColumn())) {
            info.displayColumn = "`" + dim.getDisplayColumn() + "`";
        }
        info.alias = StringUtils.hasText(ref.getAlias()) ? ref.getAlias() : dim.getDimName();
        info.tableName = buildDimensionTableRef(dim.getSchemaName(), dim.getTableName());
        return info;
    }

    // ==================== 过滤条件解析 ====================

    private String buildFilterCondition(MetricBiAnalysisCmd.FilterRef filter,
                                        List<MetricInfo> metricInfos,
                                        List<DimensionInfo> dimensionInfos,
                                        String factTableRef,
                                        Map<String, String> aliasMap) {
        String operator = filter.getOperator();
        List<String> values = filter.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }

        String column = null;
        if (StringUtils.hasText(filter.getMetricCode())) {
            for (MetricInfo info : metricInfos) {
                if (filter.getMetricCode().equals(info.metricCode)) {
                    column = extractColumnFromAgg(info.aggExpression);
                    if (aliasMap != null && StringUtils.hasText(factTableRef)) {
                        String factAlias = aliasMap.get(factTableRef);
                        if (StringUtils.hasText(factAlias)) {
                            column = factAlias + "." + column;
                        }
                    }
                    break;
                }
            }
        } else if (StringUtils.hasText(filter.getDimCode())) {
            for (DimensionInfo dim : dimensionInfos) {
                if (filter.getDimCode().equals(dim.dimCode)) {
                    column = dim.columnName;
                    if (aliasMap != null && StringUtils.hasText(dim.tableName)
                            && (!StringUtils.hasText(factTableRef) || !factTableRef.equals(dim.tableName))) {
                        String alias = aliasMap.get(dim.tableName);
                        if (StringUtils.hasText(alias)) {
                            column = alias + "." + column;
                        }
                    }
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
                                    List<DimensionInfo> dimensionInfos,
                                    String factTableRef,
                                    Map<String, String> aliasMap) {
        if (StringUtils.hasText(order.getMetricCode())) {
            for (MetricInfo info : metricInfos) {
                if (order.getMetricCode().equals(info.metricCode)) {
                    return "`" + info.alias + "`";
                }
            }
        } else if (StringUtils.hasText(order.getDimCode())) {
            for (DimensionInfo dim : dimensionInfos) {
                if (order.getDimCode().equals(dim.dimCode)) {
                    String col = resolveSelectColumn(dim);
                    if (aliasMap != null && StringUtils.hasText(dim.tableName)
                            && (!StringUtils.hasText(factTableRef) || !factTableRef.equals(dim.tableName))) {
                        String alias = aliasMap.get(dim.tableName);
                        if (StringUtils.hasText(alias)) {
                            col = alias + "." + col;
                        }
                    }
                    return col;
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

    private String buildDimensionTableRef(String schema, String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return null;
        }
        if (tableName.contains(".")) {
            return normalizeTableRef(tableName);
        }
        if (StringUtils.hasText(schema)) {
            return normalizeTableRef(schema + "." + tableName);
        }
        return normalizeTableRef(tableName);
    }

    private String normalizeTableRef(String tableRef) {
        if (!StringUtils.hasText(tableRef)) {
            return tableRef;
        }
        String[] parts = tableRef.split("\\.");
        if (parts.length == 2) {
            return defaultCatalog + "." + tableRef;
        }
        if (parts.length == 1) {
            throw new BusinessException(
                    "表引用格式错误，期望 schema.table 或 catalog.schema.table，实际: " + tableRef);
        }
        return tableRef;
    }

    // ==================== 多事实表关联分析 ====================

    private static class FactGroup {
        String tableRef;
        String cteAlias;
        int index;
        List<MetricInfo> metrics;
        Map<String, String> dimKeyMap = new HashMap<>();
        Map<String, String> dimTargetColumnMap = new HashMap<>();
    }

    private String buildMultiFactSql(Map<String, List<MetricInfo>> factGroupMap,
                                      List<DimensionInfo> dimensionInfos,
                                      MetricBiAnalysisCmd cmd) {
        for (DimensionInfo dim : dimensionInfos) {
            if (!StringUtils.hasText(dim.tableName)) {
                throw new BusinessException("多事实表关联分析要求所有维度必须配置关联维度表");
            }
        }

        List<FactGroup> factGroups = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, List<MetricInfo>> entry : factGroupMap.entrySet()) {
            FactGroup group = new FactGroup();
            group.tableRef = entry.getKey();
            group.index = idx;
            group.cteAlias = "fact_" + idx + "_agg";
            group.metrics = entry.getValue();
            factGroups.add(group);
            idx++;
        }

        queryFactDimRelations(factGroups, dimensionInfos);
        validateDimKeyCoverage(factGroups, dimensionInfos);

        StringBuilder sql = new StringBuilder();
        sql.append("WITH ");
        for (int i = 0; i < factGroups.size(); i++) {
            if (i > 0) {
                sql.append(",\n");
            }
            sql.append(buildFactCteSql(factGroups.get(i), dimensionInfos, cmd));
        }

        sql.append(",\n").append(buildCteJoinSql(factGroups, dimensionInfos));
        sql.append("\n").append(buildFinalSelectSql(factGroups, dimensionInfos, cmd));

        return sql.toString();
    }

    private void queryFactDimRelations(List<FactGroup> factGroups, List<DimensionInfo> dimensionInfos) {
        Set<String> dimTableRefs = dimensionInfos.stream()
                .map(d -> d.tableName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        if (dimTableRefs.isEmpty()) {
            return;
        }

        List<String> dimTableRefList = new ArrayList<>(dimTableRefs);
        for (FactGroup group : factGroups) {
            String[] parts = group.tableRef.split("\\.");
            List<TableRelationDTO> relations = tableRelationClient.findJoinPaths(
                    parts[0], parts[1], parts[2], dimTableRefList);
            if (relations != null) {
                for (TableRelationDTO rel : relations) {
                    String dimTable = rel.getTargetCatalog() + "." + rel.getTargetSchema() + "." + rel.getTargetTable();
                    group.dimKeyMap.put(dimTable, rel.getSourceColumn());
                    group.dimTargetColumnMap.put(dimTable, rel.getTargetColumn());
                }
            }
        }
    }

    private void validateDimKeyCoverage(List<FactGroup> factGroups, List<DimensionInfo> dimensionInfos) {
        for (FactGroup group : factGroups) {
            for (DimensionInfo dim : dimensionInfos) {
                if (!StringUtils.hasText(dim.tableName)) {
                    continue;
                }
                if (group.tableRef.equals(dim.tableName)) {
                    continue;
                }
                if (!group.dimKeyMap.containsKey(dim.tableName)) {
                    throw new BusinessException(
                            "事实表 \"" + group.tableRef + "\" 未配置到维度表 \"" + dim.tableName
                                    + "\" 的关联关系，请到【元数据平台-表关系管理】中配置关联后再试。");
                }
            }
        }
    }

    private String buildFactCteSql(FactGroup group, List<DimensionInfo> dimensionInfos,
                                    MetricBiAnalysisCmd cmd) {
        StringBuilder cte = new StringBuilder();
        cte.append(group.cteAlias).append(" AS (\n    SELECT ");

        List<String> selectCols = new ArrayList<>();
        List<String> groupByCols = new ArrayList<>();

        for (int i = 0; i < dimensionInfos.size(); i++) {
            DimensionInfo dim = dimensionInfos.get(i);
            if (!StringUtils.hasText(dim.tableName)) {
                continue;
            }
            String sourceColumn = group.dimKeyMap.get(dim.tableName);
            if (!StringUtils.hasText(sourceColumn) && group.tableRef.equals(dim.tableName)) {
                sourceColumn = dim.columnName.replace("`", "");
            }
            if (!StringUtils.hasText(sourceColumn)) {
                continue;
            }
            String dimKeyAlias = "dim_key_" + group.index + "_" + i;
            selectCols.add("`" + sourceColumn + "` AS " + dimKeyAlias);
            groupByCols.add("`" + sourceColumn + "`");
        }

        for (MetricInfo metric : group.metrics) {
            selectCols.add(metric.aggExpression + " AS `" + metric.alias + "`");
        }

        cte.append(String.join(", ", selectCols));
        cte.append("\n    FROM ").append(group.tableRef);

        List<String> conditions = new ArrayList<>();

        for (MetricInfo metric : group.metrics) {
            if (metric.filterConditions != null) {
                conditions.addAll(metric.filterConditions);
            }
        }

        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                if (StringUtils.hasText(filter.getMetricCode())) {
                    String condition = buildFilterCondition(filter, group.metrics, dimensionInfos, null, null);
                    if (StringUtils.hasText(condition)) {
                        conditions.add(condition);
                    }
                }
            }
        }

        if (!conditions.isEmpty()) {
            cte.append("\n    WHERE ").append(String.join(" AND ", conditions));
        }

        if (!groupByCols.isEmpty()) {
            cte.append("\n    GROUP BY ").append(String.join(", ", groupByCols));
        }

        cte.append("\n)");
        return cte.toString();
    }

    private boolean hasDimKey(FactGroup group, int dimIndex, DimensionInfo dim) {
        if (group.tableRef.equals(dim.tableName)) {
            return StringUtils.hasText(dim.columnName);
        }
        return StringUtils.hasText(group.dimKeyMap.get(dim.tableName));
    }

    private String buildCteJoinSql(List<FactGroup> factGroups, List<DimensionInfo> dimensionInfos) {
        StringBuilder cte = new StringBuilder();
        cte.append("joined AS (\n    SELECT ");

        List<String> selectCols = new ArrayList<>();

        for (int i = 0; i < dimensionInfos.size(); i++) {
            DimensionInfo dim = dimensionInfos.get(i);
            List<String> coalesceArgs = new ArrayList<>();
            for (FactGroup group : factGroups) {
                if (hasDimKey(group, i, dim)) {
                    coalesceArgs.add(group.cteAlias + ".dim_key_" + group.index + "_" + i);
                }
            }
            if (coalesceArgs.isEmpty()) {
                throw new BusinessException("维度 \"" + dim.alias + "\" 无法关联任何已选事实表");
            }
            selectCols.add("COALESCE(" + String.join(", ", coalesceArgs) + ") AS `" + dim.alias + "`");
        }

        for (FactGroup group : factGroups) {
            for (MetricInfo metric : group.metrics) {
                selectCols.add(group.cteAlias + ".`" + metric.alias + "`");
            }
        }

        cte.append(String.join(", ", selectCols));

        cte.append("\n    FROM ").append(factGroups.getFirst().cteAlias);
        for (int i = 1; i < factGroups.size(); i++) {
            cte.append("\n    FULL OUTER JOIN ").append(factGroups.get(i).cteAlias).append(" ON ");
            List<String> joinConditions = new ArrayList<>();
            for (int d = 0; d < dimensionInfos.size(); d++) {
                DimensionInfo dim = dimensionInfos.get(d);
                boolean allHave = true;
                for (int k = 0; k <= i; k++) {
                    if (!hasDimKey(factGroups.get(k), d, dim)) {
                        allHave = false;
                        break;
                    }
                }
                if (!allHave) {
                    continue;
                }
                String leftExpr;
                if (i == 1) {
                    leftExpr = factGroups.getFirst().cteAlias + ".dim_key_" + factGroups.getFirst().index + "_" + d;
                } else {
                    StringBuilder coalesce = new StringBuilder("COALESCE(");
                    List<String> args = new ArrayList<>();
                    for (int j = 0; j < i; j++) {
                        args.add(factGroups.get(j).cteAlias + ".dim_key_" + factGroups.get(j).index + "_" + d);
                    }
                    coalesce.append(String.join(", ", args)).append(")");
                    leftExpr = coalesce.toString();
                }
                String rightExpr = factGroups.get(i).cteAlias + ".dim_key_" + factGroups.get(i).index + "_" + d;
                joinConditions.add(leftExpr + " = " + rightExpr);
            }
            if (joinConditions.isEmpty()) {
                joinConditions.add("1 = 1");
            }
            cte.append(String.join(" AND ", joinConditions));
        }

        cte.append("\n)");
        return cte.toString();
    }

    private String buildFinalSelectSql(List<FactGroup> factGroups, List<DimensionInfo> dimensionInfos,
                                        MetricBiAnalysisCmd cmd) {
        StringBuilder sql = new StringBuilder();

        List<String> selectItems = new ArrayList<>();

        Map<String, String> dimAliasMap = new HashMap<>();
        int dimAliasIdx = 0;
        for (DimensionInfo dim : dimensionInfos) {
            String dimAlias = dimAliasMap.get(dim.tableName);
            if (dimAlias == null) {
                dimAlias = "d" + dimAliasIdx++;
                dimAliasMap.put(dim.tableName, dimAlias);
            }
            String col = resolveSelectColumn(dim);
            selectItems.add(dimAlias + "." + col + " AS `" + dim.alias + "`");
        }

        for (FactGroup group : factGroups) {
            for (MetricInfo metric : group.metrics) {
                selectItems.add("j.`" + metric.alias + "`");
            }
        }

        sql.append("SELECT ").append(String.join(", ", selectItems));
        sql.append("\nFROM joined j");

        Set<String> joinedDimTables = new HashSet<>();
        for (DimensionInfo dim : dimensionInfos) {
            if (!StringUtils.hasText(dim.tableName)) {
                continue;
            }
            if (!joinedDimTables.add(dim.tableName)) {
                continue;
            }
            String dimAlias = dimAliasMap.get(dim.tableName);
            String targetColumn = null;
            for (FactGroup group : factGroups) {
                if (group.tableRef.equals(dim.tableName)) {
                    targetColumn = group.dimKeyMap.get(dim.tableName);
                } else {
                    targetColumn = group.dimTargetColumnMap.get(dim.tableName);
                }
                if (StringUtils.hasText(targetColumn)) {
                    break;
                }
            }
            if (!StringUtils.hasText(targetColumn)) {
                continue;
            }
            sql.append("\nLEFT JOIN ").append(dim.tableName).append(" ").append(dimAlias)
                    .append(" ON j.`").append(dim.alias).append("` = ")
                    .append(dimAlias).append(".`").append(targetColumn).append("`");
        }

        List<String> conditions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cmd.getFilters())) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                if (StringUtils.hasText(filter.getDimCode())) {
                    String condition = buildFilterCondition(filter, List.of(), dimensionInfos, null, dimAliasMap);
                    if (StringUtils.hasText(condition)) {
                        conditions.add(condition);
                    }
                }
            }
        }

        if (!conditions.isEmpty()) {
            sql.append("\nWHERE ").append(String.join(" AND ", conditions));
        }

        if (!CollectionUtils.isEmpty(cmd.getOrders())) {
            List<String> orderParts = new ArrayList<>();
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                String orderCol = null;
                if (StringUtils.hasText(order.getMetricCode())) {
                    outer:
                    for (FactGroup group : factGroups) {
                        for (MetricInfo info : group.metrics) {
                            if (order.getMetricCode().equals(info.metricCode)) {
                                orderCol = "j.`" + info.alias + "`";
                                break outer;
                            }
                        }
                    }
                } else if (StringUtils.hasText(order.getDimCode())) {
                    for (DimensionInfo dim : dimensionInfos) {
                        if (order.getDimCode().equals(dim.dimCode)) {
                            String alias = dimAliasMap.get(dim.tableName);
                            if (StringUtils.hasText(alias)) {
                                orderCol = alias + "." + resolveSelectColumn(dim);
                            } else {
                                orderCol = resolveSelectColumn(dim);
                            }
                            break;
                        }
                    }
                }
                if (StringUtils.hasText(orderCol)) {
                    orderParts.add(orderCol + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append("\nORDER BY ").append(String.join(", ", orderParts));
            }
        }

        if (cmd.getLimitValue() != null && cmd.getLimitValue() > 0) {
            sql.append("\nLIMIT ").append(cmd.getLimitValue());
        }

        return sql.toString();
    }

    private static class DimensionInfo {
        String dimCode;
        String columnName;
        String displayColumn;
        String alias;
        String tableName;
    }
}
