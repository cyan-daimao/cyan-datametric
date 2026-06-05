package com.cyan.datametric.application.collection.cmd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 采集事件指标同步命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class CollectionMetricUpsertCmd {

    /**
     * 采集事件ID
     */
    private String eventId;

    /**
     * 采集事件编码
     */
    private String eventCode;

    /**
     * 采集事件名称
     */
    private String eventName;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 主题域编码
     */
    private String subjectCode;

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
     * 业务口径
     */
    private String bizCaliber;

    /**
     * 技术口径
     */
    private String techCaliber;

    /**
     * 数据密级
     */
    private String securityLevel;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 过滤条件命令
     */
    @Data
    @Accessors(chain = true)
    public static class FilterConditionCmd {

        /**
         * 字段
         */
        private String field;

        /**
         * 操作符
         */
        private String op;

        /**
         * 字段值
         */
        private String value;
    }
}
