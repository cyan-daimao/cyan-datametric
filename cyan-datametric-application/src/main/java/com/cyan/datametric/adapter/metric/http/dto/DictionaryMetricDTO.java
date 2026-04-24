package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标字典DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DictionaryMetricDTO {

    private String id;
    private String metricCode;
    private String metricName;
    private String metricType;
    private String subjectCode;
    private String subjectName;
    private String bizCaliber;
    private String status;
    private LocalDateTime updatedAt;
    private Boolean isFavorite;
}
