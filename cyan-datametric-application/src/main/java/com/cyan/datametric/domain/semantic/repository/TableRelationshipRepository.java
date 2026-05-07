package com.cyan.datametric.domain.semantic.repository;

import com.cyan.datametric.domain.semantic.TableRelationship;

import java.util.List;

/**
 * 表关联关系仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface TableRelationshipRepository {

    /**
     * 根据ID查询
     */
    TableRelationship findById(String id);

    /**
     * 查询所有关联关系
     */
    List<TableRelationship> findAll();

    /**
     * 根据左表ID查询
     */
    List<TableRelationship> findByLeftTableId(String leftTableId);

    /**
     * 根据右表ID查询
     */
    List<TableRelationship> findByRightTableId(String rightTableId);

    /**
     * 根据任意一端表ID查询
     */
    List<TableRelationship> findByTableId(String tableId);

    /**
     * 保存
     */
    TableRelationship save(TableRelationship relationship);

    /**
     * 更新
     */
    TableRelationship update(TableRelationship relationship);

    /**
     * 删除
     */
    void deleteById(String id);
}
