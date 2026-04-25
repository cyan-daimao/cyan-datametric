package com.cyan.datametric.application.metric.subject.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import com.cyan.datametric.application.metric.subject.cmd.MetricSubjectCmd;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 指标主题域应用层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface MetricSubjectAppConvert {
    MetricSubjectAppConvert INSTANCE = Mappers.getMapper(MetricSubjectAppConvert.class);

    MetricSubjectBO toMetricSubjectBO(MetricSubject subject);

    default MetricSubject toMetricSubject(MetricSubjectCmd cmd) {
        if (cmd == null) return null;
        MetricSubject s = new MetricSubject();
        s.setSubjectCode(cmd.getSubjectCode());
        s.setSubjectName(cmd.getSubjectName());
        s.setSubjectDesc(cmd.getSubjectDesc());
        s.setParentId(cmd.getParentId());
        s.setSortOrder(cmd.getSortOrder());
        s.setCreateBy(cmd.getCreateBy());
        s.setUpdateBy(cmd.getUpdateBy());
        return s;
    }
}
