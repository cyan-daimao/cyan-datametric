package com.cyan.datametric.infra.persistence.metric.convert;

import com.cyan.arch.base.mapstruct.MapstructConvert;
import com.cyan.arch.common.util.JSON;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricCompositeExt;
import com.cyan.datametric.domain.metric.MetricDerivedExt;
import com.cyan.datametric.infra.persistence.metric.dos.MetricAtomicDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricCompositeDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionHistoryDO;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDerivedDO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 指标基础设施层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = MapstructConvert.class)
public interface MetricInfraConvert {

    default Metric toMetric(MetricDefinitionDO def) {
        if (def == null) return null;
        Metric metric = new Metric();
        metric.setId(def.getId() == null ? null : String.valueOf(def.getId()));
        metric.setMetricCode(def.getMetricCode());
        metric.setMetricName(def.getMetricName());
        metric.setMetricType(def.getMetricType());
        metric.setSubjectCode(def.getSubjectCode());
        metric.setBizCaliber(def.getBizCaliber());
        metric.setTechCaliber(def.getTechCaliber());
        metric.setStatus(def.getStatus());
        metric.setSecurityLevel(def.getSecurityLevel());
        metric.setOwner(def.getOwner());
        metric.setVersion(def.getVersion());
        metric.setCreateBy(def.getCreateBy());
        metric.setUpdateBy(def.getUpdateBy());
        metric.setCreatedAt(def.getCreatedAt());
        metric.setUpdatedAt(def.getUpdatedAt());
        return metric;
    }

    default MetricDefinitionDO toMetricDefinitionDO(Metric metric) {
        if (metric == null) return null;
        MetricDefinitionDO def = new MetricDefinitionDO();
        if (metric.getId() != null) {
            def.setId(Long.parseLong(metric.getId()));
        }
        def.setMetricCode(metric.getMetricCode());
        def.setMetricName(metric.getMetricName());
        def.setMetricType(metric.getMetricType());
        def.setSubjectCode(metric.getSubjectCode());
        def.setBizCaliber(metric.getBizCaliber());
        def.setTechCaliber(metric.getTechCaliber());
        def.setStatus(metric.getStatus());
        def.setSecurityLevel(metric.getSecurityLevel());
        def.setOwner(metric.getOwner());
        def.setVersion(metric.getVersion());
        def.setCreateBy(metric.getCreateBy());
        def.setUpdateBy(metric.getUpdateBy());
        def.setCreatedAt(metric.getCreatedAt());
        def.setUpdatedAt(metric.getUpdatedAt());
        return def;
    }

    default MetricAtomicExt toAtomicExt(MetricAtomicDO atomic) {
        if (atomic == null) return null;
        MetricAtomicExt ext = new MetricAtomicExt();
        ext.setId(atomic.getId() == null ? null : String.valueOf(atomic.getId()));
        ext.setMetricId(atomic.getMetricId() == null ? null : String.valueOf(atomic.getMetricId()));
        ext.setStatFunc(atomic.getStatFunc());
        return ext;
    }

    default MetricAtomicDO toAtomicDO(MetricAtomicExt atomic) {
        if (atomic == null) return null;
        MetricAtomicDO d = new MetricAtomicDO();
        d.setId(atomic.getId() == null ? null : Long.parseLong(atomic.getId()));
        d.setMetricId(atomic.getMetricId() == null ? null : Long.parseLong(atomic.getMetricId()));
        d.setStatFunc(atomic.getStatFunc());
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

    default MetricDefinitionHistoryDO toMetricDefinitionHistoryDO(Metric metric) {
        if (metric == null) return null;
        MetricDefinitionHistoryDO d = new MetricDefinitionHistoryDO();
        d.setMetricCode(metric.getMetricCode());
        d.setMetricName(metric.getMetricName());
        d.setMetricType(metric.getMetricType() == null ? null : metric.getMetricType().name());
        d.setSubjectCode(metric.getSubjectCode());
        d.setBizCaliber(metric.getBizCaliber());
        d.setTechCaliber(metric.getTechCaliber());
        d.setStatus(metric.getStatus() == null ? null : metric.getStatus().name());
        d.setSecurityLevel(metric.getSecurityLevel());
        d.setOwner(metric.getOwner());
        d.setVersion(metric.getVersion());
        d.setCreateBy(metric.getCreateBy());
        d.setUpdateBy(metric.getUpdateBy());
        d.setCreatedAt(metric.getCreatedAt());
        d.setUpdatedAt(metric.getUpdatedAt());
        if (metric.getMetricType() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                switch (metric.getMetricType()) {
                    case ATOMIC -> {
                        if (metric.getAtomicExt() != null) {
                            d.setExtJson(mapper.writeValueAsString(metric.getAtomicExt()));
                        }
                    }
                    case DERIVED -> {
                        if (metric.getDerivedExt() != null) {
                            d.setExtJson(mapper.writeValueAsString(metric.getDerivedExt()));
                        }
                    }
                    case COMPOSITE -> {
                        if (metric.getCompositeExt() != null) {
                            d.setExtJson(mapper.writeValueAsString(metric.getCompositeExt()));
                        }
                    }
                }
            } catch (Exception e) {
                d.setExtJson(null);
            }
        }
        return d;
    }

    default Metric toMetric(MetricDefinitionHistoryDO history) {
        if (history == null) return null;
        Metric metric = new Metric();
        metric.setMetricCode(history.getMetricCode());
        metric.setMetricName(history.getMetricName());
        metric.setMetricType(history.getMetricType() == null ? null : com.cyan.datametric.enums.MetricType.valueOf(history.getMetricType()));
        metric.setSubjectCode(history.getSubjectCode());
        metric.setBizCaliber(history.getBizCaliber());
        metric.setTechCaliber(history.getTechCaliber());
        metric.setStatus(history.getStatus() == null ? null : com.cyan.datametric.enums.MetricStatus.valueOf(history.getStatus()));
        metric.setSecurityLevel(history.getSecurityLevel());
        metric.setOwner(history.getOwner());
        metric.setVersion(history.getVersion());
        metric.setCreateBy(history.getCreateBy());
        metric.setUpdateBy(history.getUpdateBy());
        metric.setCreatedAt(history.getCreatedAt());
        metric.setUpdatedAt(history.getUpdatedAt());
        if (history.getExtJson() != null && !history.getExtJson().isEmpty() && metric.getMetricType() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                switch (metric.getMetricType()) {
                    case ATOMIC -> metric.setAtomicExt(mapper.readValue(history.getExtJson(), MetricAtomicExt.class));
                    case DERIVED -> metric.setDerivedExt(mapper.readValue(history.getExtJson(), MetricDerivedExt.class));
                    case COMPOSITE -> metric.setCompositeExt(mapper.readValue(history.getExtJson(), MetricCompositeExt.class));
                }
            } catch (Exception e) {
                // ignore parse error
            }
        }
        return metric;
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
