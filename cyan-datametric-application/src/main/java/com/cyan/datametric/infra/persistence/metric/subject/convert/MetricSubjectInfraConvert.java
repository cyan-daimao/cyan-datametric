package com.cyan.datametric.infra.persistence.metric.subject.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.infra.persistence.metric.subject.MetricSubjectDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 指标主题域基础设施层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface MetricSubjectInfraConvert {
    MetricSubjectInfraConvert INSTANCE = Mappers.getMapper(MetricSubjectInfraConvert.class);

    default MetricSubject toMetricSubject(MetricSubjectDO subjectDO) {
        if (subjectDO == null) return null;
        MetricSubject s = new MetricSubject();
        s.setId(subjectDO.getId() == null ? null : String.valueOf(subjectDO.getId()));
        s.setSubjectCode(subjectDO.getSubjectCode());
        s.setSubjectName(subjectDO.getSubjectName());
        s.setSubjectDesc(subjectDO.getSubjectDesc());
        s.setParentId(subjectDO.getParentId() == null ? null : String.valueOf(subjectDO.getParentId()));
        s.setLevel(subjectDO.getLevel());
        s.setSortOrder(subjectDO.getSortOrder());
        s.setCreateBy(subjectDO.getCreateBy());
        s.setUpdateBy(subjectDO.getUpdateBy());
        s.setCreatedAt(subjectDO.getCreatedAt());
        s.setUpdatedAt(subjectDO.getUpdatedAt());
        return s;
    }

    default MetricSubjectDO toMetricSubjectDO(MetricSubject subject) {
        if (subject == null) return null;
        MetricSubjectDO d = new MetricSubjectDO();
        d.setId(subject.getId() == null ? null : Long.parseLong(subject.getId()));
        d.setSubjectCode(subject.getSubjectCode());
        d.setSubjectName(subject.getSubjectName());
        d.setSubjectDesc(subject.getSubjectDesc());
        d.setParentId(subject.getParentId() == null ? null : Long.parseLong(subject.getParentId()));
        d.setLevel(subject.getLevel());
        d.setSortOrder(subject.getSortOrder());
        d.setCreateBy(subject.getCreateBy());
        d.setUpdateBy(subject.getUpdateBy());
        d.setCreatedAt(subject.getCreatedAt());
        d.setUpdatedAt(subject.getUpdatedAt());
        return d;
    }
}
