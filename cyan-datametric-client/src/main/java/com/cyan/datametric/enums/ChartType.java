package com.cyan.datametric.enums;

/**
 * 图表类型枚举
 *
 * @author cy.Y
 * @since 1.0.0
 */
public enum ChartType {
    TABLE,
    BAR,
    LINE,
    PIE,
    SCATTER,
    AREA,
    NUMBER,
    FILTER_SELECT,
    FILTER_MULTI,
    FILTER_DATE,
    FILTER_DATE_RANGE;

    /**
     * 是否为筛选框图表
     */
    public boolean isFilter() {
        return name().startsWith("FILTER_");
    }
}
