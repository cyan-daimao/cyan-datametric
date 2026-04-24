package com.cyan.datametric.domain.config.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;

/**
 * 公共维度仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionRepository {

    /**
     * 根据ID查询
     */
    Dimension findById(String id);

    /**
     * 分页查询
     */
    Page<Dimension> page(DimensionPageQuery query);

    /**
     * 保存
     */
    Dimension save(Dimension dimension);

    /**
     * 更新
     */
    Dimension update(Dimension dimension);

    /**
     * 删除
     */
    void deleteById(String id);
}
