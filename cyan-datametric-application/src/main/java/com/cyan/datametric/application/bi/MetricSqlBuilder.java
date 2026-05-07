package com.cyan.datametric.application.bi;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.adapter.bi.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.enums.StatFunc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final DimensionRepository dimensionRepository;

    /**
     * 生成分析SQL
     *
     * @param cmd             DSL请求
     * @param resolvedMetrics 已展开的指标列表
     * @param tableName       统一的事实表名
     * @return SQL字符串
     */
    public String build(MetricBiAnalysisCmd cmd, List<ResolvedMetric> resolvedMetrics, String tableName) {
        StringBuilder sql = new StringBuilder();

        // SELECT
        sql.append("SELECT ");
        List<String> selectItems = new ArrayList<>();

        // 维度列
        List<DimensionInfo> dimensionInfos = resolveDimensions(cmd.getDimensions(), tableName);
        for (DimensionInfo dim : dimensionInfos) {
            selectItems.add(dim.getColumnExpr() + " AS `" + dim.alias() + "`");
        }

        // 指标列
        for (ResolvedMetric metric : resolvedMetrics) {
            selectItems.add(buildMetricSelect(metric));
        }
        sql.append(String.join(", ", selectItems));

        // FROM
        sql.append(" FROM ").append(tableName);

        // WHERE（仅用户过滤条件）
        List<String> whereConditions = buildWhereConditions(cmd.getFilters(), dimensionInfos);
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        // GROUP BY
        if (!dimensionInfos.isEmpty()) {
            sql.append(" GROUP BY ").append(
                    dimensionInfos.stream()
                            .map(DimensionInfo::columnName)
                            .collect(Collectors.joining(", "))
            );
        }

        // ORDER BY
        List<String> orderConditions = buildOrderConditions(cmd.getOrders(), resolvedMetrics, dimensionInfos);
        if (!orderConditions.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orderConditions));
        }

        // LIMIT
        int limit = cmd.getLimitValue() != null && cmd.getLimitValue() > 0 ? cmd.getLimitValue() : 1000;
        if (limit > 10000) {
            limit = 10000;
        }
        sql.append(" LIMIT ").append(limit);

        return sql.toString();
    }

    /**
     * 为单个指标生成SELECT表达式
     */
    private String buildMetricSelect(ResolvedMetric metric) {
        if (metric.isBaseMetric()) {
            String aggExpr = buildAggExpression(metric);
            return aggExpr + " AS `" + metric.getAlias() + "`";
        }

        // 复合指标：替换公式中的 ${Mxxx}
        String formula = metric.getFormula();
        Map<String, ResolvedMetric> refMap = metric.getRefMetrics().stream()
                .collect(Collectors.toMap(ResolvedMetric::getMetricId, m -> m, (a, b) -> a));

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}").matcher(formula);
        while (matcher.find()) {
            result.append(formula, lastEnd, matcher.start());
            String refId = matcher.group(1);
            ResolvedMetric refMetric = refMap.get(refId);
            Assert.notNull(refMetric, new BusinessException("复合指标引用的指标未找到: " + refId));
            result.append(buildAggExpression(refMetric));
            lastEnd = matcher.end();
        }
        result.append(formula.substring(lastEnd));
        return result + " AS `" + metric.getAlias() + "`";
    }

    /**
     * 生成聚合表达式（含CASE WHEN过滤）
     */
    private String buildAggExpression(ResolvedMetric metric) {
        List<String> conditions = new ArrayList<>();

        // 原子指标自带过滤
        if (metric.getAtomicFilters() != null) {
            for (MetricAtomicExt.FilterCondition f : metric.getAtomicFilters()) {
                conditions.add(f.getField() + " " + f.getOp() + " '" + f.getValue() + "'");
            }
        }

        // 修饰词过滤
        if (metric.getModifiers() != null) {
            for (Modifier m : metric.getModifiers()) {
                if (m.getFieldValues() != null && !m.getFieldValues().isEmpty()) {
                    String values = m.getFieldValues().stream()
                            .map(v -> "'" + v + "'")
                            .collect(Collectors.joining(","));
                    conditions.add(m.getFieldName() + " " + m.getOperator() + " (" + values + ")");
                }
            }
        }

        // 时间周期过滤
        if (metric.getTimePeriod() != null) {
            TimePeriod period = metric.getTimePeriod();
            if (period.getPeriodType() == PeriodType.RELATIVE && period.getRelativeValue() != null) {
                // 默认使用 dt 字段，若需要可扩展为配置化
                conditions.add("dt >= date_sub(current_date, " + Math.abs(period.getRelativeValue()) + ")");
            }
        }

        String func = metric.getStatFunc() == null ? "SUM" : metric.getStatFunc().getCode();
        String col = metric.getColName();

        if (conditions.isEmpty()) {
            return buildAggFunction(func, col);
        }

        // 有过滤条件时使用 CASE WHEN
        String caseWhen = "CASE WHEN " + String.join(" AND ", conditions) + " THEN " + col + " END";
        return buildAggFunction(func, caseWhen);
    }

    private String buildAggFunction(String func, String expr) {
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT " + expr + ")";
        }
        return func + "(" + expr + ")";
    }

    /**
     * 解析维度引用为维度信息
     */
    private List<DimensionInfo> resolveDimensions(List<MetricBiAnalysisCmd.DimensionRef> dimRefs, String tableName) {
        List<DimensionInfo> result = new ArrayList<>();
        if (dimRefs == null) {
            return result;
        }
        for (MetricBiAnalysisCmd.DimensionRef ref : dimRefs) {
            Dimension dimension = dimensionRepository.findById(ref.getDimId());
            Assert.notNull(dimension, new BusinessException(MetricBiErrorCode.DIMENSION_NOT_FOUND.getMessage()));
            String columnName = dimension.getColumnName();
            Assert.notBlank(columnName, new BusinessException("维度未配置物理字段: " + dimension.getDimName()));
            // 一期不做维度表 JOIN，维度仅提供 columnName 用于 SELECT/GROUP BY
            // 维度的 tableName 是维度表元数据，不与事实表做一致性校验
            result.add(new DimensionInfo(
                    ref.getDimId(),
                    ref.getAlias() != null && !ref.getAlias().isBlank() ? ref.getAlias() : dimension.getDimName(),
                    columnName
            ));
        }
        return result;
    }

    /**
     * 构建WHERE条件（仅用户传入的过滤条件）
     */
    private List<String> buildWhereConditions(List<MetricBiAnalysisCmd.FilterRef> filters, List<DimensionInfo> dimensionInfos) {
        List<String> conditions = new ArrayList<>();
        if (filters == null) {
            return conditions;
        }

        Map<String, DimensionInfo> dimMap = dimensionInfos.stream()
                .collect(Collectors.toMap(DimensionInfo::dimId, d -> d));

        for (MetricBiAnalysisCmd.FilterRef filter : filters) {
            if (filter.getDimId() != null && !filter.getDimId().isBlank()) {
                DimensionInfo dim = dimMap.get(filter.getDimId());
                if (dim == null) {
                    // 尝试从仓库加载维度信息
                    Dimension dimension = dimensionRepository.findById(filter.getDimId());
                    Assert.notNull(dimension, new BusinessException(MetricBiErrorCode.DIMENSION_NOT_FOUND.getMessage()));
                    dim = new DimensionInfo(filter.getDimId(), dimension.getDimName(), dimension.getColumnName());
                }
                String condition = buildFilterCondition(dim.columnName(), filter.getOperator(), filter.getValues());
                conditions.add(condition);
            }
            // 指标级过滤一期暂不处理
        }

        return conditions;
    }

    private String buildFilterCondition(String column, String operator, List<String> values) {
        return switch (operator.toUpperCase()) {
            case "EQ" -> column + " = '" + values.get(0) + "'";
            case "NE" -> column + " != '" + values.get(0) + "'";
            case "GT" -> column + " > '" + values.get(0) + "'";
            case "GTE" -> column + " >= '" + values.get(0) + "'";
            case "LT" -> column + " < '" + values.get(0) + "'";
            case "LTE" -> column + " <= '" + values.get(0) + "'";
            case "IN" -> {
                String vals = values.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
                yield column + " IN (" + vals + ")";
            }
            case "NOT_IN" -> {
                String vals = values.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
                yield column + " NOT IN (" + vals + ")";
            }
            case "BETWEEN" -> column + " BETWEEN '" + values.get(0) + "' AND '" + values.get(1) + "'";
            case "IS_NULL" -> column + " IS NULL";
            case "IS_NOT_NULL" -> column + " IS NOT NULL";
            case "LIKE" -> column + " LIKE '" + values.get(0) + "'";
            case "NOT_LIKE" -> column + " NOT LIKE '" + values.get(0) + "'";
            default -> throw new BusinessException("不支持的过滤操作符: " + operator);
        };
    }

    /**
     * 构建ORDER BY条件
     */
    private List<String> buildOrderConditions(List<MetricBiAnalysisCmd.OrderRef> orders,
                                               List<ResolvedMetric> resolvedMetrics,
                                               List<DimensionInfo> dimensionInfos) {
        List<String> conditions = new ArrayList<>();
        if (orders == null) {
            return conditions;
        }

        Map<String, ResolvedMetric> metricMap = resolvedMetrics.stream()
                .collect(Collectors.toMap(ResolvedMetric::getMetricId, m -> m));
        Map<String, DimensionInfo> dimMap = dimensionInfos.stream()
                .collect(Collectors.toMap(DimensionInfo::dimId, d -> d));

        for (MetricBiAnalysisCmd.OrderRef order : orders) {
            String expr = null;
            if (order.getMetricId() != null && !order.getMetricId().isBlank()) {
                ResolvedMetric metric = metricMap.get(order.getMetricId());
                if (metric != null) {
                    expr = "`" + metric.getAlias() + "`";
                }
            } else if (order.getDimId() != null && !order.getDimId().isBlank()) {
                DimensionInfo dim = dimMap.get(order.getDimId());
                if (dim != null) {
                    expr = dim.columnName();
                }
            }
            if (expr != null) {
                conditions.add(expr + " " + order.getDirection());
            }
        }

        return conditions;
    }

    /**
     * 维度信息内部类
     */
    private record DimensionInfo(String dimId, String alias, String columnName) {
        String getColumnExpr() {
            return columnName;
        }
    }
}
