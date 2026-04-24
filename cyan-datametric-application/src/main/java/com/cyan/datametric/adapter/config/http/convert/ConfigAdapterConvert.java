package com.cyan.datametric.adapter.config.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.config.http.dto.DimensionDTO;
import com.cyan.datametric.adapter.config.http.dto.ModifierDTO;
import com.cyan.datametric.adapter.config.http.dto.TimePeriodDTO;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 配置适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface ConfigAdapterConvert {
    ConfigAdapterConvert INSTANCE = Mappers.getMapper(ConfigAdapterConvert.class);

    ModifierDTO toModifierDTO(ModifierBO bo);

    TimePeriodDTO toTimePeriodDTO(TimePeriodBO bo);

    DimensionDTO toDimensionDTO(DimensionBO bo);
}
