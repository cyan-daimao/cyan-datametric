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
     */
    private String dsName;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tblName;

    /**
     * 字段名称
     */
    private String colName;

    /**
     * 过滤条件
     */
    private List<FilterConditionBO> filterCondition;

    @Data
    @Accessors(chain = true)
    public static class FilterConditionBO {
        private String field;
        private String op;
        private String value;
    }
}
