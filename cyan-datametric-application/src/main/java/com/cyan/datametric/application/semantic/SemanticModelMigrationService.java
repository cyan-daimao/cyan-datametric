package com.cyan.datametric.application.semantic;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.domain.semantic.LogicalTable;
import com.cyan.datametric.domain.semantic.TableRelationship;
import com.cyan.datametric.domain.semantic.repository.LogicalTableRepository;
import com.cyan.datametric.domain.semantic.repository.TableRelationshipRepository;
import com.cyan.datametric.enums.semantic.JoinType;
import com.cyan.datametric.enums.semantic.TableType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语义模型迁移服务
 * <p>
 * 兼容 Phase 1/2：将现有维度-事实关联配置自动导入为语义模型中的 LogicalTable 和 TableRelationship。
 * <p>
 * 迁移规则：
 * 1. Dimension.tableName + columnName → LogicalTable(DIMENSION) + 字段信息
 * 2. MetricAtomicExt.dbName + tblName → LogicalTable(FACT) + 字段信息
 * 3. 同一 Dimension 与 Metric 关联到同一张事实表 → TableRelationship(FACT, DIMENSION)
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticModelMigrationService {

    private final DimensionRepository dimensionRepository;
    private final MetricRepository metricRepository;
    private final LogicalTableRepository logicalTableRepository;
    private final TableRelationshipRepository tableRelationshipRepository;

    /**
     * 执行全量迁移
     *
     * @return 迁移报告
     */
    public MigrationReport migrate() {
        MigrationReport report = new MigrationReport();

        // 1. 加载所有维度
        List<Dimension> dimensions = loadAllDimensions();
        // 2. 加载所有原子指标（用于获取事实表信息）
        List<Metric> atomicMetrics = loadAllAtomicMetrics();

        // 3. 迁移维度表
        Map<String, LogicalTable> dimTableMap = new HashMap<>();
        for (Dimension dim : dimensions) {
            if (dim.getTableName() == null || dim.getTableName().isBlank()) {
                continue;
            }
            LogicalTable table = migrateDimensionTable(dim);
            if (table != null) {
                dimTableMap.put(table.getTableName(), table);
                report.incrementDimTables();
            }
        }

        // 4. 迁移事实表
        Map<String, LogicalTable> factTableMap = new HashMap<>();
        for (Metric metric : atomicMetrics) {
            if (metric.getAtomicExt() == null) {
                continue;
            }
            String fullTableName = metric.getAtomicExt().getDbName() + "." + metric.getAtomicExt().getTblName();
            if (factTableMap.containsKey(fullTableName)) {
                continue;
            }
            LogicalTable table = migrateFactTable(metric.getAtomicExt());
            if (table != null) {
                factTableMap.put(fullTableName, table);
                report.incrementFactTables();
            }
        }

        // 5. 建立关联关系
        for (Dimension dim : dimensions) {
            if (dim.getTableName() == null || dim.getColumnName() == null) {
                continue;
            }
            LogicalTable dimTable = dimTableMap.get(dim.getTableName());
            if (dimTable == null) {
                continue;
            }
            // 查找与该维度同表的事实表（Phase 1/2 的约定：维度直接关联到事实表字段）
            for (LogicalTable factTable : factTableMap.values()) {
                if (dim.getTableName().equals(factTable.getTableName())) {
                    // 维度与事实表在同一张表上（退化维度），无需建立 TableRelationship
                    continue;
                }
                // 尝试建立事实表 -> 维度表的关联（基于维度配置中的 joinKeys 或默认主键关联）
                TableRelationship relationship = buildRelationship(factTable, dimTable, dim);
                if (relationship != null) {
                    report.incrementRelationships();
                }
            }
        }

        log.info("语义模型迁移完成: {}", report);
        return report;
    }

    private List<Dimension> loadAllDimensions() {
        return dimensionRepository.page(new com.cyan.datametric.domain.config.query.DimensionPageQuery().setPageNum(1).setPageSize(10000)).getData();
    }

    private List<Metric> loadAllAtomicMetrics() {
        MetricPageQuery query = new MetricPageQuery();
        query.setPageNum(1);
        query.setPageSize(10000);
        return metricRepository.page(query).getData().stream()
                .filter(m -> m.getMetricType() == com.cyan.datametric.enums.MetricType.ATOMIC)
                .toList();
    }

    private LogicalTable migrateDimensionTable(Dimension dim) {
        LogicalTable existing = logicalTableRepository.findByTableName(dim.getTableName());
        if (existing != null) {
            return existing;
        }
        LogicalTable table = new LogicalTable();
        table.setTableName(dim.getTableName());
        table.setDisplayName(dim.getDimName());
        table.setTableType(TableType.DIMENSION);
        table.setPrimaryKey(dim.getColumnName());
        table.setSchema(List.of(new LogicalTable.ColumnSchema(dim.getColumnName(), dim.getDataType(), dim.getDimName(), true)));
        table.setDescription("从 Phase1/2 维度配置自动迁移: " + dim.getDimCode());
        try {
            return table.save(logicalTableRepository);
        } catch (Exception e) {
            log.warn("迁移维度表失败: tableName={}", dim.getTableName(), e);
            return null;
        }
    }

    private LogicalTable migrateFactTable(MetricAtomicExt ext) {
        String fullTableName = ext.getDbName() + "." + ext.getTblName();
        LogicalTable existing = logicalTableRepository.findByTableName(fullTableName);
        if (existing != null) {
            return existing;
        }
        LogicalTable table = new LogicalTable();
        table.setTableName(fullTableName);
        table.setDisplayName(ext.getTblName());
        table.setTableType(TableType.FACT);
        table.setSchema(List.of(new LogicalTable.ColumnSchema(ext.getColName(), null, null, true)));
        table.setDescription("从 Phase1/2 原子指标配置自动迁移");
        try {
            return table.save(logicalTableRepository);
        } catch (Exception e) {
            log.warn("迁移事实表失败: tableName={}", fullTableName, e);
            return null;
        }
    }

    private TableRelationship buildRelationship(LogicalTable factTable, LogicalTable dimTable, Dimension dim) {
        // 检查是否已存在关联
        List<TableRelationship> existing = tableRelationshipRepository.findByTableId(factTable.getId());
        for (TableRelationship rel : existing) {
            if (rel.getRightTableId().equals(dimTable.getId()) || rel.getLeftTableId().equals(dimTable.getId())) {
                return null; // 已存在
            }
        }

        // 默认关联：事实表字段名与维度表主键匹配（如 user_id -> id）
        // 简化策略：事实表中如果存在与维度表名相关的字段（如 dim_user -> user_id）
        String assumedFactColumn = guessFactColumn(factTable, dimTable);
        if (assumedFactColumn == null) {
            return null;
        }

        TableRelationship rel = new TableRelationship();
        rel.setLeftTableId(factTable.getId());
        rel.setRightTableId(dimTable.getId());
        rel.setJoinType(JoinType.LEFT);
        rel.setConditions(List.of(new TableRelationship.JoinCondition(assumedFactColumn, dimTable.getPrimaryKey())));
        rel.setDescription("从 Phase1/2 维度配置自动迁移: " + dim.getDimCode());
        try {
            return rel.save(tableRelationshipRepository);
        } catch (Exception e) {
            log.warn("建立关联关系失败: fact={}, dim={}", factTable.getTableName(), dimTable.getTableName(), e);
            return null;
        }
    }

    private String guessFactColumn(LogicalTable factTable, LogicalTable dimTable) {
        // 简单启发式：dim_user -> user_id；dim_product -> product_id
        String dimPureName = dimTable.getPureTableName();
        if (dimPureName == null) {
            return null;
        }
        // 去掉前缀 dim_
        String base = dimPureName.startsWith("dim_") ? dimPureName.substring(4) : dimPureName;
        String candidate = base + "_id";
        // 检查事实表 schema 中是否有该字段
        if (factTable.getSchema() != null) {
            boolean exists = factTable.getSchema().stream()
                    .anyMatch(s -> s.getColumnName().equalsIgnoreCase(candidate));
            if (exists) {
                return candidate;
            }
        }
        // fallback：直接返回候选字段名，由后续 SQL 执行校验
        return candidate;
    }

    // ==================== 迁移报告 ====================

    @lombok.Data
    @lombok.Accessors(chain = true)
    public static class MigrationReport {
        private int dimTables = 0;
        private int factTables = 0;
        private int relationships = 0;

        public void incrementDimTables() {
            this.dimTables++;
        }

        public void incrementFactTables() {
            this.factTables++;
        }

        public void incrementRelationships() {
            this.relationships++;
        }

        @Override
        public String toString() {
            return "MigrationReport{dimTables=" + dimTables + ", factTables=" + factTables + ", relationships=" + relationships + "}";
        }
    }
}
