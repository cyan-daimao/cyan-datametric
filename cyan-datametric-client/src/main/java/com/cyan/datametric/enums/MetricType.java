package com.cyan.datametric.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标类型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricType {
    ATOMIC("ATOMIC", "原子指标"),
    DERIVED("DERIVED", "派生指标"),
    COMPOSITE("COMPOSITE", "复合指标");

    private final String code;
    private final String desc;
}
