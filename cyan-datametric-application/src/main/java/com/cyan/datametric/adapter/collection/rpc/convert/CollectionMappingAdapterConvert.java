package com.cyan.datametric.adapter.collection.rpc.convert;

import com.cyan.datametric.application.collection.bo.CollectionDimensionMappingBO;
import com.cyan.datametric.application.collection.bo.CollectionMetricMappingBO;
import com.cyan.datametric.application.collection.cmd.CollectionDimensionUpsertCmd;
import com.cyan.datametric.application.collection.cmd.CollectionMetricUpsertCmd;
import com.cyan.datametric.client.collection.dto.CollectionDimensionMappingDTO;
import com.cyan.datametric.client.collection.dto.CollectionMetricMappingDTO;
import com.cyan.datametric.client.collection.request.CollectionDimensionUpsertRequest;
import com.cyan.datametric.client.collection.request.CollectionMetricUpsertRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 采集映射适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface CollectionMappingAdapterConvert {

    /**
     * 转换实例
     */
    CollectionMappingAdapterConvert INSTANCE = Mappers.getMapper(CollectionMappingAdapterConvert.class);

    /**
     * 转换维度同步命令
     */
    CollectionDimensionUpsertCmd toDimensionCmd(CollectionDimensionUpsertRequest request);

    /**
     * 转换指标同步命令
     */
    CollectionMetricUpsertCmd toMetricCmd(CollectionMetricUpsertRequest request);

    /**
     * 转换维度映射结果
     */
    CollectionDimensionMappingDTO toDimensionDTO(CollectionDimensionMappingBO bo);

    /**
     * 转换指标映射结果
     */
    CollectionMetricMappingDTO toMetricDTO(CollectionMetricMappingBO bo);
}
