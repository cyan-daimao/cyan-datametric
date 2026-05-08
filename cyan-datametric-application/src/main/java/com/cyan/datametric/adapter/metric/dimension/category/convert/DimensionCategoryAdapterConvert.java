package com.cyan.datametric.adapter.metric.dimension.category.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.metric.dimension.category.dto.DimensionCategoryDTO;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import org.mapstruct.Mapper;

/**
 * 维度分类适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface DimensionCategoryAdapterConvert {

    DimensionCategoryDTO toDimensionCategoryDTO(DimensionCategoryBO bo);
}
