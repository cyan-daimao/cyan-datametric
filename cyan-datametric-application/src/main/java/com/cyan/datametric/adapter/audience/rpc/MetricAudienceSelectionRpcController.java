package com.cyan.datametric.adapter.audience.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.application.audience.MetricAudienceSelectionService;
import com.cyan.datametric.client.audience.MetricAudienceSelectionClient;
import com.cyan.datametric.client.audience.dto.MetricAudienceEstimateDTO;
import com.cyan.datametric.client.audience.dto.MetricAudienceSelectionSqlDTO;
import com.cyan.datametric.client.audience.request.MetricAudienceSelectionCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标人群圈选 RPC 控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/audience-selection")
@RequiredArgsConstructor
public class MetricAudienceSelectionRpcController implements MetricAudienceSelectionClient {

    private final MetricAudienceSelectionService metricAudienceSelectionService;

    /**
     * 编译圈选SQL
     */
    @Override
    @PostMapping("/compile")
    public Response<MetricAudienceSelectionSqlDTO> compile(@RequestBody MetricAudienceSelectionCmd cmd) {
        return Response.success(metricAudienceSelectionService.compile(cmd, "system"));
    }

    /**
     * 预估圈选人数
     */
    @Override
    @PostMapping("/estimate")
    public Response<MetricAudienceEstimateDTO> estimate(@RequestBody MetricAudienceSelectionCmd cmd) {
        return Response.success(metricAudienceSelectionService.estimate(cmd, "system"));
    }
}
