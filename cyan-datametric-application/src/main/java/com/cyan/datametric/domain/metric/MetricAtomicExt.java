package com.cyan.datametric.domain.metric;

import com.cyan.datametric.enums.StatFunc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 原子指标扩展
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MetricAtomicExt {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标定义ID
     */
    private String metricId;

    /**
     * 统计函数
     */
    private StatFunc statFunc;

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
    private List<FilterCondition> filterCondition;

    /**
     * 过滤条件项
     */
    @Data
    @Accessors(chain = true)
    public static class FilterCondition {
        private String field;
        private String op;
        private String value;
    }
}
