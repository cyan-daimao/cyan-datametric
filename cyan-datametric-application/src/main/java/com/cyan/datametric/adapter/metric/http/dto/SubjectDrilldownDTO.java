package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 主题域下钻分析DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SubjectDrilldownDTO {

    private String subjectCode;
    private String subjectName;
    private Long totalMetrics;
    private Map<String, Long> typeDistribution;
    private Map<String, Long> statusDistribution;
    private List<SubjectDrilldownDTO> children;
}
