package com.cyan.datametric.infra.persistence.metric.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.arch.common.util.JSON;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricCompositeExt;
import com.cyan.datametric.domain.metric.MetricDerivedExt;
import com.cyan.datametric.infra.persistence.metric.dos.MetricAtomicDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricCompositeDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDerivedDO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 指标基础设施层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface MetricInfraConvert {
    MetricInfraConvert INSTANCE = Mappers.getMapper(MetricInfraConvert.class);

    Metric toMetric(MetricDefinitionDO def);

    MetricDefinitionDO toMetricDefinitionDO(Metric metric);

    default MetricAtomicExt toAtomicExt(MetricAtomicDO atomic) {
        if (atomic == null) return null;
        MetricAtomicExt ext = new MetricAtomicExt();
        ext.setId(atomic.getId() == null ? null : String.valueOf(atomic.getId()));
        ext.setMetricId(atomic.getMetricId() == null ? null : String.valueOf(atomic.getMetricId()));
        ext.setStatFunc(atomic.getStatFunc());
        ext.setDsName(atomic.getDsName());
        ext.setDbName(atomic.getDbName());
        ext.setTblName(atomic.getTblName());
        ext.setColName(atomic.getColName());
        if (atomic.getFilterCondition() != null && !atomic.getFilterCondition().isEmpty()) {
            ext.setFilterCondition(parseList(atomic.getFilterCondition(), MetricAtomicExt.FilterCondition.class));
        }
        return ext;
    }

    default MetricAtomicDO toAtomicDO(MetricAtomicExt atomic) {
        if (atomic == null) return null;
        MetricAtomicDO d = new MetricAtomicDO();
        d.setId(atomic.getId() == null ? null : Long.parseLong(atomic.getId()));
        d.setMetricId(atomic.getMetricId() == null ? null : Long.parseLong(atomic.getMetricId()));
        d.setStatFunc(atomic.getStatFunc());
        d.setDsName(atomic.getDsName());
        d.setDbName(atomic.getDbName());
        d.setTblName(atomic.getTblName());
        d.setColName(atomic.getColName());
        if (atomic.getFilterCondition() != null) {
            d.setFilterCondition(JSON.toJSONString(atomic.getFilterCondition()));
        }
        return d;
    }

    default MetricDerivedExt toDerivedExt(MetricDerivedDO derived) {
        if (derived == null) return null;
        MetricDerivedExt ext = new MetricDerivedExt();
        ext.setId(derived.getId() == null ? null : String.valueOf(derived.getId()));
        ext.setMetricId(derived.getMetricId() == null ? null : String.valueOf(derived.getMetricId()));
        ext.setAtomicMetricId(derived.getAtomicMetricId() == null ? null : String.valueOf(derived.getAtomicMetricId()));
        ext.setTimePeriodId(derived.getTimePeriodId() == null ? null : String.valueOf(derived.getTimePeriodId()));
        if (derived.getModifierIds() != null && !derived.getModifierIds().isEmpty()) {
            ext.setModifierIds(parseListString(derived.getModifierIds()));
        }
        if (derived.getDimensionIds() != null && !derived.getDimensionIds().isEmpty()) {
            ext.setDimensionIds(parseListString(derived.getDimensionIds()));
        }
        if (derived.getGroupByFields() != null && !derived.getGroupByFields().isEmpty()) {
            ext.setGroupByFields(parseList(derived.getGroupByFields(), MetricDerivedExt.GroupByField.class));
        }
        return ext;
    }

    default MetricDerivedDO toDerivedDO(MetricDerivedExt derived) {
        if (derived == null) return null;
        MetricDerivedDO d = new MetricDerivedDO();
        d.setId(derived.getId() == null ? null : Long.parseLong(derived.getId()));
        d.setMetricId(derived.getMetricId() == null ? null : Long.parseLong(derived.getMetricId()));
        d.setAtomicMetricId(derived.getAtomicMetricId() == null ? null : Long.parseLong(derived.getAtomicMetricId()));
        d.setTimePeriodId(derived.getTimePeriodId() == null ? null : Long.parseLong(derived.getTimePeriodId()));
        if (derived.getModifierIds() != null) {
            d.setModifierIds(JSON.toJSONString(derived.getModifierIds()));
        }
        if (derived.getDimensionIds() != null) {
            d.setDimensionIds(JSON.toJSONString(derived.getDimensionIds()));
        }
        if (derived.getGroupByFields() != null) {
            d.setGroupByFields(JSON.toJSONString(derived.getGroupByFields()));
        }
        return d;
    }

    default MetricCompositeExt toCompositeExt(MetricCompositeDO composite) {
        if (composite == null) return null;
        MetricCompositeExt ext = new MetricCompositeExt();
        ext.setId(composite.getId() == null ? null : String.valueOf(composite.getId()));
        ext.setMetricId(composite.getMetricId() == null ? null : String.valueOf(composite.getMetricId()));
        ext.setFormula(composite.getFormula());
        if (composite.getMetricRefs() != null && !composite.getMetricRefs().isEmpty()) {
            ext.setMetricRefs(parseListString(composite.getMetricRefs()));
        }
        return ext;
    }

    default MetricCompositeDO toCompositeDO(MetricCompositeExt composite) {
        if (composite == null) return null;
        MetricCompositeDO d = new MetricCompositeDO();
        d.setId(composite.getId() == null ? null : Long.parseLong(composite.getId()));
        d.setMetricId(composite.getMetricId() == null ? null : Long.parseLong(composite.getMetricId()));
        d.setFormula(composite.getFormula());
        if (composite.getMetricRefs() != null) {
            d.setMetricRefs(JSON.toJSONString(composite.getMetricRefs()));
        }
        return d;
    }

    private static <T> List<T> parseList(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> parseListString(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
