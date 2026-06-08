package com.cyan.datametric.infra.persistence.config.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.datametric.domain.config.BuiltinTimeDimension;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.DimensionFieldBinding;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionFieldBindingRepository;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.infra.persistence.config.convert.ConfigInfraConvert;
import com.cyan.datametric.infra.persistence.config.dos.MetricDimensionDO;
import com.cyan.datametric.infra.persistence.config.mappers.MetricDimensionMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * 公共维度仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class DimensionRepositoryImpl implements DimensionRepository {

    private final MetricDimensionMapper dimensionMapper;
    private final ConfigInfraConvert configInfraConvert;
    private final DimensionFieldBindingRepository dimensionFieldBindingRepository;


    @Override
    public Dimension findById(String id) {
        MetricDimensionDO dimensionDO = dimensionMapper.selectById(Long.parseLong(id));
        return loadBindings(configInfraConvert.toDimension(dimensionDO));
    }

    @Override
    public Dimension findByDimCode(String dimCode) {
        LambdaQueryWrapper<MetricDimensionDO> wrapper = new LambdaQueryWrapper<MetricDimensionDO>()
                .eq(MetricDimensionDO::getDimCode, dimCode);
        MetricDimensionDO dimensionDO = dimensionMapper.selectOne(wrapper);
        Dimension dimension = loadBindings(configInfraConvert.toDimension(dimensionDO));
        if (dimension == null) {
            BuiltinTimeDimension builtin = BuiltinTimeDimension.of(dimCode);
            if (builtin != null) {
                dimension = toBuiltinDimension(builtin);
            }
        }
        return dimension;
    }

    @Override
    public com.cyan.arch.common.api.Page<Dimension> page(DimensionPageQuery query) {
        Page<MetricDimensionDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<MetricDimensionDO> wrapper = new LambdaQueryWrapper<MetricDimensionDO>()
                .like(StringUtils.isNotBlank(query.getDimName()), MetricDimensionDO::getDimName, query.getDimName())
                .isNull(MetricDimensionDO::getDeletedAt)
                .orderByDesc(MetricDimensionDO::getUpdatedAt);
        if (StringUtils.isNotBlank(query.getCategoryId())) {
            wrapper.eq(MetricDimensionDO::getCategoryId, Long.parseLong(query.getCategoryId()));
        }
        Page<MetricDimensionDO> result = dimensionMapper.selectPage(page, wrapper);
        List<Dimension> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(configInfraConvert::toDimension)
                .map(this::loadBindings)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        // 第一页头部插入内置时间维度（支持 dimName 过滤）
        int builtinCount = 0;
        if (query.current() <= 1) {
            for (BuiltinTimeDimension builtin : BuiltinTimeDimension.listAll()) {
                if (StringUtils.isNotBlank(query.getDimName())) {
                    if (builtin.getDimName() == null || !builtin.getDimName().contains(query.getDimName())) {
                        continue;
                    }
                }
                list.add(0, toBuiltinDimension(builtin));
                builtinCount++;
            }
        }

        long total = result.getTotal() + builtinCount;
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), total);
    }

    @Override
    public Dimension save(Dimension dimension) {
        long id = SnowflakeIdUtil.nextId();
        dimension.setId(String.valueOf(id));
        MetricDimensionDO dimensionDO = configInfraConvert.toDimensionDO(dimension);
        dimensionMapper.insert(dimensionDO);
        saveBindings(dimension, id);
        return findById(dimension.getId());
    }

    @Override
    public Dimension update(Dimension dimension) {
        MetricDimensionDO dimensionDO = configInfraConvert.toDimensionDO(dimension);
        dimensionMapper.updateById(dimensionDO);
        saveBindings(dimension, Long.parseLong(dimension.getId()));
        return findById(dimension.getId());
    }

    @Override
    public void deleteById(String id) {
        dimensionMapper.deleteById(Long.parseLong(id));
        dimensionFieldBindingRepository.deleteByDimId(id);
    }

    /**
     * 加载维度字段绑定
     */
    private Dimension loadBindings(Dimension dimension) {
        if (dimension == null || dimension.getId() == null || BuiltinTimeDimension.of(dimension.getDimCode()) != null) {
            return dimension;
        }
        List<DimensionFieldBinding> bindings = dimensionFieldBindingRepository.findByDimId(dimension.getId());
        dimension.setFieldBindings(bindings);
        applyPrimaryBindingCompat(dimension, bindings);
        return dimension;
    }

    /**
     * 保存维度字段绑定
     */
    private void saveBindings(Dimension dimension, Long dimId) {
        dimensionFieldBindingRepository.deleteByDimId(String.valueOf(dimId));
        if (dimension.getFieldBindings() == null) {
            return;
        }
        boolean hasPrimary = dimension.getFieldBindings().stream()
                .anyMatch(binding -> Boolean.TRUE.equals(binding.getPrimaryBinding()));
        int index = 0;
        for (DimensionFieldBinding binding : dimension.getFieldBindings()) {
            binding.setId(null);
            binding.setDimId(String.valueOf(dimId));
            binding.setPrimaryBinding(hasPrimary ? Boolean.TRUE.equals(binding.getPrimaryBinding()) : index == 0);
            binding.setSortOrder(binding.getSortOrder() == null ? index : binding.getSortOrder());
            binding.save(dimensionFieldBindingRepository);
            index++;
        }
    }

    /**
     * 用主绑定填充旧展示字段
     */
    private void applyPrimaryBindingCompat(Dimension dimension, List<DimensionFieldBinding> bindings) {
        if (dimension == null || bindings == null || bindings.isEmpty()) {
            return;
        }
        DimensionFieldBinding primary = bindings.stream()
                .filter(binding -> Boolean.TRUE.equals(binding.getPrimaryBinding()))
                .findFirst()
                .orElse(bindings.getFirst());
        dimension.setSchemaName(primary.getSchemaName());
        dimension.setTableName(primary.getTableName());
        dimension.setColumnName(primary.getColumnName());
        dimension.setDisplayColumn(primary.getDisplayColumn());
        dimension.setSourceType(primary.getSourceType());
        dimension.setSourceExpr(primary.getSourceExpr());
        if (primary.factBinding()) {
            dimension.setSourceTable(primary.tableRef("iceberg"));
        }
    }

    /**
     * 将内置时间维度转换为领域对象
     *
     * @param builtin 内置时间维度枚举
     * @return 维度领域对象
     */
    private Dimension toBuiltinDimension(BuiltinTimeDimension builtin) {
        Dimension d = new Dimension();
        d.setId(builtin.name());
        d.setDimCode(builtin.name());
        d.setDimName(builtin.getDimName());
        d.setDimType("DATE");
        d.setDimensionKind("DERIVED");
        d.setDataType("STRING");
        d.setColumnName(builtin.buildExpr(null));
        d.setDescription("系统内置时间维度，无需维表");
        return d;
    }
}
