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
     * 关联数仓维表名
     */
    private String tableName;

    /**
     * 关联维表字段名
     */
    private String columnName;

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
