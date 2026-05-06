package com.cyan.datametric.application.bi;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标BI分析业务错误码
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricBiErrorCode {

    METRIC_NOT_FOUND(400001, "指标不存在或已下线"),
    DIMENSION_NOT_FOUND(400002, "维度不存在"),
    TABLE_INCONSISTENT(400003, "多指标事实表不一致，不支持跨表联合查询"),
    COMPOSITE_PARSE_FAILED(400004, "复合指标公式解析失败");

    private final int code;
    private final String message;
}
