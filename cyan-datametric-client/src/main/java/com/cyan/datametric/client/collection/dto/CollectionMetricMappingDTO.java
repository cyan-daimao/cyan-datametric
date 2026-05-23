package com.cyan.datametric.client.collection.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 采集事件指标映射结果
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class CollectionMetricMappingDTO {

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 是否新建
     */
    private Boolean created;
}
