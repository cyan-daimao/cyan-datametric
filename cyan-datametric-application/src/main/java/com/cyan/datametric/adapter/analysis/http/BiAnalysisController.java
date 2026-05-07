package com.cyan.datametric.adapter.analysis.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.analysis.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.adapter.analysis.http.dto.MetricBiChartDataDTO;
import com.cyan.datametric.application.analysis.BiAnalysisService;
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

    public BiAnalysisController(BiAnalysisService biAnalysisService) {
        this.biAnalysisService = biAnalysisService;
    }

    /**
     * 执行指标分析
     */
    @PostMapping("/execute")
    public Response<MetricBiChartDataDTO> execute(@RequestBody MetricBiAnalysisCmd cmd) {
        MetricBiChartDataDTO result = biAnalysisService.execute(cmd);
        return Response.success(result);
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
