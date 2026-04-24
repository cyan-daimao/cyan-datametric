package com.cyan.datametric.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时间周期类型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PeriodType {
    RELATIVE("RELATIVE", "相对偏移"),
    ABSOLUTE("ABSOLUTE", "绝对日期");

    private final String code;
    private final String desc;
}
