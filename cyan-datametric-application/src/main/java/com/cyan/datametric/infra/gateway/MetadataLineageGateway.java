package com.cyan.datametric.infra.gateway;

import com.cyan.dataman.client.lineage.MetadataLineageClient;
import com.cyan.dataman.client.lineage.request.MetadataLineageSyncRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 元数据血缘网关
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Component
public class MetadataLineageGateway {

    private final MetadataLineageClient metadataLineageClient;

    public MetadataLineageGateway(MetadataLineageClient metadataLineageClient) {
        this.metadataLineageClient = metadataLineageClient;
    }

    /**
     * 同步血缘
     */
    public void sync(MetadataLineageSyncRequest request) {
        try {
            metadataLineageClient.sync(request);
        } catch (Exception e) {
            log.warn("同步 datametric 血缘失败, serviceName={}, refId={}", request.getServiceName(), request.getRefId(), e);
        }
    }
}
