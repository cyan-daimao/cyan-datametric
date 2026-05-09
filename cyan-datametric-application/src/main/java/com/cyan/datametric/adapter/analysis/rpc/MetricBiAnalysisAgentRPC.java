package com.cyan.datametric.adapter.analysis.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.bi.http.convert.MetricBiAnalysisAdapterConvert;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.client.MetricBiAnalysisClient;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.client.dto.MetricBiChartDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标BI分析 RPC 服务（供内部服务调用 / Dify 工具调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/bi/analysis")
@RequiredArgsConstructor
public class MetricBiAnalysisAgentRPC implements MetricBiAnalysisClient {

    private final BiAnalysisService biAnalysisService;
    private final MetricBiAnalysisAdapterConvert metricBiAnalysisAdapterConvert;

    @Override
    @PostMapping("/execute")
    public Response<MetricBiChartDataDTO> execute(@RequestBody MetricBiAnalysisCmd cmd) {
        ChartDataBO bo = biAnalysisService.execute(cmd, null);
        MetricBiChartDataDTO dto = metricBiAnalysisAdapterConvert.toMetricBiChartDataDTO(bo);
        return Response.success(dto);
    }

    @Override
    @PostMapping("/preview-sql")
    public Response<String> previewSql(@RequestBody MetricBiAnalysisCmd cmd) {
        return Response.success(biAnalysisService.previewSql(cmd));
    }
}
