package com.cyan.datametric.infra.persistence.semantic.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.SemanticMetric;
import com.cyan.datametric.domain.semantic.repository.SemanticMetricRepository;
import com.cyan.datametric.infra.persistence.semantic.convert.SemanticInfraConvert;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticMetricDO;
import com.cyan.datametric.infra.persistence.semantic.mappers.SemanticMetricMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 语义指标仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class SemanticMetricRepositoryImpl implements SemanticMetricRepository {

    private final SemanticMetricMapper mapper;

    public SemanticMetricRepositoryImpl(SemanticMetricMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SemanticMetric findById(String id) {
        return SemanticInfraConvert.INSTANCE.toSemanticMetric(mapper.selectById(Long.parseLong(id)));
    }

    @Override
    public SemanticMetric findByMetricCode(String metricCode) {
        LambdaQueryWrapper<SemanticMetricDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticMetricDO::getMetricCode, metricCode);
        return SemanticInfraConvert.INSTANCE.toSemanticMetric(mapper.selectOne(wrapper));
    }

    @Override
    public List<SemanticMetric> findByMetricCodes(List<String> metricCodes) {
        LambdaQueryWrapper<SemanticMetricDO> wrapper = new LambdaQueryWrapper<>()
                .in(SemanticMetricDO::getMetricCode, metricCodes);
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toSemanticMetric)
                .toList();
    }

    @Override
    public List<SemanticMetric> findBySourceTableId(String sourceTableId) {
        LambdaQueryWrapper<SemanticMetricDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticMetricDO::getSourceTableId, Long.parseLong(sourceTableId));
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toSemanticMetric)
                .toList();
    }

    @Override
    public Page<SemanticMetric> page(int pageNum, int pageSize) {
        Page<SemanticMetricDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SemanticMetricDO> wrapper = new LambdaQueryWrapper<>()
                .orderByDesc(SemanticMetricDO::getUpdatedAt);
        Page<SemanticMetricDO> result = mapper.selectPage(page, wrapper);
        List<SemanticMetric> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(SemanticInfraConvert.INSTANCE::toSemanticMetric)
                .toList();
        return new Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public SemanticMetric save(SemanticMetric semanticMetric) {
        long id = SnowflakeIdUtil.nextId();
        semanticMetric.setId(String.valueOf(id));
        SemanticMetricDO d = SemanticInfraConvert.INSTANCE.toSemanticMetricDO(semanticMetric);
        mapper.insert(d);
        return findById(semanticMetric.getId());
    }

    @Override
    public SemanticMetric update(SemanticMetric semanticMetric) {
        SemanticMetricDO d = SemanticInfraConvert.INSTANCE.toSemanticMetricDO(semanticMetric);
        mapper.updateById(d);
        return findById(semanticMetric.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }
}
