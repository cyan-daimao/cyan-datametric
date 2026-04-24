package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标列表项DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricDTO {

    private String id;
    private String metricCode;
    private String metricName;
    private String metricType;
    private String subjectCode;
    private String subjectName;
    private String bizCaliber;
    private String techCaliber;
    private String status;
    private String owner;
    private String statFunc;
    private String dsName;
    private String dbName;
    private String tblName;
    private String colName;
    private Integer version;
    private LocalDateTime updatedAt;
}
