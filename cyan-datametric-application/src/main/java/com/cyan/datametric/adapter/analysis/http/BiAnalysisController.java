package com.cyan.datametric.adapter.analysis.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.bi.http.convert.MetricBiAnalysisAdapterConvert;
import com.cyan.datametric.application.analysis.BiAnalysisService;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.client.dto.MetricBiChartDataDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标 BI 分析控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/bi/analysis")
public class BiAnalysisController {

    private final BiAnalysisService biAnalysisService;
    private final MetricBiAnalysisAdapterConvert metricBiAnalysisAdapterConvert;

    public BiAnalysisController(BiAnalysisService biAnalysisService,
                                MetricBiAnalysisAdapterConvert metricBiAnalysisAdapterConvert) {
        this.biAnalysisService = biAnalysisService;
        this.metricBiAnalysisAdapterConvert = metricBiAnalysisAdapterConvert;
    }

    /**
     * 执行指标分析
     */
    @PostMapping("/execute")
    public Response<MetricBiChartDataDTO> execute(@RequestBody MetricBiAnalysisCmd cmd) {
        String executor = UserContextHolder.getCurrentEmployee().getPassport();
        ChartDataBO bo = biAnalysisService.execute(cmd, executor);
        MetricBiChartDataDTO dto = metricBiAnalysisAdapterConvert.toMetricBiChartDataDTO(bo);
        return Response.success(dto);
    }

    /**
     * 预览 SQL（不执行）
     */
    @PostMapping("/preview-sql")
    public Response<String> previewSql(@RequestBody MetricBiAnalysisCmd cmd) {
        String sql = biAnalysisService.previewSql(cmd);
        return Response.success(sql);
    }
}
