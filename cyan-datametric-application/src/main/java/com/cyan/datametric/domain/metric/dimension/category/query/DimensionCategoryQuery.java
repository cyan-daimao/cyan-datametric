package com.cyan.datametric.domain.metric.dimension.category.query;

import com.cyan.arch.common.api.Pageable;
import lombok.Data;

/**
 * 维度分类查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class DimensionCategoryQuery implements Pageable {

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页大小
     */
    private long pageSize = 20;

    /**
     * 分类名称（模糊）
     */
    private String name;

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
