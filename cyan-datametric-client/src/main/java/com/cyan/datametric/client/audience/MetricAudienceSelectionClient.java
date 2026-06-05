package com.cyan.datametric.client.audience;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.client.audience.dto.MetricAudienceEstimateDTO;
import com.cyan.datametric.client.audience.dto.MetricAudienceSelectionSqlDTO;
import com.cyan.datametric.client.audience.request.MetricAudienceSelectionCmd;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 指标人群圈选 RPC 客户端
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "cyan-datametric", contextId = "metricAudienceSelectionClient", path = "/rpc/v1/metrics/audience-selection", url = "${feign.cyan-datametric.url:}")
public interface MetricAudienceSelectionClient {

    /**
     * 编译圈选SQL
     *
     * @param cmd 圈选命令
     * @return 圈选SQL
     */
    @PostMapping("/compile")
    Response<MetricAudienceSelectionSqlDTO> compile(@RequestBody MetricAudienceSelectionCmd cmd);

    /**
     * 预估圈选人数
     *
     * @param cmd 圈选命令
     * @return 预估结果
     */
    @PostMapping("/estimate")
    Response<MetricAudienceEstimateDTO> estimate(@RequestBody MetricAudienceSelectionCmd cmd);
}
