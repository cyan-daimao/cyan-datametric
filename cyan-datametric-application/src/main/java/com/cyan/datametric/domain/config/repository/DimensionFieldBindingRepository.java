package com.cyan.datametric.domain.config.repository;

import com.cyan.datametric.domain.config.DimensionFieldBinding;

import java.util.List;

/**
 * 维度字段绑定仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionFieldBindingRepository {

    /**
     * 根据ID查询绑定
     */
    DimensionFieldBinding findById(String id);

    /**
     * 查询维度的字段绑定
     */
    List<DimensionFieldBinding> findByDimId(String dimId);

    /**
     * 保存绑定
     */
    DimensionFieldBinding save(DimensionFieldBinding binding);

    /**
     * 更新绑定
     */
    DimensionFieldBinding update(DimensionFieldBinding binding);

    /**
     * 删除绑定
     */
    void deleteById(String id);

    /**
     * 删除维度下所有绑定
     */
    void deleteByDimId(String dimId);

    /**
     * 设置主绑定
     */
    void setPrimary(String dimId, String bindingId);
}
