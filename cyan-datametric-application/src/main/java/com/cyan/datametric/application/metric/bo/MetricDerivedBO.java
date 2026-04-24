package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 派生指标扩展BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricDerivedBO {

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
    private List<GroupByFieldBO> groupByFields;

    @Data
    @Accessors(chain = true)
    public static class GroupByFieldBO {
        private String col;
    }
}
