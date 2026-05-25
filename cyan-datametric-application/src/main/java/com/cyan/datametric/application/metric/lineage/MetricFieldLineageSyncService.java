package com.cyan.datametric.application.metric.lineage;

import com.alibaba.fastjson2.JSON;
import com.cyan.dataman.client.lineage.dto.MetadataLineageEdgeDTO;
import com.cyan.dataman.client.lineage.dto.MetadataLineageNodeDTO;
import com.cyan.dataman.client.lineage.request.MetadataLineageSyncRequest;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricDerivedExt;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.infra.gateway.MetadataLineageGateway;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 指标字段血缘同步服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
public class MetricFieldLineageSyncService {

    private static final String SERVICE_NAME = "cyan-datametric";
    private static final String NODE_FIELD = "FIELD";
    private static final String NODE_METRIC = "METRIC";
    private static final String EDGE_USES_FIELD = "USES_FIELD";
    private static final String EDGE_DERIVES_METRIC = "DERIVES_METRIC";

    private final MetadataLineageGateway metadataLineageGateway;
    private final ModifierRepository modifierRepository;

    public MetricFieldLineageSyncService(MetadataLineageGateway metadataLineageGateway,
                                         ModifierRepository modifierRepository) {
        this.metadataLineageGateway = metadataLineageGateway;
        this.modifierRepository = modifierRepository;
    }

    /**
     * 同步指标字段血缘
     */
    public void sync(Metric metric, Metric atomicMetric, List<Metric> refMetrics) {
        if (metric == null || metric.getId() == null || metric.getId().isBlank()) {
            return;
        }
        Map<String, MetadataLineageNodeDTO> nodes = new LinkedHashMap<>();
        List<MetadataLineageEdgeDTO> edges = new ArrayList<>();
        String metricKey = metricKey(metric.getId());
        nodes.put(metricKey, toMetricNode(metric));

        if (metric.getMetricType() == MetricType.ATOMIC) {
            collectAtomic(metric, metric, nodes, edges);
        }
        if (metric.getMetricType() == MetricType.DERIVED) {
            if (atomicMetric != null) {
                nodes.put(metricKey(atomicMetric.getId()), toMetricNode(atomicMetric));
                edges.add(toEdge(metricKey(atomicMetric.getId()), metricKey, EDGE_DERIVES_METRIC, metric.getId()));
                collectAtomic(atomicMetric, metric, nodes, edges);
                collectDerivedExtraFields(metric, atomicMetric, nodes, edges);
            }
        }
        if (metric.getMetricType() == MetricType.COMPOSITE) {
            for (Metric refMetric : Optional.ofNullable(refMetrics).orElse(List.of())) {
                nodes.put(metricKey(refMetric.getId()), toMetricNode(refMetric));
                edges.add(toEdge(metricKey(refMetric.getId()), metricKey, EDGE_DERIVES_METRIC, metric.getId()));
            }
        }

        metadataLineageGateway.sync(new MetadataLineageSyncRequest()
                .setServiceName(SERVICE_NAME)
                .setRefId(metric.getId())
                .setNodes(new ArrayList<>(nodes.values()))
                .setEdges(edges));
    }

    /**
     * 清理指标血缘
     */
    public void clear(String metricId) {
        if (metricId == null || metricId.isBlank()) {
            return;
        }
        metadataLineageGateway.sync(new MetadataLineageSyncRequest()
                .setServiceName(SERVICE_NAME)
                .setRefId(metricId)
                .setNodes(List.of())
                .setEdges(List.of()));
    }

    /**
     * 收集原子指标字段
     */
    private void collectAtomic(Metric atomicMetric, Metric targetMetric, Map<String, MetadataLineageNodeDTO> nodes, List<MetadataLineageEdgeDTO> edges) {
        MetricAtomicExt ext = atomicMetric.getAtomicExt();
        if (ext == null) {
            return;
        }
        addField(ext.getDsName(), ext.getDbName(), ext.getTblName(), ext.getColName(), targetMetric.getId(), nodes, edges);
        for (MetricAtomicExt.FilterCondition filter : Optional.ofNullable(ext.getFilterCondition()).orElse(List.of())) {
            addField(ext.getDsName(), ext.getDbName(), ext.getTblName(), filter.getField(), targetMetric.getId(), nodes, edges);
        }
    }

    /**
     * 收集派生指标额外字段
     */
    private void collectDerivedExtraFields(Metric metric, Metric atomicMetric, Map<String, MetadataLineageNodeDTO> nodes, List<MetadataLineageEdgeDTO> edges) {
        MetricDerivedExt ext = metric.getDerivedExt();
        MetricAtomicExt atomicExt = atomicMetric.getAtomicExt();
        if (ext == null || atomicExt == null) {
            return;
        }
        for (MetricDerivedExt.GroupByField groupByField : Optional.ofNullable(ext.getGroupByFields()).orElse(List.of())) {
            addField(atomicExt.getDsName(), atomicExt.getDbName(), atomicExt.getTblName(), groupByField.getCol(), metric.getId(), nodes, edges);
        }
        List<Modifier> modifiers = modifierRepository.findByIds(Optional.ofNullable(ext.getModifierIds()).orElse(List.of()));
        for (Modifier modifier : Optional.ofNullable(modifiers).orElse(List.of())) {
            addField(atomicExt.getDsName(), atomicExt.getDbName(), atomicExt.getTblName(), modifier.getFieldName(), metric.getId(), nodes, edges);
        }
    }

    /**
     * 添加字段血缘
     */
    private void addField(String catalog, String schema, String table, String column, String metricId,
                          Map<String, MetadataLineageNodeDTO> nodes, List<MetadataLineageEdgeDTO> edges) {
        if (schema == null || schema.isBlank() || table == null || table.isBlank() || column == null || column.isBlank()) {
            return;
        }
        String normalizedCatalog = normalizeCatalog(catalog);
        String fieldKey = "field:" + normalizedCatalog + "." + schema + "." + table + "." + column;
        nodes.putIfAbsent(fieldKey, new MetadataLineageNodeDTO()
                .setNodeKey(fieldKey)
                .setNodeType(NODE_FIELD)
                .setNodeName(column)
                .setServiceName(SERVICE_NAME)
                .setTableRef(normalizedCatalog + "." + schema + "." + table)
                .setColumnName(column));
        edges.add(toEdge(fieldKey, metricKey(metricId), EDGE_USES_FIELD, metricId));
    }

    /**
     * 转换指标节点
     */
    private MetadataLineageNodeDTO toMetricNode(Metric metric) {
        return new MetadataLineageNodeDTO()
                .setNodeKey(metricKey(metric.getId()))
                .setNodeType(NODE_METRIC)
                .setNodeName(metric.getMetricName())
                .setServiceName(SERVICE_NAME)
                .setRefId(metric.getId())
                .setPropertiesJson(metricProperties(metric));
    }

    /**
     * 转换血缘边
     */
    private MetadataLineageEdgeDTO toEdge(String sourceKey, String targetKey, String edgeType, String refId) {
        return new MetadataLineageEdgeDTO()
                .setSourceKey(sourceKey)
                .setTargetKey(targetKey)
                .setEdgeType(edgeType)
                .setServiceName(SERVICE_NAME)
                .setRefId(refId);
    }

    /**
     * 指标节点唯一键
     */
    private String metricKey(String metricId) {
        return "metric:datametric:" + metricId;
    }

    /**
     * 归一化元数据 catalog
     */
    private String normalizeCatalog(String catalog) {
        if (catalog == null || catalog.isBlank() || "cyan_iceberg".equals(catalog)) {
            return "iceberg";
        }
        return catalog;
    }

    /**
     * 构建指标属性 JSON
     */
    private String metricProperties(Metric metric) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("metricCode", metric.getMetricCode());
        properties.put("metricType", metric.getMetricType());
        return JSON.toJSONString(properties);
    }
}
