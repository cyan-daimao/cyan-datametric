package com.cyan.datametric.domain.config.query;

import com.cyan.arch.common.api.Pageable;
import lombok.Data;

/**
 * 修饰词分页查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class ModifierPageQuery implements Pageable {

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页大小
     */
    private long pageSize = 20;

    /**
     * 修饰词名称（模糊）
     */
    private String modifierName;

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
