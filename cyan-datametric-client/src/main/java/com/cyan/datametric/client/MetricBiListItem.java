package com.cyan.datametric.client;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * BI 指标简化列表项（Client DTO）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricBiListItem {

    private String id;
    private String metricCode;
    private String metricName;
    private String metricType;
    private String subjectCode;
    private String subjectName;
    private String statFunc;
    private String dataType;
    private String description;

    /**
     * 事实表全名（catalog.schema.table）
     */
    private String tableRef;
}
