package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.datametric.enums.StatFunc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 原子指标扩展表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_atomic")
public class MetricAtomicDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 指标定义ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * 统计函数
     */
    @TableField("stat_func")
    private StatFunc statFunc;

    /**
     * 数据源名称
     */
    @TableField("ds_name")
    private String dsName;

    /**
     * 数据库名称
     */
    @TableField("db_name")
    private String dbName;

    /**
     * 表名称
     */
    @TableField("tbl_name")
    private String tblName;

    /**
     * 字段名称
     */
    @TableField("col_name")
    private String colName;

    /**
     * 过滤条件JSON
     */
    @TableField("filter_condition")
    private String filterCondition;
}
