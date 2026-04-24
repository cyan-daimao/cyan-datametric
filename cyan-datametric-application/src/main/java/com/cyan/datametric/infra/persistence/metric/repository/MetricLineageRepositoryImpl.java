package com.cyan.datametric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.metric.LineageNode;
import com.cyan.datametric.domain.metric.repository.MetricLineageRepository;
import com.cyan.datametric.infra.persistence.metric.convert.MetricInfraConvert;
import com.cyan.datametric.infra.persistence.metric.dos.MetricLineageDO;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricLineageMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标血缘仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class MetricLineageRepositoryImpl implements MetricLineageRepository {

    private final MetricLineageMapper lineageMapper;

    public MetricLineageRepositoryImpl(MetricLineageMapper lineageMapper) {
        this.lineageMapper = lineageMapper;
    }

    @Override
    public List<LineageNode> findUpstream(String metricId) {
        List<MetricLineageDO> list = lineageMapper.selectUpstream(Long.parseLong(metricId));
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(this::toLineageNode)
                .toList();
    }

    @Override
    public List<LineageNode> findDownstream(String metricId) {
        List<MetricLineageDO> list = lineageMapper.selectDownstream(Long.parseLong(metricId));
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(this::toLineageNode)
                .toList();
    }

    @Override
    public void saveAll(List<LineageNode> nodes) {
        for (LineageNode node : nodes) {
            MetricLineageDO lineageDO = new MetricLineageDO();
            lineageDO.setId(SnowflakeIdUtil.nextId());
            lineageDO.setMetricId(Long.parseLong(node.getMetricId()));
            lineageDO.setParentMetricId(node.getParentMetricId() == null ? null : Long.parseLong(node.getParentMetricId()));
            lineageDO.setUpstreamType(node.getUpstreamType());
            lineageDO.setUpstreamId(node.getUpstreamId());
            lineageDO.setUpstreamName(node.getUpstreamName());
            lineageDO.setLineageType(node.getLineageType());
            lineageDO.setLevel(node.getLevel());
            lineageMapper.insert(lineageDO);
        }
    }

    @Override
    public void deleteByMetricId(String metricId) {
        LambdaQueryWrapper<MetricLineageDO> wrapper = new LambdaQueryWrapper<MetricLineageDO>()
                .eq(MetricLineageDO::getMetricId, Long.parseLong(metricId))
                .or()
                .eq(MetricLineageDO::getParentMetricId, Long.parseLong(metricId));
        lineageMapper.delete(wrapper);
    }

    private LineageNode toLineageNode(MetricLineageDO lineageDO) {
        LineageNode node = new LineageNode();
        node.setId(String.valueOf(lineageDO.getId()));
        node.setMetricId(String.valueOf(lineageDO.getMetricId()));
        node.setParentMetricId(lineageDO.getParentMetricId() == null ? null : String.valueOf(lineageDO.getParentMetricId()));
        node.setUpstreamType(lineageDO.getUpstreamType());
        node.setUpstreamId(lineageDO.getUpstreamId());
        node.setUpstreamName(lineageDO.getUpstreamName());
        node.setLineageType(lineageDO.getLineageType());
        node.setLevel(lineageDO.getLevel());
        return node;
    }
}
