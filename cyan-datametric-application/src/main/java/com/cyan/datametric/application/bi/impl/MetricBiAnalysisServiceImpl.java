package com.cyan.datametric.application.bi.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.Response;
import com.cyan.dataauth.client.AuthCheckClient;
import com.cyan.dataauth.client.AuthMetricClient;
import com.cyan.dataauth.cmd.MetricCheckCmd;
import com.cyan.dataauth.cmd.MetricFilterSqlCmd;
import com.cyan.dataauth.dto.MetricCheckResult;
import com.cyan.dataauth.dto.MetricResourceDTO;
import com.cyan.dataauth.dto.UserSecurityLevelDTO;
import com.cyan.dataauth.enums.SecurityLevel;
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
import com.cyan.datametric.domain.config.BuiltinTimeDimension;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private final AuthMetricClient authMetricClient;
    private final AuthCheckClient authCheckClient;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    @Override
    public ChartDataBO execute(MetricBiAnalysisCmd cmd, String executor) {
        long startTime = System.currentTimeMillis();

        // 1. 提取指标编码和维度编码
        List<String> metricCodes = extractMetricCodes(cmd);
        List<String> dimCodes = extractDimCodes(cmd);

        // 2. 权限校验（USE）
        List<MetricCheckCmd.CheckItem> checkItems = buildCheckItems(metricCodes, dimCodes, "USE");
        MetricCheckCmd metricCheckCmd = new MetricCheckCmd(executor, checkItems);
        Response<MetricCheckResult> checkResp = authMetricClient.metricCheck(metricCheckCmd);
        if (checkResp == null || checkResp.getData() == null || !checkResp.getData().isAllPermitted()) {
            throw new BusinessException("无权限使用指定指标或维度");
        }

        // 3. 填充 dimName / metricName 到 DSL（供前端统一显示）
        enrichDimMetricNames(cmd);

        // 4. 生成 SQL
        String sql = previewSql(cmd);

        // 5. SQL 改写（指标层行过滤）
        MetricFilterSqlCmd filterCmd = new MetricFilterSqlCmd(executor, sql, metricCodes, dimCodes, null);
        Response<com.cyan.dataauth.dto.MetricFilterSqlResult> filterResp = authMetricClient.metricFilterSql(filterCmd);
        if (filterResp == null || filterResp.getData() == null) {
            throw new BusinessException("SQL 权限校验失败");
        }
        com.cyan.dataauth.dto.MetricFilterSqlResult filterResult = filterResp.getData();
        if (!filterResult.isPermitted()) {
            throw new BusinessException(filterResult.getReason() != null ? filterResult.getReason() : "SQL 权限校验未通过");
        }
        String rewrittenSql = filterResult.getRewrittenSql();

        // 6. 执行 SQL
        SqlExecuteCmd executeCmd = new SqlExecuteCmd()
                .setSql(rewrittenSql)
                .setPassport(executor);

        com.cyan.arch.common.api.Response<SqlExecuteResultDTO> response =
                sqlGatewayClient.executeMetricSql(executeCmd);

        long costTimeMs = System.currentTimeMillis() - startTime;
        String chartType = cmd.getChartType();

        if (response == null || response.getCode() != 200 || response.getData() == null) {
            return metricBiAnalysisAppConvert.toChartDataBO(
                    "FAILED", costTimeMs, new ArrayList<>(), new ArrayList<>(), rewrittenSql,
                    chartType, cmd, response != null ? response.getMessage() : "执行结果为空");
        }

        SqlExecuteResultDTO result = response.getData();
        List<String> columns;
        List<Map<String, Object>> rows;

        if (result.getData() != null && !result.getData().isEmpty()) {
            rows = result.getData();
            columns = new ArrayList<>(result.getData().get(0).keySet());
        } else {
            columns = new ArrayList<>();
            rows = new ArrayList<>();
        }

        return metricBiAnalysisAppConvert.toChartDataBO(
                result.getStatus(),
                result.getCostTimeMs() != null ? result.getCostTimeMs() : costTimeMs,
                columns, rows, rewrittenSql, chartType, cmd, result.getErrorMessage());
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

        List<BiMetricBO> allMetrics = page.getData().stream()
                .map(m -> metricBiAnalysisAppConvert.toBiMetricBO(m, subjectNameMap.get(m.getSubjectCode())))
                .toList();

        // 按密级过滤：L1 默认所有人可见，L2~L4 按用户 maxSecurityLevel 过滤
        String passport = getCurrentPassport();
        String userMaxLevel = getUserMaxSecurityLevel(passport);
        return allMetrics.stream()
                .filter(m -> canAccess(m.getSecurityLevel(), userMaxLevel))
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

        List<BiDimensionBO> allDimensions = page.getData().stream()
                .map(d -> metricBiAnalysisAppConvert.toBiDimensionBO(d, categoryNameMap.get(d.getCategoryId())))
                .filter(d -> {
                    // name 参数对内置时间维度也生效（Repository 层未对插入的内置维度做过滤）
                    if (StringUtils.hasText(name) && d != null) {
                        return d.getDimName() != null && d.getDimName().contains(name);
                    }
                    return true;
                })
                .toList();

        // 分离内置时间维度（不参与 dataauth 权限过滤）
        List<BiDimensionBO> builtinDimensions = new ArrayList<>();
        List<BiDimensionBO> dbDimensions = new ArrayList<>();
        for (BiDimensionBO d : allDimensions) {
            if (d != null && BuiltinTimeDimension.of(d.getDimCode()) != null) {
                builtinDimensions.add(d);
            } else {
                dbDimensions.add(d);
            }
        }

        // 调用 dataauth 过滤无 VIEW 权限的数据库维度
        String passport = getCurrentPassport();
        if (passport == null) {
            log.warn("listDimensions 无法获取当前用户上下文，跳过权限过滤");
            List<BiDimensionBO> result = new ArrayList<>(dbDimensions);
            result.addAll(builtinDimensions);
            return result;
        }

        Response<List<MetricResourceDTO>> permittedResp =
                authMetricClient.listMetrics(passport, "DIMENSION", null, null);
        if (permittedResp == null || permittedResp.getData() == null) {
            log.warn("listDimensions 获取权限列表失败，降级返回全量维度");
            List<BiDimensionBO> result = new ArrayList<>(dbDimensions);
            result.addAll(builtinDimensions);
            return result;
        }

        List<String> permittedCodes = permittedResp.getData().stream()
                .map(MetricResourceDTO::getCode)
                .filter(Objects::nonNull)
                .toList();
        if (permittedCodes.isEmpty()) {
            // 有权限接口但无任何权限，降级返回全量维度（不局限于内置维度）
            List<BiDimensionBO> result = new ArrayList<>(dbDimensions);
            result.addAll(builtinDimensions);
            return result;
        }

        Set<String> permittedSet = new HashSet<>(permittedCodes);
        List<BiDimensionBO> result = dbDimensions.stream()
                .filter(d -> d.getDimCode() != null && permittedSet.contains(d.getDimCode()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        result.addAll(builtinDimensions);
        return result;
    }

    @Override
    public List<DimensionValueBO> listDimensionValues(String dimCode) {
        // 校验维度值查询权限（VALUES）
        String passport = getCurrentPassport();
        if (passport != null) {
            List<MetricCheckCmd.CheckItem> checkItems = List.of(
                    new MetricCheckCmd.CheckItem("DIMENSION", dimCode, "VALUES"));
            MetricCheckCmd metricCheckCmd = new MetricCheckCmd(passport, checkItems);
            Response<MetricCheckResult> checkResp = authMetricClient.metricCheck(metricCheckCmd);
            if (checkResp != null && checkResp.getData() != null && !checkResp.getData().isAllPermitted()) {
                log.warn("用户无维度值查询权限: passport={}, dimCode={}", passport, dimCode);
                return List.of();
            }
        } else {
            log.warn("listDimensionValues 无法获取当前用户上下文，跳过权限校验");
        }

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
                sqlGatewayClient.executeMetricSql(executeCmd);

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

    private String getCurrentPassport() {
        EmployeeDTO employee = UserContextHolder.getCurrentEmployee();
        return employee != null ? employee.getPassport() : null;
    }

    /**
     * 获取用户最高可访问密级，降级为 L1
     */
    private String getUserMaxSecurityLevel(String passport) {
        if (passport == null) {
            return "L1";
        }
        try {
            Response<UserSecurityLevelDTO> resp = authCheckClient.getUserMaxSecurityLevel(passport);
            if (resp != null && resp.getData() != null && resp.getData().getMaxSecurityLevel() != null) {
                return resp.getData().getMaxSecurityLevel();
            }
        } catch (Exception e) {
            log.warn("获取用户密级失败，降级为 L1, passport={}", passport, e);
        }
        return "L1";
    }

    /**
     * 判断用户是否可以访问目标密级的数据
     */
    private boolean canAccess(String metricSecurityLevel, String userMaxLevel) {
        if (metricSecurityLevel == null || "L1".equalsIgnoreCase(metricSecurityLevel)) {
            return true;
        }
        SecurityLevel userLevel = SecurityLevel.of(userMaxLevel);
        SecurityLevel metricLevel = SecurityLevel.of(metricSecurityLevel);
        if (userLevel == null) {
            return false;
        }
        return userLevel.permits(metricLevel);
    }

    private List<String> extractMetricCodes(MetricBiAnalysisCmd cmd) {
        if (cmd.getMetrics() == null) {
            return List.of();
        }
        return cmd.getMetrics().stream()
                .map(MetricBiAnalysisCmd.MetricRef::getMetricCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<String> extractDimCodes(MetricBiAnalysisCmd cmd) {
        Set<String> dimCodes = new HashSet<>();
        if (cmd.getDimensions() != null) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                if (ref.getDimCode() != null) {
                    dimCodes.add(ref.getDimCode());
                }
            }
        }
        if (cmd.getFilters() != null) {
            for (MetricBiAnalysisCmd.FilterRef ref : cmd.getFilters()) {
                if (ref.getDimCode() != null) {
                    dimCodes.add(ref.getDimCode());
                }
            }
        }
        if (cmd.getOrders() != null) {
            for (MetricBiAnalysisCmd.OrderRef ref : cmd.getOrders()) {
                if (ref.getDimCode() != null) {
                    dimCodes.add(ref.getDimCode());
                }
            }
        }
        return new ArrayList<>(dimCodes);
    }

    /**
     * 根据编码查询并填充 dimName / metricName 到 DSL
     */
    private void enrichDimMetricNames(MetricBiAnalysisCmd cmd) {
        if (cmd.getMetrics() != null) {
            for (MetricBiAnalysisCmd.MetricRef ref : cmd.getMetrics()) {
                if (ref.getMetricCode() != null && !StringUtils.hasText(ref.getMetricName())) {
                    com.cyan.datametric.domain.metric.Metric metric = metricRepository.findByMetricCode(ref.getMetricCode());
                    if (metric != null) {
                        ref.setMetricName(metric.getMetricName());
                    }
                }
            }
        }
        if (cmd.getDimensions() != null) {
            for (MetricBiAnalysisCmd.DimensionRef ref : cmd.getDimensions()) {
                if (ref.getDimCode() != null && !StringUtils.hasText(ref.getDimName())) {
                    Dimension dim = dimensionRepository.findByDimCode(ref.getDimCode());
                    if (dim != null) {
                        ref.setDimName(dim.getDimName());
                    }
                }
            }
        }
    }

    private List<MetricCheckCmd.CheckItem> buildCheckItems(List<String> metricCodes, List<String> dimCodes, String action) {
        List<MetricCheckCmd.CheckItem> items = new ArrayList<>();
        for (String code : metricCodes) {
            items.add(new MetricCheckCmd.CheckItem("METRIC", code, action));
        }
        for (String code : dimCodes) {
            items.add(new MetricCheckCmd.CheckItem("DIMENSION", code, action));
        }
        return items;
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
