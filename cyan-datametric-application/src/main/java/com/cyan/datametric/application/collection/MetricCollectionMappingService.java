package com.cyan.datametric.application.collection;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.application.collection.bo.CollectionDimensionMappingBO;
import com.cyan.datametric.application.collection.bo.CollectionMetricMappingBO;
import com.cyan.datametric.application.collection.cmd.CollectionDimensionUpsertCmd;
import com.cyan.datametric.application.collection.cmd.CollectionMetricUpsertCmd;
import com.cyan.datametric.application.collection.convert.CollectionMappingAppConvert;
import com.cyan.datametric.application.config.DimensionService;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.MetricBO;
import com.cyan.datametric.application.metric.cmd.AtomicMetricCmd;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采集平台指标映射服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MetricCollectionMappingService {

    private final DimensionService dimensionService;
    private final MetricService metricService;
    private final DimensionRepository dimensionRepository;
    private final MetricRepository metricRepository;
    private final CollectionMappingAppConvert collectionMappingAppConvert;

    /**
     * 采集属性幂等同步为维度
     *
     * @param cmd 采集属性维度命令
     * @return 维度映射结果
     */
    @Transactional
    public CollectionDimensionMappingBO upsertDimensionFromProperty(CollectionDimensionUpsertCmd cmd) {
        Assert.notBlank(cmd.getPropertyCode(), new BusinessException("采集属性编码不能为空"));
        DimensionCmd dimensionCmd = collectionMappingAppConvert.toDimensionCmd(cmd);
        boolean created = dimensionRepository.findByDimCode(dimensionCmd.getDimCode()) == null;
        DimensionBO bo = dimensionService.upsertByDimCode(dimensionCmd);
        return new CollectionDimensionMappingBO()
                .setDimId(bo.getId())
                .setDimCode(bo.getDimCode())
                .setDimName(bo.getDimName())
                .setCreated(created);
    }

    /**
     * 采集事件幂等同步为原子指标
     *
     * @param cmd 采集事件指标命令
     * @return 指标映射结果
     */
    @Transactional
    public CollectionMetricMappingBO upsertAtomicMetricFromEvent(CollectionMetricUpsertCmd cmd) {
        Assert.notBlank(cmd.getEventCode(), new BusinessException("采集事件编码不能为空"));
        Assert.notBlank(cmd.getTblName(), new BusinessException("指标来源表不能为空"));
        AtomicMetricCmd atomicMetricCmd = collectionMappingAppConvert.toAtomicMetricCmd(cmd);
        boolean created = metricRepository.findByMetricCode(atomicMetricCmd.getMetricCode()) == null;
        MetricBO bo = metricService.upsertAtomicByMetricCode(atomicMetricCmd);
        return new CollectionMetricMappingBO()
                .setMetricId(bo.getId())
                .setMetricCode(bo.getMetricCode())
                .setMetricName(bo.getMetricName())
                .setCreated(created);
    }
}
