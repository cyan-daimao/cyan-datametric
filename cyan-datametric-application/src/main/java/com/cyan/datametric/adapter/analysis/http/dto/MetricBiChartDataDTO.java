package com.cyan.datametric.adapter.analysis.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 指标 BI 分析结果（返回前端）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricBiChartDataDTO {

    /**
     * 执行状态：SUCCESS / FAILED
     */
    private String status;

    /**
     * 执行耗时（毫秒）
     */
    private Long costTimeMs;

    /**
     * 列名列表
     */
    private List<String> columns;

    /**
     * 数据行列表
     */
    private List<Map<String, Object>> rows;

    /**
     * 执行的 SQL
     */
    private String sql;

    /**
     * 错误信息
     */
    private String errorMessage;
}
