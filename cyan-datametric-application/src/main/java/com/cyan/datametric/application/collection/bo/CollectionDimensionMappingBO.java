package com.cyan.datametric.application.collection.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 采集属性维度映射业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class CollectionDimensionMappingBO {

    /**
     * 维度ID
     */
    private String dimId;

    /**
     * 维度编码
     */
    private String dimCode;

    /**
     * 维度名称
     */
    private String dimName;

    /**
     * 是否新建
     */
    private Boolean created;
}
