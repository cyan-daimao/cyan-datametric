package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

import java.util.List;

/**
 * 原子指标创建/更新命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class AtomicMetricCmd {

    /**
     * 指标名称
     */
    private String metricName;

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
    private List<FilterConditionCmd> filterCondition;

    /**
     * 主题域编码
     */
    private String subjectCode;

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
