package com.cyan.datametric.infra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * SQL 执行命令（发给 datagateway）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SqlExecuteCmd {

    /**
     * SQL 内容
     */
    private String sql;

    /**
     * 目标数据库
     */
    private String database;

    /**
     * 超时时间（毫秒）
     */
    private Long timeoutMs = 60000L;
}
