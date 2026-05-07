package com.cyan.datametric.infra.client;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.infra.client.dto.SqlExecuteCmd;
import com.cyan.datametric.infra.client.dto.SqlExecuteResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 数据网关客户端（调用 datagateway 执行 SQL）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "cyan-datagateway", path = "/api/v1/starrocks/sql")
public interface DatagatewayClient {

    /**
     * 执行 SQL
     *
     * @param cmd SQL 执行命令
     * @return 执行结果
     */
    @PostMapping("/execute")
    Response<SqlExecuteResultDTO> execute(@RequestBody SqlExecuteCmd cmd);
}
