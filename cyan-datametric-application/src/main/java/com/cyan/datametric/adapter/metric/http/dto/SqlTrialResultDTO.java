package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * SQL试算结果DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SqlTrialResultDTO {

    private List<ColumnDTO> columns;
    private List<List<Object>> rows;
    private String sql;
    private Long costTime;

    @Data
    @Accessors(chain = true)
    public static class ColumnDTO {
        private String name;
        private String type;
    }
}
