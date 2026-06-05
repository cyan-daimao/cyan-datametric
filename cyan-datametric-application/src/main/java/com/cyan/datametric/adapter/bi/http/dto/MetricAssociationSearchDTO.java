package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标维度可关联搜索结果
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricAssociationSearchDTO {

    /**
     * 可关联指标列表
     */
    private List<BiMetricDTO> metrics;

    /**
     * 可关联维度列表
     */
    private List<BiDimensionDTO> dimensions;
}
