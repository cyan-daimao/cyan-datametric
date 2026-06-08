package com.cyan.datametric.domain.metric.repository;

import com.cyan.datametric.domain.metric.MetricFieldBinding;

import java.util.List;

/**
 * 指标字段绑定仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricFieldBindingRepository {

    /**
     * 根据ID查询绑定
     */
    MetricFieldBinding findById(String id);

    /**
     * 查询指标的字段绑定
     */
    List<MetricFieldBinding> findByMetricId(String metricId);

    /**
     * 保存绑定
     */
    MetricFieldBinding save(MetricFieldBinding binding);

    /**
     * 更新绑定
     */
    MetricFieldBinding update(MetricFieldBinding binding);

    /**
     * 删除绑定
     */
    void deleteById(String id);

    /**
     * 删除指标下所有绑定
     */
    void deleteByMetricId(String metricId);

    /**
     * 设置主绑定
     */
    void setPrimary(String metricId, String bindingId);
}
