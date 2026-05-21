package com.cyan.datametric.client;

import com.cyan.arch.common.api.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ChatBI RPC 客户端（服务间内部调用 / Dify 工具调用，无登录拦截器）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "cyan-datametric", contextId = "cyan-datametric.chatbi", path = "/rpc/v1/metrics/bi")
public interface ChatBiRpcClient {

    /**
     * 查询可用指标列表
     */
    @GetMapping("/list")
    Response<List<MetricBiListItem>> listMetrics(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "subjectCode", required = false) String subjectCode,
            @RequestParam(name = "metricType", required = false) String metricType);

    /**
     * 查询可用维度列表
     */
    @GetMapping("/dimensions")
    Response<List<DimensionBiListItem>> listDimensions(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "categoryId", required = false) String categoryId);

    /**
     * 查询维度可选值
     */
    @GetMapping("/dimensions/{dimCode}/values")
    Response<List<DimensionValueItem>> listDimensionValues(
            @PathVariable("dimCode") String dimCode);
}


