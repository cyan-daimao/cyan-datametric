package com.cyan.datametric.infra.persistence.config.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.config.DimensionFieldBinding;
import com.cyan.datametric.domain.config.repository.DimensionFieldBindingRepository;
import com.cyan.datametric.infra.persistence.config.dos.DimensionFieldBindingDO;
import com.cyan.datametric.infra.persistence.config.mappers.DimensionFieldBindingMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 维度字段绑定仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class DimensionFieldBindingRepositoryImpl implements DimensionFieldBindingRepository {

    private final DimensionFieldBindingMapper mapper;

    @Override
    public DimensionFieldBinding findById(String id) {
        return toDomain(mapper.selectById(Long.parseLong(id)));
    }

    @Override
    public List<DimensionFieldBinding> findByDimId(String dimId) {
        LambdaQueryWrapper<DimensionFieldBindingDO> wrapper = new LambdaQueryWrapper<DimensionFieldBindingDO>()
                .eq(DimensionFieldBindingDO::getDimId, Long.parseLong(dimId))
                .orderByDesc(DimensionFieldBindingDO::getPrimaryBinding)
                .orderByAsc(DimensionFieldBindingDO::getSortOrder)
                .orderByAsc(DimensionFieldBindingDO::getId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public DimensionFieldBinding save(DimensionFieldBinding binding) {
        DimensionFieldBindingDO dataObject = toDataObject(binding);
        dataObject.setId(SnowflakeIdUtil.nextId());
        mapper.insert(dataObject);
        if (Boolean.TRUE.equals(binding.getPrimaryBinding())) {
            setPrimary(binding.getDimId(), String.valueOf(dataObject.getId()));
        }
        return findById(String.valueOf(dataObject.getId()));
    }

    @Override
    public DimensionFieldBinding update(DimensionFieldBinding binding) {
        DimensionFieldBindingDO dataObject = toDataObject(binding);
        mapper.updateById(dataObject);
        if (Boolean.TRUE.equals(binding.getPrimaryBinding())) {
            setPrimary(binding.getDimId(), binding.getId());
        }
        return findById(binding.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }

    @Override
    public void deleteByDimId(String dimId) {
        mapper.delete(new LambdaQueryWrapper<DimensionFieldBindingDO>()
                .eq(DimensionFieldBindingDO::getDimId, Long.parseLong(dimId)));
    }

    @Override
    public void setPrimary(String dimId, String bindingId) {
        List<DimensionFieldBindingDO> bindings = mapper.selectList(new LambdaQueryWrapper<DimensionFieldBindingDO>()
                .eq(DimensionFieldBindingDO::getDimId, Long.parseLong(dimId)));
        for (DimensionFieldBindingDO binding : bindings) {
            binding.setPrimaryBinding(binding.getId().equals(Long.parseLong(bindingId)));
            mapper.updateById(binding);
        }
    }

    private DimensionFieldBinding toDomain(DimensionFieldBindingDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new DimensionFieldBinding()
                .setId(dataObject.getId() == null ? null : String.valueOf(dataObject.getId()))
                .setDimId(dataObject.getDimId() == null ? null : String.valueOf(dataObject.getDimId()))
                .setTableRole(dataObject.getTableRole())
                .setCatalogName(dataObject.getCatalogName())
                .setSchemaName(dataObject.getSchemaName())
                .setTableName(dataObject.getTableName())
                .setColumnName(dataObject.getColumnName())
                .setDisplayColumn(dataObject.getDisplayColumn())
                .setSourceType(dataObject.getSourceType())
                .setSourceExpr(dataObject.getSourceExpr())
                .setPrimaryBinding(dataObject.getPrimaryBinding())
                .setSortOrder(dataObject.getSortOrder())
                .setCreateBy(dataObject.getCreateBy())
                .setUpdateBy(dataObject.getUpdateBy())
                .setCreatedAt(dataObject.getCreatedAt())
                .setUpdatedAt(dataObject.getUpdatedAt());
    }

    private DimensionFieldBindingDO toDataObject(DimensionFieldBinding binding) {
        DimensionFieldBindingDO dataObject = new DimensionFieldBindingDO()
                .setDimId(binding.getDimId() == null ? null : Long.parseLong(binding.getDimId()))
                .setTableRole(binding.getTableRole())
                .setCatalogName(binding.getCatalogName())
                .setSchemaName(binding.getSchemaName())
                .setTableName(binding.getTableName())
                .setColumnName(binding.getColumnName())
                .setDisplayColumn(binding.getDisplayColumn())
                .setSourceType(binding.getSourceType())
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
        return dataObject;
    }
}
