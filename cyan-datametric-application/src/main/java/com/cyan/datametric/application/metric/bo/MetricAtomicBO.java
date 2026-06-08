package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 原子指标扩展BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricAtomicBO {

    /**
     * 统计函数
     */
    private String statFunc;

    /**
     * 数据源名称
     *
     * @deprecated 仅用于兼容旧展示。
     */
    @Deprecated
    private String dsName;

    /**
     * 数据库名称
     *
     * @deprecated 仅用于兼容旧展示。
     */
    @Deprecated
    private String dbName;

    /**
     * 表名称
     *
     * @deprecated 仅用于兼容旧展示。
     */
    @Deprecated
    private String tblName;

    /**
     * 字段名称
     *
     * @deprecated 仅用于兼容旧展示。
     */
    @Deprecated
    private String colName;

    /**
     * 过滤条件
     *
     * @deprecated 仅用于兼容旧展示。
     */
    @Deprecated
    private List<FilterConditionBO> filterCondition;

    /**
     * 字段绑定列表
     */
    private List<MetricFieldBindingBO> fieldBindings;

    @Data
    @Accessors(chain = true)
    public static class FilterConditionBO {
        private String field;
        private String op;
        private String value;
    }
}
