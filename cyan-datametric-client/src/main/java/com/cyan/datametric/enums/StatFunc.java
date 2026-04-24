package com.cyan.datametric.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统计函数
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StatFunc {
    SUM("SUM", "求和"),
    AVG("AVG", "平均值"),
    COUNT("COUNT", "计数"),
    COUNT_DISTINCT("COUNT_DISTINCT", "去重计数"),
    MAX("MAX", "最大值"),
    MIN("MIN", "最小值");

    private final String code;
    private final String desc;
}
