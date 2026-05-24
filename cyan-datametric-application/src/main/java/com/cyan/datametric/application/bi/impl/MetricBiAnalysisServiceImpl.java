package com.cyan.datametric.application.bi.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.arch.common.api.Response;
import com.cyan.dataauth.client.AuthMetricClient;
import com.cyan.dataauth.cmd.MetricCheckCmd;
import com.cyan.dataauth.cmd.MetricFilterSqlCmd;
import com.cyan.dataauth.dto.MetricCheckResult;
import com.cyan.dataauth.dto.MetricResourceDTO;
import com.cyan.dataauth.dto.UserSecurityLevelDTO;
import com.cyan.dataauth.enums.SecurityLevel;
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
import com.cyan.datametric.application.bi.bo.MetricAssociationGraphBO;
import com.cyan.datametric.application.bi.bo.MetricAssociationSearchBO;
import com.cyan.datametric.application.bi.convert.MetricBiAnalysisAppConvert;
import com.cyan.datametric.application.bi.query.MetricAssociationSearchQuery;
import com.cyan.datametric.client.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.domain.config.BuiltinTimeDimension;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import com.cyan.datametric.infra.gateway.AuthCheckGateway;
import com.cyan.datametric.infra.gateway.SqlGateway;
import com.cyan.datametric.infra.gateway.TableRelationGateway;
import com.cyan.dataman.client.table.dto.JoinPathsRequestDTO;
import com.cyan.dataman.client.table.dto.TableRelationDTO;
import com.cyan.employee.client.dto.EmployeeDTO;
import com.cyan.employee.login.filter.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;

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
    private final SqlGateway sqlGateway;
    private final MetricBiAnalysisAppConvert metricBiAnalysisAppConvert;
    private final AuthMetricClient authMetricClient;
    private final AuthCheckGateway authCheckGateway;
    private final TableRelationGateway tableRelationGateway;

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
                sqlGateway.executeMetricSql(executeCmd);

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
                .map(this::loadMetricExt)
                .filter(Objects::nonNull)
                .map(m -> metricBiAnalysisAppConvert.toBiMetricBO(m, subjectNameMap.get(m.getSubjectCode()))
                        .setTableRef(primaryTableRef(resolveMetricContext(m))))
                .toList();

        // 过滤已下线指标
        List<BiMetricBO> onlineMetrics = allMetrics.stream()
                .filter(m -> m.getStatus() != com.cyan.datametric.enums.MetricStatus.OFFLINE)
                .toList();

        // 按密级过滤：L1 默认所有人可见，L2~L4 按用户 maxSecurityLevel 过滤
        String passport = getCurrentPassport();
        String userMaxLevel = getUserMaxSecurityLevel(passport);
        return onlineMetrics.stream()
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
                sqlGateway.executeMetricSql(executeCmd);

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

    @Override
    public MetricAssociationSearchBO searchAssociations(MetricAssociationSearchQuery query) {
        MetricAssociationSearchQuery safeQuery = query == null ? new MetricAssociationSearchQuery() : query;
        boolean includeSelected = Boolean.TRUE.equals(safeQuery.getIncludeSelected());
        Map<String, AssociationRelation> relationCache = new HashMap<>();

        List<MetricContext> selectedMetrics = normalizeCodes(safeQuery.getMetricCodes()).stream()
                .map(this::resolveMetricContextByCode)
                .filter(Objects::nonNull)
                .toList();
        List<DimensionContext> selectedDimensions = normalizeCodes(safeQuery.getDimCodes()).stream()
                .map(this::resolveDimensionContextByCode)
                .filter(Objects::nonNull)
                .toList();
        Set<String> selectedMetricCodes = selectedMetrics.stream()
                .map(MetricContext::metricCode)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> selectedDimCodes = selectedDimensions.stream()
                .map(DimensionContext::dimCode)
                .collect(java.util.stream.Collectors.toSet());

        List<BiMetricBO> metrics = listMetrics(
                trimToNull(safeQuery.getMetricName()),
                trimToNull(safeQuery.getSubjectCode()),
                trimToNull(safeQuery.getMetricType())).stream()
                .filter(m -> includeSelected || !selectedMetricCodes.contains(m.getMetricCode()))
                .filter(m -> {
                    MetricContext candidate = resolveMetricContextByCode(m.getMetricCode());
                    if (candidate == null) {
                        return false;
                    }
                    List<MetricContext> nextMetrics = new ArrayList<>(selectedMetrics);
                    nextMetrics.add(candidate);
                    return isAssociationSetValid(nextMetrics, selectedDimensions, relationCache);
                })
                .toList();

        List<BiMetricBO> allAccessibleMetrics = null;
        List<BiDimensionBO> dimensions = listDimensions(
                trimToNull(safeQuery.getDimName()),
                trimToNull(safeQuery.getCategoryId())).stream()
                .filter(d -> includeSelected || !selectedDimCodes.contains(d.getDimCode()))
                .filter(d -> {
                    DimensionContext candidate = resolveDimensionContextByCode(d.getDimCode());
                    if (candidate == null) {
                        return false;
                    }
                    List<DimensionContext> nextDimensions = new ArrayList<>(selectedDimensions);
                    nextDimensions.add(candidate);
                    if (!selectedMetrics.isEmpty()) {
                        return isAssociationSetValid(selectedMetrics, nextDimensions, relationCache);
                    }
                    if (!selectedDimensions.isEmpty()) {
                        return hasAnyMetricForDimensions(nextDimensions, relationCache);
                    }
                    return true;
                })
                .toList();

        return new MetricAssociationSearchBO()
                .setMetrics(metrics)
                .setDimensions(dimensions);
    }

    @Override
    public MetricAssociationGraphBO associationGraph(String metricCode) {
        MetricContext centerMetric = resolveMetricContextByCode(metricCode);
        Assert.notNull(centerMetric, new BusinessException("指标不存在或不可用于关联图谱"));

        MetricAssociationSearchBO searchBO = searchAssociations(new MetricAssociationSearchQuery()
                .setMetricCodes(List.of(centerMetric.metricCode()))
                .setIncludeSelected(false));
        Map<String, AssociationRelation> relationCache = new HashMap<>();
        Map<String, MetricAssociationGraphBO.Node> nodeMap = new LinkedHashMap<>();
        List<MetricAssociationGraphBO.Edge> edges = new ArrayList<>();

        MetricAssociationGraphBO.Node center = toGraphMetricNode(centerMetric);
        nodeMap.put(center.getId(), center);

        for (BiMetricBO metricBO : searchBO.getMetrics()) {
            MetricContext metric = resolveMetricContextByCode(metricBO.getMetricCode());
            if (metric == null) {
                continue;
            }
            MetricAssociationGraphBO.Node node = toGraphMetricNode(metric);
            nodeMap.put(node.getId(), node);
            edges.add(new MetricAssociationGraphBO.Edge()
                    .setSource(center.getId())
                    .setTarget(node.getId())
                    .setRelationType("SAME_FACT")
                    .setSourceTable(primaryTableRef(centerMetric))
                    .setTargetTable(primaryTableRef(metric))
                    .setDescription("同事实表指标，可直接共同查询"));
        }

        for (BiDimensionBO dimensionBO : searchBO.getDimensions()) {
            DimensionContext dimension = resolveDimensionContextByCode(dimensionBO.getDimCode());
            if (dimension == null) {
                continue;
            }
            MetricAssociationGraphBO.Node node = toGraphDimensionNode(dimension);
            nodeMap.put(node.getId(), node);
            AssociationRelation relation = relationBetweenMetricAndDimension(centerMetric, dimension, relationCache);
            edges.add(new MetricAssociationGraphBO.Edge()
                    .setSource(center.getId())
                    .setTarget(node.getId())
                    .setRelationType(relation.relationType())
                    .setJoinType(relation.joinType())
                    .setSourceColumn(relation.sourceColumn())
                    .setTargetColumn(relation.targetColumn())
                    .setSourceTable(relation.sourceTable())
                    .setTargetTable(relation.targetTable())
                    .setDescription(relation.description()));
        }

        return new MetricAssociationGraphBO()
                .setCenter(center)
                .setNodes(new ArrayList<>(nodeMap.values()))
                .setEdges(edges);
    }

    private boolean hasAnyMetricForDimensions(List<DimensionContext> dimensions,
                                               Map<String, AssociationRelation> relationCache) {
        return listMetrics(null, null, null).stream()
                .map(m -> resolveMetricContextByCode(m.getMetricCode()))
                .filter(Objects::nonNull)
                .anyMatch(metric -> isAssociationSetValid(List.of(metric), dimensions, relationCache));
    }

    private boolean isAssociationSetValid(List<MetricContext> metrics,
                                          List<DimensionContext> dimensions,
                                          Map<String, AssociationRelation> relationCache) {
        if (metrics == null || metrics.isEmpty()) {
            return true;
        }
        Set<String> factTables = metrics.stream()
                .flatMap(m -> m.tableRefs().stream())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (factTables.isEmpty()) {
            return false;
        }
        if (dimensions == null || dimensions.isEmpty()) {
            return factTables.size() <= 1;
        }
        for (MetricContext metric : metrics) {
            for (DimensionContext dimension : dimensions) {
                AssociationRelation relation = relationBetweenMetricAndDimension(metric, dimension, relationCache);
                if (!relation.associated()) {
                    return false;
                }
            }
        }
        return true;
    }

    private AssociationRelation relationBetweenMetricAndDimension(MetricContext metric,
                                                                  DimensionContext dimension,
                                                                  Map<String, AssociationRelation> relationCache) {
        if (dimension.builtin()) {
            return AssociationRelation.associated("BUILTIN_TIME", primaryTableRef(metric), null,
                    null, null, null, "内置时间维度，可用于所有事实表");
        }

        if (StringUtils.hasText(dimension.sourceTableRef())) {
            boolean allMatched = metric.tableRefs().stream().allMatch(dimension.sourceTableRef()::equals);
            return allMatched
                    ? AssociationRelation.associated("SOURCE_TABLE", dimension.sourceTableRef(), dimension.sourceTableRef(),
                    null, dimension.columnName(), null, "事实表本地维度")
                    : AssociationRelation.notAssociated();
        }

        if (!StringUtils.hasText(dimension.tableRef())) {
            return AssociationRelation.associated("LOCAL_EXPRESSION", primaryTableRef(metric), null,
                    null, dimension.columnName(), null, "事实表本地表达式维度");
        }

        AssociationRelation firstRelation = null;
        for (String factTable : metric.tableRefs()) {
            if (factTable.equals(dimension.tableRef())) {
                firstRelation = AssociationRelation.associated("SAME_FACT", factTable, dimension.tableRef(),
                        null, dimension.columnName(), null, "维度字段与指标位于同一事实表");
                continue;
            }
            AssociationRelation relation = queryJoinRelation(factTable, dimension.tableRef(), relationCache);
            if (!relation.associated()) {
                return AssociationRelation.notAssociated();
            }
            if (firstRelation == null) {
                firstRelation = relation;
            }
        }
        return firstRelation != null ? firstRelation : AssociationRelation.notAssociated();
    }

    private AssociationRelation queryJoinRelation(String factTableRef,
                                                  String dimTableRef,
                                                  Map<String, AssociationRelation> relationCache) {
        String cacheKey = factTableRef + "->" + dimTableRef;
        if (relationCache.containsKey(cacheKey)) {
            return relationCache.get(cacheKey);
        }
        String[] factParts = factTableRef.split("\\.");
        String[] dimParts = dimTableRef.split("\\.");
        if (factParts.length != 3 || dimParts.length != 3) {
            AssociationRelation relation = AssociationRelation.notAssociated();
            relationCache.put(cacheKey, relation);
            return relation;
        }
        JoinPathsRequestDTO request = new JoinPathsRequestDTO()
                .setFactTable(new JoinPathsRequestDTO.TableRefDTO(factParts[0], factParts[1], factParts[2]))
                .setDimensionTables(List.of(new JoinPathsRequestDTO.TableRefDTO(dimParts[0], dimParts[1], dimParts[2])));
        try {
            Response<List<TableRelationDTO>> response = tableRelationGateway.findJoinPaths(request);
            TableRelationDTO join = response == null || response.getData() == null
                    ? null
                    : response.getData().stream().findFirst().orElse(null);
            AssociationRelation relation = join == null
                    ? AssociationRelation.notAssociated()
                    : AssociationRelation.associated("JOIN", factTableRef, dimTableRef,
                    join.getSourceColumn(), join.getTargetColumn(), join.getJoinType(),
                    StringUtils.hasText(join.getDescription()) ? join.getDescription() : "通过元数据表关系可 JOIN");
            relationCache.put(cacheKey, relation);
            return relation;
        } catch (Exception e) {
            log.warn("查询指标维度可关联关系失败, factTableRef={}, dimTableRef={}", factTableRef, dimTableRef, e);
            AssociationRelation relation = AssociationRelation.notAssociated();
            relationCache.put(cacheKey, relation);
            return relation;
        }
    }

    private MetricContext resolveMetricContextByCode(String metricCode) {
        if (!StringUtils.hasText(metricCode)) {
            return null;
        }
        return resolveMetricContext(metricRepository.findByMetricCode(metricCode));
    }

    private MetricContext resolveMetricContext(Metric metric) {
        if (metric == null || metric.getMetricType() == null) {
            return null;
        }
        Set<String> tableRefs = new LinkedHashSet<>();
        collectMetricTableRefs(metric, tableRefs, new HashSet<>());
        if (tableRefs.isEmpty()) {
            return null;
        }
        return new MetricContext(metric.getId(), metric.getMetricCode(), metric.getMetricName(),
                metric.getMetricType().getCode(), tableRefs);
    }

    private void collectMetricTableRefs(Metric metric, Set<String> tableRefs, Set<String> visitedMetricIds) {
        if (metric == null || metric.getMetricType() == null || !StringUtils.hasText(metric.getId())) {
            return;
        }
        if (!visitedMetricIds.add(metric.getId())) {
            return;
        }
        switch (metric.getMetricType()) {
            case ATOMIC -> {
                if (metric.getAtomicExt() != null
                        && StringUtils.hasText(metric.getAtomicExt().getDbName())
                        && StringUtils.hasText(metric.getAtomicExt().getTblName())) {
                    tableRefs.add(normalizeTableRef(metric.getAtomicExt().getDbName()
                            + "." + metric.getAtomicExt().getTblName()));
                }
            }
            case DERIVED -> {
                if (metric.getDerivedExt() != null && StringUtils.hasText(metric.getDerivedExt().getAtomicMetricId())) {
                    collectMetricTableRefs(metricRepository.findById(metric.getDerivedExt().getAtomicMetricId()),
                            tableRefs, visitedMetricIds);
                }
            }
            case COMPOSITE -> {
                if (metric.getCompositeExt() != null && metric.getCompositeExt().getMetricRefs() != null) {
                    for (String metricId : metric.getCompositeExt().getMetricRefs()) {
                        collectMetricTableRefs(metricRepository.findById(metricId), tableRefs, visitedMetricIds);
                    }
                }
            }
        }
    }

    private DimensionContext resolveDimensionContextByCode(String dimCode) {
        if (!StringUtils.hasText(dimCode)) {
            return null;
        }
        BuiltinTimeDimension builtin = BuiltinTimeDimension.of(dimCode);
        if (builtin != null) {
            return new DimensionContext(dimCode, builtin.getDimName(), null, "dt", null, null, true);
        }
        Dimension dimension = dimensionRepository.findByDimCode(dimCode);
        if (dimension == null) {
            return null;
        }
        String tableRef = buildDimensionTableRef(dimension.getSchemaName(), dimension.getTableName());
        String sourceTableRef = StringUtils.hasText(dimension.getSourceTable())
                ? normalizeTableRef(dimension.getSourceTable())
                : null;
        return new DimensionContext(dimension.getDimCode(), dimension.getDimName(), tableRef,
                dimension.getColumnName(), dimension.getDisplayColumn(), sourceTableRef, false);
    }

    private Metric loadMetricExt(Metric metric) {
        if (metric == null || !StringUtils.hasText(metric.getId())) {
            return metric;
        }
        Metric loaded = metricRepository.findById(metric.getId());
        return loaded == null ? metric : loaded;
    }

    private String primaryTableRef(MetricContext metric) {
        if (metric == null || metric.tableRefs().isEmpty()) {
            return null;
        }
        return metric.tableRefs().iterator().next();
    }

    private MetricAssociationGraphBO.Node toGraphMetricNode(MetricContext metric) {
        return new MetricAssociationGraphBO.Node()
                .setId("M:" + metric.metricCode())
                .setCode(metric.metricCode())
                .setName(metric.metricName())
                .setNodeType("METRIC")
                .setMetricType(metric.metricType())
                .setTableRef(String.join(",", metric.tableRefs()));
    }

    private MetricAssociationGraphBO.Node toGraphDimensionNode(DimensionContext dimension) {
        return new MetricAssociationGraphBO.Node()
                .setId("D:" + dimension.dimCode())
                .setCode(dimension.dimCode())
                .setName(dimension.dimName())
                .setNodeType("DIMENSION")
                .setTableRef(StringUtils.hasText(dimension.tableRef()) ? dimension.tableRef() : dimension.sourceTableRef());
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return codes.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record MetricContext(String id, String metricCode, String metricName,
                                 String metricType, Set<String> tableRefs) {
    }

    private record DimensionContext(String dimCode, String dimName, String tableRef,
                                    String columnName, String displayColumn,
                                    String sourceTableRef, boolean builtin) {
    }

    private record AssociationRelation(boolean associated, String relationType, String sourceTable,
                                       String targetTable, String sourceColumn, String targetColumn,
                                       String joinType, String description) {

        private static AssociationRelation associated(String relationType, String sourceTable, String targetTable,
                                                      String sourceColumn, String targetColumn, String joinType,
                                                      String description) {
            return new AssociationRelation(true, relationType, sourceTable, targetTable,
                    sourceColumn, targetColumn, joinType, description);
        }

        private static AssociationRelation notAssociated() {
            return new AssociationRelation(false, null, null, null, null, null, null, null);
        }
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
            Response<UserSecurityLevelDTO> resp = authCheckGateway.getUserMaxSecurityLevel(passport);
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
