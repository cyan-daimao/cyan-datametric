package com.cyan.datametric.application.config;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.DimensionFieldBindingBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.cmd.DimensionFieldBindingCmd;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.DimensionFieldBinding;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.config.repository.DimensionFieldBindingRepository;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;

/**
 * 公共维度服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DimensionService {

    private final DimensionRepository dimensionRepository;
    private final DimensionFieldBindingRepository dimensionFieldBindingRepository;
    private final DimensionCategoryRepository dimensionCategoryRepository;
    private final ConfigAppConvert configAppConvert;


    public DimensionBO create(DimensionCmd cmd) {
        if (cmd.getDimCode() == null || cmd.getDimCode().isBlank()) {
            cmd.setDimCode("DIM" + SnowflakeIdUtil.nextId());
        }
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension = dimension.save(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    /**
     * 按维度编码幂等创建或更新维度
     *
     * @param cmd 维度命令
     * @return 维度业务对象
     */
    public DimensionBO upsertByDimCode(DimensionCmd cmd) {
        if (cmd.getDimCode() == null || cmd.getDimCode().isBlank()) {
            cmd.setDimCode("DIM" + SnowflakeIdUtil.nextId());
        }
        Dimension existing = dimensionRepository.findByDimCode(cmd.getDimCode());
        if (existing == null) {
            return create(cmd);
        }
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension.setId(existing.getId());
        dimension.setDimCode(existing.getDimCode());
        dimension.setCreateBy(existing.getCreateBy());
        dimension.setCreatedAt(existing.getCreatedAt());
        dimension = dimension.update(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    public DimensionBO update(String id, DimensionCmd cmd) {
        assertNumericId(id);
        Dimension existing = dimensionRepository.findById(id);
        Assert.notNull(existing, new BusinessException("维度不存在"));
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension.setId(id);
        dimension.setDimCode(existing.getDimCode());
        dimension = dimension.update(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    public void delete(String id) {
        assertNumericId(id);
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        dimension.delete(dimensionRepository);
    }

    public DimensionBO detail(String id) {
        assertNumericId(id);
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        assembleTableName(bo);
        return bo;
    }

    public Page<DimensionBO> page(DimensionPageQuery query) {
        Page<Dimension> page = dimensionRepository.page(query);
        List<DimensionBO> list = page.getData().stream()
                .map(d -> {
                    DimensionBO bo = configAppConvert.toDimensionBO(d);
                    assembleCategoryName(bo);
                    assembleTableName(bo);
                    return bo;
                })
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    private void assembleCategoryName(DimensionBO bo) {
        if (bo.getCategoryId() != null && !bo.getCategoryId().isBlank()) {
            DimensionCategory category = dimensionCategoryRepository.findById(bo.getCategoryId());
            if (category != null) {
                bo.setCategoryName(category.getName());
            }
        }
    }

    private void assembleTableName(DimensionBO bo) {
        if (bo.getTableName() != null && !bo.getTableName().isBlank()) {
            // TODO: 待 dataman-client 升级后启用 Feign 调用
            // try {
            //     Response<MetadataTableDTO> response = datamanTableClient.getMetadataTableByName(bo.getTableName());
            //     if (response != null && response.getCode() == 200 && response.getData() != null) {
            //         bo.setTableName(response.getData().getName());
            //     }
            // } catch (Exception e) {
            //     // Feign 调用失败不抛异常，tableName 保持原值
            // }
        }
    }

    /**
     * 查询维度字段绑定
     */
    public List<DimensionFieldBindingBO> listFieldBindings(String dimId) {
        assertNumericId(dimId);
        Dimension dimension = dimensionRepository.findById(dimId);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        return toDimensionFieldBindingBOs(dimensionFieldBindingRepository.findByDimId(dimId));
    }

    /**
     * 保存维度字段绑定
     */
    public DimensionFieldBindingBO saveFieldBinding(String dimId, DimensionFieldBindingCmd cmd, String operator) {
        assertNumericId(dimId);
        Dimension dimension = dimensionRepository.findById(dimId);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        DimensionFieldBinding binding = configAppConvert.toDimensionFieldBinding(cmd);
        binding.setDimId(dimId);
        binding.setUpdateBy(operator);
        DimensionFieldBinding saved;
        if (binding.getId() != null && !binding.getId().isBlank()) {
            saved = binding.update(dimensionFieldBindingRepository);
        } else {
            binding.setCreateBy(operator);
            saved = binding.save(dimensionFieldBindingRepository);
        }
        return toDimensionFieldBindingBO(saved);
    }

    /**
     * 删除维度字段绑定
     */
    public void deleteFieldBinding(String dimId, String bindingId) {
        assertNumericId(dimId);
        DimensionFieldBinding binding = dimensionFieldBindingRepository.findById(bindingId);
        Assert.notNull(binding, new BusinessException("维度字段绑定不存在"));
        Assert.isTrue(dimId.equals(binding.getDimId()), new BusinessException("维度字段绑定不属于当前维度"));
        binding.delete(dimensionFieldBindingRepository);
    }

    /**
     * 设置主维度字段绑定
     */
    public void setPrimaryFieldBinding(String dimId, String bindingId) {
        assertNumericId(dimId);
        DimensionFieldBinding binding = dimensionFieldBindingRepository.findById(bindingId);
        Assert.notNull(binding, new BusinessException("维度字段绑定不存在"));
        Assert.isTrue(dimId.equals(binding.getDimId()), new BusinessException("维度字段绑定不属于当前维度"));
        dimensionFieldBindingRepository.setPrimary(dimId, bindingId);
    }

    private List<DimensionFieldBindingBO> toDimensionFieldBindingBOs(List<DimensionFieldBinding> bindings) {
        if (bindings == null) {
            return List.of();
        }
        return bindings.stream().map(this::toDimensionFieldBindingBO).toList();
    }

    private DimensionFieldBindingBO toDimensionFieldBindingBO(DimensionFieldBinding binding) {
        return new DimensionFieldBindingBO()
                .setId(binding.getId())
                .setDimId(binding.getDimId())
                .setTableRole(binding.getTableRole())
                .setCatalogName(binding.getCatalogName())
                .setSchemaName(binding.getSchemaName())
                .setTableName(binding.getTableName())
                .setColumnName(binding.getColumnName())
                .setDisplayColumn(binding.getDisplayColumn())
                .setSourceType(binding.getSourceType())
                .setSourceExpr(binding.getSourceExpr())
                .setPrimaryBinding(binding.getPrimaryBinding())
                .setSortOrder(binding.getSortOrder())
                .setUpdatedAt(binding.getUpdatedAt());
    }

    /**
     * 校验维度ID格式
     */
    private void assertNumericId(String id) {
        Assert.isTrue(id != null && !id.isBlank() && id.chars().allMatch(Character::isDigit),
                new BusinessException("维度不存在"));
    }
}
