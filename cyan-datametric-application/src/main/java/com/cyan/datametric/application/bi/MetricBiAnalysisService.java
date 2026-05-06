package com.cyan.datametric.application.bi;

import com.cyan.datametric.adapter.bi.http.dto.BiDimensionDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiMetricDTO;
import com.cyan.datametric.adapter.bi.http.dto.ChartDataDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricBiAnalysisCmd;

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
    ChartDataDTO execute(MetricBiAnalysisCmd cmd, String executor);

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
    List<BiMetricDTO> listMetrics(String name, String subjectCode, String metricType);

    /**
     * 查询维度列表（BI用）
     *
     * @param name       名称模糊搜索
     * @param categoryId 分类ID
     * @return 简化维度列表
     */
    List<BiDimensionDTO> listDimensions(String name, String categoryId);
}
