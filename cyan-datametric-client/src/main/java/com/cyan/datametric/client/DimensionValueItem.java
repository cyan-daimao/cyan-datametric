package com.cyan.datametric.client;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度可选值项（Client DTO）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionValueItem {

    /**
     * 关联字段值（物理字段）
     */
    private String value;

    /**
     * 显示字段值（展示用）
     */
    private String label;
}
