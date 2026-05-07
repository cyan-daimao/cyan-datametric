package com.cyan.datametric.adapter.semantic.http.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 语义查询命令 DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SemanticQueryCmdDTO {

    @NotEmpty(message = "指标编码列表不能为空")
    private List<String> metricCodes;

    private List<DimensionRefDTO> dimensions;

    private List<FilterRefDTO> filters;

    private List<OrderRefDTO> orders;

    private Integer limit;

    @Data
    @Accessors(chain = true)
    public static class DimensionRefDTO {
        private String tableId;
        private String columnName;
        private String alias;
    }

    @Data
    @Accessors(chain = true)
    public static class FilterRefDTO {
        private String tableId;
        private String columnName;
        private String operator;
        private List<String> values;
    }

    @Data
    @Accessors(chain = true)
    public static class OrderRefDTO {
        private String metricCode;
        private String tableId;
        private String columnName;
        private String direction;
    }
}
