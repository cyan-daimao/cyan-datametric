package com.cyan.datametric.domain.config.query;

import com.cyan.arch.common.api.Pageable;
import lombok.Data;

/**
 * 公共维度分页查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
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

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
