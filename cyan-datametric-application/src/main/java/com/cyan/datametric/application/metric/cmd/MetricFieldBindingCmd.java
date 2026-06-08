package com.cyan.datametric.application.metric.cmd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标字段绑定命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricFieldBindingCmd {

    /**
     * 绑定ID
     */
    private String id;

    /**
     * catalog 名称
     */
    private String catalogName;

    /**
     * schema 名称
     */
    private String schemaName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 过滤条件
     */
    private List<AtomicMetricCmd.FilterConditionCmd> filterCondition;

    /**
     * 是否主绑定
     */
    private Boolean primaryBinding;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
