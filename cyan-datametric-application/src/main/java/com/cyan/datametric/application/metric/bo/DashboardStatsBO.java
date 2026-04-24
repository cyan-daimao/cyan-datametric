package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dashboard统计数据BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DashboardStatsBO {

    private Long totalMetrics;
    private Long atomicCount;
    private Long derivedCount;
    private Long compositeCount;
    private Long publishedCount;
    private Long draftCount;
    private Long offlineCount;
    private List<SubjectDistributionBO> subjectDistribution;
    private List<RecentUpdateBO> recentUpdates;

    @Data
    @Accessors(chain = true)
    public static class SubjectDistributionBO {
        private String subjectCode;
        private String subjectName;
        private Long count;
    }

    @Data
    @Accessors(chain = true)
    public static class RecentUpdateBO {
        private String metricCode;
        private String metricName;
        private String action;
        private String operator;
        private LocalDateTime time;
    }
}
