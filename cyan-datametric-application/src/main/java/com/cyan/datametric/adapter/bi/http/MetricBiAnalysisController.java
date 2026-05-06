package com.cyan.datametric.adapter.bi.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.bi.http.dto.BiDimensionDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiMetricDTO;
import com.cyan.datametric.adapter.bi.http.dto.ChartDataDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.application.bi.MetricBiAnalysisService;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指标BI分析控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/bi")
@RequiredArgsConstructor
public class MetricBiAnalysisController {

    private final MetricBiAnalysisService metricBiAnalysisService;

    /**
     * 指标分析执行
     */
    @PostMapping("/analysis/execute")
    // API: ready
    public Response<ChartDataDTO> execute(@RequestBody @Valid MetricBiAnalysisCmd cmd) {
        String executor = UserContextHolder.getCurrentEmployee().getPassport();
        ChartDataDTO dto = metricBiAnalysisService.execute(cmd, executor);
        return Response.success(dto);
    }

    /**
     * 指标分析SQL预览
     */
    @PostMapping("/analysis/preview-sql")
    // API: ready
    public Response<String> previewSql(@RequestBody @Valid MetricBiAnalysisCmd cmd) {
        String sql = metricBiAnalysisService.previewSql(cmd);
        return Response.success(sql);
    }

    /**
     * 指标列表（BI用）
     */
    @GetMapping("/list")
    // API: ready
    public Response<List<BiMetricDTO>> listMetrics(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "subjectCode", required = false) String subjectCode,
            @RequestParam(name = "metricType", required = false) String metricType) {
        List<BiMetricDTO> list = metricBiAnalysisService.listMetrics(name, subjectCode, metricType);
        return Response.success(list);
    }

    /**
     * 维度列表（BI用）
     */
    @GetMapping("/dimensions")
    // API: ready
    public Response<List<BiDimensionDTO>> listDimensions(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "categoryId", required = false) String categoryId) {
        List<BiDimensionDTO> list = metricBiAnalysisService.listDimensions(name, categoryId);
        return Response.success(list);
    }
}
