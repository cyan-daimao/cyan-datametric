package com.cyan.datametric.application.bi;

import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.application.bi.bo.MetricAssociationGraphBO;
import com.cyan.datametric.application.bi.bo.MetricAssociationSearchBO;
import com.cyan.datametric.application.bi.query.MetricAssociationSearchQuery;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;

import java.util.List;

/**
 * 指标BI分析服务接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricBiAnalysisService {

    /**
     * 执行指标分析
     *
     * @param cmd      DSL请求
     * @param executor 执行人
     * @return 图表数据
     */
    ChartDataBO execute(MetricBiAnalysisCmd cmd, String executor);

    /**
     * 预览SQL
     *
     * @param cmd DSL请求
     * @return SQL字符串
     */
    String previewSql(MetricBiAnalysisCmd cmd);

    /**
     * 查询指标列表（BI用）
     *
     * @param name        名称模糊搜索
     * @param subjectCode 主题域编码
     * @param metricType  指标类型
     * @return 简化指标列表
     */
    List<BiMetricBO> listMetrics(String name, String subjectCode, String metricType);

    /**
     * 查询维度列表（BI用）
     *
     * @param name       名称模糊搜索
     * @param categoryId 分类ID
     * @return 简化维度列表
     */
    List<BiDimensionBO> listDimensions(String name, String categoryId);

    /**
     * 查询维度可选值（BI用）
     *
     * @param dimCode 维度编码
     * @return 维度值列表（value=物理字段值, label=显示字段值）
     */
    List<DimensionValueBO> listDimensionValues(String dimCode);

    /**
     * 搜索当前已选指标维度可继续关联的候选对象
     *
     * @param query 可关联搜索查询
     * @return 可关联指标和维度
     */
    MetricAssociationSearchBO searchAssociations(MetricAssociationSearchQuery query);

    /**
     * 查询单个指标的可关联图谱
     *
     * @param metricCode 指标编码
     * @return 指标可关联图谱
     */
    MetricAssociationGraphBO associationGraph(String metricCode);
}
