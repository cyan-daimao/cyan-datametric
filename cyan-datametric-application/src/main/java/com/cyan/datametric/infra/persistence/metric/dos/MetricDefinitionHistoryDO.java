package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("metric_definition_history")
public class MetricDefinitionHistoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String metricCode;
    private String metricName;
    private String metricType;
    private String subjectCode;
    private String bizCaliber;
    private String techCaliber;
    private String status;
    private String owner;
    private Integer version;
    private String createBy;
    private String updateBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String extJson;
    private LocalDateTime snapshotTime;
}
