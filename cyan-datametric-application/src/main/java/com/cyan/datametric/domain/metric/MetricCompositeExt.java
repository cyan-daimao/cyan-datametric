package com.cyan.datametric.domain.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 复合指标扩展
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MetricCompositeExt {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标定义ID
     */
    private String metricId;

    /**
     * 计算公式
     */
    private String formula;

    /**
     * 引用的指标ID列表
     */
    private List<String> metricRefs;
}
