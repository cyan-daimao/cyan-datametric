package com.cyan.datametric.infra.persistence.config.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.infra.persistence.config.dos.MetricTimePeriodDO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class ConfigInfraConvertImpl implements ConfigInfraConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public TimePeriod toTimePeriod(MetricTimePeriodDO periodDO) {
        if ( periodDO == null ) {
            return null;
        }

        TimePeriod timePeriod = new TimePeriod();

        timePeriod.setId( mapstructConvert.toString( periodDO.getId() ) );
        timePeriod.setPeriodCode( mapstructConvert.toString( periodDO.getPeriodCode() ) );
        timePeriod.setPeriodName( mapstructConvert.toString( periodDO.getPeriodName() ) );
        timePeriod.setPeriodType( periodDO.getPeriodType() );
        timePeriod.setRelativeValue( mapstructConvert.toInteger( periodDO.getRelativeValue() ) );
        timePeriod.setRelativeUnit( periodDO.getRelativeUnit() );
        timePeriod.setStartDate( periodDO.getStartDate() );
        timePeriod.setEndDate( periodDO.getEndDate() );
        timePeriod.setCreateBy( mapstructConvert.toString( periodDO.getCreateBy() ) );
        timePeriod.setUpdateBy( mapstructConvert.toString( periodDO.getUpdateBy() ) );
        timePeriod.setCreatedAt( periodDO.getCreatedAt() );
        timePeriod.setUpdatedAt( periodDO.getUpdatedAt() );

        return timePeriod;
    }

    @Override
    public MetricTimePeriodDO toTimePeriodDO(TimePeriod timePeriod) {
        if ( timePeriod == null ) {
            return null;
        }

        MetricTimePeriodDO metricTimePeriodDO = new MetricTimePeriodDO();

        metricTimePeriodDO.setId( mapstructConvert.toLong( timePeriod.getId() ) );
        metricTimePeriodDO.setPeriodCode( mapstructConvert.toString( timePeriod.getPeriodCode() ) );
        metricTimePeriodDO.setPeriodName( mapstructConvert.toString( timePeriod.getPeriodName() ) );
        metricTimePeriodDO.setPeriodType( timePeriod.getPeriodType() );
        metricTimePeriodDO.setRelativeValue( mapstructConvert.toInteger( timePeriod.getRelativeValue() ) );
        metricTimePeriodDO.setRelativeUnit( timePeriod.getRelativeUnit() );
        metricTimePeriodDO.setStartDate( timePeriod.getStartDate() );
        metricTimePeriodDO.setEndDate( timePeriod.getEndDate() );
        metricTimePeriodDO.setCreateBy( mapstructConvert.toString( timePeriod.getCreateBy() ) );
        metricTimePeriodDO.setUpdateBy( mapstructConvert.toString( timePeriod.getUpdateBy() ) );
        metricTimePeriodDO.setCreatedAt( timePeriod.getCreatedAt() );
        metricTimePeriodDO.setUpdatedAt( timePeriod.getUpdatedAt() );

        return metricTimePeriodDO;
    }
}
