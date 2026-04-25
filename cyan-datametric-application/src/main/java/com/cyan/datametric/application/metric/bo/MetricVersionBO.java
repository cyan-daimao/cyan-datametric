package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MetricVersionBO {
    private Integer version;
    private String metricName;
    private String status;
    private LocalDateTime snapshotTime;
    private String updateBy;
}
