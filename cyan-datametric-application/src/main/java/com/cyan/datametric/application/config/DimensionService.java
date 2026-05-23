package com.cyan.datametric.application.config;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.BuiltinTimeDimension;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
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
    private final DimensionCategoryRepository dimensionCategoryRepository;
    private final ConfigAppConvert configAppConvert;


    public DimensionBO create(DimensionCmd cmd) {
        if (cmd.getDimCode() == null || cmd.getDimCode().isBlank()) {
            cmd.setDimCode("DIM" + SnowflakeIdUtil.nextId());
        }
        Assert.isTrue(BuiltinTimeDimension.of(cmd.getDimCode()) == null,
                new BusinessException("维度编码 '" + cmd.getDimCode() + "' 为系统内置维度，不可创建"));
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
        Assert.isTrue(BuiltinTimeDimension.of(cmd.getDimCode()) == null,
                new BusinessException("维度编码 '" + cmd.getDimCode() + "' 为系统内置维度，不可创建"));
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
        BuiltinTimeDimension builtin = BuiltinTimeDimension.of(id);
        if (builtin != null) {
            throw new BusinessException("维度编码 '" + id + "' 为系统内置维度，不可修改");
        }
        Dimension existing = dimensionRepository.findById(id);
        Assert.isTrue(BuiltinTimeDimension.of(existing.getDimCode()) == null,
                new BusinessException("维度编码 '" + existing.getDimCode() + "' 为系统内置维度，不可修改"));
        Assert.isTrue(BuiltinTimeDimension.of(cmd.getDimCode()) == null,
                new BusinessException("维度编码 '" + cmd.getDimCode() + "' 为系统内置维度，不可修改"));
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension.setId(id);
        dimension.setDimCode(existing.getDimCode());
        dimension = dimension.update(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    public void delete(String id) {
        BuiltinTimeDimension builtin = BuiltinTimeDimension.of(id);
        if (builtin != null) {
            throw new BusinessException("维度编码 '" + id + "' 为系统内置维度，不可删除");
        }
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        Assert.isTrue(BuiltinTimeDimension.of(dimension.getDimCode()) == null,
                new BusinessException("维度编码 '" + dimension.getDimCode() + "' 为系统内置维度，不可删除"));
        dimension.delete(dimensionRepository);
    }

    public DimensionBO detail(String id) {
        // 先检查是否是内置维度编码
        BuiltinTimeDimension builtin = BuiltinTimeDimension.of(id);
        if (builtin != null) {
            return toBuiltinDimensionBO(builtin);
        }
        Dimension dimension = dimensionRepository.findById(id);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        assembleTableName(bo);
        return bo;
    }

    public Page<DimensionBO> page(DimensionPageQuery query) {
        Page<Dimension> page = dimensionRepository.page(query);
        List<DimensionBO> list = page.getData().stream()
                .map(d -> {
                    if (BuiltinTimeDimension.of(d.getDimCode()) != null) {
                        return toBuiltinDimensionBO(BuiltinTimeDimension.of(d.getDimCode()));
                    }
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
     * 将内置时间维度转换为业务对象
     *
     * @param builtin 内置时间维度枚举
     * @return 维度业务对象
     */
    private DimensionBO toBuiltinDimensionBO(BuiltinTimeDimension builtin) {
        DimensionBO bo = new DimensionBO();
        bo.setId(builtin.name());
        bo.setDimCode(builtin.name());
        bo.setDimName(builtin.getDimName());
        bo.setDimType("DATE");
        bo.setDataType("STRING");
        bo.setColumnName(builtin.buildExpr(null));
        bo.setDescription("系统内置时间维度，无需维表");
        return bo;
    }
}
