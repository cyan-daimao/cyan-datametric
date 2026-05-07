package com.cyan.datametric.domain.semantic.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.SemanticMetric;

import java.util.List;

/**
 * 语义指标仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface SemanticMetricRepository {

    /**
     * 根据ID查询
     */
    SemanticMetric findById(String id);

    /**
     * 根据指标编码查询
     */
    SemanticMetric findByMetricCode(String metricCode);

    /**
     * 根据编码列表批量查询
     */
    List<SemanticMetric> findByMetricCodes(List<String> metricCodes);

    /**
     * 根据来源逻辑表ID查询
     */
    List<SemanticMetric> findBySourceTableId(String sourceTableId);

    /**
     * 分页查询
     */
    Page<SemanticMetric> page(int pageNum, int pageSize);

    /**
     * 保存
     */
    SemanticMetric save(SemanticMetric semanticMetric);

    /**
     * 更新
     */
    SemanticMetric update(SemanticMetric semanticMetric);

    /**
     * 删除
     */
    void deleteById(String id);
}
