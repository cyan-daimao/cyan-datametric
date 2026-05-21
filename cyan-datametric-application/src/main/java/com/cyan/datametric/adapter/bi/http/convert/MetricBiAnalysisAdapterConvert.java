package com.cyan.datametric.adapter.bi.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.bi.http.dto.BiDimensionDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiMetricDTO;
import com.cyan.datametric.adapter.bi.http.dto.ChartDataDTO;
import com.cyan.datametric.adapter.bi.http.dto.DimensionValueDTO;
import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.client.DimensionBiListItem;
import com.cyan.datametric.client.DimensionValueItem;
import com.cyan.datametric.client.MetricBiListItem;
import com.cyan.datametric.client.dto.MetricBiChartDataDTO;
import org.mapstruct.Mapper;

/**
 * 指标BI分析适配器层转换器
 * <p>
 * 负责 BO → DTO 的转换（包括 HTTP DTO、RPC Client DTO）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricBiAnalysisAdapterConvert {

    BiMetricDTO toBiMetricDTO(BiMetricBO bo);

    BiDimensionDTO toBiDimensionDTO(BiDimensionBO bo);

    DimensionValueDTO toDimensionValueDTO(DimensionValueBO bo);

    ChartDataDTO toChartDataDTO(ChartDataBO bo);

    MetricBiChartDataDTO toMetricBiChartDataDTO(ChartDataBO bo);

    MetricBiListItem toMetricBiListItem(BiMetricBO bo);

    DimensionBiListItem toDimensionBiListItem(BiDimensionBO bo);

    DimensionValueItem toDimensionValueItem(DimensionValueBO bo);
}
