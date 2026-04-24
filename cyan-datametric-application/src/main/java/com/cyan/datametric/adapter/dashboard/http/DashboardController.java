package com.cyan.datametric.adapter.dashboard.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.metric.http.convert.MetricAdapterConvert;
import com.cyan.datametric.adapter.metric.http.dto.DashboardStatsDTO;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.DashboardStatsBO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标概览看板控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/dashboard")
public class DashboardController {

    private final MetricService metricService;

    public DashboardController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/stats")
    public Response<DashboardStatsDTO> stats() {
        DashboardStatsBO bo = metricService.dashboardStats();
        return Response.success(MetricAdapterConvert.INSTANCE.toDashboardStatsDTO(bo));
    }
}
