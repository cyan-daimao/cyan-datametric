package com.cyan.datametric.client;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * BI 维度简化列表项（Client DTO）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionBiListItem {

    private String id;
    private String dimCode;
    private String dimName;
    private String dimType;
    private String dimensionKind;
    private String dataType;
    private String tableName;
    private String sourceTable;
    private String columnName;
    private String displayColumn;
    private String categoryName;
    private String hierarchyCode;
    private String hierarchyName;
    private String parentDimCode;
    private Integer hierarchyLevel;
    private Integer sortOrder;
}
