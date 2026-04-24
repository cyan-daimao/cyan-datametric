package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dashboard统计数据DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DashboardStatsDTO {

    private Long totalMetrics;
    private Long atomicCount;
    private Long derivedCount;
    private Long compositeCount;
    private Long publishedCount;
    private Long draftCount;
    private Long offlineCount;
    private List<SubjectDistributionDTO> subjectDistribution;
    private List<RecentUpdateDTO> recentUpdates;

    @Data
    @Accessors(chain = true)
    public static class SubjectDistributionDTO {
        private String subjectCode;
        private String subjectName;
        private Long count;
    }

    @Data
    @Accessors(chain = true)
    public static class RecentUpdateDTO {
        private String metricCode;
        private String metricName;
        private String action;
        private String operator;
        private LocalDateTime time;
    }
}
