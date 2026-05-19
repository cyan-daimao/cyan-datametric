package com.cyan.datametric.adapter.metric.subject;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.metric.subject.convert.MetricSubjectAdapterConvert;
import com.cyan.datametric.adapter.metric.subject.dto.MetricSubjectDTO;
import com.cyan.datametric.application.metric.subject.MetricSubjectService;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标主题域 RPC 服务（供内部服务调用 / Dify 工具调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/subjects")
@RequiredArgsConstructor
public class MetricSubjectRpcController {

    private final MetricSubjectService metricSubjectService;
    private final MetricSubjectAdapterConvert metricSubjectAdapterConvert;

    /**
     * 查询主题域树形列表
     */
    @GetMapping
    public Response<List<MetricSubjectDTO>> list() {
        List<MetricSubjectBO> bos = metricSubjectService.tree();
        List<MetricSubjectDTO> result = bos.stream()
                .map(metricSubjectAdapterConvert::toMetricSubjectDTO)
                .toList();
        return Response.success(result);
    }
}
