package com.cyan.datametric.application.semantic;

import com.cyan.datametric.domain.semantic.MaterializedView;
import com.cyan.datametric.enums.semantic.MvStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询路由器
 * <p>
 * 根据查询请求（指标编码列表 + 维度字段列表）判断是否可以命中物化视图：
 * 1. 精确匹配：指标+维度完全一致
 * 2. 上卷匹配：物化粒度更粗，查询可二次聚合
 * 3. 部分匹配：物化视图包含查询所需全部字段，可加 WHERE 过滤
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Component
public class QueryRouter {

    /**
     * 路由决策结果
     */
    public record RouteDecision(
            boolean hit,
            String mvId,
            String rewrittenSql,
            String routeType // EXACT / ROLLUP / PARTIAL / NONE
    ) {
    }

    /**
     * 尝试路由到物化视图
     *
     * @param metricCodes   查询的指标编码列表
     * @param dimensions    查询的维度字段列表（格式：tableId.columnName）
     * @param candidateMvs  候选物化视图列表（通常为 ACTIVE 状态）
     * @return 路由决策
     */
    public RouteDecision route(List<String> metricCodes,
                                List<String> dimensions,
                                List<MaterializedView> candidateMvs) {
        if (CollectionUtils.isEmpty(candidateMvs) || CollectionUtils.isEmpty(metricCodes)) {
            return new RouteDecision(false, null, null, "NONE");
        }

        Set<String> metricSet = new HashSet<>(metricCodes);
        Set<String> dimSet = new HashSet<>(dimensions);

        // 1. 精确匹配
        for (MaterializedView mv : candidateMvs) {
            if (mv.getStatus() != MvStatus.ACTIVE) {
                continue;
            }
            if (isExactMatch(metricSet, dimSet, mv)) {
                log.info("精确匹配到物化视图: mvId={}, name={}", mv.getId(), mv.getName());
                return new RouteDecision(true, mv.getId(),
                        "SELECT * FROM " + mv.getName(), "EXACT");
            }
        }

        // 2. 上卷匹配（物化粒度更粗，查询可二次聚合）
        for (MaterializedView mv : candidateMvs) {
            if (mv.getStatus() != MvStatus.ACTIVE) {
                continue;
            }
            RollupCheckResult rollup = checkRollupMatch(metricSet, dimSet, mv);
            if (rollup.isMatch()) {
                log.info("上卷匹配到物化视图: mvId={}, name={}", mv.getId(), mv.getName());
                String rewrittenSql = buildRollupSql(mv, dimensions, rollup);
                return new RouteDecision(true, mv.getId(), rewrittenSql, "ROLLUP");
            }
        }

        // 3. 部分匹配（物化视图包含查询所需全部字段，可加过滤）
        for (MaterializedView mv : candidateMvs) {
            if (mv.getStatus() != MvStatus.ACTIVE) {
                continue;
            }
            if (isPartialMatch(metricSet, dimSet, mv)) {
                log.info("部分匹配到物化视图: mvId={}, name={}", mv.getId(), mv.getName());
                String rewrittenSql = buildPartialSql(mv, metricCodes, dimensions);
                return new RouteDecision(true, mv.getId(), rewrittenSql, "PARTIAL");
            }
        }

        return new RouteDecision(false, null, null, "NONE");
    }

    /**
     * 精确匹配：指标集合和维度集合完全一致
     */
    private boolean isExactMatch(Set<String> metricSet, Set<String> dimSet, MaterializedView mv) {
        Set<String> mvMetrics = toSet(mv.getMetrics());
        Set<String> mvDims = toSet(mv.getDimensions());
        return metricSet.equals(mvMetrics) && dimSet.equals(mvDims);
    }

    /**
     * 上卷匹配检查
     * <p>
     * 条件：
     * 1. 物化视图的指标集合 ⊇ 查询指标集合
     * 2. 物化视图的维度集合 ⊃ 查询维度集合（严格包含，物化粒度更粗）
     * 3. 查询维度是物化维度的上卷维度（如 日 -> 周 -> 月）
     * <p>
     * 简化实现：假设维度字段名包含层级关系（如 dt 可上卷到 week(dt)）
     * 实际生产环境可配置维度层级映射。
     */
    private RollupCheckResult checkRollupMatch(Set<String> metricSet, Set<String> dimSet, MaterializedView mv) {
        Set<String> mvMetrics = toSet(mv.getMetrics());
        Set<String> mvDims = toSet(mv.getDimensions());

        if (!mvMetrics.containsAll(metricSet)) {
            return RollupCheckResult.noMatch();
        }
        if (!mvDims.containsAll(dimSet)) {
            return RollupCheckResult.noMatch();
        }
        if (mvDims.size() <= dimSet.size()) {
            return RollupCheckResult.noMatch();
        }

        // 判断多余的维度是否可上卷：简化策略，若多余维度是时间维度 dt，则认为支持上卷
        Set<String> extraDims = new HashSet<>(mvDims);
        extraDims.removeAll(dimSet);
        boolean hasTimeRollup = extraDims.stream().anyMatch(d -> d.toLowerCase().contains("dt") || d.toLowerCase().contains("date"));

        return new RollupCheckResult(true, extraDims, hasTimeRollup);
    }

    /**
     * 部分匹配：物化视图包含查询所需全部字段（指标+维度）
     */
    private boolean isPartialMatch(Set<String> metricSet, Set<String> dimSet, MaterializedView mv) {
        Set<String> mvMetrics = toSet(mv.getMetrics());
        Set<String> mvDims = toSet(mv.getDimensions());
        return mvMetrics.containsAll(metricSet) && mvDims.containsAll(dimSet);
    }

    private String buildRollupSql(MaterializedView mv, List<String> queryDimensions, RollupCheckResult rollup) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // 查询维度（可能需要上卷函数）
        List<String> selectItems = new ArrayList<>();
        for (String dim : queryDimensions) {
            if (dim.toLowerCase().contains("week") && rollup.hasTimeRollup) {
                selectItems.add("WEEK(dt) AS " + dim);
            } else {
                selectItems.add(dim);
            }
        }

        // 指标需要二次聚合（SUM / COUNT 等可上卷）
        // 简化：假设物化视图中的指标已经是聚合后的别名，直接 SUM
        for (String metric : mv.getMetrics()) {
            selectItems.add("SUM(" + metric + ") AS " + metric);
        }
        sql.append(String.join(", ", selectItems));
        sql.append(" FROM ").append(mv.getName());
        sql.append(" GROUP BY ").append(String.join(", ", queryDimensions));
        return sql.toString();
    }

    private String buildPartialSql(MaterializedView mv, List<String> metricCodes, List<String> dimensions) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        List<String> selectItems = new ArrayList<>();
        for (String dim : dimensions) {
            selectItems.add(dim);
        }
        for (String metric : metricCodes) {
            selectItems.add(metric);
        }
        sql.append(String.join(", ", selectItems));
        sql.append(" FROM ").append(mv.getName());
        // 部分匹配场景下，用户可在外层继续加 WHERE，此处返回基础 SQL
        return sql.toString();
    }

    private Set<String> toSet(List<String> list) {
        if (list == null) {
            return Set.of();
        }
        return new HashSet<>(list);
    }

    // ==================== 内部类 ====================

    private record RollupCheckResult(boolean match, Set<String> extraDimensions, boolean hasTimeRollup) {
        static RollupCheckResult noMatch() {
            return new RollupCheckResult(false, Set.of(), false);
        }
    }
}
