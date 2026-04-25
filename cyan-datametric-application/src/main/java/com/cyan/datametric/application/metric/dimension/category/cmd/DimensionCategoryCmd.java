package com.cyan.datametric.application.metric.dimension.category.cmd;

import lombok.Data;

/**
 * 维度分类命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class DimensionCategoryCmd {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父节点ID
     */
    private String parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
