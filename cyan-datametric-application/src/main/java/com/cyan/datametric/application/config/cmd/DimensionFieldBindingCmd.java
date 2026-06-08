package com.cyan.datametric.application.config.cmd;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度字段绑定命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionFieldBindingCmd {

    /**
     * 绑定ID
     */
    private String id;

    /**
     * 表角色：FACT/DIMENSION
     */
    private String tableRole;

    /**
     * catalog 名称
     */
    private String catalogName;

    /**
     * schema 名称
     */
    private String schemaName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 展示字段
     */
    private String displayColumn;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 是否主绑定
     */
    private Boolean primaryBinding;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
