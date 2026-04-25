package com.cyan.datametric.application.metric.dimension.category;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import com.cyan.datametric.application.metric.dimension.category.cmd.DimensionCategoryCmd;
import com.cyan.datametric.domain.metric.dimension.category.query.DimensionCategoryQuery;

import java.util.List;

/**
 * 维度分类服务接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionCategoryService {

    /**
     * 创建维度分类
     */
    DimensionCategoryBO create(DimensionCategoryCmd cmd);

    /**
     * 更新维度分类
     */
    DimensionCategoryBO update(String id, DimensionCategoryCmd cmd);

    /**
     * 删除维度分类
     */
    void delete(String id);

    /**
     * 详情
     */
    DimensionCategoryBO detail(String id);

    /**
     * 分页查询
     */
    Page<DimensionCategoryBO> page(DimensionCategoryQuery query);

    /**
     * 树形结构查询
     */
    List<DimensionCategoryBO> tree();
}
