package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 主题域下钻分析BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SubjectDrilldownBO {

    private String subjectCode;
    private String subjectName;
    private Long totalMetrics;
    private Map<String, Long> typeDistribution;
    private Map<String, Long> statusDistribution;
    private List<SubjectDrilldownBO> children;
}
