package com.cyan.datametric.adapter.analysis.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.metric.http.convert.MetricAdapterConvert;
import com.cyan.datametric.adapter.metric.http.dto.SubjectDrilldownDTO;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.SubjectDrilldownBO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标分析控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/analysis")
public class AnalysisController {

    private final MetricService metricService;

    public AnalysisController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/subject-drilldown")
    public Response<List<SubjectDrilldownDTO>> subjectDrilldown(@RequestParam(name = "subjectCode", required = false) String subjectCode) {
        List<SubjectDrilldownBO> bos = metricService.subjectDrilldown(subjectCode);
        return Response.success(bos.stream()
                .map(MetricAdapterConvert.INSTANCE::toSubjectDrilldownDTO)
                .toList());
    }
}
