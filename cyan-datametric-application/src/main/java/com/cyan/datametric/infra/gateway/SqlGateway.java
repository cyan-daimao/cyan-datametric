package com.cyan.datametric.infra.gateway;

import com.cyan.arch.common.api.Response;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SQL 网关封装层
 * <p>
 * 封装对 cyan-datagateway 的 Feign 调用，Application 层通过此类访问外部 SQL 执行能力。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SqlGateway {

    private final SqlGatewayClient sqlGatewayClient;

    /**
     * 执行 StarRocks SQL
     *
     * @param cmd SQL 执行命令
     * @return 执行结果
     */
    public Response<SqlExecuteResultDTO> executeStarRocksSql(SqlExecuteCmd cmd) {
        return sqlGatewayClient.executeStarRocksSql(cmd);
    }

    /**
     * 执行 Spark SQL
     *
     * @param cmd SQL 执行命令
     * @return 执行结果
     */
    public Response<SqlExecuteResultDTO> executeSparkSql(SqlExecuteCmd cmd) {
        return sqlGatewayClient.executeSparkSql(cmd);
    }

    /**
     * 执行 Metric SQL
     *
     * @param cmd SQL 执行命令
     * @return 执行结果
     */
    public Response<SqlExecuteResultDTO> executeMetricSql(SqlExecuteCmd cmd) {
        return sqlGatewayClient.executeMetricSql(cmd);
    }
}
