package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

/**
 * SQL试算命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class SqlTrialCmd {

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 定义体
     */
    private SqlPreviewCmd.DefinitionBody definitionBody;

    /**
     * 限制条数
     */
    private Integer limit;
}
