package com.cyan.datametric.application.config.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 维度字段绑定业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionFieldBindingBO {

    /**
     * 绑定ID
     */
    private String id;

    /**
     * 维度ID
     */
    private String dimId;

    /**
     * 表角色
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

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
