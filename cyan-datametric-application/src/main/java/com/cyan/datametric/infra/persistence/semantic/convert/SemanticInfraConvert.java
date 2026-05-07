package com.cyan.datametric.infra.persistence.semantic.convert;

import com.cyan.arch.common.util.JSON;
import com.cyan.datametric.domain.semantic.*;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.enums.StatFunc;
import com.cyan.datametric.enums.semantic.*;
import com.cyan.datametric.infra.persistence.semantic.dos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 语义层基础设施转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SemanticInfraConvert {

    SemanticInfraConvert INSTANCE = Mappers.getMapper(SemanticInfraConvert.class);

    // ==================== LogicalTable ====================
    default LogicalTable toLogicalTable(SemanticLogicalTableDO d) {
        if (d == null) return null;
        LogicalTable t = new LogicalTable();
        t.setId(d.getId() == null ? null : String.valueOf(d.getId()));
        t.setTableName(d.getTableName());
        t.setDisplayName(d.getDisplayName());
        t.setTableType(d.getTableType() == null ? null : TableType.valueOf(d.getTableType()));
        t.setPrimaryKey(d.getPrimaryKey());
        t.setTimeColumn(d.getTimeColumn());
        if (d.getSchemaJson() != null && !d.getSchemaJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                t.setSchema(mapper.readValue(d.getSchemaJson(), new TypeReference<List<LogicalTable.ColumnSchema>>() {}));
            } catch (Exception e) {
                t.setSchema(null);
            }
        }
        t.setDescription(d.getDescription());
        t.setCreateBy(d.getCreateBy());
        t.setUpdateBy(d.getUpdateBy());
        t.setCreatedAt(d.getCreatedAt());
        t.setUpdatedAt(d.getUpdatedAt());
        return t;
    }

    default SemanticLogicalTableDO toLogicalTableDO(LogicalTable t) {
        if (t == null) return null;
        SemanticLogicalTableDO d = new SemanticLogicalTableDO();
        d.setId(t.getId() == null ? null : Long.parseLong(t.getId()));
        d.setTableName(t.getTableName());
        d.setDisplayName(t.getDisplayName());
        d.setTableType(t.getTableType() == null ? null : t.getTableType().getCode());
        d.setPrimaryKey(t.getPrimaryKey());
        d.setTimeColumn(t.getTimeColumn());
        if (t.getSchema() != null) {
            d.setSchemaJson(JSON.toJSONString(t.getSchema()));
        }
        d.setDescription(t.getDescription());
        d.setCreateBy(t.getCreateBy());
        d.setUpdateBy(t.getUpdateBy());
        d.setCreatedAt(t.getCreatedAt());
        d.setUpdatedAt(t.getUpdatedAt());
        return d;
    }

    // ==================== TableRelationship ====================
    default TableRelationship toTableRelationship(SemanticTableRelationshipDO d) {
        if (d == null) return null;
        TableRelationship t = new TableRelationship();
        t.setId(d.getId() == null ? null : String.valueOf(d.getId()));
        t.setLeftTableId(d.getLeftTableId() == null ? null : String.valueOf(d.getLeftTableId()));
        t.setRightTableId(d.getRightTableId() == null ? null : String.valueOf(d.getRightTableId()));
        t.setJoinType(d.getJoinType() == null ? null : JoinType.valueOf(d.getJoinType()));
        if (d.getConditionsJson() != null && !d.getConditionsJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                t.setConditions(mapper.readValue(d.getConditionsJson(), new TypeReference<List<TableRelationship.JoinCondition>>() {}));
            } catch (Exception e) {
                t.setConditions(null);
            }
        }
        t.setDescription(d.getDescription());
        t.setCreateBy(d.getCreateBy());
        t.setUpdateBy(d.getUpdateBy());
        t.setCreatedAt(d.getCreatedAt());
        t.setUpdatedAt(d.getUpdatedAt());
        return t;
    }

    default SemanticTableRelationshipDO toTableRelationshipDO(TableRelationship t) {
        if (t == null) return null;
        SemanticTableRelationshipDO d = new SemanticTableRelationshipDO();
        d.setId(t.getId() == null ? null : Long.parseLong(t.getId()));
        d.setLeftTableId(t.getLeftTableId() == null ? null : Long.parseLong(t.getLeftTableId()));
        d.setRightTableId(t.getRightTableId() == null ? null : Long.parseLong(t.getRightTableId()));
        d.setJoinType(t.getJoinType() == null ? null : t.getJoinType().getCode());
        if (t.getConditions() != null) {
            d.setConditionsJson(JSON.toJSONString(t.getConditions()));
        }
        d.setDescription(t.getDescription());
        d.setCreateBy(t.getCreateBy());
        d.setUpdateBy(t.getUpdateBy());
        d.setCreatedAt(t.getCreatedAt());
        d.setUpdatedAt(t.getUpdatedAt());
        return d;
    }

    // ==================== SemanticMetric ====================
    default SemanticMetric toSemanticMetric(SemanticMetricDO d) {
        if (d == null) return null;
        SemanticMetric m = new SemanticMetric();
        m.setId(d.getId() == null ? null : String.valueOf(d.getId()));
        m.setMetricCode(d.getMetricCode());
        m.setMetricName(d.getMetricName());
        m.setMetricType(d.getMetricType() == null ? null : MetricType.valueOf(d.getMetricType()));
        m.setSourceTableId(d.getSourceTableId() == null ? null : String.valueOf(d.getSourceTableId()));
        m.setSourceColumn(d.getSourceColumn());
        m.setStatFunc(d.getStatFunc() == null ? null : StatFunc.valueOf(d.getStatFunc()));
        m.setFormula(d.getFormula());
        if (d.getModifiersJson() != null && !d.getModifiersJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                m.setModifiers(mapper.readValue(d.getModifiersJson(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                m.setModifiers(null);
            }
        }
        m.setTimePeriodId(d.getTimePeriodId() == null ? null : String.valueOf(d.getTimePeriodId()));
        m.setDescription(d.getDescription());
        m.setCreateBy(d.getCreateBy());
        m.setUpdateBy(d.getUpdateBy());
        m.setCreatedAt(d.getCreatedAt());
        m.setUpdatedAt(d.getUpdatedAt());
        return m;
    }

    default SemanticMetricDO toSemanticMetricDO(SemanticMetric m) {
        if (m == null) return null;
        SemanticMetricDO d = new SemanticMetricDO();
        d.setId(m.getId() == null ? null : Long.parseLong(m.getId()));
        d.setMetricCode(m.getMetricCode());
        d.setMetricName(m.getMetricName());
        d.setMetricType(m.getMetricType() == null ? null : m.getMetricType().getCode());
        d.setSourceTableId(m.getSourceTableId() == null ? null : Long.parseLong(m.getSourceTableId()));
        d.setSourceColumn(m.getSourceColumn());
        d.setStatFunc(m.getStatFunc() == null ? null : m.getStatFunc().getCode());
        d.setFormula(m.getFormula());
        if (m.getModifiers() != null) {
            d.setModifiersJson(JSON.toJSONString(m.getModifiers()));
        }
        d.setTimePeriodId(m.getTimePeriodId() == null ? null : Long.parseLong(m.getTimePeriodId()));
        d.setDescription(m.getDescription());
        d.setCreateBy(m.getCreateBy());
        d.setUpdateBy(m.getUpdateBy());
        d.setCreatedAt(m.getCreatedAt());
        d.setUpdatedAt(m.getUpdatedAt());
        return d;
    }

    // ==================== MaterializedView ====================
    default MaterializedView toMaterializedView(SemanticMaterializedViewDO d) {
        if (d == null) return null;
        MaterializedView m = new MaterializedView();
        m.setId(d.getId() == null ? null : String.valueOf(d.getId()));
        m.setName(d.getName());
        m.setDefinitionSql(d.getDefinitionSql());
        if (d.getSourceTablesJson() != null && !d.getSourceTablesJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                m.setSourceTables(mapper.readValue(d.getSourceTablesJson(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                m.setSourceTables(null);
            }
        }
        if (d.getDimensionsJson() != null && !d.getDimensionsJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                m.setDimensions(mapper.readValue(d.getDimensionsJson(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                m.setDimensions(null);
            }
        }
        if (d.getMetricsJson() != null && !d.getMetricsJson().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                m.setMetrics(mapper.readValue(d.getMetricsJson(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                m.setMetrics(null);
            }
        }
        m.setRefreshStrategy(d.getRefreshStrategy() == null ? null : RefreshStrategy.valueOf(d.getRefreshStrategy()));
        m.setCronExpression(d.getCronExpression());
        m.setLastRefreshTime(d.getLastRefreshTime());
        m.setStatus(d.getStatus() == null ? null : MvStatus.valueOf(d.getStatus()));
        m.setHitCount(d.getHitCount());
        m.setLastHitTime(d.getLastHitTime());
        m.setStorageSize(d.getStorageSize());
        m.setCreateBy(d.getCreateBy());
        m.setUpdateBy(d.getUpdateBy());
        m.setCreatedAt(d.getCreatedAt());
        m.setUpdatedAt(d.getUpdatedAt());
        return m;
    }

    default SemanticMaterializedViewDO toMaterializedViewDO(MaterializedView m) {
        if (m == null) return null;
        SemanticMaterializedViewDO d = new SemanticMaterializedViewDO();
        d.setId(m.getId() == null ? null : Long.parseLong(m.getId()));
        d.setName(m.getName());
        d.setDefinitionSql(m.getDefinitionSql());
        if (m.getSourceTables() != null) {
            d.setSourceTablesJson(JSON.toJSONString(m.getSourceTables()));
        }
        if (m.getDimensions() != null) {
            d.setDimensionsJson(JSON.toJSONString(m.getDimensions()));
        }
        if (m.getMetrics() != null) {
            d.setMetricsJson(JSON.toJSONString(m.getMetrics()));
        }
        d.setRefreshStrategy(m.getRefreshStrategy() == null ? null : m.getRefreshStrategy().getCode());
        d.setCronExpression(m.getCronExpression());
        d.setLastRefreshTime(m.getLastRefreshTime());
        d.setStatus(m.getStatus() == null ? null : m.getStatus().getCode());
        d.setHitCount(m.getHitCount());
        d.setLastHitTime(m.getLastHitTime());
        d.setStorageSize(m.getStorageSize());
        d.setCreateBy(m.getCreateBy());
        d.setUpdateBy(m.getUpdateBy());
        d.setCreatedAt(m.getCreatedAt());
        d.setUpdatedAt(m.getUpdatedAt());
        return d;
    }
}
