package com.cyan.datametric.adapter.config.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公共维度DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionDTO {

    private String id;
    private String dimCode;
    private String dimName;
    private String dimType;
    private String dimensionKind;
    private String dataType;
    private List<String> dimValues;
    private String categoryId;
    private String categoryName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String displayColumn;
    private String sourceType;
    private String sourceExpr;
    private String sourceTable;
    private String hierarchyCode;
    private String hierarchyName;
    private String parentDimCode;
    private Integer hierarchyLevel;
    private Integer sortOrder;
    private String description;
    private LocalDateTime updatedAt;
}
