package com.cyan.datametric.adapter.collection.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.collection.rpc.convert.CollectionMappingAdapterConvert;
import com.cyan.datametric.application.collection.MetricCollectionMappingService;
import com.cyan.datametric.application.collection.bo.CollectionDimensionMappingBO;
import com.cyan.datametric.application.collection.bo.CollectionMetricMappingBO;
import com.cyan.datametric.client.collection.MetricCollectionMappingClient;
import com.cyan.datametric.client.collection.dto.CollectionDimensionMappingDTO;
import com.cyan.datametric.client.collection.dto.CollectionMetricMappingDTO;
import com.cyan.datametric.client.collection.request.CollectionDimensionUpsertRequest;
import com.cyan.datametric.client.collection.request.CollectionMetricUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采集平台指标映射 RPC 控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/metrics/collection")
@RequiredArgsConstructor
public class MetricCollectionMappingRpcController implements MetricCollectionMappingClient {

    private final MetricCollectionMappingService metricCollectionMappingService;

    @Override
    public Response<CollectionDimensionMappingDTO> upsertDimensionFromProperty(CollectionDimensionUpsertRequest request) {
        CollectionDimensionMappingBO bo = metricCollectionMappingService.upsertDimensionFromProperty(
                CollectionMappingAdapterConvert.INSTANCE.toDimensionCmd(request));
        return Response.success(CollectionMappingAdapterConvert.INSTANCE.toDimensionDTO(bo));
    }

    @Override
    public Response<CollectionMetricMappingDTO> upsertAtomicMetricFromEvent(CollectionMetricUpsertRequest request) {
        CollectionMetricMappingBO bo = metricCollectionMappingService.upsertAtomicMetricFromEvent(
                CollectionMappingAdapterConvert.INSTANCE.toMetricCmd(request));
        return Response.success(CollectionMappingAdapterConvert.INSTANCE.toMetricDTO(bo));
    }
}
