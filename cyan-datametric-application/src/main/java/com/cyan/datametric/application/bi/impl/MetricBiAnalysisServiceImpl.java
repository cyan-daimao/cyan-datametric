package com.cyan.datametric.application.bi.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.datagateway.client.SqlGatewayClient;
import com.cyan.datagateway.client.cmd.SqlExecuteCmd;
import com.cyan.datagateway.client.dto.SqlExecuteResultDTO;
import com.cyan.datametric.application.bi.MetricBiAnalysisService;
import com.cyan.datametric.application.bi.MetricBiErrorCode;
import com.cyan.datametric.application.bi.MetricResolver;
import com.cyan.datametric.application.bi.MetricSqlBuilder;
import com.cyan.datametric.application.bi.ResolvedMetric;
import com.cyan.datametric.application.bi.TableConsistencyChecker;
import com.cyan.datametric.application.bi.bo.BiDimensionBO;
import com.cyan.datametric.application.bi.bo.BiMetricBO;
import com.cyan.datametric.application.bi.bo.ChartDataBO;
import com.cyan.datametric.application.bi.bo.DimensionValueBO;
import com.cyan.datametric.application.bi.convert.MetricBiAnalysisAppConvert;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
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
    private final MetricBiAnalysisAppConvert metricBiAnalysisAppConvert;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    @Override
    public ChartDataBO execute(MetricBiAnalysisCmd cmd, String executor) {
        long startTime = System.currentTimeMillis();
        String sql = previewSql(cmd);

        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(sql)
                .setPassport(executor);

        com.cyan.arch.common.api.Response<SqlExecuteResultDTO> response =
                sqlGatewayClient.executeStarRocksSql(executeCmd);

        long costTimeMs = System.currentTimeMillis() - startTime;

        String chartType = cmd.getChartType();

        if (response == null || response.getCode() != 200 || response.getData() == null) {
            return metricBiAnalysisAppConvert.toChartDataBO(
                    "FAILED", costTimeMs, new ArrayList<>(), new ArrayList<>(), sql,
                    chartType, response != null ? response.getMessage() : "执行结果为空");
        }

        SqlExecuteResultDTO result = response.getData();
        List<String> columns;
        List<Map<String, Object>> rows;

        if (result.getData() != null && !result.getData().isEmpty()) {
            rows = result.getData();
            columns = new ArrayList<>(result.getData().getFirst().keySet());
        } else {
            columns = new ArrayList<>();
            rows = new ArrayList<>();
        }

        return metricBiAnalysisAppConvert.toChartDataBO(
                result.getStatus(),
                result.getCostTimeMs() != null ? result.getCostTimeMs() : costTimeMs,
                columns, rows, sql, chartType, result.getErrorMessage());
    }

    @Override
    public String previewSql(MetricBiAnalysisCmd cmd) {
        List<ResolvedMetric> resolvedMetrics = metricResolver.resolve(cmd.getMetrics());
        tableConsistencyChecker.check(resolvedMetrics);
        String tableName = tableConsistencyChecker.getUnifiedTableName(resolvedMetrics);
        return metricSqlBuilder.build(cmd, resolvedMetrics, tableName);
    }

    @Override
    public List<BiMetricBO> listMetrics(String name, String subjectCode, String metricType) {
        MetricPageQuery query = new MetricPageQuery();
        query.setPageNum(1);
        query.setPageSize(10000);
        query.setMetricName(name);
        query.setSubjectCode(subjectCode);
        query.setMetricType(metricType);

        Page<com.cyan.datametric.domain.metric.Metric> page = metricRepository.page(query);

        List<String> subjectCodes = page.getData().stream()
                .map(com.cyan.datametric.domain.metric.Metric::getSubjectCode)
                .filter(sc -> sc != null && !sc.isBlank())
                .distinct()
                .toList();

        Map<String, String> subjectNameMap = new HashMap<>();
        if (!subjectCodes.isEmpty()) {
            List<MetricSubject> subjects = metricSubjectRepository.findBySubjectCodes(subjectCodes);
            for (MetricSubject s : subjects) {
                if (s != null && s.getSubjectCode() != null) {
                    subjectNameMap.put(s.getSubjectCode(), s.getSubjectName());
                }
            }
        }

        return page.getData().stream()
                .map(m -> metricBiAnalysisAppConvert.toBiMetricBO(m, subjectNameMap.get(m.getSubjectCode())))
                .toList();
    }

    @Override
    public List<BiDimensionBO> listDimensions(String name, String categoryId) {
        DimensionPageQuery query = new DimensionPageQuery();
        query.setPageNum(1);
        query.setPageSize(10000);
        query.setDimName(name);
        query.setCategoryId(categoryId);

        Page<Dimension> page = dimensionRepository.page(query);

        List<String> categoryIds = page.getData().stream()
                .map(Dimension::getCategoryId)
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
                .map(d -> metricBiAnalysisAppConvert.toBiDimensionBO(d, categoryNameMap.get(d.getCategoryId())))
                .toList();
    }

    @Override
    public List<DimensionValueBO> listDimensionValues(String dimCode) {
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
                    return metricBiAnalysisAppConvert.toDimensionValueBO(
                            value != null ? value.toString() : null,
                            label != null ? label.toString() : null);
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
