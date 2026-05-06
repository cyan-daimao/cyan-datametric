package com.cyan.datametric.application.bi;

import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.enums.StatFunc;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 展开后的指标信息（用于BI SQL生成）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class ResolvedMetric {

    /**
     * 原始指标ID
     */
    private String metricId;

    /**
     * 显示别名
     */
    private String alias;

    /**
     * 原始指标类型
     */
    private MetricType originalType;

    // ====== 原子/派生指标字段 ======

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
     * 聚合函数
     */
    private StatFunc statFunc;

    /**
     * 原子指标自带过滤条件
     */
    private List<MetricAtomicExt.FilterCondition> atomicFilters;

    /**
     * 派生指标修饰词过滤
     */
    private List<Modifier> modifiers;

    /**
     * 派生指标时间周期
     */
    private TimePeriod timePeriod;

    // ====== 复合指标字段 ======

    /**
     * 计算公式
     */
    private String formula;

    /**
     * 引用的已展开指标列表
     */
    private List<ResolvedMetric> refMetrics;

    /**
     * 获取完整表名
     */
    public String getFullTableName() {
        if (dbName == null || tblName == null) {
            return null;
        }
        return dbName + "." + tblName;
    }

    /**
     * 是否为基础指标（原子或派生）
     */
    public boolean isBaseMetric() {
        return originalType == MetricType.ATOMIC || originalType == MetricType.DERIVED;
    }

    /**
     * 扁平化获取所有基础原子指标
     */
    public List<ResolvedMetric> flattenBaseMetrics() {
        List<ResolvedMetric> result = new ArrayList<>();
        if (isBaseMetric()) {
            result.add(this);
        } else if (refMetrics != null) {
            for (ResolvedMetric ref : refMetrics) {
                result.addAll(ref.flattenBaseMetrics());
            }
        }
        return result;
    }
}
