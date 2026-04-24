package com.cyan.datametric.domain.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 派生指标扩展
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MetricDerivedExt {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标定义ID
     */
    private String metricId;

    /**
     * 关联原子指标ID
     */
    private String atomicMetricId;

    /**
     * 时间周期ID
     */
    private String timePeriodId;

    /**
     * 修饰词ID列表
     */
    private List<String> modifierIds;

    /**
     * 维度ID列表
     */
    private List<String> dimensionIds;

    /**
     * 分组字段
     */
    private List<GroupByField> groupByFields;

    /**
     * 分组字段项
     */
    @Data
    @Accessors(chain = true)
    public static class GroupByField {
        private String col;
    }
}
