package com.cyan.datametric.enums.semantic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JOIN 类型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum JoinType {
    INNER("INNER", "内连接"),
    LEFT("LEFT", "左连接"),
    RIGHT("RIGHT", "右连接"),
    FULL("FULL", "全连接");

    private final String code;
    private final String desc;
}
