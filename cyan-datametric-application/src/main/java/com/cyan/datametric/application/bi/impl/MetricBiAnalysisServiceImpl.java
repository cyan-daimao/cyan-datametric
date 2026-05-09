package com.cyan.datametric.application.bi.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiDimensionDTO;
import com.cyan.datametric.adapter.bi.http.dto.BiMetricDTO;
import com.cyan.datametric.adapter.bi.http.dto.ChartDataDTO;
import com.cyan.datametric.adapter.bi.http.dto.DimensionValueDTO;
import com.cyan.datametric.adapter.bi.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.application.bi.MetricBiAnalysisService;
import com.cyan.datametric.application.bi.MetricBiErrorCode;
import com.cyan.datametric.application.bi.MetricResolver;
import com.cyan.datametric.application.bi.MetricSqlBuilder;
import com.cyan.datametric.application.bi.ResolvedMetric;
import com.cyan.datametric.application.bi.TableConsistencyChecker;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标BI分析服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricBiAnalysisServiceImpl implements MetricBiAnalysisService {

    private final MetricResolver metricResolver;
    private final TableConsistencyChecker tableConsistencyChecker;
    private final MetricSqlBuilder metricSqlBuilder;
    private final MetricRepository metricRepository;
    private final DimensionRepository dimensionRepository;
    private final DimensionCategoryRepository dimensionCategoryRepository;
    private final MetricSubjectRepository metricSubjectRepository;
    private final SqlGatewayClient sqlGatewayClient;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    @Override
    public ChartDataDTO execute(MetricBiAnalysisCmd cmd, String executor) {
        long startTime = System.currentTimeMillis();
        String sql = previewSql(cmd);

        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(sql)
                .setPassport(executor);

        com.cyan.arch.common.api.Response<SqlExecuteResultDTO> response =
                sqlGatewayClient.executeStarRocksSql(executeCmd);

        ChartDataDTO dto = new ChartDataDTO();
        dto.setSql(sql);
        dto.setCostTimeMs(System.currentTimeMillis() - startTime);

        if (response == null || response.getCode() != 200 || response.getData() == null) {
            dto.setStatus("FAILED");
            dto.setErrorMessage(response != null ? response.getMessage() : "执行结果为空");
            return dto;
        }

        SqlExecuteResultDTO result = response.getData();
        dto.setStatus(result.getStatus());
        // costTimeMs 保持为总后端耗时（SQL生成 + 网关执行）
        dto.setErrorMessage(result.getErrorMessage());

        if (result.getData() != null && !result.getData().isEmpty()) {
            dto.setRows(result.getData());
            // 从第一行数据提取列名
            dto.setColumns(new ArrayList<>(result.getData().getFirst().keySet()));
        } else {
            dto.setColumns(new ArrayList<>());
            dto.setRows(new ArrayList<>());
        }

        return dto;
    }

    @Override
    public String previewSql(MetricBiAnalysisCmd cmd) {
        // 1. 展开指标
        List<ResolvedMetric> resolvedMetrics = metricResolver.resolve(cmd.getMetrics());

        // 2. 检查表一致性
        tableConsistencyChecker.check(resolvedMetrics);
        String tableName = tableConsistencyChecker.getUnifiedTableName(resolvedMetrics);

        // 3. 生成SQL
        return metricSqlBuilder.build(cmd, resolvedMetrics, tableName);
    }

    @Override
    public List<BiMetricDTO> listMetrics(String name, String subjectCode, String metricType) {
        MetricPageQuery query = new MetricPageQuery();
        query.setPageNum(1);
        query.setPageSize(10000);
        query.setMetricName(name);
        query.setSubjectCode(subjectCode);
        query.setMetricType(metricType);

        Page<com.cyan.datametric.domain.metric.Metric> page = metricRepository.page(query);

        // 加载主题域名称
        List<String> subjectCodes = page.getData().stream()
                .map(com.cyan.datametric.domain.metric.Metric::getSubjectCode)
                .filter(sc -> sc != null && !sc.isBlank())
                .distinct()
                .toList();

        Map<String, String> subjectNameMap;
        if (!subjectCodes.isEmpty()) {
            List<MetricSubject> subjects = metricSubjectRepository.findBySubjectCodes(subjectCodes);
            subjectNameMap = subjects.stream()
                    .filter(s -> s.getSubjectCode() != null)
                    .collect(Collectors.toMap(MetricSubject::getSubjectCode, MetricSubject::getSubjectName, (a, b) -> a));
        } else {
            subjectNameMap = Map.of();
        }

        return page.getData().stream()
                .map(m -> {
                    BiMetricDTO dto = new BiMetricDTO();
                    dto.setId(m.getId());
                    dto.setMetricCode(m.getMetricCode());
                    dto.setMetricName(m.getMetricName());
                    dto.setMetricType(m.getMetricType() == null ? null : m.getMetricType().getCode());
                    dto.setSubjectCode(m.getSubjectCode());
                    dto.setSubjectName(subjectNameMap.get(m.getSubjectCode()));
                    if (m.getAtomicExt() != null) {
                        dto.setStatFunc(m.getAtomicExt().getStatFunc() == null ? null : m.getAtomicExt().getStatFunc().getCode());
                        // dataType 当前数据模型未存储，留空
                        dto.setDataType(null);
                    }
                    dto.setDescription(m.getBizCaliber());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<BiDimensionDTO> listDimensions(String name, String categoryId) {
        DimensionPageQuery query = new DimensionPageQuery();
        query.setPageNum(1);
        query.setPageSize(10000);
        query.setDimName(name);
        query.setCategoryId(categoryId);

        Page<com.cyan.datametric.domain.config.Dimension> page = dimensionRepository.page(query);

        // 批量加载分类名称
        List<String> categoryIds = page.getData().stream()
                .map(com.cyan.datametric.domain.config.Dimension::getCategoryId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Map<String, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (String id : categoryIds) {
                DimensionCategory cat = dimensionCategoryRepository.findById(id);
                if (cat != null && cat.getName() != null) {
                    categoryNameMap.put(id, cat.getName());
                }
            }
        }

        return page.getData().stream()
                .map(d -> {
                    BiDimensionDTO dto = new BiDimensionDTO();
                    dto.setId(d.getId());
                    dto.setDimCode(d.getDimCode());
                    dto.setDimName(d.getDimName());
                    dto.setDimType(d.getDimType());
                    dto.setDataType(d.getDataType());
                    dto.setTableName(d.getTableName());
                    dto.setColumnName(d.getColumnName());
                    dto.setDisplayColumn(d.getDisplayColumn());
                    dto.setCategoryName(categoryNameMap.get(d.getCategoryId()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<DimensionValueDTO> listDimensionValues(String dimCode) {
        Dimension dimension = dimensionRepository.findByDimCode(dimCode);
        Assert.notNull(dimension, new BusinessException(MetricBiErrorCode.DIMENSION_NOT_FOUND.getMessage()));

        String tableRef = buildDimensionTableRef(dimension.getSchemaName(), dimension.getTableName());
        String columnName = dimension.getColumnName();
        String displayColumn = dimension.getDisplayColumn();

        Assert.notBlank(columnName, new BusinessException("维度 '" + dimCode + "' 未配置物理字段"));
        Assert.notBlank(tableRef, new BusinessException("维度 '" + dimCode + "' 未配置维表"));

        String sql;
        if (StringUtils.hasText(displayColumn) && !displayColumn.equals(columnName)) {
            sql = "SELECT DISTINCT `" + columnName + "` AS `value`, `" + displayColumn + "` AS `label` FROM " + tableRef + " LIMIT 1000";
        } else {
            sql = "SELECT DISTINCT `" + columnName + "` AS `value`, `" + columnName + "` AS `label` FROM " + tableRef + " LIMIT 1000";
        }

        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(sql)
                .setPassport("system");

        com.cyan.arch.common.api.Response<SqlExecuteResultDTO> response =
                sqlGatewayClient.executeStarRocksSql(executeCmd);

        if (response == null || response.getCode() != 200 || response.getData() == null || response.getData().getData() == null) {
            log.warn("查询维度值失败: dimCode={}, message={}", dimCode, response != null ? response.getMessage() : "null");
            return List.of();
        }

        return response.getData().getData().stream()
                .map(row -> {
                    Object value = row.get("value");
                    Object label = row.get("label");
                    return new DimensionValueDTO()
                            .setValue(value != null ? value.toString() : null)
                            .setLabel(label != null ? label.toString() : null);
                })
                .filter(d -> d.getValue() != null)
                .toList();
    }

    private String buildDimensionTableRef(String schema, String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return null;
        }
        if (tableName.contains(".")) {
            return normalizeTableRef(tableName);
        }
        if (StringUtils.hasText(schema)) {
            return normalizeTableRef(schema + "." + tableName);
        }
        return normalizeTableRef(tableName);
    }

    private String normalizeTableRef(String tableRef) {
        if (!StringUtils.hasText(tableRef)) {
            return tableRef;
        }
        String[] parts = tableRef.split("\\.");
        if (parts.length == 2) {
            return defaultCatalog + "." + tableRef;
        }
        return tableRef;
    }
}
