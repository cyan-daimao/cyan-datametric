package com.cyan.datametric.application.bi.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指标列表项业务对象（BI分析用）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class BiMetricBO {

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

    /**
     * 事实表全名（catalog.schema.table）
     */
    private String tableRef;

    /**
     * 数据密级: L1/L2/L3/L4
     */
    private String securityLevel;

    /**
     * 指标状态: DRAFT/PUBLISHED/OFFLINE
     */
    private com.cyan.datametric.enums.MetricStatus status;
}
