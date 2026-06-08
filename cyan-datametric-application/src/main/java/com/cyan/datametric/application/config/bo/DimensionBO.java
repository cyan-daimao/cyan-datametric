package com.cyan.datametric.application.config.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公共维度业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionBO {

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

    /**
     * 维度字段绑定列表
     */
    private List<DimensionFieldBindingBO> fieldBindings;
}
