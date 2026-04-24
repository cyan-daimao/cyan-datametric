package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标详情DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricDetailDTO {

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
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MetricAtomicDTO atomic;
    private MetricDerivedDTO derived;
    private MetricCompositeDTO composite;

    @Data
    @Accessors(chain = true)
    public static class MetricAtomicDTO {
        private String statFunc;
        private String dsName;
        private String dbName;
        private String tblName;
        private String colName;
        private List<FilterConditionDTO> filterCondition;
    }

    @Data
    @Accessors(chain = true)
    public static class FilterConditionDTO {
        private String field;
        private String op;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class MetricDerivedDTO {
        private String atomicMetricId;
        private String timePeriodId;
        private List<String> modifierIds;
        private List<String> dimensionIds;
        private List<GroupByFieldDTO> groupByFields;
    }

    @Data
    @Accessors(chain = true)
    public static class GroupByFieldDTO {
        private String col;
    }

    @Data
    @Accessors(chain = true)
    public static class MetricCompositeDTO {
        private String formula;
        private List<String> metricRefs;
    }
}
