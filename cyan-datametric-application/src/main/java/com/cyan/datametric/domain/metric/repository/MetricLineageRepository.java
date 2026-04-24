package com.cyan.datametric.domain.metric.repository;

import com.cyan.datametric.domain.metric.LineageNode;

import java.util.List;

/**
 * 指标血缘仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricLineageRepository {

    /**
     * 查询上游血缘
     */
    List<LineageNode> findUpstream(String metricId);

    /**
     * 查询下游血缘
     */
    List<LineageNode> findDownstream(String metricId);

    /**
     * 保存血缘关系
     */
    void saveAll(List<LineageNode> nodes);

    /**
     * 删除指标相关血缘
     */
    void deleteByMetricId(String metricId);
}
