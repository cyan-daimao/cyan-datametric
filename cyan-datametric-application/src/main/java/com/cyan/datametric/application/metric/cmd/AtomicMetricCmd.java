package com.cyan.datametric.application.metric.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 原子指标创建/更新命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AtomicMetricCmd {

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 业务口径
     */
    private String bizCaliber;

    /**
     * 技术口径
     */
    private String techCaliber;

    /**
     * 统计函数
     */
    private String statFunc;

    /**
     * 数据源名称
     *
     * @deprecated 请使用 fieldBindings。
     */
    @Deprecated
    private String dsName;

    /**
     * 数据库名称
     *
     * @deprecated 请使用 fieldBindings。
     */
    @Deprecated
    private String dbName;

    /**
     * 表名称
     *
     * @deprecated 请使用 fieldBindings。
     */
    @Deprecated
    private String tblName;

    /**
     * 字段名称
     *
     * @deprecated 请使用 fieldBindings。
     */
    @Deprecated
    private String colName;

    /**
     * 过滤条件
     *
     * @deprecated 请使用 fieldBindings。
     */
    @Deprecated
    private List<FilterConditionCmd> filterCondition;

    /**
     * 指标字段绑定列表
     */
    private List<MetricFieldBindingCmd> fieldBindings;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 数据密级
     */
    private String securityLevel;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    @Data
    public static class FilterConditionCmd {
        private String field;
        private String op;
        private String value;
    }
}
