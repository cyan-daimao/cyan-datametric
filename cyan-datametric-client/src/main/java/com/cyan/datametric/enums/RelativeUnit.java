package com.cyan.datametric.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 相对时间单位
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RelativeUnit {
    DAY("DAY", "天"),
    WEEK("WEEK", "周"),
    MONTH("MONTH", "月"),
    YEAR("YEAR", "年");

    private final String code;
    private final String desc;
}
