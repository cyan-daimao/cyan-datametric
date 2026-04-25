package com.cyan.datametric.infra.persistence.metric.dimension.category.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.infra.persistence.metric.dimension.category.DimensionCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 维度分类基础设施层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface DimensionCategoryInfraConvert {
    DimensionCategoryInfraConvert INSTANCE = Mappers.getMapper(DimensionCategoryInfraConvert.class);

    default DimensionCategory toDimensionCategory(DimensionCategoryDO categoryDO) {
        if (categoryDO == null) return null;
        DimensionCategory c = new DimensionCategory();
        c.setId(categoryDO.getId() == null ? null : String.valueOf(categoryDO.getId()));
        c.setName(categoryDO.getName());
        c.setParentId(categoryDO.getParentId() == null ? null : String.valueOf(categoryDO.getParentId()));
        c.setLevel(categoryDO.getLevel());
        c.setSortOrder(categoryDO.getSortOrder());
        c.setCreateBy(categoryDO.getCreateBy());
        c.setUpdateBy(categoryDO.getUpdateBy());
        c.setCreatedAt(categoryDO.getCreatedAt());
        c.setUpdatedAt(categoryDO.getUpdatedAt());
        return c;
    }

    default DimensionCategoryDO toDimensionCategoryDO(DimensionCategory category) {
        if (category == null) return null;
        DimensionCategoryDO d = new DimensionCategoryDO();
        d.setId(category.getId() == null ? null : Long.parseLong(category.getId()));
        d.setName(category.getName());
        d.setParentId(category.getParentId() == null ? null : Long.parseLong(category.getParentId()));
        d.setLevel(category.getLevel());
        d.setSortOrder(category.getSortOrder());
        d.setCreateBy(category.getCreateBy());
        d.setUpdateBy(category.getUpdateBy());
        d.setCreatedAt(category.getCreatedAt());
        d.setUpdatedAt(category.getUpdatedAt());
        return d;
    }
}
