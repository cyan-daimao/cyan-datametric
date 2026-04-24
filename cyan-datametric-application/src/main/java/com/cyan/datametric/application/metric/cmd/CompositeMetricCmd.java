package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

import java.util.List;

/**
 * 复合指标创建/更新命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class CompositeMetricCmd {

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
     * 计算公式
     */
    private String formula;

    /**
     * 引用的指标ID列表
     */
    private List<String> metricRefs;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
