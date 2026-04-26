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
     * 按ID列表分页查询
     */
    Page<Metric> pageByIds(List<String> ids, MetricPageQuery query);

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

    /**
     * 保存指标快照到历史表
     */
    void saveSnapshot(Metric metric);

    /**
     * 查询指标的历史版本列表
     */
    List<Metric> findHistoryByMetricCode(String metricCode);

    /**
     * 根据指标编码和版本号查询历史记录
     */
    Metric findHistoryByVersion(String metricCode, Integer version);

    /**
     * 回退：用历史记录覆盖主表
     */
    Metric rollbackFromHistory(Metric historyMetric);
}
