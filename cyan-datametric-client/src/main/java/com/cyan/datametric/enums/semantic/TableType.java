package com.cyan.datametric.enums.semantic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑表类型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TableType {
    FACT("FACT", "事实表"),
    DIMENSION("DIMENSION", "维度表"),
    BRIDGE("BRIDGE", "桥接表");

    private final String code;
    private final String desc;
}
