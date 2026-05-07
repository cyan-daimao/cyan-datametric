package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * BI 维度简化列表项 DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionBiListItemDTO {

    private String id;
    private String dimCode;
    private String dimName;
    private String dimType;
    private String dataType;
    private String tableName;
    private String columnName;
    private String categoryName;
}
