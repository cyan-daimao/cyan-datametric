package com.cyan.datametric.application.bi.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度可选值业务对象（BI分析用）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionValueBO {

    /**
     * 关联字段值（物理字段）
     */
    private String value;

    /**
     * 显示字段值（展示用）
     */
    private String label;
}
