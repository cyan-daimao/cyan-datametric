package com.cyan.datametric.adapter.metric.dimension.category.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.metric.dimension.category.dto.DimensionCategoryDTO;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 维度分类适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface DimensionCategoryAdapterConvert {
    DimensionCategoryAdapterConvert INSTANCE = Mappers.getMapper(DimensionCategoryAdapterConvert.class);

    DimensionCategoryDTO toDimensionCategoryDTO(DimensionCategoryBO bo);
}
