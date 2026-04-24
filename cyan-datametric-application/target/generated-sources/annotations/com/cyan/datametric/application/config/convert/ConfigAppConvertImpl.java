package com.cyan.datametric.application.config.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class ConfigAppConvertImpl implements ConfigAppConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public ModifierBO toModifierBO(Modifier modifier) {
        if ( modifier == null ) {
            return null;
        }

        ModifierBO modifierBO = new ModifierBO();

        modifierBO.setId( mapstructConvert.toString( modifier.getId() ) );
        modifierBO.setModifierCode( mapstructConvert.toString( modifier.getModifierCode() ) );
        modifierBO.setModifierName( mapstructConvert.toString( modifier.getModifierName() ) );
        modifierBO.setFieldName( mapstructConvert.toString( modifier.getFieldName() ) );
        modifierBO.setOperator( mapstructConvert.toString( modifier.getOperator() ) );
        List<String> list = modifier.getFieldValues();
        if ( list != null ) {
            modifierBO.setFieldValues( new ArrayList<String>( list ) );
        }
        modifierBO.setDescription( mapstructConvert.toString( modifier.getDescription() ) );
        modifierBO.setUpdatedAt( modifier.getUpdatedAt() );

        return modifierBO;
    }

    @Override
    public TimePeriodBO toTimePeriodBO(TimePeriod timePeriod) {
        if ( timePeriod == null ) {
            return null;
        }

        TimePeriodBO timePeriodBO = new TimePeriodBO();

        timePeriodBO.setId( mapstructConvert.toString( timePeriod.getId() ) );
        timePeriodBO.setPeriodCode( mapstructConvert.toString( timePeriod.getPeriodCode() ) );
        timePeriodBO.setPeriodName( mapstructConvert.toString( timePeriod.getPeriodName() ) );
        timePeriodBO.setPeriodType( mapstructConvert.toString( timePeriod.getPeriodType() ) );
        timePeriodBO.setRelativeValue( mapstructConvert.toInteger( timePeriod.getRelativeValue() ) );
        timePeriodBO.setRelativeUnit( mapstructConvert.toString( timePeriod.getRelativeUnit() ) );
        timePeriodBO.setStartDate( timePeriod.getStartDate() );
        timePeriodBO.setEndDate( timePeriod.getEndDate() );
        timePeriodBO.setUpdatedAt( timePeriod.getUpdatedAt() );

        return timePeriodBO;
    }

    @Override
    public DimensionBO toDimensionBO(Dimension dimension) {
        if ( dimension == null ) {
            return null;
        }

        DimensionBO dimensionBO = new DimensionBO();

        dimensionBO.setId( mapstructConvert.toString( dimension.getId() ) );
        dimensionBO.setDimCode( mapstructConvert.toString( dimension.getDimCode() ) );
        dimensionBO.setDimName( mapstructConvert.toString( dimension.getDimName() ) );
        dimensionBO.setDsName( mapstructConvert.toString( dimension.getDsName() ) );
        dimensionBO.setDbName( mapstructConvert.toString( dimension.getDbName() ) );
        dimensionBO.setTblName( mapstructConvert.toString( dimension.getTblName() ) );
        dimensionBO.setColName( mapstructConvert.toString( dimension.getColName() ) );
        dimensionBO.setDescription( mapstructConvert.toString( dimension.getDescription() ) );
        dimensionBO.setUpdatedAt( dimension.getUpdatedAt() );

        return dimensionBO;
    }
}
