package com.cyan.datametric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.infra.persistence.metric.convert.MetricInfraConvert;
import com.cyan.datametric.infra.persistence.metric.dos.MetricAtomicDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricCompositeDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDerivedDO;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricAtomicMapper;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricCompositeMapper;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricDefinitionMapper;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricDerivedMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 指标仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class MetricRepositoryImpl implements MetricRepository {

    private final MetricDefinitionMapper definitionMapper;
    private final MetricAtomicMapper atomicMapper;
    private final MetricDerivedMapper derivedMapper;
    private final MetricCompositeMapper compositeMapper;

    public MetricRepositoryImpl(MetricDefinitionMapper definitionMapper,
                                MetricAtomicMapper atomicMapper,
                                MetricDerivedMapper derivedMapper,
                                MetricCompositeMapper compositeMapper) {
        this.definitionMapper = definitionMapper;
        this.atomicMapper = atomicMapper;
        this.derivedMapper = derivedMapper;
        this.compositeMapper = compositeMapper;
    }

    @Override
    public Metric findById(String id) {
        MetricDefinitionDO def = definitionMapper.selectById(Long.parseLong(id));
        if (def == null) {
            return null;
        }
        Metric metric = MetricInfraConvert.INSTANCE.toMetric(def);
        loadExt(metric);
        return metric;
    }

    @Override
    public com.cyan.arch.common.api.Page<Metric> page(MetricPageQuery query) {
        Page<MetricDefinitionDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<MetricDefinitionDO> wrapper = new LambdaQueryWrapper<MetricDefinitionDO>()
                .like(StringUtils.isNotBlank(query.getMetricName()), MetricDefinitionDO::getMetricName, query.getMetricName())
                .eq(StringUtils.isNotBlank(query.getMetricType()), MetricDefinitionDO::getMetricType, MetricType.of(query.getMetricType()))
                .eq(StringUtils.isNotBlank(query.getSubjectCode()), MetricDefinitionDO::getSubjectCode, query.getSubjectCode())
                .eq(StringUtils.isNotBlank(query.getStatus()), MetricDefinitionDO::getStatus, MetricStatus.of(query.getStatus()))
                .orderByDesc(MetricDefinitionDO::getUpdatedAt);
        Page<MetricDefinitionDO> result = definitionMapper.selectPage(page, wrapper);
        List<Metric> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(MetricInfraConvert.INSTANCE::toMetric)
                .toList();
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Metric findByName(String metricName) {
        LambdaQueryWrapper<MetricDefinitionDO> wrapper = new LambdaQueryWrapper<MetricDefinitionDO>()
                .eq(MetricDefinitionDO::getMetricName, metricName);
        MetricDefinitionDO def = definitionMapper.selectOne(wrapper);
        if (def == null) {
            return null;
        }
        Metric metric = MetricInfraConvert.INSTANCE.toMetric(def);
        loadExt(metric);
        return metric;
    }

    @Override
    public Metric save(Metric metric) {
        long id = SnowflakeIdUtil.nextId();
        metric.setId(String.valueOf(id));
        MetricDefinitionDO def = MetricInfraConvert.INSTANCE.toMetricDefinitionDO(metric);
        definitionMapper.insert(def);
        saveExt(metric);
        return findById(metric.getId());
    }

    @Override
    public Metric update(Metric metric) {
        MetricDefinitionDO def = MetricInfraConvert.INSTANCE.toMetricDefinitionDO(metric);
        definitionMapper.updateById(def);
        updateExt(metric);
        return findById(metric.getId());
    }

    @Override
    public void deleteById(String id) {
        definitionMapper.deleteById(Long.parseLong(id));
        deleteExt(id);
    }

    @Override
    public List<Metric> findDownstreamMetrics(String metricId) {
        LambdaQueryWrapper<MetricDerivedDO> derivedWrapper = new LambdaQueryWrapper<MetricDerivedDO>()
                .eq(MetricDerivedDO::getAtomicMetricId, Long.parseLong(metricId));
        List<MetricDerivedDO> derivedList = derivedMapper.selectList(derivedWrapper);
        List<Long> derivedMetricIds = derivedList.stream().map(MetricDerivedDO::getMetricId).toList();

        LambdaQueryWrapper<MetricCompositeDO> compositeWrapper = new LambdaQueryWrapper<MetricCompositeDO>()
                .like(MetricCompositeDO::getMetricRefs, metricId);
        List<MetricCompositeDO> compositeList = compositeMapper.selectList(compositeWrapper);
        List<Long> compositeMetricIds = compositeList.stream().map(MetricCompositeDO::getMetricId).toList();

        java.util.Set<Long> allIds = new java.util.HashSet<>();
        allIds.addAll(derivedMetricIds);
        allIds.addAll(compositeMetricIds);

        if (allIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<MetricDefinitionDO> defWrapper = new LambdaQueryWrapper<MetricDefinitionDO>()
                .in(MetricDefinitionDO::getId, allIds);
        List<MetricDefinitionDO> defs = definitionMapper.selectList(defWrapper);
        return defs.stream().map(MetricInfraConvert.INSTANCE::toMetric).toList();
    }

    @Override
    public long countByType(String metricType) {
        LambdaQueryWrapper<MetricDefinitionDO> wrapper = new LambdaQueryWrapper<MetricDefinitionDO>()
                .eq(MetricDefinitionDO::getMetricType, MetricType.valueOf(metricType));
        return definitionMapper.selectCount(wrapper);
    }

    @Override
    public long countByStatus(String status) {
        LambdaQueryWrapper<MetricDefinitionDO> wrapper = new LambdaQueryWrapper<MetricDefinitionDO>()
                .eq(MetricDefinitionDO::getStatus, MetricStatus.valueOf(status));
        return definitionMapper.selectCount(wrapper);
    }

    @Override
    public List<Map<String, Object>> countBySubject() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        List<MetricDefinitionDO> list = definitionMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, java.util.Map<String, Object>> group = new java.util.HashMap<>();
        for (MetricDefinitionDO d : list) {
            String sc = d.getSubjectCode() == null ? "" : d.getSubjectCode();
            group.computeIfAbsent(sc, k -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("subjectCode", sc);
                m.put("count", 0L);
                return m;
            });
            group.get(sc).put("count", ((Long) group.get(sc).get("count")) + 1);
        }
        result.addAll(group.values());
        return result;
    }

    private void loadExt(Metric metric) {
        if (metric == null || metric.getMetricType() == null) {
            return;
        }
        Long mid = Long.parseLong(metric.getId());
        switch (metric.getMetricType()) {
            case ATOMIC -> {
                LambdaQueryWrapper<MetricAtomicDO> w = new LambdaQueryWrapper<MetricAtomicDO>()
                        .eq(MetricAtomicDO::getMetricId, mid);
                MetricAtomicDO atomic = atomicMapper.selectOne(w);
                if (atomic != null) {
                    metric.setAtomicExt(MetricInfraConvert.INSTANCE.toAtomicExt(atomic));
                }
            }
            case DERIVED -> {
                LambdaQueryWrapper<MetricDerivedDO> w = new LambdaQueryWrapper<MetricDerivedDO>()
                        .eq(MetricDerivedDO::getMetricId, mid);
                MetricDerivedDO derived = derivedMapper.selectOne(w);
                if (derived != null) {
                    metric.setDerivedExt(MetricInfraConvert.INSTANCE.toDerivedExt(derived));
                }
            }
            case COMPOSITE -> {
                LambdaQueryWrapper<MetricCompositeDO> w = new LambdaQueryWrapper<MetricCompositeDO>()
                        .eq(MetricCompositeDO::getMetricId, mid);
                MetricCompositeDO composite = compositeMapper.selectOne(w);
                if (composite != null) {
                    metric.setCompositeExt(MetricInfraConvert.INSTANCE.toCompositeExt(composite));
                }
            }
        }
    }

    private void saveExt(Metric metric) {
        if (metric == null || metric.getMetricType() == null) {
            return;
        }
        Long mid = Long.parseLong(metric.getId());
        switch (metric.getMetricType()) {
            case ATOMIC -> {
                if (metric.getAtomicExt() != null) {
                    MetricAtomicDO d = MetricInfraConvert.INSTANCE.toAtomicDO(metric.getAtomicExt());
                    d.setId(SnowflakeIdUtil.nextId());
                    d.setMetricId(mid);
                    atomicMapper.insert(d);
                }
            }
            case DERIVED -> {
                if (metric.getDerivedExt() != null) {
                    MetricDerivedDO d = MetricInfraConvert.INSTANCE.toDerivedDO(metric.getDerivedExt());
                    d.setId(SnowflakeIdUtil.nextId());
                    d.setMetricId(mid);
                    derivedMapper.insert(d);
                }
            }
            case COMPOSITE -> {
                if (metric.getCompositeExt() != null) {
                    MetricCompositeDO d = MetricInfraConvert.INSTANCE.toCompositeDO(metric.getCompositeExt());
                    d.setId(SnowflakeIdUtil.nextId());
                    d.setMetricId(mid);
                    compositeMapper.insert(d);
                }
            }
        }
    }

    private void updateExt(Metric metric) {
        deleteExt(metric.getId());
        saveExt(metric);
    }

    private void deleteExt(String id) {
        Long mid = Long.parseLong(id);
        LambdaQueryWrapper<MetricAtomicDO> aw = new LambdaQueryWrapper<MetricAtomicDO>().eq(MetricAtomicDO::getMetricId, mid);
        atomicMapper.delete(aw);
        LambdaQueryWrapper<MetricDerivedDO> dw = new LambdaQueryWrapper<MetricDerivedDO>().eq(MetricDerivedDO::getMetricId, mid);
        derivedMapper.delete(dw);
        LambdaQueryWrapper<MetricCompositeDO> cw = new LambdaQueryWrapper<MetricCompositeDO>().eq(MetricCompositeDO::getMetricId, mid);
        compositeMapper.delete(cw);
    }
}
