package com.cyan.datametric.application.bi.convert;

import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.metric.Metric;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 指标BI分析应用层转换器
 * <p>
 * 负责 Domain → BO 的转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
public class MetricBiAnalysisAppConvert {

    /**
     * 指标领域对象转业务对象
     *
     * @param metric      指标领域对象
     * @param subjectName 主题域名称（由 Application 层组装）
     * @return 指标业务对象
     */
    public BiMetricBO toBiMetricBO(Metric metric, String subjectName) {
        if (metric == null) {
            return null;
        }
        BiMetricBO bo = new BiMetricBO();
        bo.setId(metric.getId());
        bo.setMetricCode(metric.getMetricCode());
        bo.setMetricName(metric.getMetricName());
        bo.setMetricType(metric.getMetricType() == null ? null : metric.getMetricType().getCode());
        bo.setSubjectCode(metric.getSubjectCode());
        bo.setSubjectName(subjectName);
        if (metric.getAtomicExt() != null) {
            bo.setStatFunc(metric.getAtomicExt().getStatFunc() == null ? null : metric.getAtomicExt().getStatFunc().getCode());
        }
        bo.setDescription(metric.getBizCaliber());
        return bo;
    }

    /**
     * 维度领域对象转业务对象
     *
     * @param dimension    维度领域对象
     * @param categoryName 分类名称（由 Application 层组装）
     * @return 维度业务对象
     */
    public BiDimensionBO toBiDimensionBO(Dimension dimension, String categoryName) {
        if (dimension == null) {
            return null;
        }
        BiDimensionBO bo = new BiDimensionBO();
        bo.setId(dimension.getId());
        bo.setDimCode(dimension.getDimCode());
        bo.setDimName(dimension.getDimName());
        bo.setDimType(dimension.getDimType());
        bo.setDataType(dimension.getDataType());
        bo.setTableName(dimension.getTableName());
        bo.setColumnName(dimension.getColumnName());
        bo.setDisplayColumn(dimension.getDisplayColumn());
        bo.setCategoryName(categoryName);
        return bo;
    }

    /**
     * 构建维度值业务对象
     *
     * @param value 物理字段值
     * @param label 显示字段值
     * @return 维度值业务对象
     */
    public DimensionValueBO toDimensionValueBO(String value, String label) {
        return new DimensionValueBO()
                .setValue(value)
                .setLabel(label);
    }

    /**
     * 构建分析结果业务对象
     *
     * @param status      执行状态
     * @param costTimeMs  执行耗时
     * @param columns     列名列表
     * @param rows        数据行列表
     * @param sql         执行的SQL
     * @param errorMessage 错误信息
     * @return 分析结果业务对象
     */
    public ChartDataBO toChartDataBO(String status, Long costTimeMs,
                                     List<String> columns, List<Map<String, Object>> rows,
                                     String sql, String errorMessage) {
        return new ChartDataBO()
                .setStatus(status)
                .setCostTimeMs(costTimeMs)
                .setColumns(columns)
                .setRows(rows)
                .setSql(sql)
                .setErrorMessage(errorMessage);
    }
}
