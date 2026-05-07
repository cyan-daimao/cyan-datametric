package com.cyan.datametric.application.analysis;

import com.cyan.datametric.adapter.analysis.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.adapter.analysis.http.dto.MetricBiChartDataDTO;

/**
 * 指标 BI 分析服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface BiAnalysisService {

    /**
     * 执行指标分析
     *
     * @param cmd 分析命令
     * @return 分析结果
     */
    MetricBiChartDataDTO execute(MetricBiAnalysisCmd cmd);

    /**
     * 预览 SQL（不执行）
     *
     * @param cmd 分析命令
     * @return 生成的 SQL
     */
    String previewSql(MetricBiAnalysisCmd cmd);
}
