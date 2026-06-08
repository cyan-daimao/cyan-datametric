package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标字段绑定DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricFieldBindingDTO {

    /**
     * 绑定ID
     */
    private String id;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * catalog 名称
     */
    private String catalogName;

    /**
     * schema 名称
     */
    private String schemaName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 过滤条件
     */
    private List<MetricDetailDTO.FilterConditionDTO> filterCondition;

    /**
     * 是否主绑定
     */
    private Boolean primaryBinding;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
