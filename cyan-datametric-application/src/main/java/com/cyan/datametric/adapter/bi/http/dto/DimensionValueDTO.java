package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 维度可选值DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionValueDTO {

    /**
     * 关联字段值（物理字段）
     */
    private String value;

    /**
     * 显示字段值（展示用）
     */
    private String label;
}
