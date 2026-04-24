package com.cyan.datametric.application.metric.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.metric.bo.MetricBO;
import com.cyan.datametric.domain.metric.Metric;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-24T02:23:14+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Arch Linux)"
)
public class MetricAppConvertImpl implements MetricAppConvert {

    private final MapstructConvert mapstructConvert = new MapstructConvert();

    @Override
    public MetricBO toMetricBO(Metric metric) {
        if ( metric == null ) {
            return null;
        }

        MetricBO metricBO = new MetricBO();

        metricBO.setId( mapstructConvert.toString( metric.getId() ) );
        metricBO.setMetricCode( mapstructConvert.toString( metric.getMetricCode() ) );
        metricBO.setMetricName( mapstructConvert.toString( metric.getMetricName() ) );
        metricBO.setMetricType( metric.getMetricType() );
        metricBO.setSubjectCode( mapstructConvert.toString( metric.getSubjectCode() ) );
        metricBO.setBizCaliber( mapstructConvert.toString( metric.getBizCaliber() ) );
        metricBO.setTechCaliber( mapstructConvert.toString( metric.getTechCaliber() ) );
        metricBO.setStatus( metric.getStatus() );
        metricBO.setOwner( mapstructConvert.toString( metric.getOwner() ) );
        metricBO.setVersion( mapstructConvert.toInteger( metric.getVersion() ) );
        metricBO.setCreatedAt( metric.getCreatedAt() );
        metricBO.setUpdatedAt( metric.getUpdatedAt() );

        return metricBO;
    }
}
