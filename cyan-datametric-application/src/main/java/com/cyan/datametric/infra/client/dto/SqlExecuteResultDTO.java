package com.cyan.datametric.infra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * SQL 执行结果（datagateway 返回）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SqlExecuteResultDTO {

    /**
     * 执行 ID
     */
    private String executeId;

    /**
     * 执行状态：SUCCESS / FAILED
     */
    private String status;

    /**
     * 执行耗时（毫秒）
     */
    private Long costTimeMs;

    /**
     * 结果数据
     */
    private List<Map<String, Object>> data;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行的 SQL
     */
    private String sql;
}
