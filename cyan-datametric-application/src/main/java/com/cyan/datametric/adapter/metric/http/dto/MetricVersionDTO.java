package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetricVersionDTO {
    private Integer version;
    private String metricName;
    private String status;
    private LocalDateTime snapshotTime;
    private String updateBy;
}
