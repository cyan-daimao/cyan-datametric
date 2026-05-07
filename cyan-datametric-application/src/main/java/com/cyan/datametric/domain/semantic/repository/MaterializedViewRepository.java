package com.cyan.datametric.domain.semantic.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.MaterializedView;

import java.util.List;

/**
 * 物化视图仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MaterializedViewRepository {

    /**
     * 根据ID查询
     */
    MaterializedView findById(String id);

    /**
     * 查询所有活跃物化视图
     */
    List<MaterializedView> findActiveAll();

    /**
     * 分页查询
     */
    Page<MaterializedView> page(int pageNum, int pageSize);

    /**
     * 保存
     */
    MaterializedView save(MaterializedView materializedView);

    /**
     * 更新
     */
    MaterializedView update(MaterializedView materializedView);

    /**
     * 删除
     */
    void deleteById(String id);
}
