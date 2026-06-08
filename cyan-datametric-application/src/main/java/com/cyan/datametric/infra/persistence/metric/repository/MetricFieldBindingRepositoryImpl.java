package com.cyan.datametric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.arch.common.util.JSON;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricFieldBinding;
import com.cyan.datametric.domain.metric.repository.MetricFieldBindingRepository;
import com.cyan.datametric.infra.persistence.metric.dos.MetricFieldBindingDO;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricFieldBindingMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 指标字段绑定仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class MetricFieldBindingRepositoryImpl implements MetricFieldBindingRepository {

    private final MetricFieldBindingMapper mapper;

    @Override
    public MetricFieldBinding findById(String id) {
        MetricFieldBindingDO dataObject = mapper.selectById(Long.parseLong(id));
        return toDomain(dataObject);
    }

    @Override
    public List<MetricFieldBinding> findByMetricId(String metricId) {
        LambdaQueryWrapper<MetricFieldBindingDO> wrapper = new LambdaQueryWrapper<MetricFieldBindingDO>()
                .eq(MetricFieldBindingDO::getMetricId, Long.parseLong(metricId))
                .orderByDesc(MetricFieldBindingDO::getPrimaryBinding)
                .orderByAsc(MetricFieldBindingDO::getSortOrder)
                .orderByAsc(MetricFieldBindingDO::getId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public MetricFieldBinding save(MetricFieldBinding binding) {
        MetricFieldBindingDO dataObject = toDataObject(binding);
        dataObject.setId(SnowflakeIdUtil.nextId());
        mapper.insert(dataObject);
        if (Boolean.TRUE.equals(binding.getPrimaryBinding())) {
            setPrimary(binding.getMetricId(), String.valueOf(dataObject.getId()));
        }
        return findById(String.valueOf(dataObject.getId()));
    }

    @Override
    public MetricFieldBinding update(MetricFieldBinding binding) {
        MetricFieldBindingDO dataObject = toDataObject(binding);
        mapper.updateById(dataObject);
        if (Boolean.TRUE.equals(binding.getPrimaryBinding())) {
            setPrimary(binding.getMetricId(), binding.getId());
        }
        return findById(binding.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }

    @Override
    public void deleteByMetricId(String metricId) {
        mapper.delete(new LambdaQueryWrapper<MetricFieldBindingDO>()
                .eq(MetricFieldBindingDO::getMetricId, Long.parseLong(metricId)));
    }

    @Override
    public void setPrimary(String metricId, String bindingId) {
        List<MetricFieldBindingDO> bindings = mapper.selectList(new LambdaQueryWrapper<MetricFieldBindingDO>()
                .eq(MetricFieldBindingDO::getMetricId, Long.parseLong(metricId)));
        for (MetricFieldBindingDO binding : bindings) {
            binding.setPrimaryBinding(binding.getId().equals(Long.parseLong(bindingId)));
            mapper.updateById(binding);
        }
    }

    private MetricFieldBinding toDomain(MetricFieldBindingDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MetricFieldBinding binding = new MetricFieldBinding()
                .setId(dataObject.getId() == null ? null : String.valueOf(dataObject.getId()))
                .setMetricId(dataObject.getMetricId() == null ? null : String.valueOf(dataObject.getMetricId()))
                .setCatalogName(dataObject.getCatalogName())
                .setSchemaName(dataObject.getSchemaName())
                .setTableName(dataObject.getTableName())
                .setColumnName(dataObject.getColumnName())
                .setSourceExpr(dataObject.getSourceExpr())
                .setPrimaryBinding(dataObject.getPrimaryBinding())
                .setSortOrder(dataObject.getSortOrder())
                .setCreateBy(dataObject.getCreateBy())
                .setUpdateBy(dataObject.getUpdateBy())
                .setCreatedAt(dataObject.getCreatedAt())
                .setUpdatedAt(dataObject.getUpdatedAt());
        if (StringUtils.hasText(dataObject.getFilterCondition())) {
            binding.setFilterCondition(parseFilterCondition(dataObject.getFilterCondition()));
        }
        return binding;
    }

    private MetricFieldBindingDO toDataObject(MetricFieldBinding binding) {
        MetricFieldBindingDO dataObject = new MetricFieldBindingDO()
                .setMetricId(binding.getMetricId() == null ? null : Long.parseLong(binding.getMetricId()))
                .setCatalogName(binding.getCatalogName())
                .setSchemaName(binding.getSchemaName())
                .setTableName(binding.getTableName())
                .setColumnName(binding.getColumnName())
                .setSourceExpr(binding.getSourceExpr())
                .setPrimaryBinding(Boolean.TRUE.equals(binding.getPrimaryBinding()))
                .setSortOrder(binding.getSortOrder() == null ? 0 : binding.getSortOrder())
                .setCreateBy(binding.getCreateBy())
                .setUpdateBy(binding.getUpdateBy())
                .setCreatedAt(binding.getCreatedAt())
                .setUpdatedAt(binding.getUpdatedAt());
        if (binding.getId() != null) {
            dataObject.setId(Long.parseLong(binding.getId()));
        }
        if (binding.getFilterCondition() != null) {
            dataObject.setFilterCondition(JSON.toJSONString(binding.getFilterCondition()));
        }
        return dataObject;
    }

    private List<MetricAtomicExt.FilterCondition> parseFilterCondition(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<MetricAtomicExt.FilterCondition>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
