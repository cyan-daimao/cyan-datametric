package com.cyan.datametric.application.metric.dimension.category.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import com.cyan.datametric.application.metric.dimension.category.cmd.DimensionCategoryCmd;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 维度分类应用层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface DimensionCategoryAppConvert {
    DimensionCategoryAppConvert INSTANCE = Mappers.getMapper(DimensionCategoryAppConvert.class);

    DimensionCategoryBO toDimensionCategoryBO(DimensionCategory category);

    default DimensionCategory toDimensionCategory(DimensionCategoryCmd cmd) {
        if (cmd == null) return null;
        DimensionCategory c = new DimensionCategory();
        c.setName(cmd.getName());
        c.setParentId(cmd.getParentId());
        c.setSortOrder(cmd.getSortOrder());
        c.setCreateBy(cmd.getCreateBy());
        c.setUpdateBy(cmd.getUpdateBy());
        return c;
    }
}
