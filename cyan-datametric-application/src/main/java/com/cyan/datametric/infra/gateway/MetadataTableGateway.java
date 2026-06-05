package com.cyan.datametric.infra.gateway;

import com.cyan.arch.common.api.Response;
import com.cyan.dataman.client.table.MetadataTableClient;
import com.cyan.dataman.client.table.dto.MetadataColumnDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元数据表网关封装层
 * <p>
 * 封装对 cyan-dataman 元数据表服务的 Feign 调用。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataTableGateway {

    private final MetadataTableClient metadataTableClient;

    /**
     * 查询元数据表字段列表
     *
     * @param catalog 数据目录
     * @param schema 数据库/schema
     * @param name 表名
     * @return 字段列表
     */
    public List<MetadataColumnDTO> listColumns(String catalog, String schema, String name) {
        try {
            Response<List<MetadataColumnDTO>> response = metadataTableClient.listColumns(catalog, schema, name);
            if (response == null || response.getCode() != 200 || response.getData() == null) {
                return List.of();
            }
            return response.getData();
        } catch (Exception e) {
            log.warn("查询元数据表字段失败, catalog={}, schema={}, name={}", catalog, schema, name, e);
            return List.of();
        }
    }
}
