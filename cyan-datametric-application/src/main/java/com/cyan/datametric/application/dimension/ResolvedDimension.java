package com.cyan.datametric.application.dimension;

import com.cyan.datametric.domain.config.DimensionFieldBinding;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 已解析维度 SQL 信息
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class ResolvedDimension {

    /**
     * 维度编码
     */
    private String dimCode;

    /**
     * 维度名称
     */
    private String dimName;

    /**
     * 维度实现类型
     */
    private String dimensionKind;

    /**
     * SELECT 表达式
     */
    private String selectExpr;

    /**
     * GROUP BY 表达式
     */
    private String groupExpr;

    /**
     * FILTER 表达式
     */
    private String filterExpr;

    /**
     * 维表引用
     */
    private String tableRef;

    /**
     * 来源事实表引用
     */
    private String sourceTableRef;

    /**
     * 是否需要 JOIN 维表
     */
    private boolean requiresJoin;

    /**
     * 物理字段名
     */
    private String columnName;

    /**
     * 显示字段名
     */
    private String displayColumn;

    /**
     * 是否系统内置维度
     */
    private boolean builtin;

    /**
     * 字段绑定候选
     */
    private List<DimensionFieldBinding> fieldBindings;
}
