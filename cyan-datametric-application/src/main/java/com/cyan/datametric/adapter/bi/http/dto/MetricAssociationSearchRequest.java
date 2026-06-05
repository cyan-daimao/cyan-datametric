package com.cyan.datametric.adapter.bi.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标维度可关联搜索请求
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricAssociationSearchRequest {

    /**
     * 已选指标编码列表
     */
    private List<String> metricCodes;

    /**
     * 已选维度编码列表
     */
    private List<String> dimCodes;

    /**
     * 指标名称模糊搜索
     */
    private String metricName;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 维度名称模糊搜索
     */
    private String dimName;

    /**
     * 维度分类ID
     */
    private String categoryId;

    /**
     * 是否在候选中包含已选对象
     */
    private Boolean includeSelected;
}
