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
     *
     * @deprecated 物理字段已迁移到 MetricFieldBinding，仅用于兼容旧展示。
     */
    @Deprecated
    private String dsName;

    /**
     * 数据库名称
     *
     * @deprecated 物理字段已迁移到 MetricFieldBinding，仅用于兼容旧展示。
     */
    @Deprecated
    private String dbName;

    /**
     * 表名称
     *
     * @deprecated 物理字段已迁移到 MetricFieldBinding，仅用于兼容旧展示。
     */
    @Deprecated
    private String tblName;

    /**
     * 字段名称
     *
     * @deprecated 物理字段已迁移到 MetricFieldBinding，仅用于兼容旧展示。
     */
    @Deprecated
    private String colName;

    /**
     * 过滤条件
     *
     * @deprecated 过滤条件已迁移到 MetricFieldBinding，仅用于兼容旧展示。
     */
    @Deprecated
    private List<FilterCondition> filterCondition;

    /**
     * 字段绑定列表
     */
    private List<MetricFieldBinding> fieldBindings;

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
