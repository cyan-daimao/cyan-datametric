package com.cyan.datametric.adapter.config.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.config.http.dto.DimensionDTO;
import com.cyan.datametric.adapter.config.http.dto.ModifierDTO;
import com.cyan.datametric.adapter.config.http.dto.TimePeriodDTO;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class ConfigAdapterConvertImpl implements ConfigAdapterConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public ModifierDTO toModifierDTO(ModifierBO bo) {
        if ( bo == null ) {
            return null;
        }

        ModifierDTO modifierDTO = new ModifierDTO();

        modifierDTO.setId( mapstructConvert.toString( bo.getId() ) );
        modifierDTO.setModifierCode( mapstructConvert.toString( bo.getModifierCode() ) );
        modifierDTO.setModifierName( mapstructConvert.toString( bo.getModifierName() ) );
        modifierDTO.setFieldName( mapstructConvert.toString( bo.getFieldName() ) );
        modifierDTO.setOperator( mapstructConvert.toString( bo.getOperator() ) );
        List<String> list = bo.getFieldValues();
        if ( list != null ) {
            modifierDTO.setFieldValues( new ArrayList<String>( list ) );
        }
        modifierDTO.setDescription( mapstructConvert.toString( bo.getDescription() ) );
        modifierDTO.setUpdatedAt( bo.getUpdatedAt() );

        return modifierDTO;
    }

    @Override
    public TimePeriodDTO toTimePeriodDTO(TimePeriodBO bo) {
        if ( bo == null ) {
            return null;
        }

        TimePeriodDTO timePeriodDTO = new TimePeriodDTO();

        timePeriodDTO.setId( mapstructConvert.toString( bo.getId() ) );
        timePeriodDTO.setPeriodCode( mapstructConvert.toString( bo.getPeriodCode() ) );
        timePeriodDTO.setPeriodName( mapstructConvert.toString( bo.getPeriodName() ) );
        timePeriodDTO.setPeriodType( mapstructConvert.toString( bo.getPeriodType() ) );
        timePeriodDTO.setRelativeValue( mapstructConvert.toInteger( bo.getRelativeValue() ) );
        timePeriodDTO.setRelativeUnit( mapstructConvert.toString( bo.getRelativeUnit() ) );
        timePeriodDTO.setStartDate( bo.getStartDate() );
        timePeriodDTO.setEndDate( bo.getEndDate() );
        timePeriodDTO.setUpdatedAt( bo.getUpdatedAt() );

        return timePeriodDTO;
    }

    @Override
    public DimensionDTO toDimensionDTO(DimensionBO bo) {
        if ( bo == null ) {
            return null;
        }

        DimensionDTO dimensionDTO = new DimensionDTO();

        dimensionDTO.setId( mapstructConvert.toString( bo.getId() ) );
        dimensionDTO.setDimCode( mapstructConvert.toString( bo.getDimCode() ) );
        dimensionDTO.setDimName( mapstructConvert.toString( bo.getDimName() ) );
        dimensionDTO.setDsName( mapstructConvert.toString( bo.getDsName() ) );
        dimensionDTO.setDbName( mapstructConvert.toString( bo.getDbName() ) );
        dimensionDTO.setTblName( mapstructConvert.toString( bo.getTblName() ) );
        dimensionDTO.setColName( mapstructConvert.toString( bo.getColName() ) );
        dimensionDTO.setDescription( mapstructConvert.toString( bo.getDescription() ) );
        dimensionDTO.setUpdatedAt( bo.getUpdatedAt() );

        return dimensionDTO;
    }
}
