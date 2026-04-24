package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标血缘表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_lineage")
public class MetricLineageDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 当前指标ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * 上游指标ID
     */
    @TableField("parent_metric_id")
    private Long parentMetricId;

    /**
     * 上游类型
     */
    @TableField("upstream_type")
    private String upstreamType;

    /**
     * 上游节点ID
     */
    @TableField("upstream_id")
    private String upstreamId;

    /**
     * 上游节点名称
     */
    @TableField("upstream_name")
    private String upstreamName;

    /**
     * 血缘方向
     */
    @TableField("lineage_type")
    private String lineageType;

    /**
     * 血缘层级
     */
    @TableField("level")
    private Integer level;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
