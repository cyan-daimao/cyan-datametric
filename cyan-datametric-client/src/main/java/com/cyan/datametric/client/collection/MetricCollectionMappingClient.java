package com.cyan.datametric.client.collection;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.client.collection.dto.CollectionDimensionMappingDTO;
import com.cyan.datametric.client.collection.dto.CollectionMetricMappingDTO;
import com.cyan.datametric.client.collection.request.CollectionDimensionUpsertRequest;
import com.cyan.datametric.client.collection.request.CollectionMetricUpsertRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 采集平台指标映射 RPC 客户端
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "cyan-datametric", contextId = "metricCollectionMappingClient", path = "/rpc/v1/metrics/collection", url = "${feign.cyan-datametric.url:}")
public interface MetricCollectionMappingClient {

    /**
     * 根据采集属性幂等创建或更新维度
     *
     * @param request 采集属性维度请求
     * @return 维度映射结果
     */
    @PostMapping("/dimensions/upsert-from-property")
    Response<CollectionDimensionMappingDTO> upsertDimensionFromProperty(@RequestBody CollectionDimensionUpsertRequest request);

    /**
     * 根据采集事件幂等创建或更新原子指标
     *
     * @param request 采集事件指标请求
     * @return 指标映射结果
     */
    @PostMapping("/atomic/upsert-from-event")
    Response<CollectionMetricMappingDTO> upsertAtomicMetricFromEvent(@RequestBody CollectionMetricUpsertRequest request);
}
