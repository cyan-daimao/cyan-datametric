package com.cyan.datametric.domain.semantic.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.LogicalTable;

import java.util.List;

/**
 * 逻辑表仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface LogicalTableRepository {

    /**
     * 根据ID查询
     */
    LogicalTable findById(String id);

    /**
     * 根据物理表名查询
     */
    LogicalTable findByTableName(String tableName);

    /**
     * 分页查询
     */
    Page<LogicalTable> page(int pageNum, int pageSize);

    /**
     * 查询所有
     */
    List<LogicalTable> findAll();

    /**
     * 根据类型查询
     */
    List<LogicalTable> findByTableType(String tableType);

    /**
     * 保存
     */
    LogicalTable save(LogicalTable logicalTable);

    /**
     * 更新
     */
    LogicalTable update(LogicalTable logicalTable);

    /**
     * 删除
     */
    void deleteById(String id);
}
