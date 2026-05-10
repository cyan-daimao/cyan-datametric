package com.cyan.datametric.adapter.bi.http.convert;

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
import org.springframework.stereotype.Component;

/**
 * 指标BI分析适配器层转换器
 * <p>
 * 负责 BO → DTO 的转换（包括 HTTP DTO、RPC Client DTO）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
public class MetricBiAnalysisAdapterConvert {

    /**
     * 指标业务对象转HTTP响应DTO
     */
    public BiMetricDTO toBiMetricDTO(BiMetricBO bo) {
        if (bo == null) {
            return null;
        }
        return new BiMetricDTO()
                .setId(bo.getId())
                .setMetricCode(bo.getMetricCode())
                .setMetricName(bo.getMetricName())
                .setMetricType(bo.getMetricType())
                .setSubjectCode(bo.getSubjectCode())
                .setSubjectName(bo.getSubjectName())
                .setStatFunc(bo.getStatFunc())
                .setDataType(bo.getDataType())
                .setDescription(bo.getDescription())
                .setTableRef(bo.getTableRef());
    }

    /**
     * 维度业务对象转HTTP响应DTO
     */
    public BiDimensionDTO toBiDimensionDTO(BiDimensionBO bo) {
        if (bo == null) {
            return null;
        }
        return new BiDimensionDTO()
                .setId(bo.getId())
                .setDimCode(bo.getDimCode())
                .setDimName(bo.getDimName())
                .setDimType(bo.getDimType())
                .setDataType(bo.getDataType())
                .setTableName(bo.getTableName())
                .setColumnName(bo.getColumnName())
                .setDisplayColumn(bo.getDisplayColumn())
                .setCategoryName(bo.getCategoryName());
    }

    /**
     * 维度值业务对象转HTTP响应DTO
     */
    public DimensionValueDTO toDimensionValueDTO(DimensionValueBO bo) {
        if (bo == null) {
            return null;
        }
        return new DimensionValueDTO()
                .setValue(bo.getValue())
                .setLabel(bo.getLabel());
    }

    /**
     * 分析结果业务对象转HTTP响应DTO
     */
    public ChartDataDTO toChartDataDTO(ChartDataBO bo) {
        if (bo == null) {
            return null;
        }
        return new ChartDataDTO()
                .setStatus(bo.getStatus())
                .setCostTimeMs(bo.getCostTimeMs())
                .setColumns(bo.getColumns())
                .setRows(bo.getRows())
                .setSql(bo.getSql())
                .setChartType(bo.getChartType())
                .setErrorMessage(bo.getErrorMessage());
    }

    /**
     * 分析结果业务对象转RPC Client DTO
     */
    public MetricBiChartDataDTO toMetricBiChartDataDTO(ChartDataBO bo) {
        if (bo == null) {
            return null;
        }
        return new MetricBiChartDataDTO()
                .setStatus(bo.getStatus())
                .setCostTimeMs(bo.getCostTimeMs())
                .setColumns(bo.getColumns())
                .setRows(bo.getRows())
                .setSql(bo.getSql())
                .setChartType(bo.getChartType())
                .setErrorMessage(bo.getErrorMessage());
    }

    /**
     * 指标业务对象转RPC列表项
     */
    public MetricBiListItem toMetricBiListItem(BiMetricBO bo) {
        if (bo == null) {
            return null;
        }
        return new MetricBiListItem()
                .setId(bo.getId())
                .setMetricCode(bo.getMetricCode())
                .setMetricName(bo.getMetricName())
                .setMetricType(bo.getMetricType())
                .setSubjectCode(bo.getSubjectCode())
                .setSubjectName(bo.getSubjectName())
                .setStatFunc(bo.getStatFunc())
                .setDataType(bo.getDataType())
                .setDescription(bo.getDescription())
                .setTableRef(bo.getTableRef());
    }

    /**
     * 维度业务对象转RPC列表项
     */
    public DimensionBiListItem toDimensionBiListItem(BiDimensionBO bo) {
        if (bo == null) {
            return null;
        }
        return new DimensionBiListItem()
                .setId(bo.getId())
                .setDimCode(bo.getDimCode())
                .setDimName(bo.getDimName())
                .setDimType(bo.getDimType())
                .setDataType(bo.getDataType())
                .setTableName(bo.getTableName())
                .setColumnName(bo.getColumnName())
                .setDisplayColumn(bo.getDisplayColumn())
                .setCategoryName(bo.getCategoryName());
    }

    /**
     * 维度值业务对象转RPC列表项
     */
    public DimensionValueItem toDimensionValueItem(DimensionValueBO bo) {
        if (bo == null) {
            return null;
        }
        return new DimensionValueItem()
                .setValue(bo.getValue())
                .setLabel(bo.getLabel());
    }
}
