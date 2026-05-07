package com.cyan.datametric.enums.semantic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询路由类型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RouteType {
    MATERIALIZED("MATERIALIZED", "命中物化视图"),
    REALTIME("REALTIME", "实时计算"),
    FALLBACK("FALLBACK", "降级到旧引擎");

    private final String code;
    private final String desc;
}
