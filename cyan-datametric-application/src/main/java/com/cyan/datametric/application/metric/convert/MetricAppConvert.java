package com.cyan.datametric.application.metric.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.metric.bo.MetricBO;
import com.cyan.datametric.application.metric.cmd.AtomicMetricCmd;
import com.cyan.datametric.application.metric.cmd.DerivedMetricCmd;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricCompositeExt;
import com.cyan.datametric.domain.metric.MetricDerivedExt;
import com.cyan.datametric.enums.StatFunc;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

/**
 * 指标应用层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricAppConvert {

    MetricBO toMetricBO(Metric metric);

    default MetricAtomicExt toAtomicExt(AtomicMetricCmd cmd) {
        if (cmd == null) return null;
        MetricAtomicExt ext = new MetricAtomicExt();
        ext.setStatFunc(StatFunc.valueOf(cmd.getStatFunc()));
        ext.setDsName(cmd.getDsName());
        ext.setDbName(cmd.getDbName());
        ext.setTblName(cmd.getTblName());
        ext.setColName(cmd.getColName());
        if (cmd.getFilterCondition() != null) {
            ext.setFilterCondition(cmd.getFilterCondition().stream()
                    .map(f -> new MetricAtomicExt.FilterCondition().setField(f.getField()).setOp(f.getOp()).setValue(f.getValue()))
                    .collect(Collectors.toList()));
        }
        return ext;
    }

    default MetricDerivedExt toDerivedExt(DerivedMetricCmd cmd) {
        if (cmd == null) return null;
        MetricDerivedExt ext = new MetricDerivedExt();
        ext.setAtomicMetricId(cmd.getAtomicMetricId());
        ext.setTimePeriodId(cmd.getTimePeriodId());
        ext.setModifierIds(cmd.getModifierIds());
        ext.setDimensionIds(cmd.getDimensionIds());
        if (cmd.getGroupByFields() != null) {
            ext.setGroupByFields(cmd.getGroupByFields().stream()
                    .map(g -> new MetricDerivedExt.GroupByField().setCol(g.getCol()))
                    .collect(Collectors.toList()));
        }
        return ext;
    }

    default MetricCompositeExt toCompositeExt(com.cyan.datametric.application.metric.cmd.CompositeMetricCmd cmd) {
        if (cmd == null) return null;
        MetricCompositeExt ext = new MetricCompositeExt();
        ext.setFormula(cmd.getFormula());
        ext.setMetricRefs(cmd.getMetricRefs());
        return ext;
    }

    /**
     * 原子指标命令转换为领域对象
     */
    default Metric toMetric(AtomicMetricCmd cmd) {
        if (cmd == null) return null;
        Metric metric = new Metric();
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricCode(cmd.getMetricCode());
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setSecurityLevel(cmd.getSecurityLevel());
        metric.setOwner(cmd.getOwner());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setAtomicExt(toAtomicExt(cmd));
        return metric;
    }

    /**
     * 派生指标命令转换为领域对象
     */
    default Metric toMetric(DerivedMetricCmd cmd) {
        if (cmd == null) return null;
        Metric metric = new Metric();
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricCode(cmd.getMetricCode());
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setSecurityLevel(cmd.getSecurityLevel());
        metric.setOwner(cmd.getOwner());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setDerivedExt(toDerivedExt(cmd));
        return metric;
    }

    /**
     * 复合指标命令转换为领域对象
     */
    default Metric toMetric(com.cyan.datametric.application.metric.cmd.CompositeMetricCmd cmd) {
        if (cmd == null) return null;
        Metric metric = new Metric();
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricCode(cmd.getMetricCode());
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setSecurityLevel(cmd.getSecurityLevel());
        metric.setOwner(cmd.getOwner());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setCompositeExt(toCompositeExt(cmd));
        return metric;
    }
}
