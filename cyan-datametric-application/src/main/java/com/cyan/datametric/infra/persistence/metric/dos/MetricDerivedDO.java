package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 派生指标扩展表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_derived")
public class MetricDerivedDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 指标定义ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * 关联原子指标ID
     */
    @TableField("atomic_metric_id")
    private Long atomicMetricId;

    /**
     * 时间周期ID
     */
    @TableField("time_period_id")
    private Long timePeriodId;

    /**
     * 修饰词ID列表JSON
     */
    @TableField("modifier_ids")
    private String modifierIds;

    /**
     * 维度ID列表JSON
     */
    @TableField("dimension_ids")
    private String dimensionIds;

    /**
     * 分组字段JSON
     */
    @TableField("group_by_fields")
    private String groupByFields;
}
