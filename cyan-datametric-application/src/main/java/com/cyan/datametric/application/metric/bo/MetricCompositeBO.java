package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 复合指标扩展BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricCompositeBO {

    /**
     * 计算公式
     */
    private String formula;

    /**
     * 引用的指标ID列表
     */
    private List<String> metricRefs;
}
