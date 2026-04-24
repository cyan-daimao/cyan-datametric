package com.cyan.datametric.adapter.metric.http.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.adapter.metric.http.dto.*;
import com.cyan.datametric.application.metric.bo.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 指标适配层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface MetricAdapterConvert {
    MetricAdapterConvert INSTANCE = Mappers.getMapper(MetricAdapterConvert.class);

    default MetricDTO toMetricDTO(MetricBO bo) {
        if (bo == null) return null;
        MetricDTO dto = new MetricDTO();
        dto.setId(bo.getId());
        dto.setMetricCode(bo.getMetricCode());
        dto.setMetricName(bo.getMetricName());
        dto.setMetricType(bo.getMetricType() == null ? null : bo.getMetricType().getCode());
        dto.setSubjectCode(bo.getSubjectCode());
        dto.setSubjectName(bo.getSubjectName());
        dto.setBizCaliber(bo.getBizCaliber());
        dto.setTechCaliber(bo.getTechCaliber());
        dto.setStatus(bo.getStatus() == null ? null : bo.getStatus().getCode());
        dto.setOwner(bo.getOwner());
        dto.setStatFunc(bo.getStatFunc());
        dto.setDsName(bo.getDsName());
        dto.setDbName(bo.getDbName());
        dto.setTblName(bo.getTblName());
        dto.setColName(bo.getColName());
        dto.setVersion(bo.getVersion());
        dto.setUpdatedAt(bo.getUpdatedAt());
        return dto;
    }

    default DictionaryMetricDTO toDictionaryMetricDTO(MetricBO bo) {
        if (bo == null) return null;
        DictionaryMetricDTO dto = new DictionaryMetricDTO();
        dto.setId(bo.getId());
        dto.setMetricCode(bo.getMetricCode());
        dto.setMetricName(bo.getMetricName());
        dto.setMetricType(bo.getMetricType() == null ? null : bo.getMetricType().getCode());
        dto.setSubjectCode(bo.getSubjectCode());
        dto.setSubjectName(bo.getSubjectName());
        dto.setBizCaliber(bo.getBizCaliber());
        dto.setStatus(bo.getStatus() == null ? null : bo.getStatus().getCode());
        dto.setUpdatedAt(bo.getUpdatedAt());
        dto.setIsFavorite(bo.getIsFavorite());
        return dto;
    }

    default MetricDetailDTO toMetricDetailDTO(MetricBO bo) {
        if (bo == null) return null;
        MetricDetailDTO dto = new MetricDetailDTO();
        dto.setId(bo.getId());
        dto.setMetricCode(bo.getMetricCode());
        dto.setMetricName(bo.getMetricName());
        dto.setMetricType(bo.getMetricType() == null ? null : bo.getMetricType().getCode());
        dto.setSubjectCode(bo.getSubjectCode());
        dto.setSubjectName(bo.getSubjectName());
        dto.setBizCaliber(bo.getBizCaliber());
        dto.setTechCaliber(bo.getTechCaliber());
        dto.setStatus(bo.getStatus() == null ? null : bo.getStatus().getCode());
        dto.setOwner(bo.getOwner());
        dto.setVersion(bo.getVersion());
        dto.setUpdatedAt(bo.getUpdatedAt());
        dto.setCreatedAt(bo.getCreatedAt());
        dto.setAtomic(toAtomicDTO(bo.getAtomic()));
        dto.setDerived(toDerivedDTO(bo.getDerived()));
        dto.setComposite(toCompositeDTO(bo.getComposite()));
        return dto;
    }

    default MetricDetailDTO.MetricAtomicDTO toAtomicDTO(MetricAtomicBO bo) {
        if (bo == null) return null;
        MetricDetailDTO.MetricAtomicDTO dto = new MetricDetailDTO.MetricAtomicDTO();
        dto.setStatFunc(bo.getStatFunc());
        dto.setDsName(bo.getDsName());
        dto.setDbName(bo.getDbName());
        dto.setTblName(bo.getTblName());
        dto.setColName(bo.getColName());
        if (bo.getFilterCondition() != null) {
            dto.setFilterCondition(bo.getFilterCondition().stream()
                    .map(f -> new MetricDetailDTO.FilterConditionDTO().setField(f.getField()).setOp(f.getOp()).setValue(f.getValue()))
                    .toList());
        }
        return dto;
    }

    default MetricDetailDTO.MetricDerivedDTO toDerivedDTO(MetricDerivedBO bo) {
        if (bo == null) return null;
        MetricDetailDTO.MetricDerivedDTO dto = new MetricDetailDTO.MetricDerivedDTO();
        dto.setAtomicMetricId(bo.getAtomicMetricId());
        dto.setTimePeriodId(bo.getTimePeriodId());
        dto.setModifierIds(bo.getModifierIds());
        dto.setDimensionIds(bo.getDimensionIds());
        if (bo.getGroupByFields() != null) {
            dto.setGroupByFields(bo.getGroupByFields().stream()
                    .map(g -> new MetricDetailDTO.GroupByFieldDTO().setCol(g.getCol()))
                    .toList());
        }
        return dto;
    }

    default MetricDetailDTO.MetricCompositeDTO toCompositeDTO(MetricCompositeBO bo) {
        if (bo == null) return null;
        MetricDetailDTO.MetricCompositeDTO dto = new MetricDetailDTO.MetricCompositeDTO();
        dto.setFormula(bo.getFormula());
        dto.setMetricRefs(bo.getMetricRefs());
        return dto;
    }

    SqlTrialResultDTO toSqlTrialResultDTO(SqlTrialResultBO bo);

    DashboardStatsDTO toDashboardStatsDTO(DashboardStatsBO bo);

    SubjectDrilldownDTO toSubjectDrilldownDTO(SubjectDrilldownBO bo);

    default LineageTreeDTO toLineageTreeDTO(LineageTreeBO bo) {
        if (bo == null) return null;
        LineageTreeDTO dto = new LineageTreeDTO();
        dto.setUpstream(toLineageNodeDTO(bo.getUpstream()));
        dto.setDownstream(toLineageNodeDTO(bo.getDownstream()));
        return dto;
    }

    default LineageTreeDTO.LineageNodeDTO toLineageNodeDTO(LineageTreeBO.LineageNodeBO bo) {
        if (bo == null) return null;
        LineageTreeDTO.LineageNodeDTO dto = new LineageTreeDTO.LineageNodeDTO();
        dto.setId(bo.getId());
        dto.setName(bo.getName());
        dto.setNodeType(bo.getNodeType());
        if (bo.getChildren() != null) {
            dto.setChildren(bo.getChildren().stream().map(this::toLineageNodeDTO).toList());
        }
        return dto;
    }
}
