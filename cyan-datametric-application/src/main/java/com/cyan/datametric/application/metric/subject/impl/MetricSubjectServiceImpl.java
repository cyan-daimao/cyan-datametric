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
import lombok.RequiredArgsConstructor;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;

/**
 * 指标主题域服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MetricSubjectServiceImpl implements MetricSubjectService {

    private final MetricSubjectRepository metricSubjectRepository;
    private final MetricSubjectAppConvert metricSubjectAppConvert;


    @Override
    public MetricSubjectBO create(MetricSubjectCmd cmd) {
        if (cmd.getSubjectCode() == null || cmd.getSubjectCode().isBlank()) {
            cmd.setSubjectCode("SUB_" + System.currentTimeMillis());
        }
        MetricSubject subject = metricSubjectAppConvert.toMetricSubject(cmd);
        subject = subject.save(metricSubjectRepository);
        return metricSubjectAppConvert.toMetricSubjectBO(subject);
    }

    @Override
    public MetricSubjectBO update(String id, MetricSubjectCmd cmd) {
        MetricSubject existing = metricSubjectRepository.findById(id);
        Assert.notNull(existing, new BusinessException("指标主题域不存在"));
        MetricSubject subject = metricSubjectAppConvert.toMetricSubject(cmd);
        subject.setId(id);
        subject.setCreateBy(existing.getCreateBy());
        subject = subject.update(metricSubjectRepository);
        return metricSubjectAppConvert.toMetricSubjectBO(subject);
    }

    @Override
    public void delete(String id) {
        MetricSubject subject = metricSubjectRepository.findById(id);
        Assert.notNull(subject, new BusinessException("指标主题域不存在"));
        subject.delete(metricSubjectRepository);
    }

    @Override
    public MetricSubjectBO detail(String id) {
        MetricSubject subject = metricSubjectRepository.findById(id);
        return metricSubjectAppConvert.toMetricSubjectBO(subject);
    }

    @Override
    public Page<MetricSubjectBO> page(MetricSubjectQuery query) {
        Page<MetricSubject> page = metricSubjectRepository.page(query);
        List<MetricSubjectBO> list = page.getData().stream()
                .map(metricSubjectAppConvert::toMetricSubjectBO)
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public List<MetricSubjectBO> tree() {
        List<MetricSubject> all = metricSubjectRepository.findAll();
        List<MetricSubjectBO> bos = all.stream()
                .map(metricSubjectAppConvert::toMetricSubjectBO)
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
            children.sort(Comparator.comparing(MetricSubjectBO::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));
            bo.setChildren(children);
        }
        roots.sort(Comparator.comparing(MetricSubjectBO::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        return roots;
    }
}
