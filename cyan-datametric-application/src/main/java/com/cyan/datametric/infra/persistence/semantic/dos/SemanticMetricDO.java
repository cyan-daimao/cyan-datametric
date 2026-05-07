package com.cyan.datametric.infra.persistence.semantic.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 语义指标 DO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("semantic_metric")
public class SemanticMetricDO {

    @TableId("id")
    private Long id;

    @TableField("metric_code")
    private String metricCode;

    @TableField("metric_name")
    private String metricName;

    @TableField("metric_type")
    private String metricType;

    @TableField("source_table_id")
    private Long sourceTableId;

    @TableField("source_column")
    private String sourceColumn;

    @TableField("stat_func")
    private String statFunc;

    @TableField("formula")
    private String formula;

    @TableField("modifiers_json")
    private String modifiersJson;

    @TableField("time_period_id")
    private Long timePeriodId;

    @TableField("description")
    private String description;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
