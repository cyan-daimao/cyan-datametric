package com.cyan.datametric.application.metric.bo;

import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricBO {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private MetricType metricType;

    /**
     * 关联主题域编码
     */
    private String subjectCode;

    /**
     * 主题域名称
     */
    private String subjectName;

    /**
     * 业务口径
     */
    private String bizCaliber;

    /**
     * 技术口径
     */
    private String techCaliber;

    /**
     * 状态
     */
    private MetricStatus status;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 统计函数（原子指标）
     */
    private String statFunc;

    /**
     * 数据源名称（原子指标）
     */
    private String dsName;

    /**
     * 数据库名称（原子指标）
     */
    private String dbName;

    /**
     * 表名称（原子指标）
     */
    private String tblName;

    /**
     * 字段名称（原子指标）
     */
    private String colName;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否已收藏（字典使用）
     */
    private Boolean isFavorite;

    /**
     * 原子指标扩展详情
     */
    private MetricAtomicBO atomic;

    /**
     * 派生指标扩展详情
     */
    private MetricDerivedBO derived;

    /**
     * 复合指标扩展详情
     */
    private MetricCompositeBO composite;
}
