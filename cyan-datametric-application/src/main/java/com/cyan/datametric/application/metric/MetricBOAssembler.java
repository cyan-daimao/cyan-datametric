package com.cyan.datametric.application.metric;

import com.cyan.datametric.application.metric.bo.MetricAtomicBO;
import com.cyan.datametric.application.metric.bo.MetricBO;
import com.cyan.datametric.application.metric.bo.MetricCompositeBO;
import com.cyan.datametric.application.metric.bo.MetricDerivedBO;
import com.cyan.datametric.application.metric.convert.MetricAppConvert;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标业务对象组装器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MetricBOAssembler {

    private final MetricAppConvert metricAppConvert;
    private final MetricSubjectRepository metricSubjectRepository;

    /**
     * 组装基础指标BO
     */
    public MetricBO assembleBasic(Metric metric) {
        if (metric == null) return null;
        MetricBO bo = metricAppConvert.toMetricBO(metric);
        bo.setSecurityLevel(metric.getSecurityLevel());
        if (metric.getAtomicExt() != null) {
            MetricAtomicExt ext = metric.getAtomicExt();
            bo.setStatFunc(ext.getStatFunc() == null ? null : ext.getStatFunc().getCode());
            bo.setDsName(ext.getDsName());
            bo.setDbName(ext.getDbName());
            bo.setTblName(ext.getTblName());
            bo.setColName(ext.getColName());
        }
        return bo;
    }

    /**
     * 组装指标详情BO（含原子/派生/复合扩展）
     */
    public MetricBO assembleDetail(Metric metric) {
        MetricBO bo = assembleBasic(metric);
        if (metric.getAtomicExt() != null) {
            MetricAtomicBO atomic = new MetricAtomicBO();
            atomic.setStatFunc(metric.getAtomicExt().getStatFunc() == null ? null : metric.getAtomicExt().getStatFunc().getCode());
            atomic.setDsName(metric.getAtomicExt().getDsName());
            atomic.setDbName(metric.getAtomicExt().getDbName());
            atomic.setTblName(metric.getAtomicExt().getTblName());
            atomic.setColName(metric.getAtomicExt().getColName());
            if (metric.getAtomicExt().getFilterCondition() != null) {
                atomic.setFilterCondition(metric.getAtomicExt().getFilterCondition().stream()
                        .map(f -> new MetricAtomicBO.FilterConditionBO().setField(f.getField()).setOp(f.getOp()).setValue(f.getValue()))
                        .toList());
            }
            bo.setAtomic(atomic);
        }
        if (metric.getDerivedExt() != null) {
            MetricDerivedBO derived = new MetricDerivedBO();
            derived.setAtomicMetricId(metric.getDerivedExt().getAtomicMetricId());
            derived.setTimePeriodId(metric.getDerivedExt().getTimePeriodId());
            derived.setModifierIds(metric.getDerivedExt().getModifierIds());
            derived.setDimensionIds(metric.getDerivedExt().getDimensionIds());
            if (metric.getDerivedExt().getGroupByFields() != null) {
                derived.setGroupByFields(metric.getDerivedExt().getGroupByFields().stream()
                        .map(g -> new MetricDerivedBO.GroupByFieldBO().setCol(g.getCol()))
                        .toList());
            }
            bo.setDerived(derived);
        }
        if (metric.getCompositeExt() != null) {
            MetricCompositeBO composite = new MetricCompositeBO();
            composite.setFormula(metric.getCompositeExt().getFormula());
            composite.setMetricRefs(metric.getCompositeExt().getMetricRefs());
            bo.setComposite(composite);
        }
        return bo;
    }

    /**
     * 填充主题域名称（批量）
     */
    public void fillSubjectName(List<MetricBO> bos) {
        if (bos == null || bos.isEmpty()) {
            return;
        }
        List<String> subjectCodes = bos.stream()
                .map(MetricBO::getSubjectCode)
                .filter(sc -> sc != null && !sc.isBlank())
                .distinct()
                .toList();
        if (subjectCodes.isEmpty()) {
            return;
        }
        List<MetricSubject> subjects = metricSubjectRepository.findBySubjectCodes(subjectCodes);
        Map<String, String> nameMap = subjects.stream()
                .filter(s -> s.getSubjectCode() != null)
                .collect(Collectors.toMap(MetricSubject::getSubjectCode, MetricSubject::getSubjectName, (a, b) -> a));
        for (MetricBO bo : bos) {
            if (bo.getSubjectCode() != null) {
                bo.setSubjectName(nameMap.get(bo.getSubjectCode()));
            }
        }
    }

    /**
     * 填充主题域名称（单个）
     */
    public void fillSubjectName(MetricBO bo) {
        if (bo == null || bo.getSubjectCode() == null || bo.getSubjectCode().isBlank()) {
            return;
        }
        MetricSubject subject = metricSubjectRepository.findBySubjectCode(bo.getSubjectCode());
        if (subject != null) {
            bo.setSubjectName(subject.getSubjectName());
        }
    }
}
