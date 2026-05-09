package com.cyan.datametric.application.bi.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度列表项业务对象（BI分析用）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class BiDimensionBO {

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
     * 数据类型
     */
    private String dataType;

    /**
     * 关联维表名
     */
    private String tableName;

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
}
