package com.cyan.datametric.client.audience.request;

import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标人群圈选命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricAudienceSelectionCmd {

    /**
     * 圈选实体类型
     */
    private String entityType;

    /**
     * 实体ID维度编码
     */
    private String entityIdDimCode;

    /**
     * 实体ID物理字段名（兼容旧规则）
     */
    private String entityIdColumn;

    /**
     * 指标列表
     */
    private List<MetricBiAnalysisCmd.MetricRef> metrics;

    /**
     * 维度列表
     */
    private List<MetricBiAnalysisCmd.DimensionRef> dimensions;

    /**
     * 过滤条件
     */
    private List<MetricBiAnalysisCmd.FilterRef> filters;

    /**
     * 排序配置
     */
    private List<MetricBiAnalysisCmd.OrderRef> orders;

    /**
     * 限制条数
     */
    private Integer limitValue;
}
