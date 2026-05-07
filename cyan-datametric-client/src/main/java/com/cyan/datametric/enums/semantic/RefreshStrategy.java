package com.cyan.datametric.enums.semantic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物化视图刷新策略
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RefreshStrategy {
    FULL("FULL", "全量刷新"),
    INCREMENTAL("INCREMENTAL", "增量刷新"),
    REALTIME("REALTIME", "实时刷新"),
    ON_DEMAND("ON_DEMAND", "按需刷新");

    private final String code;
    private final String desc;
}
