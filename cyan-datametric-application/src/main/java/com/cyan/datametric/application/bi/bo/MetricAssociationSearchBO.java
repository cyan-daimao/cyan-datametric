package com.cyan.datametric.application.bi.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标维度可关联搜索业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricAssociationSearchBO {

    /**
     * 可关联指标列表
     */
    private List<BiMetricBO> metrics;

    /**
     * 可关联维度列表
     */
    private List<BiDimensionBO> dimensions;
}
