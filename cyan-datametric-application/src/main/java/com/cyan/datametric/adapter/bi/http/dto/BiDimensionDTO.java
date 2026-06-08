package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度列表项（BI用）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class BiDimensionDTO {

    /**
     * 维度ID
     */
    private String id;

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
     * 维度实现类型
     */
    private String dimensionKind;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 关联维表名
     */
    private String tableName;

    /**
     * 来源事实表
     */
    private String sourceTable;

    /**
     * 物理字段名
     */
    private String columnName;

    /**
     * 显示字段名（BI展示用）
     */
    private String displayColumn;

    /**
     * 所属分类名称
     */
    private String categoryName;

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
}
