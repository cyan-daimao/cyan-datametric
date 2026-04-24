package com.cyan.datametric.domain.config.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.query.ModifierPageQuery;

import java.util.List;

/**
 * 修饰词仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface ModifierRepository {

    /**
     * 根据ID查询
     */
    Modifier findById(String id);

    /**
     * 分页查询
     */
    Page<Modifier> page(ModifierPageQuery query);

    /**
     * 保存
     */
    Modifier save(Modifier modifier);

    /**
     * 更新
     */
    Modifier update(Modifier modifier);

    /**
     * 删除
     */
    void deleteById(String id);

    /**
     * 根据ID列表查询
     */
    List<Modifier> findByIds(List<String> ids);
}
