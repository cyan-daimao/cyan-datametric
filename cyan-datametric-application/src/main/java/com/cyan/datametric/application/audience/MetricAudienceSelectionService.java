package com.cyan.datametric.application.audience;

import com.cyan.datametric.client.audience.dto.MetricAudienceEstimateDTO;
import com.cyan.datametric.client.audience.dto.MetricAudienceSelectionSqlDTO;
import com.cyan.datametric.client.audience.request.MetricAudienceSelectionCmd;

/**
 * 指标人群圈选服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricAudienceSelectionService {

    /**
     * 编译圈选SQL
     *
     * @param cmd 圈选命令
     * @param executor 执行人
     * @return 圈选SQL
     */
    MetricAudienceSelectionSqlDTO compile(MetricAudienceSelectionCmd cmd, String executor);

    /**
     * 预估圈选人数
     *
     * @param cmd 圈选命令
     * @param executor 执行人
     * @return 预估结果
     */
    MetricAudienceEstimateDTO estimate(MetricAudienceSelectionCmd cmd, String executor);
}
