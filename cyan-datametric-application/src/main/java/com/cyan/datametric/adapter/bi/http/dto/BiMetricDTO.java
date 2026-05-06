package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指标列表项（BI用）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class BiMetricDTO {

    /**
     * 指标ID
     */
    private String id;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 主题域名称
     */
    private String subjectName;

    /**
     * 聚合函数
     */
    private String statFunc;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 描述
     */
    private String description;
}
