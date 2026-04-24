package com.cyan.datametric.domain.metric.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;

import java.util.List;

/**
 * 指标仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricRepository {

    /**
     * 根据ID查询
     */
    Metric findById(String id);

    /**
     * 分页查询
     */
    Page<Metric> page(MetricPageQuery query);

    /**
     * 根据名称查询
     */
    Metric findByName(String metricName);

    /**
     * 保存指标
     */
    Metric save(Metric metric);

    /**
     * 更新指标
     */
    Metric update(Metric metric);

    /**
     * 删除指标
     */
    void deleteById(String id);

    /**
     * 查询下游引用该指标的指标列表
     */
    List<Metric> findDownstreamMetrics(String metricId);

    /**
     * 统计指标数量
     */
    long countByType(String metricType);

    /**
     * 统计各状态数量
     */
    long countByStatus(String status);

    /**
     * 按主题域统计
     */
    List<java.util.Map<String, Object>> countBySubject();
}
