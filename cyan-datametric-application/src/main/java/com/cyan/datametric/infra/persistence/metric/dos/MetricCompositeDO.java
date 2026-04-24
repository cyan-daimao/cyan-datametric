package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 复合指标扩展表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_composite")
public class MetricCompositeDO {

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
     * 计算公式
     */
    @TableField("formula")
    private String formula;

    /**
     * 引用的指标ID列表JSON
     */
    @TableField("metric_refs")
    private String metricRefs;
}
