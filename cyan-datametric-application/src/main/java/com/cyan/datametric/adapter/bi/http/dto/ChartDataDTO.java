package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 分析执行响应DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class ChartDataDTO {

    /**
     * 执行状态：SUCCESS | FAILED
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
     * 执行的SQL
     */
    private String sql;

    /**
     * 错误信息
     */
    private String errorMessage;
}
