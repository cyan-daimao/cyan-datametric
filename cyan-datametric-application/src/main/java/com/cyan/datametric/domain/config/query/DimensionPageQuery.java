package com.cyan.datametric.domain.config.query;

import com.cyan.arch.common.api.Pageable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 公共维度分页查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DimensionPageQuery implements Pageable {

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页大小
     */
    private long pageSize = 20;

    /**
     * 维度名称（模糊）
     */
    private String dimName;

    /**
     * 维度分类ID
     */
    private String categoryId;

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
