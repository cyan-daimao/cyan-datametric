package com.cyan.datametric.application.config;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
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
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension = dimension.save(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    public DimensionBO update(String id, DimensionCmd cmd) {
        Dimension existing = dimensionRepository.findById(id);
        Dimension dimension = configAppConvert.toDimension(cmd);
        dimension.setId(id);
        dimension.setDimCode(existing.getDimCode());
        dimension = dimension.update(dimensionRepository);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        return bo;
    }

    public void delete(String id) {
        Dimension dimension = dimensionRepository.findById(id);
        Assert.notNull(dimension, new BusinessException("维度不存在"));
        dimension.delete(dimensionRepository);
    }

    public DimensionBO detail(String id) {
        Dimension dimension = dimensionRepository.findById(id);
        DimensionBO bo = configAppConvert.toDimensionBO(dimension);
        assembleCategoryName(bo);
        assembleTableName(bo);
        return bo;
    }

    public Page<DimensionBO> page(DimensionPageQuery query) {
        Page<Dimension> page = dimensionRepository.page(query);
        List<DimensionBO> list = page.getData().stream()
                .map(configAppConvert::toDimensionBO)
                .peek(this::assembleCategoryName)
                .peek(this::assembleTableName)
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
}
