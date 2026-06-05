package com.cyan.datametric.application.collection.cmd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 采集属性维度同步命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class CollectionDimensionUpsertCmd {

    /**
     * 采集属性ID
     */
    private String propertyId;

    /**
     * 采集属性编码
     */
    private String propertyCode;

    /**
     * 采集属性名称
     */
    private String propertyName;

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
     * 维度可选值
     */
    private List<String> dimValues;

    /**
     * 维度分类ID
     */
    private String categoryId;

    /**
     * 来源表
     */
    private String sourceTable;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 描述
     */
    private String description;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 操作人
     */
    private String operator;
}
