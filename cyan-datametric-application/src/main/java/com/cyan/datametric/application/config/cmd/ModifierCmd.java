package com.cyan.datametric.application.config.cmd;

import lombok.Data;

import java.util.List;

/**
 * 修饰词命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class ModifierCmd {

    /**
     * 修饰词编码
     */
    private String modifierCode;

    /**
     * 修饰词名称
     */
    private String modifierName;

    /**
     * 关联字段名
     */
    private String fieldName;

    /**
     * 运算符
     */
    private String operator;

    /**
     * 可选值
     */
    private List<String> fieldValues;

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
