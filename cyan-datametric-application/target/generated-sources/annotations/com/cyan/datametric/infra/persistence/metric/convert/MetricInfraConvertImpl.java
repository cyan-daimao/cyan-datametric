package com.cyan.datametric.infra.persistence.metric.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionDO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class MetricInfraConvertImpl implements MetricInfraConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public Metric toMetric(MetricDefinitionDO def) {
        if ( def == null ) {
            return null;
        }

        Metric metric = new Metric();

        metric.setId( mapstructConvert.toString( def.getId() ) );
        metric.setMetricCode( mapstructConvert.toString( def.getMetricCode() ) );
        metric.setMetricName( mapstructConvert.toString( def.getMetricName() ) );
        metric.setMetricType( def.getMetricType() );
        metric.setSubjectCode( mapstructConvert.toString( def.getSubjectCode() ) );
        metric.setBizCaliber( mapstructConvert.toString( def.getBizCaliber() ) );
        metric.setTechCaliber( mapstructConvert.toString( def.getTechCaliber() ) );
        metric.setStatus( def.getStatus() );
        metric.setOwner( mapstructConvert.toString( def.getOwner() ) );
        metric.setVersion( mapstructConvert.toInteger( def.getVersion() ) );
        metric.setCreateBy( mapstructConvert.toString( def.getCreateBy() ) );
        metric.setUpdateBy( mapstructConvert.toString( def.getUpdateBy() ) );
        metric.setCreatedAt( def.getCreatedAt() );
        metric.setUpdatedAt( def.getUpdatedAt() );

        return metric;
    }

    @Override
    public MetricDefinitionDO toMetricDefinitionDO(Metric metric) {
        if ( metric == null ) {
            return null;
        }

        MetricDefinitionDO metricDefinitionDO = new MetricDefinitionDO();

        metricDefinitionDO.setId( mapstructConvert.toLong( metric.getId() ) );
        metricDefinitionDO.setMetricCode( mapstructConvert.toString( metric.getMetricCode() ) );
        metricDefinitionDO.setMetricName( mapstructConvert.toString( metric.getMetricName() ) );
        metricDefinitionDO.setMetricType( metric.getMetricType() );
        metricDefinitionDO.setSubjectCode( mapstructConvert.toString( metric.getSubjectCode() ) );
        metricDefinitionDO.setBizCaliber( mapstructConvert.toString( metric.getBizCaliber() ) );
        metricDefinitionDO.setTechCaliber( mapstructConvert.toString( metric.getTechCaliber() ) );
        metricDefinitionDO.setStatus( metric.getStatus() );
        metricDefinitionDO.setOwner( mapstructConvert.toString( metric.getOwner() ) );
        metricDefinitionDO.setVersion( mapstructConvert.toInteger( metric.getVersion() ) );
        metricDefinitionDO.setCreateBy( mapstructConvert.toString( metric.getCreateBy() ) );
        metricDefinitionDO.setUpdateBy( mapstructConvert.toString( metric.getUpdateBy() ) );
        metricDefinitionDO.setCreatedAt( metric.getCreatedAt() );
        metricDefinitionDO.setUpdatedAt( metric.getUpdatedAt() );

        return metricDefinitionDO;
    }
}
