package com.cyan.datametric.adapter.config.http.convert;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.config.http.dto.DimensionDTO;
import com.cyan.datametric.adapter.config.http.dto.DimensionFieldBindingDTO;
import com.cyan.datametric.adapter.config.http.dto.ModifierDTO;
import com.cyan.datametric.adapter.config.http.dto.TimePeriodDTO;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.DimensionFieldBindingBO;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import org.mapstruct.Mapper;

/**
 * 配置适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface ConfigAdapterConvert {

    ModifierDTO toModifierDTO(ModifierBO bo);

    TimePeriodDTO toTimePeriodDTO(TimePeriodBO bo);

    DimensionDTO toDimensionDTO(DimensionBO bo);

    DimensionFieldBindingDTO toDimensionFieldBindingDTO(DimensionFieldBindingBO bo);
}
