package com.cyan.datametric.adapter.bi.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.bi.http.convert.MetricBiAnalysisAdapterConvert;
import com.cyan.datametric.application.bi.MetricBiAnalysisService;
import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.client.DimensionBiListItem;
import com.cyan.datametric.client.DimensionValueItem;
import com.cyan.datametric.client.MetricBiListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指标/维度列表 RPC 服务（供内部服务调用 / Dify 工具调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/bi")
@RequiredArgsConstructor
public class MetricBiListRpcController {

    private final MetricBiAnalysisService metricBiAnalysisService;
    private final MetricBiAnalysisAdapterConvert metricBiAnalysisAdapterConvert;

    /**
     * 查询可用指标列表
     */
    @GetMapping("/list")
    public Response<List<MetricBiListItem>> listMetrics(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "subjectCode", required = false) String subjectCode,
            @RequestParam(name = "metricType", required = false) String metricType) {

        List<BiMetricBO> bos = metricBiAnalysisService.listMetrics(
                trimToNull(name), trimToNull(subjectCode), trimToNull(metricType));
        List<MetricBiListItem> result = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toMetricBiListItem)
                .toList();
        return Response.success(result);
    }

    /**
     * 查询可用维度列表
     */
    @GetMapping("/dimensions")
    public Response<List<DimensionBiListItem>> listDimensions(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "categoryId", required = false) String categoryId) {

        List<BiDimensionBO> bos = metricBiAnalysisService.listDimensions(
                trimToNull(name), trimToNull(categoryId));
        List<DimensionBiListItem> result = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toDimensionBiListItem)
                .toList();
        return Response.success(result);
    }

    /**
     * 查询维度可选值
     */
    @GetMapping("/dimensions/{dimCode}/values")
    public Response<List<DimensionValueItem>> listDimensionValues(
            @PathVariable("dimCode") String dimCode) {

        List<DimensionValueBO> bos = metricBiAnalysisService.listDimensionValues(trimToNull(dimCode));
        List<DimensionValueItem> result = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toDimensionValueItem)
                .toList();
        return Response.success(result);
    }

    /**
     * 去除首尾空白，空字符串/纯空白转为 null
     */
    private String trimToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
