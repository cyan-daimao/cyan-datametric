package com.cyan.datametric.application.config.cmd;

import lombok.Data;

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
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tblName;

    /**
     * 字段名称
     */
    private String colName;

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
