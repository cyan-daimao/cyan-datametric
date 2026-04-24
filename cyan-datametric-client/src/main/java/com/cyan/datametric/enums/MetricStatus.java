package com.cyan.datametric.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标状态
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricStatus {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    OFFLINE("OFFLINE", "已下线");

    private final String code;
    private final String desc;

    public static MetricStatus of(String code) {
        for (MetricStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
