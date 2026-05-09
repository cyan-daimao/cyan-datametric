package com.cyan.datametric.adapter.analysis.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.client.MetricBiAnalysisClient;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.client.dto.MetricBiChartDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标 BI 分析 RPC 服务（供内部服务调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/bi/analysis")
@RequiredArgsConstructor
public class MetricBiAnalysisAgentRPC implements MetricBiAnalysisClient {

    private final BiAnalysisService biAnalysisService;

    @Override
    public Response<MetricBiChartDataDTO> execute(MetricBiAnalysisCmd cmd) {
        MetricBiChartDataDTO result = biAnalysisService.execute(cmd, null);
        return Response.success(result);
    }

    @Override
    public Response<String> previewSql(MetricBiAnalysisCmd cmd) {
        String sql = biAnalysisService.previewSql(cmd);
        return Response.success(sql);
    }
}
