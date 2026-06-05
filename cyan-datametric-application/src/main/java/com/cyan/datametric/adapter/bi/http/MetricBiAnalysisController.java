package com.cyan.datametric.adapter.bi.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.bi.http.convert.MetricBiAnalysisAdapterConvert;
import com.cyan.datametric.adapter.bi.http.dto.BiDimensionDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiMetricDTO;
import com.cyan.datametric.adapter.bi.http.dto.DimensionValueDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricAssociationGraphDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricAssociationSearchDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricAssociationSearchRequest;
import com.cyan.datametric.application.bi.MetricBiAnalysisService;
import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.application.bi.bo.MetricAssociationGraphBO;
import com.cyan.datametric.application.bi.bo.MetricAssociationSearchBO;
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
    private final MetricBiAnalysisAdapterConvert metricBiAnalysisAdapterConvert;

    /**
     * 指标列表（BI用）
     */
    @GetMapping("/list")
    public Response<List<BiMetricDTO>> listMetrics(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "subjectCode", required = false) String subjectCode,
            @RequestParam(name = "metricType", required = false) String metricType) {
        List<BiMetricBO> bos = metricBiAnalysisService.listMetrics(name, subjectCode, metricType);
        List<BiMetricDTO> dtos = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toBiMetricDTO)
                .toList();
        return Response.success(dtos);
    }

    /**
     * 维度列表（BI用）
     */
    @GetMapping("/dimensions")
    public Response<List<BiDimensionDTO>> listDimensions(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "categoryId", required = false) String categoryId) {
        List<BiDimensionBO> bos = metricBiAnalysisService.listDimensions(name, categoryId);
        List<BiDimensionDTO> dtos = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toBiDimensionDTO)
                .toList();
        return Response.success(dtos);
    }

    /**
     * 维度可选值（BI用）
     */
    @GetMapping("/dimensions/{dimCode}/values")
    public Response<List<DimensionValueDTO>> listDimensionValues(
            @PathVariable("dimCode") String dimCode) {
        List<DimensionValueBO> bos = metricBiAnalysisService.listDimensionValues(dimCode);
        List<DimensionValueDTO> dtos = bos.stream()
                .map(metricBiAnalysisAdapterConvert::toDimensionValueDTO)
                .toList();
        return Response.success(dtos);
    }

    /**
     * 搜索当前已选指标维度可继续关联的候选对象
     */
    @PostMapping("/associations/search")
    public Response<MetricAssociationSearchDTO> searchAssociations(
            @RequestBody MetricAssociationSearchRequest request) {
        MetricAssociationSearchBO bo = metricBiAnalysisService.searchAssociations(
                metricBiAnalysisAdapterConvert.toMetricAssociationSearchQuery(request));
        return Response.success(metricBiAnalysisAdapterConvert.toMetricAssociationSearchDTO(bo));
    }

    /**
     * 查询单个指标的可关联图谱
     */
    @GetMapping("/associations/graph")
    public Response<MetricAssociationGraphDTO> associationGraph(
            @RequestParam("metricCode") String metricCode) {
        MetricAssociationGraphBO bo = metricBiAnalysisService.associationGraph(metricCode);
        return Response.success(metricBiAnalysisAdapterConvert.toMetricAssociationGraphDTO(bo));
    }
}
