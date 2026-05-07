package com.cyan.datametric.adapter.semantic.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 语义查询结果 DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SemanticQueryResultDTO {

    private String status;
    private String sql;
    private String routeType;
    private Long costTimeMs;
    private String errorMessage;
    private List<String> columns;
    private List<Map<String, Object>> rows;
}
