package com.cyan.datametric.application.bi;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Response;
import com.cyan.datametric.application.dimension.DimensionResolver;
import com.cyan.datametric.application.dimension.ResolvedDimension;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.domain.config.DimensionFieldBinding;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricFieldBinding;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.infra.gateway.TableRelationGateway;
import com.cyan.dataman.client.table.dto.JoinPathsRequestDTO;
import com.cyan.dataman.client.table.dto.TableRelationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 指标SQL生成器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MetricSqlBuilder {

    private final DimensionResolver dimensionResolver;
    private final TableRelationGateway tableRelationGateway;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    /**
     * 生成分析SQL
     *
     * @param cmd             DSL请求
     * @param resolvedMetrics 已展开的指标列表
     * @param ignoredTableName 兼容旧签名，实际由绑定规划器选择事实表
     * @return SQL字符串
     */
    public String build(MetricBiAnalysisCmd cmd, List<ResolvedMetric> resolvedMetrics, String ignoredTableName) {
        List<ResolvedDimension> dimensions = resolveDimensions(cmd.getDimensions());
        List<ResolvedDimension> planningDimensions = resolvePlanningDimensions(cmd, dimensions);
        List<ResolvedMetric> planMetrics = planningMetrics(resolvedMetrics);
        QueryPlan plan = choosePlan(planMetrics, planningDimensions);
        if (plan.factTables().size() > 1 && dimensions.isEmpty()) {
            throw new BusinessException("跨事实表指标查询必须选择至少一个公共维度，避免指标结果被错误合并");
        }
        String sql = plan.factTables().size() == 1
                ? buildSingleFactSql(cmd, resolvedMetrics, dimensions, planningDimensions, plan)
                : buildMultiFactSql(cmd, resolvedMetrics, dimensions, planningDimensions, plan);
        int limit = cmd.getLimitValue() != null && cmd.getLimitValue() > 0 ? Math.min(cmd.getLimitValue(), 10000) : 1000;
        return sql + " LIMIT " + limit;
    }

    /**
     * 判断当前指标维度是否存在可执行绑定计划。
     *
     * @param resolvedMetrics    已展开指标
     * @param resolvedDimensions 已解析维度
     * @return 是否存在可执行计划
     */
    public boolean canPlan(List<ResolvedMetric> resolvedMetrics, List<ResolvedDimension> resolvedDimensions) {
        try {
            List<ResolvedMetric> planMetrics = planningMetrics(resolvedMetrics);
            QueryPlan plan = choosePlan(planMetrics, resolvedDimensions == null ? List.of() : resolvedDimensions);
            return plan.factTables().size() <= 1 || (resolvedDimensions != null && !resolvedDimensions.isEmpty());
        } catch (BusinessException e) {
            return false;
        }
    }

    /**
     * 展开参与物理规划的基础指标
     */
    private List<ResolvedMetric> planningMetrics(List<ResolvedMetric> metrics) {
        Map<String, ResolvedMetric> planMetrics = new LinkedHashMap<>();
        for (ResolvedMetric metric : metrics) {
            for (ResolvedMetric baseMetric : metric.flattenBaseMetrics()) {
                planMetrics.putIfAbsent(baseMetric.getMetricId(), baseMetric);
            }
        }
        return new ArrayList<>(planMetrics.values());
    }

    /**
     * 选择最少JOIN的绑定计划
     */
    private QueryPlan choosePlan(List<ResolvedMetric> metrics, List<ResolvedDimension> dimensions) {
        List<QueryPlan> candidates = new ArrayList<>();
        enumerateMetricBindings(metrics, 0, new LinkedHashMap<>(), candidates, dimensions);
        return candidates.stream()
                .min(Comparator.comparingInt(QueryPlan::score)
                        .thenComparing(QueryPlan::stableKey))
                .orElseThrow(() -> new BusinessException("当前指标和维度不存在可执行的字段绑定组合"));
    }

    /**
     * 枚举指标绑定组合
     */
    private void enumerateMetricBindings(List<ResolvedMetric> metrics,
                                         int index,
                                         Map<ResolvedMetric, MetricFieldBinding> selected,
                                         List<QueryPlan> candidates,
                                         List<ResolvedDimension> dimensions) {
        if (index >= metrics.size()) {
            buildCandidate(selected, dimensions).ifPresent(candidates::add);
            return;
        }
        ResolvedMetric metric = metrics.get(index);
        for (MetricFieldBinding binding : metricBindings(metric)) {
            selected.put(metric, binding);
            enumerateMetricBindings(metrics, index + 1, selected, candidates, dimensions);
            selected.remove(metric);
        }
    }

    /**
     * 构建候选计划
     */
    private java.util.Optional<QueryPlan> buildCandidate(Map<ResolvedMetric, MetricFieldBinding> metricBindings,
                                                         List<ResolvedDimension> dimensions) {
        Set<String> factTables = metricBindings.values().stream()
                .map(binding -> binding.tableRef(defaultCatalog))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Map<String, TableRelationDTO>> joins = new HashMap<>();
        Map<String, DimensionBindingPlan> dimensionPlans = new LinkedHashMap<>();

        for (ResolvedDimension dimension : dimensions) {
            DimensionBindingPlan dimensionPlan = chooseDimensionBinding(dimension, factTables, joins);
            if (dimensionPlan == null) {
                return java.util.Optional.empty();
            }
            dimensionPlans.put(dimension.getDimCode(), dimensionPlan);
        }

        int joinCount = joins.values().stream().mapToInt(Map::size).sum();
        int nonPrimary = (int) metricBindings.values().stream().filter(binding -> !Boolean.TRUE.equals(binding.getPrimaryBinding())).count()
                + (int) dimensionPlans.values().stream().filter(plan -> !Boolean.TRUE.equals(plan.binding().getPrimaryBinding())).count();
        int score = factTables.size() * 1000 + joinCount * 100 + nonPrimary * 10;
        String stableKey = metricBindings.values().stream().map(MetricFieldBinding::getId).collect(Collectors.joining(","))
                + "|"
                + dimensionPlans.values().stream().map(plan -> plan.binding().getId()).collect(Collectors.joining(","));
        return java.util.Optional.of(new QueryPlan(metricBindings, dimensionPlans, factTables, joins, score, stableKey));
    }

    /**
     * 选择维度绑定
     */
    private DimensionBindingPlan chooseDimensionBinding(ResolvedDimension dimension,
                                                       Set<String> factTables,
                                                       Map<String, Map<String, TableRelationDTO>> joins) {
        List<DimensionBindingPlan> plans = new ArrayList<>();
        for (DimensionFieldBinding binding : dimensionBindings(dimension)) {
            String tableRef = binding.tableRef(defaultCatalog);
            int joinCount = 0;
            boolean ok = true;
            if (binding.factBinding()) {
                ok = factTables.stream().allMatch(tableRef::equals);
            } else {
                for (String factTable : factTables) {
                    if (factTable.equals(tableRef)) {
                        continue;
                    }
                    TableRelationDTO relation = findJoin(factTable, tableRef);
                    if (relation == null) {
                        ok = false;
                        break;
                    }
                    joins.computeIfAbsent(factTable, k -> new LinkedHashMap<>()).put(tableRef, relation);
                    joinCount++;
                }
            }
            if (ok) {
                plans.add(new DimensionBindingPlan(dimension, binding, tableRef, joinCount));
            }
        }
        return plans.stream()
                .min(Comparator.comparingInt(DimensionBindingPlan::joinCount)
                        .thenComparing(plan -> Boolean.TRUE.equals(plan.binding().getPrimaryBinding()) ? 0 : 1)
                        .thenComparing(plan -> plan.binding().getId()))
                .orElse(null);
    }

    /**
     * 构建单事实表SQL
     */
    private String buildSingleFactSql(MetricBiAnalysisCmd cmd,
                                      List<ResolvedMetric> metrics,
                                      List<ResolvedDimension> dimensions,
                                      List<ResolvedDimension> planningDimensions,
                                      QueryPlan plan) {
        String factTable = plan.factTables().iterator().next();
        String factAlias = "f0";
        StringBuilder sql = new StringBuilder("SELECT ");
        List<String> selectItems = new ArrayList<>();
        for (ResolvedDimension dimension : dimensions) {
            DimensionBindingPlan dimensionPlan = plan.dimensionPlans().get(dimension.getDimCode());
            String alias = dimensionAlias(cmd, dimension);
            selectItems.add(dimensionExpression(dimensionPlan, factTable, factAlias, plan, 1) + " AS `" + alias + "`");
        }
        for (ResolvedMetric metric : metrics) {
            selectItems.add(buildMetricSelect(metric, factAlias, plan));
        }
        sql.append(String.join(", ", selectItems))
                .append(" FROM ").append(factTable).append(" ").append(factAlias);

        appendDimensionJoins(sql, factTable, factAlias, plan);

        List<String> conditions = buildWhereConditions(cmd.getFilters(), planningDimensions, plan, factTable, factAlias);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        appendGroupBy(sql, dimensions, plan, factTable, factAlias);
        appendOrderBy(sql, cmd, metrics, dimensions);
        return sql.toString();
    }

    /**
     * 构建多事实表SQL
     */
    private String buildMultiFactSql(MetricBiAnalysisCmd cmd,
                                     List<ResolvedMetric> metrics,
                                     List<ResolvedDimension> dimensions,
                                     List<ResolvedDimension> planningDimensions,
                                     QueryPlan plan) {
        Map<String, List<ResolvedMetric>> metricGroups = metrics.stream()
                .peek(metric -> {
                    Set<String> tables = metric.flattenBaseMetrics().stream()
                            .map(baseMetric -> plan.metricBindings().get(baseMetric).tableRef(defaultCatalog))
                            .collect(Collectors.toSet());
                    if (tables.size() > 1) {
                        throw new BusinessException("复合指标 '" + metric.getAlias() + "' 引用了多个事实表，暂不支持跨事实表公式直接展开");
                    }
                })
                .collect(Collectors.groupingBy(metric -> firstMetricTable(metric, plan),
                        LinkedHashMap::new, Collectors.toList()));
        List<String> subQueries = new ArrayList<>();
        int factIndex = 0;
        for (Map.Entry<String, List<ResolvedMetric>> entry : metricGroups.entrySet()) {
            String factTable = entry.getKey();
            String factAlias = "f";
            List<String> selectItems = new ArrayList<>();
            for (ResolvedDimension dimension : dimensions) {
                DimensionBindingPlan dimensionPlan = plan.dimensionPlans().get(dimension.getDimCode());
                String alias = dimensionAlias(cmd, dimension);
                selectItems.add(dimensionExpression(dimensionPlan, factTable, factAlias, plan, 1) + " AS `" + alias + "`");
            }
            for (ResolvedMetric metric : entry.getValue()) {
                selectItems.add(buildMetricSelect(metric, factAlias, plan));
            }
            StringBuilder sub = new StringBuilder("SELECT ")
                    .append(String.join(", ", selectItems))
                    .append(" FROM ").append(factTable).append(" ").append(factAlias);
            appendDimensionJoins(sub, factTable, factAlias, plan);
            List<String> conditions = buildWhereConditions(cmd.getFilters(), planningDimensions, plan, factTable, factAlias);
            if (!conditions.isEmpty()) {
                sub.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            appendGroupBy(sub, dimensions, plan, factTable, factAlias);
            subQueries.add("(" + sub + ") q" + factIndex++);
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        List<String> outerSelect = new ArrayList<>();
        for (ResolvedDimension dimension : dimensions) {
            String alias = dimensionAlias(cmd, dimension);
            outerSelect.add("q0.`" + alias + "` AS `" + alias + "`");
        }
        for (ResolvedMetric metric : metrics) {
            int groupIndex = groupIndex(metricGroups, firstMetricTable(metric, plan));
            outerSelect.add("q" + groupIndex + ".`" + metric.getAlias() + "` AS `" + metric.getAlias() + "`");
        }
        sql.append(String.join(", ", outerSelect)).append(" FROM ").append(subQueries.getFirst());
        for (int i = 1; i < subQueries.size(); i++) {
            sql.append(" LEFT JOIN ").append(subQueries.get(i)).append(" ON ");
            int right = i;
            sql.append(dimensions.stream()
                    .map(dimension -> {
                        String alias = dimensionAlias(cmd, dimension);
                        return "q0.`" + alias + "` = q" + right + ".`" + alias + "`";
                    })
                    .collect(Collectors.joining(" AND ")));
        }
        appendOrderBy(sql, cmd, metrics, dimensions);
        return sql.toString();
    }

    private int groupIndex(Map<String, List<ResolvedMetric>> groups, String tableRef) {
        int index = 0;
        for (String key : groups.keySet()) {
            if (key.equals(tableRef)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    /**
     * 追加维表JOIN
     */
    private void appendDimensionJoins(StringBuilder sql, String factTable, String factAlias, QueryPlan plan) {
        Map<String, TableRelationDTO> joins = plan.joins().get(factTable);
        if (joins == null || joins.isEmpty()) {
            return;
        }
        int index = 1;
        for (Map.Entry<String, TableRelationDTO> entry : joins.entrySet()) {
            TableRelationDTO relation = entry.getValue();
            String dimAlias = "d" + index++;
            String joinType = StringUtils.hasText(relation.getJoinType()) ? relation.getJoinType() : "LEFT";
            sql.append(" ").append(joinType).append(" JOIN ").append(entry.getKey()).append(" ").append(dimAlias)
                    .append(" ON ").append(factAlias).append(".`").append(relation.getSourceColumn()).append("`")
                    .append(" = ").append(dimAlias).append(".`").append(relation.getTargetColumn()).append("`");
        }
    }

    /**
     * 维度表达式
     */
    private String dimensionExpression(DimensionBindingPlan dimensionPlan,
                                       String factTable,
                                       String factAlias,
                                       QueryPlan plan,
                                       int firstDimAliasIndex) {
        ResolvedDimension dimension = dimensionPlan.dimension();
        DimensionFieldBinding binding = dimensionPlan.binding();
        String expr = resolveDimensionExpression(binding, true);
        if (binding.factBinding() || factTable.equals(binding.tableRef(defaultCatalog))) {
            return qualifyExpression(expr, factAlias);
        }
        int index = firstDimAliasIndex;
        Map<String, TableRelationDTO> joins = plan.joins().get(factTable);
        if (joins != null) {
            for (String dimTable : joins.keySet()) {
                if (dimTable.equals(binding.tableRef(defaultCatalog))) {
                    return qualifyExpression(expr, "d" + index);
                }
                index++;
            }
        }
        return qualifyExpression(expr, factAlias);
    }

    /**
     * 追加GROUP BY
     */
    private void appendGroupBy(StringBuilder sql,
                               List<ResolvedDimension> dimensions,
                               QueryPlan plan,
                               String factTable,
                               String factAlias) {
        if (dimensions.isEmpty()) {
            return;
        }
        String groupBy = dimensions.stream()
                .map(dimension -> dimensionExpression(plan.dimensionPlans().get(dimension.getDimCode()), factTable, factAlias, plan, 1))
                .collect(Collectors.joining(", "));
        sql.append(" GROUP BY ").append(groupBy);
    }

    /**
     * 追加ORDER BY
     */
    private void appendOrderBy(StringBuilder sql,
                               MetricBiAnalysisCmd cmd,
                               List<ResolvedMetric> metrics,
                               List<ResolvedDimension> dimensions) {
        if (CollectionUtils.isEmpty(cmd.getOrders())) {
            return;
        }
        Map<String, String> metricAliases = metrics.stream()
                .collect(Collectors.toMap(ResolvedMetric::getMetricId, ResolvedMetric::getAlias, (a, b) -> a));
        Map<String, String> dimensionAliases = dimensions.stream()
                .collect(Collectors.toMap(ResolvedDimension::getDimCode, dimension -> dimensionAlias(cmd, dimension), (a, b) -> a));
        List<String> orderItems = new ArrayList<>();
        for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
            if (StringUtils.hasText(order.getMetricCode()) && metricAliases.containsKey(order.getMetricCode())) {
                orderItems.add("`" + metricAliases.get(order.getMetricCode()) + "` " + safeDirection(order.getDirection()));
            }
            if (StringUtils.hasText(order.getDimCode()) && dimensionAliases.containsKey(order.getDimCode())) {
                orderItems.add("`" + dimensionAliases.get(order.getDimCode()) + "` " + safeDirection(order.getDirection()));
            }
        }
        if (!orderItems.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderItems));
        }
    }

    /**
     * 构建WHERE条件
     */
    private List<String> buildWhereConditions(List<MetricBiAnalysisCmd.FilterRef> filters,
                                              List<ResolvedDimension> dimensions,
                                              QueryPlan plan,
                                              String factTable,
                                              String factAlias) {
        if (CollectionUtils.isEmpty(filters)) {
            return List.of();
        }
        Map<String, ResolvedDimension> dimensionMap = dimensions.stream()
                .collect(Collectors.toMap(ResolvedDimension::getDimCode, d -> d, (a, b) -> a));
        List<String> conditions = new ArrayList<>();
        for (MetricBiAnalysisCmd.FilterRef filter : filters) {
            if (!StringUtils.hasText(filter.getDimCode())) {
                continue;
            }
            ResolvedDimension dimension = dimensionMap.get(filter.getDimCode());
            if (dimension == null) {
                dimension = dimensionResolver.resolve(filter.getDimCode());
            }
            DimensionBindingPlan dimensionPlan = plan.dimensionPlans().get(dimension.getDimCode());
            if (dimensionPlan == null) {
                continue;
            }
            String expr = dimensionExpression(dimensionPlan, factTable, factAlias, plan, 1);
            String condition = buildFilterCondition(expr, filter.getOperator(), filter.getValues());
            if (StringUtils.hasText(condition)) {
                conditions.add(condition);
            }
        }
        return conditions;
    }

    /**
     * 为单个指标生成SELECT表达式
     */
    private String buildMetricSelect(ResolvedMetric metric, String factAlias, QueryPlan plan) {
        if (metric.isBaseMetric()) {
            MetricFieldBinding binding = plan.metricBindings().get(metric);
            return buildAggExpression(metric, binding, factAlias) + " AS `" + metric.getAlias() + "`";
        }
        String formula = metric.getFormula();
        Map<String, ResolvedMetric> refMap = metric.getRefMetrics().stream()
                .collect(Collectors.toMap(ResolvedMetric::getMetricId, m -> m, (a, b) -> a));
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\$\\{([^}]+)}").matcher(formula);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(formula, lastEnd, matcher.start());
            ResolvedMetric refMetric = refMap.get(matcher.group(1));
            Assert.notNull(refMetric, new BusinessException("复合指标引用的指标未找到: " + matcher.group(1)));
            MetricFieldBinding refBinding = plan.metricBindings().get(refMetric);
            result.append(buildAggExpression(refMetric, refBinding, factAlias));
            lastEnd = matcher.end();
        }
        result.append(formula.substring(lastEnd));
        return result + " AS `" + metric.getAlias() + "`";
    }

    private String firstMetricTable(ResolvedMetric metric, QueryPlan plan) {
        ResolvedMetric baseMetric = metric.flattenBaseMetrics().getFirst();
        MetricFieldBinding binding = plan.metricBindings().get(baseMetric);
        return binding.tableRef(defaultCatalog);
    }

    /**
     * 构建聚合表达式
     */
    private String buildAggExpression(ResolvedMetric metric, MetricFieldBinding binding, String factAlias) {
        List<String> conditions = new ArrayList<>();
        for (MetricAtomicExt.FilterCondition filter : java.util.Optional.ofNullable(binding.getFilterCondition()).orElse(List.of())) {
            conditions.add(factAlias + ".`" + filter.getField() + "` " + filter.getOp() + " '" + escapeValue(filter.getValue()) + "'");
        }
        if (metric.getModifiers() != null) {
            for (Modifier modifier : metric.getModifiers()) {
                if (modifier.getFieldValues() != null && !modifier.getFieldValues().isEmpty()) {
                    String values = modifier.getFieldValues().stream()
                            .map(v -> "'" + escapeValue(v) + "'")
                            .collect(Collectors.joining(","));
                    conditions.add(factAlias + ".`" + modifier.getFieldName() + "` " + modifier.getOperator() + " (" + values + ")");
                }
            }
        }
        if (metric.getTimePeriod() != null) {
            TimePeriod period = metric.getTimePeriod();
            if (period.getPeriodType() == PeriodType.RELATIVE && period.getRelativeValue() != null) {
                conditions.add(factAlias + ".`dt` >= date_sub(current_date, " + Math.abs(period.getRelativeValue()) + ")");
            }
        }
        String sourceExpr = StringUtils.hasText(binding.getSourceExpr())
                ? qualifyExpression(binding.getSourceExpr(), factAlias)
                : factAlias + ".`" + binding.getColumnName() + "`";
        String func = metric.getStatFunc() == null ? "SUM" : metric.getStatFunc().getCode();
        if (conditions.isEmpty()) {
            return buildAggFunction(func, sourceExpr);
        }
        return buildAggFunction(func, "CASE WHEN " + String.join(" AND ", conditions) + " THEN " + sourceExpr + " END");
    }

    private String buildAggFunction(String func, String expr) {
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT " + expr + ")";
        }
        return func + "(" + expr + ")";
    }

    private List<ResolvedDimension> resolveDimensions(List<MetricBiAnalysisCmd.DimensionRef> dimRefs) {
        if (dimRefs == null) {
            return List.of();
        }
        List<ResolvedDimension> dimensions = new ArrayList<>();
        for (MetricBiAnalysisCmd.DimensionRef ref : dimRefs) {
            dimensions.add(dimensionResolver.resolve(ref.getDimCode()));
        }
        return dimensions;
    }

    /**
     * 解析参与绑定规划的维度。筛选和排序维度即使不展示，也必须参与 JOIN/绑定选择。
     */
    private List<ResolvedDimension> resolvePlanningDimensions(MetricBiAnalysisCmd cmd,
                                                              List<ResolvedDimension> selectedDimensions) {
        Map<String, ResolvedDimension> dimensions = new LinkedHashMap<>();
        for (ResolvedDimension dimension : selectedDimensions) {
            dimensions.put(dimension.getDimCode(), dimension);
        }
        if (cmd.getFilters() != null) {
            for (MetricBiAnalysisCmd.FilterRef filter : cmd.getFilters()) {
                if (StringUtils.hasText(filter.getDimCode()) && !dimensions.containsKey(filter.getDimCode())) {
                    dimensions.put(filter.getDimCode(), dimensionResolver.resolve(filter.getDimCode()));
                }
            }
        }
        if (cmd.getOrders() != null) {
            for (MetricBiAnalysisCmd.OrderRef order : cmd.getOrders()) {
                if (StringUtils.hasText(order.getDimCode()) && !dimensions.containsKey(order.getDimCode())) {
                    dimensions.put(order.getDimCode(), dimensionResolver.resolve(order.getDimCode()));
                }
            }
        }
        return new ArrayList<>(dimensions.values());
    }

    private List<MetricFieldBinding> metricBindings(ResolvedMetric metric) {
        if (metric.getFieldBindings() != null && !metric.getFieldBindings().isEmpty()) {
            return metric.getFieldBindings();
        }
        throw new BusinessException("指标 '" + metric.getAlias() + "' 未配置物理字段绑定");
    }

    private List<DimensionFieldBinding> dimensionBindings(ResolvedDimension dimension) {
        if (dimension.getFieldBindings() != null && !dimension.getFieldBindings().isEmpty()) {
            return dimension.getFieldBindings();
        }
        throw new BusinessException("维度 '" + dimension.getDimName() + "' 未配置物理字段绑定");
    }

    private String resolveDimensionExpression(DimensionFieldBinding binding, boolean display) {
        if (display && StringUtils.hasText(binding.getDisplayColumn())) {
            return "`" + binding.getDisplayColumn() + "`";
        }
        String sourceType = StringUtils.hasText(binding.getSourceType()) ? binding.getSourceType() : "COLUMN";
        if ("EXPRESSION".equals(sourceType)) {
            return binding.getSourceExpr();
        }
        if ("JSON_PATH".equals(sourceType)) {
            String path = StringUtils.hasText(binding.getSourceExpr()) ? binding.getSourceExpr() : "$.properties." + binding.getColumnName();
            return "JSON_VALUE(`properties`, '" + path.replace("'", "''") + "')";
        }
        return "`" + binding.getColumnName() + "`";
    }

    private TableRelationDTO findJoin(String factTableRef, String dimTableRef) {
        String[] factParts = factTableRef.split("\\.");
        String[] dimParts = dimTableRef.split("\\.");
        if (factParts.length != 3 || dimParts.length != 3) {
            return null;
        }
        JoinPathsRequestDTO request = new JoinPathsRequestDTO()
                .setFactTable(new JoinPathsRequestDTO.TableRefDTO(factParts[0], factParts[1], factParts[2]))
                .setDimensionTables(List.of(new JoinPathsRequestDTO.TableRefDTO(dimParts[0], dimParts[1], dimParts[2])));
        Response<List<TableRelationDTO>> response = tableRelationGateway.findJoinPaths(request);
        return response == null || response.getData() == null ? null : response.getData().stream().findFirst().orElse(null);
    }

    private String qualifyExpression(String expression, String alias) {
        if (!StringUtils.hasText(expression)) {
            return expression;
        }
        if (expression.matches("`[^`]+`")) {
            return alias + "." + expression;
        }
        if (expression.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return alias + ".`" + expression + "`";
        }
        String qualified = expression.replaceAll("`([^`]+)`", alias + ".`$1`");
        return qualified.replaceAll("\\bdt\\b", alias + ".`dt`");
    }

    private String buildFilterCondition(String column, String operator, List<String> values) {
        String op = StringUtils.hasText(operator) ? operator.toUpperCase() : "EQ";
        if (("IS_NULL".equals(op) || "IS_NOT_NULL".equals(op)) && (values == null || values.isEmpty())) {
            return "IS_NULL".equals(op) ? column + " IS NULL" : column + " IS NOT NULL";
        }
        if (values == null || values.isEmpty()) {
            return null;
        }
        return switch (op) {
            case "EQ", "=" -> column + " = '" + escapeValue(values.getFirst()) + "'";
            case "NE", "!=" -> column + " != '" + escapeValue(values.getFirst()) + "'";
            case "GT", ">" -> column + " > '" + escapeValue(values.getFirst()) + "'";
            case "GTE", ">=" -> column + " >= '" + escapeValue(values.getFirst()) + "'";
            case "LT", "<" -> column + " < '" + escapeValue(values.getFirst()) + "'";
            case "LTE", "<=" -> column + " <= '" + escapeValue(values.getFirst()) + "'";
            case "IN" -> column + " IN (" + values.stream().map(v -> "'" + escapeValue(v) + "'").collect(Collectors.joining(",")) + ")";
            case "NOT_IN" -> column + " NOT IN (" + values.stream().map(v -> "'" + escapeValue(v) + "'").collect(Collectors.joining(",")) + ")";
            case "BETWEEN" -> column + " BETWEEN '" + escapeValue(values.get(0)) + "' AND '" + escapeValue(values.get(1)) + "'";
            case "LIKE" -> column + " LIKE '%" + escapeValue(values.getFirst()) + "%'";
            case "NOT_LIKE" -> column + " NOT LIKE '%" + escapeValue(values.getFirst()) + "%'";
            default -> throw new BusinessException("不支持的过滤操作符: " + operator);
        };
    }

    private String dimensionAlias(MetricBiAnalysisCmd cmd, ResolvedDimension dimension) {
        if (cmd.getDimensions() != null) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                if (dimension.getDimCode().equals(ref.getDimCode()) && StringUtils.hasText(ref.getAlias())) {
                    return ref.getAlias();
                }
            }
        }
        return dimension.getDimName();
    }

    private String safeDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    private String escapeValue(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    /**
     * 查询计划
     */
    private record QueryPlan(Map<ResolvedMetric, MetricFieldBinding> metricBindings,
                             Map<String, DimensionBindingPlan> dimensionPlans,
                             Set<String> factTables,
                             Map<String, Map<String, TableRelationDTO>> joins,
                             int score,
                             String stableKey) {
    }

    /**
     * 维度绑定计划
     */
    private record DimensionBindingPlan(ResolvedDimension dimension,
                                        DimensionFieldBinding binding,
                                        String tableRef,
                                        int joinCount) {
    }
}
