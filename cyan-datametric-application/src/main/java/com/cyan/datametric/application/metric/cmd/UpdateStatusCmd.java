package com.cyan.datametric.application.metric.cmd;

import lombok.Data;

/**
 * 更新指标状态命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class UpdateStatusCmd {

    /**
     * 状态
     */
    private String status;
}
