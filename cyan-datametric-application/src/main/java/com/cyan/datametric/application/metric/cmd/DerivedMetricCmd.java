package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

import java.util.List;

/**
 * 派生指标创建/更新命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class DerivedMetricCmd {

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 业务口径
     */
    private String bizCaliber;

    /**
     * 技术口径
     */
    private String techCaliber;

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
    private List<GroupByFieldCmd> groupByFields;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    @Data
    public static class GroupByFieldCmd {
        private String col;
    }
}
