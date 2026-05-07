package com.cyan.datametric.enums.semantic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物化视图状态
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MvStatus {
    ACTIVE("ACTIVE", "可用"),
    REFRESHING("REFRESHING", "刷新中"),
    FAILED("FAILED", "刷新失败"),
    DISABLED("DISABLED", "已禁用");

    private final String code;
    private final String desc;
}
