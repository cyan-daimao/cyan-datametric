package com.cyan.datametric.application.metric.subject.impl;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.subject.MetricSubjectService;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import com.cyan.datametric.application.metric.subject.cmd.MetricSubjectCmd;
import com.cyan.datametric.application.metric.subject.convert.MetricSubjectAppConvert;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.query.MetricSubjectQuery;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标主题域服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
public class MetricSubjectServiceImpl implements MetricSubjectService {

    private final MetricSubjectRepository metricSubjectRepository;

    public MetricSubjectServiceImpl(MetricSubjectRepository metricSubjectRepository) {
        this.metricSubjectRepository = metricSubjectRepository;
    }

    @Override
    public MetricSubjectBO create(MetricSubjectCmd cmd) {
        if (cmd.getSubjectCode() == null || cmd.getSubjectCode().isBlank()) {
            cmd.setSubjectCode("SUB_" + System.currentTimeMillis());
        }
        MetricSubject subject = MetricSubjectAppConvert.INSTANCE.toMetricSubject(cmd);
        subject = subject.save(metricSubjectRepository);
        return MetricSubjectAppConvert.INSTANCE.toMetricSubjectBO(subject);
    }

    @Override
    public MetricSubjectBO update(String id, MetricSubjectCmd cmd) {
        MetricSubject subject = MetricSubjectAppConvert.INSTANCE.toMetricSubject(cmd);
        subject.setId(id);
        subject = subject.update(metricSubjectRepository);
        return MetricSubjectAppConvert.INSTANCE.toMetricSubjectBO(subject);
    }

    @Override
    public void delete(String id) {
        MetricSubject subject = new MetricSubject();
        subject.setId(id);
        subject.delete(metricSubjectRepository);
    }

    @Override
    public MetricSubjectBO detail(String id) {
        MetricSubject subject = metricSubjectRepository.findById(id);
        return MetricSubjectAppConvert.INSTANCE.toMetricSubjectBO(subject);
    }

    @Override
    public Page<MetricSubjectBO> page(MetricSubjectQuery query) {
        Page<MetricSubject> page = metricSubjectRepository.page(query);
        List<MetricSubjectBO> list = page.getData().stream()
                .map(MetricSubjectAppConvert.INSTANCE::toMetricSubjectBO)
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public List<MetricSubjectBO> tree() {
        List<MetricSubject> all = metricSubjectRepository.findAll();
        List<MetricSubjectBO> bos = all.stream()
                .map(MetricSubjectAppConvert.INSTANCE::toMetricSubjectBO)
                .toList();

        Map<String, List<MetricSubjectBO>> parentMap = bos.stream()
                .filter(b -> b.getParentId() != null && !b.getParentId().isBlank())
                .collect(Collectors.groupingBy(MetricSubjectBO::getParentId));

        List<MetricSubjectBO> roots = new ArrayList<>();
        for (MetricSubjectBO bo : bos) {
            if (bo.getParentId() == null || bo.getParentId().isBlank()) {
                roots.add(bo);
            }
            List<MetricSubjectBO> children = parentMap.getOrDefault(bo.getId(), new ArrayList<>());
            children.sort(Comparator.comparingInt(MetricSubjectBO::getSortOrder));
            bo.setChildren(children);
        }
        roots.sort(Comparator.comparingInt(MetricSubjectBO::getSortOrder));
        return roots;
    }
}
