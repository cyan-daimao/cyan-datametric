package com.cyan.datametric.domain.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.repository.LogicalTableRepository;
import com.cyan.datametric.enums.semantic.TableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 逻辑表领域对象（充血模型）
 * <p>
 * 语义层核心概念：一张逻辑表对应一个物理数据表（事实表、维度表或桥接表），
 * 包含字段 Schema、主键、时间字段等元数据，用于自动 JOIN 路径计算。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class LogicalTable {

    /**
     * 主键
     */
    private String id;

    /**
     * 物理表全名（如 db_name.table_name）
     */
    private String tableName;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 表类型：FACT / DIMENSION / BRIDGE
     */
    private TableType tableType;

    /**
     * 主键字段
     */
    private String primaryKey;

    /**
     * 时间字段（用于增量刷新识别）
     */
    private String timeColumn;

    /**
     * 字段 Schema 列表
     */
    private List<ColumnSchema> schema;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 校验基础信息
     */
    private void validate() {
        Assert.notBlank(this.tableName, new BusinessException("物理表名不能为空"));
        Assert.notBlank(this.displayName, new BusinessException("展示名称不能为空"));
        Assert.notNull(this.tableType, new BusinessException("表类型不能为空"));
    }

    /**
     * 保存逻辑表
     */
    public LogicalTable save(LogicalTableRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新逻辑表
     */
    public LogicalTable update(LogicalTableRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除逻辑表
     */
    public void delete(LogicalTableRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 是否为事实表
     */
    public boolean isFactTable() {
        return this.tableType == TableType.FACT;
    }

    /**
     * 是否为维度表
     */
    public boolean isDimensionTable() {
        return this.tableType == TableType.DIMENSION;
    }

    /**
     * 获取数据库名
     */
    public String getDbName() {
        if (this.tableName == null || !this.tableName.contains(".")) {
            return null;
        }
        return this.tableName.substring(0, this.tableName.indexOf('.'));
    }

    /**
     * 获取纯表名（不含数据库前缀）
     */
    public String getPureTableName() {
        if (this.tableName == null || !this.tableName.contains(".")) {
            return this.tableName;
        }
        return this.tableName.substring(this.tableName.indexOf('.') + 1);
    }

    /**
     * 字段 Schema 内部类
     */
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnSchema {
        private String columnName;
        private String dataType;
        private String comment;
        private Boolean isNullable;
    }
}
