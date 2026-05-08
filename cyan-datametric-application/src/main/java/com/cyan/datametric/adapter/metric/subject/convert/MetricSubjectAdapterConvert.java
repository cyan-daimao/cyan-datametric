package com.cyan.datametric.adapter.metric.subject.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.metric.subject.dto.MetricSubjectDTO;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import org.mapstruct.Mapper;

/**
 * 指标主题域适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricSubjectAdapterConvert {

    MetricSubjectDTO toMetricSubjectDTO(MetricSubjectBO bo);
}
