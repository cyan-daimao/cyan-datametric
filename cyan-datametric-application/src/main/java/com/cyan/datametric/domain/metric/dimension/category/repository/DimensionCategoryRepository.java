package com.cyan.datametric.domain.metric.dimension.category.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.query.DimensionCategoryQuery;

import java.util.List;

/**
 * 维度分类仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionCategoryRepository {

    /**
     * 根据ID查询
     */
    DimensionCategory findById(String id);

    /**
     * 分页查询
     */
    Page<DimensionCategory> page(DimensionCategoryQuery query);

    /**
     * 查询全部
     */
    List<DimensionCategory> findAll();

    /**
     * 保存
     */
    DimensionCategory save(DimensionCategory category);

    /**
     * 更新
     */
    DimensionCategory update(DimensionCategory category);

    /**
     * 删除
     */
    void deleteById(String id);
}
