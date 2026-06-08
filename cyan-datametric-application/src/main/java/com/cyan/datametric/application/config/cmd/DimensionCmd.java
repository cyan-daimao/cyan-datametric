package com.cyan.datametric.application.config.cmd;

import lombok.Data;

import java.util.List;

/**
 * 公共维度命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class DimensionCmd {

    /**
     * 维度编码
     */
    private String dimCode;

    /**
     * 维度名称
     */
    private String dimName;

    /**
     * 维度类型
     */
    private String dimType;

    /**
     * 维度实现类型：NORMAL/DEGENERATE/HIERARCHY/DERIVED
     */
    private String dimensionKind;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 维度可选值
     */
    private List<String> dimValues;

    /**
     * 维度分类ID
     */
    private String categoryId;

    /**
     * 数仓维表所在 schema
     */
    private String schemaName;

    /**
     * 关联数仓维表名
     */
    private String tableName;

    /**
     * 关联维表字段名
     */
    private String columnName;

    /**
     * 显示字段名（BI展示用）
     */
    private String displayColumn;

    /**
     * 来源类型：COLUMN/JSON_PATH/EXPRESSION
     */
    private String sourceType;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 来源事实表
     */
    private String sourceTable;

    /**
     * 层级编码
     */
    private String hierarchyCode;

    /**
     * 层级名称
     */
    private String hierarchyName;

    /**
     * 父级维度编码
     */
    private String parentDimCode;

    /**
     * 层级级别
     */
    private Integer hierarchyLevel;

    /**
     * 排序号
     */
    private Integer sortOrder;

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
}
