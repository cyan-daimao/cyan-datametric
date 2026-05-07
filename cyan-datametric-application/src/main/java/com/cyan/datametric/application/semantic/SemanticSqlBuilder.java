package com.cyan.datametric.application.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.application.semantic.JoinPathResolver.JoinEdge;
import com.cyan.datametric.domain.semantic.LogicalTable;
import com.cyan.datametric.domain.semantic.SemanticMetric;
import com.cyan.datametric.enums.MetricType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语义层 SQL 生成器
 * <p>
 * 基于 JOIN 路径和语义指标/维度定义，生成可在 StarRocks 执行的 SQL。
 * 兼容 Phase 1/2 的聚合表达式逻辑，支持跨表自动 JOIN。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
public class SemanticSqlBuilder {

    /**
     * 生成分析 SQL
     *
     * @param metrics      语义指标列表
     * @param dimensions   维度定义（维度表ID -> 维度字段）
     * @param joinEdges    JOIN 路径
     * @param tableMap     逻辑表 ID -> LogicalTable 映射
     * @param filters      用户过滤条件
     * @param orders       排序条件
     * @param limit        限制行数
     * @return 可执行 SQL 字符串
     */
    public String build(List<SemanticMetric> metrics,
                        List<DimensionRef> dimensions,
                        List<JoinEdge> joinEdges,
                        Map<String, LogicalTable> tableMap,
                        List<FilterRef> filters,
                        List<OrderRef> orders,
                        Integer limit) {
        Assert.notEmpty(metrics, new BusinessException("指标列表不能为空"));

        // 表别名映射：逻辑表ID -> 别名
        Map<String, String> aliasMap = buildAliasMap(joinEdges);
        // 起始事实表（主表）
        String primaryFactId = resolvePrimaryFactId(metrics, joinEdges);
        LogicalTable primaryFact = tableMap.get(primaryFactId);
        Assert.notNull(primaryFact, new BusinessException("主事实表不存在"));

        StringBuilder sql = new StringBuilder();

        // === SELECT ===
        sql.append("SELECT ");
        List<String> selectItems = new ArrayList<>();

        // 维度列
        for (DimensionRef dim : dimensions) {
            String alias = aliasMap.get(dim.getTableId());
            if (alias == null) {
                // 维度可能在主事实表上
                alias = aliasMap.get(primaryFactId);
            }
            String colExpr = alias + "." + dim.getColumnName();
            selectItems.add(colExpr + " AS `" + dim.getAlias() + "`");
        }

        // 指标列
        for (SemanticMetric metric : metrics) {
            selectItems.add(buildMetricSelect(metric, aliasMap, tableMap));
        }
        sql.append(String.join(", ", selectItems));

        // === FROM ===
        sql.append(" FROM ").append(primaryFact.getTableName()).append(" ").append(aliasMap.get(primaryFactId));

        // === JOIN ===
        if (!CollectionUtils.isEmpty(joinEdges)) {
            for (JoinEdge edge : joinEdges) {
                LogicalTable rightTable = tableMap.get(edge.getRightTableId());
                Assert.notNull(rightTable, new BusinessException("JOIN 右表不存在: " + edge.getRightTableId()));
                String joinSql = edge.getRelationship().buildJoinSql(edge.getLeftAlias(), edge.getRightAlias(), rightTable.getTableName());
                sql.append(" ").append(joinSql);
            }
        }

        // === WHERE ===
        List<String> whereConditions = new ArrayList<>();

        // 指标自带过滤（原子/派生指标）
        for (SemanticMetric metric : metrics) {
            if (metric.getMetricType() == MetricType.ATOMIC || metric.getMetricType() == MetricType.DERIVED) {
                // TODO: 从原子指标扩展中解析过滤条件并加入 WHERE
                // 当前简化处理，实际需加载原子指标过滤配置
            }
        }

        // 用户过滤条件
        if (!CollectionUtils.isEmpty(filters)) {
            for (FilterRef filter : filters) {
                String condition = buildFilterCondition(filter, aliasMap, tableMap);
                if (StringUtils.hasText(condition)) {
                    whereConditions.add(condition);
                }
            }
        }

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        // === GROUP BY ===
        if (!CollectionUtils.isEmpty(dimensions)) {
            String groupBy = dimensions.stream()
                    .map(dim -> {
                        String alias = aliasMap.get(dim.getTableId());
                        if (alias == null) {
                            alias = aliasMap.get(primaryFactId);
                        }
                        return alias + "." + dim.getColumnName();
                    })
                    .collect(Collectors.joining(", "));
            sql.append(" GROUP BY ").append(groupBy);
        }

        // === ORDER BY ===
        if (!CollectionUtils.isEmpty(orders)) {
            List<String> orderParts = new ArrayList<>();
            for (OrderRef order : orders) {
                String expr = resolveOrderExpr(order, metrics, dimensions, aliasMap, primaryFactId);
                if (StringUtils.hasText(expr)) {
                    orderParts.add(expr + " " + order.getDirection());
                }
            }
            if (!orderParts.isEmpty()) {
                sql.append(" ORDER BY ").append(String.join(", ", orderParts));
            }
        }

        // === LIMIT ===
        int limitValue = (limit != null && limit > 0) ? limit : 1000;
        if (limitValue > 10000) {
            limitValue = 10000;
        }
        sql.append(" LIMIT ").append(limitValue);

        return sql.toString();
    }

    /**
     * 为单个指标生成 SELECT 表达式
     */
    private String buildMetricSelect(SemanticMetric metric,
                                     Map<String, String> aliasMap,
                                     Map<String, LogicalTable> tableMap) {
        String alias = aliasMap.get(metric.getSourceTableId());
        if (alias == null) {
            throw new BusinessException("指标来源表未在 JOIN 路径中: " + metric.getMetricCode());
        }

        if (metric.isComposite()) {
            // 复合指标：替换公式中的 ${metricCode} 为对应聚合表达式
            return buildCompositeExpression(metric, aliasMap, tableMap);
        }

        String aggExpr = metric.buildAggExpression(alias);
        return aggExpr + " AS `" + metric.getMetricCode() + "`";
    }

    /**
     * 构建复合指标表达式
     */
    private String buildCompositeExpression(SemanticMetric metric,
                                            Map<String, String> aliasMap,
                                            Map<String, LogicalTable> tableMap) {
        // 简化实现：直接返回公式（假设引用指标已在同一查询中定义，需要子查询或 WITH 支持）
        // 实际生产环境建议：复合指标通过子查询或 WITH 语句实现
        String formula = metric.getFormula();
        if (formula == null) {
            return "0 AS `" + metric.getMetricCode() + "`";
        }
        // 替换 ${Mxxx} 为占位（这里简化处理，实际应递归解析）
        String simplified = formula.replace("${", "").replace("}", "");
        return simplified + " AS `" + metric.getMetricCode() + "`";
    }

    /**
     * 构建过滤条件 SQL
     */
    private String buildFilterCondition(FilterRef filter,
                                        Map<String, String> aliasMap,
                                        Map<String, LogicalTable> tableMap) {
        String column = filter.getColumnName();
        String tableId = filter.getTableId();
        if (StringUtils.hasText(tableId)) {
            String alias = aliasMap.get(tableId);
            if (alias != null) {
                column = alias + "." + column;
            }
        }

        List<String> values = filter.getValues();
        if (values == null || values.isEmpty()) {
            return null;
        }

        String operator = filter.getOperator();
        return switch (operator.toUpperCase()) {
            case "EQ", "=" -> column + " = '" + escapeSql(values.get(0)) + "'";
            case "NE", "!=" -> column + " != '" + escapeSql(values.get(0)) + "'";
            case "GT", ">" -> column + " > '" + escapeSql(values.get(0)) + "'";
            case "GTE", ">=" -> column + " >= '" + escapeSql(values.get(0)) + "'";
            case "LT", "<" -> column + " < '" + escapeSql(values.get(0)) + "'";
            case "LTE", "<=" -> column + " <= '" + escapeSql(values.get(0)) + "'";
            case "IN" -> {
                String vals = values.stream().map(this::escapeSql).map(v -> "'" + v + "'")
                        .collect(Collectors.joining(","));
                yield column + " IN (" + vals + ")";
            }
            case "NOT_IN" -> {
                String vals = values.stream().map(this::escapeSql).map(v -> "'" + v + "'")
                        .collect(Collectors.joining(","));
                yield column + " NOT IN (" + vals + ")";
            }
            case "BETWEEN" -> {
                if (values.size() >= 2) {
                    yield column + " BETWEEN '" + escapeSql(values.get(0)) + "' AND '" + escapeSql(values.get(1)) + "'";
                }
                yield null;
            }
            case "IS_NULL" -> column + " IS NULL";
            case "IS_NOT_NULL" -> column + " IS NOT NULL";
            case "LIKE" -> column + " LIKE '" + escapeSql(values.get(0)) + "'";
            case "NOT_LIKE" -> column + " NOT LIKE '" + escapeSql(values.get(0)) + "'";
            default -> throw new BusinessException("不支持的过滤操作符: " + operator);
        };
    }

    private String resolveOrderExpr(OrderRef order,
                                    List<SemanticMetric> metrics,
                                    List<DimensionRef> dimensions,
                                    Map<String, String> aliasMap,
                                    String primaryFactId) {
        if (StringUtils.hasText(order.getMetricCode())) {
            return "`" + order.getMetricCode() + "`";
        } else if (StringUtils.hasText(order.getColumnName())) {
            String alias = aliasMap.get(order.getTableId());
            if (alias == null) {
                alias = aliasMap.get(primaryFactId);
            }
            return alias + "." + order.getColumnName();
        }
        return null;
    }

    private Map<String, String> buildAliasMap(List<JoinEdge> joinEdges) {
        Map<String, String> map = new HashMap<>();
        if (joinEdges == null) {
            return map;
        }
        for (JoinEdge edge : joinEdges) {
            if (!map.containsKey(edge.getLeftTableId())) {
                map.put(edge.getLeftTableId(), edge.getLeftAlias());
            }
            if (!map.containsKey(edge.getRightTableId())) {
                map.put(edge.getRightTableId(), edge.getRightAlias());
            }
        }
        return map;
    }

    private String resolvePrimaryFactId(List<SemanticMetric> metrics, List<JoinEdge> joinEdges) {
        // 取第一个指标的来源表作为主事实表
        return metrics.get(0).getSourceTableId();
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    // ==================== 内部引用类 ====================

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class DimensionRef {
        private String tableId;
        private String columnName;
        private String alias;
    }

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class FilterRef {
        private String tableId;
        private String columnName;
        private String operator;
        private List<String> values;
    }

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class OrderRef {
        private String metricCode;
        private String tableId;
        private String columnName;
        private String direction;
    }
}
