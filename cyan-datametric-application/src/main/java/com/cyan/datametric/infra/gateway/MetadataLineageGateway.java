package com.cyan.datametric.infra.gateway;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Response;
import com.cyan.dataman.client.lineage.MetadataLineageClient;
import com.cyan.dataman.client.lineage.request.MetadataLineageSyncRequest;
import org.springframework.stereotype.Component;

/**
 * 元数据血缘网关
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
public class MetadataLineageGateway {

    private static final int SUCCESS_CODE = 200;

    private final MetadataLineageClient metadataLineageClient;

    public MetadataLineageGateway(MetadataLineageClient metadataLineageClient) {
        this.metadataLineageClient = metadataLineageClient;
    }

    /**
     * 同步血缘
     */
    public void sync(MetadataLineageSyncRequest request) {
        Response<Void> response = metadataLineageClient.sync(request);
        if (response == null) {
            throw new BusinessException("同步指标字段血缘失败：元数据服务无响应");
        }
        if (response.getCode() != SUCCESS_CODE) {
            throw new BusinessException("同步指标字段血缘失败：" + response.getMessage());
        }
    }
}
