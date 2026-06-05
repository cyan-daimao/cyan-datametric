package com.cyan.datametric.infra.gateway;

import com.cyan.arch.common.api.Response;
import com.cyan.dataman.client.table.TableRelationClient;
import com.cyan.dataman.client.table.dto.JoinPathsRequestDTO;
import com.cyan.dataman.client.table.dto.TableRelationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 表关系网关封装层
 * <p>
 * 封装对 cyan-dataman 表关系服务的 Feign 调用，Application 层通过此类访问外部表关系能力。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TableRelationGateway {

    private final TableRelationClient tableRelationClient;

    /**
     * 批量查询 JOIN 路径
     *
     * @param request 请求体
     * @return JOIN 关系列表
     */
    public Response<List<TableRelationDTO>> findJoinPaths(JoinPathsRequestDTO request) {
        return tableRelationClient.findJoinPaths(request);
    }
}
