package com.cyan.datametric.application.metric.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.MetricBOAssembler;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.application.metric.convert.MetricAppConvert;
import com.cyan.datametric.application.metric.lineage.MetricFieldLineageSyncService;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import com.cyan.datametric.domain.metric.LineageNode;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.MetricFieldBinding;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricFavoriteRepository;
import com.cyan.datametric.domain.metric.repository.MetricFieldBindingRepository;
import com.cyan.datametric.domain.metric.repository.MetricLineageRepository;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.dataauth.dto.UserSecurityLevelDTO;
import com.cyan.dataauth.enums.SecurityLevel;
import com.cyan.datametric.infra.gateway.AuthCheckGateway;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;
    private final MetricLineageRepository lineageRepository;
    private final MetricFavoriteRepository favoriteRepository;
    private final MetricFieldBindingRepository metricFieldBindingRepository;
    private final ModifierRepository modifierRepository;
    private final TimePeriodRepository timePeriodRepository;
    private final MetricAppConvert metricAppConvert;
    private final MetricBOAssembler metricBOAssembler;
    private final AuthCheckGateway authCheckGateway;
    private final MetricFieldLineageSyncService metricFieldLineageSyncService;

    @Value("${datametric.default-datasource:cyan_iceberg}")
    private String defaultDatasource;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;


    @Override
    public Page<MetricBO> page(MetricPageQuery query, String currentUser) {
        com.cyan.arch.common.api.Page<Metric> page = metricRepository.page(query);
        List<MetricBO> list = page.getData().stream()
                .map(metricBOAssembler::assembleBasic)
                .toList();
        metricBOAssembler.fillSubjectName(list);
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public MetricBO detail(String id, String currentUser) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        String userMaxLevel = getUserMaxSecurityLevel(currentUser);
        Assert.isTrue(canAccess(metric.getSecurityLevel(), userMaxLevel),
                new BusinessException("您没有权限查看该密级的指标"));
        MetricBO bo = metricBOAssembler.assembleDetail(metric);
        metricBOAssembler.fillSubjectName(bo);
        return bo;
    }

    @Override
    @Transactional
    public MetricBO createAtomic(AtomicMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        if (!org.springframework.util.StringUtils.hasText(cmd.getDsName())) {
            cmd.setDsName(defaultDatasource);
        }
        Metric metric = metricAppConvert.toMetric(cmd);
        if (!org.springframework.util.StringUtils.hasText(metric.getMetricCode())) {
            metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        }
        metric.setMetricType(MetricType.ATOMIC);
        normalizeAtomicBindings(metric);
        metric = metric.save(metricRepository);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public MetricBO upsertAtomicByMetricCode(AtomicMetricCmd cmd) {
        Metric existing = metricRepository.findByMetricCode(cmd.getMetricCode());
        if (existing == null) {
            return createAtomic(cmd);
        }
        return updateAtomic(existing.getId(), cmd);
    }

    @Override
    @Transactional
    public MetricBO updateAtomic(String id, AtomicMetricCmd cmd) {
        Metric existing = metricRepository.findById(id);
        Assert.notNull(existing, new BusinessException("指标不存在"));
        checkNameDuplicateForUpdate(cmd.getMetricName(), id);
        if (!org.springframework.util.StringUtils.hasText(cmd.getDsName()) && existing.getAtomicExt() != null) {
            cmd.setDsName(existing.getAtomicExt().getDsName());
        }
        // 如果当前是已发布状态，先保存快照，然后生成新版本草稿
        if (existing.getStatus() == MetricStatus.PUBLISHED) {
            metricRepository.saveSnapshot(existing);
        }
        Metric metric = metricAppConvert.toMetric(cmd);
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        metric.setMetricType(MetricType.ATOMIC);
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setCreateBy(existing.getCreateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        if (metric.getSecurityLevel() == null) {
            metric.setSecurityLevel(existing.getSecurityLevel());
        }
        normalizeAtomicBindings(metric);
        metric = metric.update(metricRepository);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public MetricBO createDerived(DerivedMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        Metric metric = metricAppConvert.toMetric(cmd);
        if (!org.springframework.util.StringUtils.hasText(metric.getMetricCode())) {
            metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        }
        metric.setMetricType(MetricType.DERIVED);
        metric = metric.save(metricRepository);
        buildLineage(metric);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public MetricBO updateDerived(String id, DerivedMetricCmd cmd) {
        Metric existing = metricRepository.findById(id);
        Assert.notNull(existing, new BusinessException("指标不存在"));
        checkNameDuplicateForUpdate(cmd.getMetricName(), id);
        // 如果当前是已发布状态，先保存快照，然后生成新版本草稿
        if (existing.getStatus() == MetricStatus.PUBLISHED) {
            metricRepository.saveSnapshot(existing);
        }
        Metric metric = metricAppConvert.toMetric(cmd);
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        if (metric.getSecurityLevel() == null) {
            metric.setSecurityLevel(existing.getSecurityLevel());
        }
        metric.setMetricType(MetricType.DERIVED);
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setCreateBy(existing.getCreateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric = metric.update(metricRepository);
        lineageRepository.deleteByMetricId(id);
        buildLineage(metric);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public MetricBO createComposite(CompositeMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        Metric metric = metricAppConvert.toMetric(cmd);
        if (!org.springframework.util.StringUtils.hasText(metric.getMetricCode())) {
            metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        }
        metric.setMetricType(MetricType.COMPOSITE);
        metric = metric.save(metricRepository);
        buildLineage(metric);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public MetricBO updateComposite(String id, CompositeMetricCmd cmd) {
        Metric existing = metricRepository.findById(id);
        Assert.notNull(existing, new BusinessException("指标不存在"));
        checkNameDuplicateForUpdate(cmd.getMetricName(), id);
        // 如果当前是已发布状态，先保存快照，然后生成新版本草稿
        if (existing.getStatus() == MetricStatus.PUBLISHED) {
            metricRepository.saveSnapshot(existing);
        }
        Metric metric = metricAppConvert.toMetric(cmd);
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        if (metric.getSecurityLevel() == null) {
            metric.setSecurityLevel(existing.getSecurityLevel());
        }
        metric.setMetricType(MetricType.COMPOSITE);
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setCreateBy(existing.getCreateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric = metric.update(metricRepository);
        lineageRepository.deleteByMetricId(id);
        buildLineage(metric);
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        List<Metric> downstream = metricRepository.findDownstreamMetrics(id);
        Assert.isTrue(downstream.isEmpty(), new BusinessException("该指标被下游指标引用，不可删除"));
        metric.delete(metricRepository);
        lineageRepository.deleteByMetricId(id);
        metricFieldLineageSyncService.clear(id);
    }

    @Override
    @Transactional
    public MetricBO updateStatus(String id, UpdateStatusCmd cmd) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        MetricStatus newStatus = MetricStatus.valueOf(cmd.getStatus());
        if (newStatus == MetricStatus.PUBLISHED) {
            metric = metric.publish(metricRepository);
        } else if (newStatus == MetricStatus.OFFLINE) {
            metric = metric.offline(metricRepository);
        } else {
            throw new BusinessException("不支持的状态变更");
        }
        syncMetricFieldLineage(metric);
        return metricBOAssembler.assembleBasic(metric);
    }

    @Override
    public String previewSql(SqlPreviewCmd cmd) {
        return buildSql(cmd.getMetricType(), cmd.getDefinitionBody());
    }

    @Override
    public Page<MetricBO> dictionaryPage(MetricPageQuery query, String currentUser) {
        String userMaxLevel = getUserMaxSecurityLevel(currentUser);
        List<String> favoriteIds = favoriteRepository.findFavoriteMetricIds(currentUser);
        com.cyan.arch.common.api.Page<Metric> page;

        if (query.getFavorite() != null && query.getFavorite()) {
            // 只看收藏
            if (favoriteIds.isEmpty()) {
                return new Page<>(List.of(), 1, query.size(), 0);
            }
            page = metricRepository.pageByIds(favoriteIds, query);
        } else {
            page = metricRepository.page(query);
        }

        Set<String> favSet = new HashSet<>(favoriteIds);
        List<MetricBO> list = page.getData().stream()
                .filter(m -> m.getStatus() != com.cyan.datametric.enums.MetricStatus.OFFLINE)
                .filter(m -> canAccess(m.getSecurityLevel(), userMaxLevel))
                .map(m -> {
                    MetricBO bo = metricBOAssembler.assembleBasic(m);
                    bo.setIsFavorite(favSet.contains(m.getId()));
                    return bo;
                })
                .toList();
        metricBOAssembler.fillSubjectName(list);
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public void favorite(String id, String userId) {
        favoriteRepository.favorite(id, userId);
    }

    @Override
    public void unfavorite(String id, String userId) {
        favoriteRepository.unfavorite(id, userId);
    }

    @Override
    public LineageTreeBO lineage(String id, String direction, int maxLevel) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        LineageTreeBO tree = new LineageTreeBO();
        if ("UPSTREAM".equals(direction) || "BOTH".equals(direction)) {
            List<LineageNode> nodes = lineageRepository.findUpstream(id);
            tree.setUpstream(buildTreeNode(metric, nodes, "UPSTREAM", maxLevel));
        }
        if ("DOWNSTREAM".equals(direction) || "BOTH".equals(direction)) {
            List<LineageNode> nodes = lineageRepository.findDownstream(id);
            tree.setDownstream(buildTreeNode(metric, nodes, "DOWNSTREAM", maxLevel));
        }
        return tree;
    }

    @Override
    public DashboardStatsBO dashboardStats() {
        DashboardStatsBO bo = new DashboardStatsBO();
        long atomic = metricRepository.countByType("ATOMIC");
        long derived = metricRepository.countByType("DERIVED");
        long composite = metricRepository.countByType("COMPOSITE");
        long published = metricRepository.countByStatus("PUBLISHED");
        long draft = metricRepository.countByStatus("DRAFT");
        long offline = metricRepository.countByStatus("OFFLINE");
        bo.setTotalMetrics(atomic + derived + composite);
        bo.setAtomicCount(atomic);
        bo.setDerivedCount(derived);
        bo.setCompositeCount(composite);
        bo.setPublishedCount(published);
        bo.setDraftCount(draft);
        bo.setOfflineCount(offline);
        List<Map<String, Object>> subjectList = metricRepository.countBySubject();
        bo.setSubjectDistribution(subjectList.stream()
                .map(m -> new DashboardStatsBO.SubjectDistributionBO()
                        .setSubjectCode((String) m.get("subjectCode"))
                        .setCount(((Long) m.get("count"))))
                .toList());
        bo.setRecentUpdates(List.of());
        return bo;
    }

    @Override
    public List<SubjectDrilldownBO> subjectDrilldown(String subjectCode) {
        SubjectDrilldownBO bo = new SubjectDrilldownBO();
        bo.setSubjectCode(subjectCode == null ? "ALL" : subjectCode);
        bo.setSubjectName(subjectCode == null ? "全部" : subjectCode);
        bo.setTotalMetrics(0L);
        bo.setTypeDistribution(new HashMap<>());
        bo.setStatusDistribution(new HashMap<>());
        bo.setChildren(List.of());
        return List.of(bo);
    }

    @Override
    public List<MetricVersionBO> listVersions(String metricId) {
        Metric metric = metricRepository.findById(metricId);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        return metricRepository.findHistoryByMetricCode(metric.getMetricCode()).stream()
                .map(m -> {
                    MetricVersionBO bo = new MetricVersionBO();
                    bo.setVersion(m.getVersion());
                    bo.setMetricName(m.getMetricName());
                    bo.setStatus(m.getStatus().name());
                    bo.setSnapshotTime(m.getUpdatedAt());
                    bo.setUpdateBy(m.getUpdateBy());
                    return bo;
                })
                .toList();
    }

    @Override
    @Transactional
    public MetricBO rollback(String metricId, Integer version) {
        Metric current = metricRepository.findById(metricId);
        Assert.notNull(current, new BusinessException("指标不存在"));

        // 1. 先快照当前状态
        metricRepository.saveSnapshot(current);

        // 2. 查找目标历史版本
        Metric history = metricRepository.findHistoryByVersion(current.getMetricCode(), version);
        Assert.notNull(history, new BusinessException("目标版本不存在"));

        // 3. 历史覆盖主表（保持 id 不变）
        history.setId(current.getId());
        metricRepository.rollbackFromHistory(history);

        // 4. 重建血缘
        lineageRepository.deleteByMetricId(metricId);
        buildLineage(history);
        syncMetricFieldLineage(metricRepository.findById(metricId));

        return metricBOAssembler.assembleBasic(metricRepository.findById(metricId));
    }

    // ==================== 私有方法 ====================


    // ==================== 密级权限 ====================

    /**
     * 同步字段级指标血缘
     */
    private void syncMetricFieldLineage(Metric metric) {
        if (metric == null) {
            return;
        }
        Metric atomicMetric = null;
        List<Metric> refMetrics = List.of();
        if (metric.getMetricType() == MetricType.DERIVED
                && metric.getDerivedExt() != null
                && metric.getDerivedExt().getAtomicMetricId() != null) {
            atomicMetric = metricRepository.findById(metric.getDerivedExt().getAtomicMetricId());
        }
        if (metric.getMetricType() == MetricType.COMPOSITE
                && metric.getCompositeExt() != null
                && metric.getCompositeExt().getMetricRefs() != null) {
            refMetrics = metric.getCompositeExt().getMetricRefs().stream()
                    .map(metricRepository::findById)
                    .filter(Objects::nonNull)
                    .toList();
        }
        metricFieldLineageSyncService.sync(metric, atomicMetric, refMetrics);
    }

    /**
     * 归一化原子指标字段绑定
     */
    private void normalizeAtomicBindings(Metric metric) {
        if (metric == null || metric.getAtomicExt() == null) {
            return;
        }
        MetricAtomicExt atomicExt = metric.getAtomicExt();
        if (atomicExt.getFieldBindings() != null && !atomicExt.getFieldBindings().isEmpty()) {
            return;
        }
        if (!org.springframework.util.StringUtils.hasText(atomicExt.getDbName())
                || !org.springframework.util.StringUtils.hasText(atomicExt.getTblName())
                || !org.springframework.util.StringUtils.hasText(atomicExt.getColName())) {
            return;
        }
        atomicExt.setFieldBindings(List.of(new com.cyan.datametric.domain.metric.MetricFieldBinding()
                .setCatalogName(org.springframework.util.StringUtils.hasText(atomicExt.getDsName())
                        ? atomicExt.getDsName()
                        : defaultDatasource)
                .setSchemaName(atomicExt.getDbName())
                .setTableName(atomicExt.getTblName())
                .setColumnName(atomicExt.getColName())
                .setFilterCondition(atomicExt.getFilterCondition())
                .setPrimaryBinding(true)
                .setSortOrder(0)));
    }

    @Override
    public List<MetricFieldBindingBO> listFieldBindings(String metricId) {
        return toMetricFieldBindingBOs(metricFieldBindingRepository.findByMetricId(metricId));
    }

    @Override
    @Transactional
    public MetricFieldBindingBO saveFieldBinding(String metricId, MetricFieldBindingCmd cmd, String operator) {
        Metric metric = metricRepository.findById(metricId);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        MetricFieldBinding binding = metricAppConvert.toMetricFieldBinding(cmd);
        binding.setMetricId(metricId);
        binding.setUpdateBy(operator);
        MetricFieldBinding saved;
        if (org.springframework.util.StringUtils.hasText(binding.getId())) {
            saved = binding.update(metricFieldBindingRepository);
        } else {
            binding.setCreateBy(operator);
            saved = binding.save(metricFieldBindingRepository);
        }
        return toMetricFieldBindingBO(saved);
    }

    @Override
    @Transactional
    public void deleteFieldBinding(String metricId, String bindingId) {
        MetricFieldBinding binding = metricFieldBindingRepository.findById(bindingId);
        Assert.notNull(binding, new BusinessException("指标字段绑定不存在"));
        Assert.isTrue(metricId.equals(binding.getMetricId()), new BusinessException("指标字段绑定不属于当前指标"));
        binding.delete(metricFieldBindingRepository);
    }

    @Override
    @Transactional
    public void setPrimaryFieldBinding(String metricId, String bindingId) {
        MetricFieldBinding binding = metricFieldBindingRepository.findById(bindingId);
        Assert.notNull(binding, new BusinessException("指标字段绑定不存在"));
        Assert.isTrue(metricId.equals(binding.getMetricId()), new BusinessException("指标字段绑定不属于当前指标"));
        metricFieldBindingRepository.setPrimary(metricId, bindingId);
    }

    /**
     * 转换指标字段绑定列表
     */
    private List<MetricFieldBindingBO> toMetricFieldBindingBOs(List<MetricFieldBinding> bindings) {
        if (bindings == null) {
            return List.of();
        }
        return bindings.stream().map(this::toMetricFieldBindingBO).toList();
    }

    /**
     * 转换指标字段绑定
     */
    private MetricFieldBindingBO toMetricFieldBindingBO(MetricFieldBinding binding) {
        MetricFieldBindingBO bo = new MetricFieldBindingBO()
                .setId(binding.getId())
                .setMetricId(binding.getMetricId())
                .setCatalogName(binding.getCatalogName())
                .setSchemaName(binding.getSchemaName())
                .setTableName(binding.getTableName())
                .setColumnName(binding.getColumnName())
                .setSourceExpr(binding.getSourceExpr())
                .setPrimaryBinding(binding.getPrimaryBinding())
                .setSortOrder(binding.getSortOrder())
                .setUpdatedAt(binding.getUpdatedAt());
        if (binding.getFilterCondition() != null) {
            bo.setFilterCondition(binding.getFilterCondition().stream()
                    .map(f -> new MetricAtomicBO.FilterConditionBO().setField(f.getField()).setOp(f.getOp()).setValue(f.getValue()))
                    .toList());
        }
        return bo;
    }

    /**
     * 获取用户最高可访问密级，降级为 L1
     */
    private String getUserMaxSecurityLevel(String passport) {
        try {
            com.cyan.arch.common.api.Response<UserSecurityLevelDTO> resp = authCheckGateway.getUserMaxSecurityLevel(passport);
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

    // ==================== 私有方法 ====================

    private void checkNameDuplicate(String metricName) {
        Metric existing = metricRepository.findByName(metricName);
        Assert.isNull(existing, new BusinessException("指标名称已存在"));
    }

    private void checkNameDuplicateForUpdate(String metricName, String id) {
        Metric existing = metricRepository.findByName(metricName);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("指标名称已存在");
        }
    }


    private String buildSql(String metricType, SqlPreviewCmd.DefinitionBody body) {
        return switch (MetricType.valueOf(metricType)) {
            case ATOMIC -> buildAtomicSql(body);
            case DERIVED -> buildDerivedSql(body);
            case COMPOSITE -> buildCompositeSql(body);
        };
    }

    private String buildAggExpression(String func, String col) {
        Assert.notBlank(func, new BusinessException("统计函数不能为空"));
        Assert.notBlank(col, new BusinessException("指标字段或表达式不能为空"));
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT " + col + ")";
        }
        return func + "(" + col + ")";
    }

    private MetricFieldBinding resolvePreviewBinding(SqlPreviewCmd.DefinitionBody body) {
        if (body == null || !hasText(body.getMetricId())) {
            return null;
        }
        return primaryBinding(metricFieldBindingRepository.findByMetricId(body.getMetricId()));
    }

    private MetricFieldBinding resolvePreviewBinding(String metricId, MetricAtomicExt ext) {
        MetricFieldBinding binding = hasText(metricId)
                ? primaryBinding(metricFieldBindingRepository.findByMetricId(metricId))
                : null;
        if (binding != null) {
            return binding;
        }
        return primaryBinding(ext == null ? null : ext.getFieldBindings());
    }

    private MetricFieldBinding primaryBinding(List<MetricFieldBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return null;
        }
        return bindings.stream()
                .filter(binding -> Boolean.TRUE.equals(binding.getPrimaryBinding()))
                .findFirst()
                .orElse(bindings.getFirst());
    }

    private String resolveMetricExpression(MetricFieldBinding binding, String fallbackColumn) {
        if (binding != null && hasText(binding.getSourceExpr())) {
            return binding.getSourceExpr();
        }
        String column = binding != null && hasText(binding.getColumnName()) ? binding.getColumnName() : fallbackColumn;
        Assert.notBlank(column, new BusinessException("指标未配置物理字段绑定，请先在详情中配置字段绑定"));
        return quoteIdentifier(column);
    }

    private String resolveMetricAlias(MetricFieldBinding binding, String fallbackColumn) {
        if (binding != null && hasText(binding.getColumnName())) {
            return binding.getColumnName();
        }
        if (hasText(fallbackColumn)) {
            return fallbackColumn;
        }
        return "metric_value";
    }

    private String resolveMetricTable(MetricFieldBinding binding, SqlPreviewCmd.DefinitionBody body) {
        if (binding != null) {
            return binding.tableRef(defaultCatalog);
        }
        Assert.notBlank(body.getDbName(), new BusinessException("指标未配置物理字段绑定，请先在详情中配置字段绑定"));
        Assert.notBlank(body.getTblName(), new BusinessException("指标未配置物理字段绑定，请先在详情中配置字段绑定"));
        return body.getDbName() + "." + body.getTblName();
    }

    private String resolveMetricTable(MetricFieldBinding binding, MetricAtomicExt ext) {
        if (binding != null) {
            return binding.tableRef(defaultCatalog);
        }
        Assert.notBlank(ext.getDbName(), new BusinessException("原子指标未配置物理字段绑定"));
        Assert.notBlank(ext.getTblName(), new BusinessException("原子指标未配置物理字段绑定"));
        return ext.getDbName() + "." + ext.getTblName();
    }

    private String buildCondition(String field, String op, String value) {
        Assert.notBlank(field, new BusinessException("过滤字段不能为空"));
        Assert.notBlank(op, new BusinessException("过滤运算符不能为空"));
        String operator = op.trim();
        if ("IN".equalsIgnoreCase(operator)) {
            List<String> values = Arrays.stream(String.valueOf(value).split(","))
                    .map(String::trim)
                    .filter(this::hasText)
                    .map(this::quoteLiteral)
                    .toList();
            Assert.isTrue(!values.isEmpty(), new BusinessException("过滤值不能为空"));
            return quoteIdentifier(field) + " IN (" + String.join(",", values) + ")";
        }
        Assert.notBlank(value, new BusinessException("过滤值不能为空"));
        return quoteIdentifier(field) + " " + operator + " " + quoteLiteral(value);
    }

    private String quoteIdentifier(String identifier) {
        if (!hasText(identifier)) {
            return identifier;
        }
        if (identifier.contains("(") || identifier.contains(" ") || identifier.contains(".")) {
            return identifier;
        }
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String quoteLiteral(String value) {
        return "'" + String.valueOf(value).replace("'", "''") + "'";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildAtomicSql(SqlPreviewCmd.DefinitionBody body) {
        String func = body.getStatFunc();
        MetricFieldBinding binding = resolvePreviewBinding(body);
        String col = resolveMetricExpression(binding, body.getColName());
        String alias = quoteIdentifier(resolveMetricAlias(binding, body.getColName()));
        String table = resolveMetricTable(binding, body);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(buildAggExpression(func, col)).append(" as ").append(alias).append(" FROM ").append(table);
        List<String> conditions = new ArrayList<>();
        if (binding != null && binding.getFilterCondition() != null) {
            for (MetricAtomicExt.FilterCondition f : binding.getFilterCondition()) {
                conditions.add(buildCondition(f.getField(), f.getOp(), f.getValue()));
            }
        }
        if (body.getFilterCondition() != null) {
            for (AtomicMetricCmd.FilterConditionCmd f : body.getFilterCondition()) {
                conditions.add(buildCondition(f.getField(), f.getOp(), f.getValue()));
            }
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        return sql.toString();
    }

    private String buildDerivedSql(SqlPreviewCmd.DefinitionBody body) {
        Metric atomic = metricRepository.findById(body.getAtomicMetricId());
        Assert.notNull(atomic, new BusinessException("原子指标不存在"));
        Assert.notNull(atomic.getAtomicExt(), new BusinessException("原子指标扩展信息不存在"));
        MetricAtomicExt ext = atomic.getAtomicExt();
        MetricFieldBinding binding = resolvePreviewBinding(body.getAtomicMetricId(), ext);
        String func = ext.getStatFunc().getCode();
        String col = resolveMetricExpression(binding, ext.getColName());
        String alias = resolveMetricAlias(binding, ext.getColName());
        String table = resolveMetricTable(binding, ext);
        StringBuilder sql = new StringBuilder();

        List<String> selectCols = new ArrayList<>();
        if (body.getGroupByFields() != null) {
            for (DerivedMetricCmd.GroupByFieldCmd g : body.getGroupByFields()) {
                selectCols.add(g.getCol());
            }
        }
        selectCols.add(buildAggExpression(func, col) + " as " + quoteIdentifier(alias));
        sql.append("SELECT ").append(String.join(", ", selectCols)).append(" FROM ").append(table);

        List<String> conditions = new ArrayList<>();
        if (binding != null && binding.getFilterCondition() != null) {
            for (MetricAtomicExt.FilterCondition f : binding.getFilterCondition()) {
                conditions.add(buildCondition(f.getField(), f.getOp(), f.getValue()));
            }
        }
        if (binding == null && ext.getFilterCondition() != null) {
            for (MetricAtomicExt.FilterCondition f : ext.getFilterCondition()) {
                conditions.add(buildCondition(f.getField(), f.getOp(), f.getValue()));
            }
        }
        if (body.getModifierIds() != null && !body.getModifierIds().isEmpty()) {
            List<Modifier> modifiers = modifierRepository.findByIds(body.getModifierIds());
            for (Modifier m : modifiers) {
                if (m.getFieldValues() != null && !m.getFieldValues().isEmpty()) {
                    String values = m.getFieldValues().stream()
                            .map(this::quoteLiteral)
                            .collect(Collectors.joining(","));
                    conditions.add(quoteIdentifier(m.getFieldName()) + " " + m.getOperator() + " (" + values + ")");
                }
            }
        }
        if (body.getTimePeriodId() != null) {
            TimePeriod period = timePeriodRepository.findById(body.getTimePeriodId());
            if (period != null && period.getPeriodType() == PeriodType.RELATIVE) {
                conditions.add(col + " >= date_sub(current_date, " + Math.abs(period.getRelativeValue()) + ")");
            }
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (body.getGroupByFields() != null && !body.getGroupByFields().isEmpty()) {
            sql.append(" GROUP BY ").append(body.getGroupByFields().stream().map(DerivedMetricCmd.GroupByFieldCmd::getCol).collect(Collectors.joining(", ")));
        }
        return sql.toString();
    }

    private String buildCompositeSql(SqlPreviewCmd.DefinitionBody body) {
        String formula = body.getFormula();
        if (formula == null) {
            return "SELECT 0";
        }
        return "SELECT " + formula.replace("${", "").replace("}", "");
    }

    private void buildLineage(Metric metric) {
        List<LineageNode> nodes = new ArrayList<>();
        switch (metric.getMetricType()) {
            case DERIVED -> {
                if (metric.getDerivedExt() != null && metric.getDerivedExt().getAtomicMetricId() != null) {
                    String atomicId = metric.getDerivedExt().getAtomicMetricId();
                    Metric atomic = metricRepository.findById(atomicId);
                    if (atomic != null) {
                        LineageNode node = new LineageNode();
                        node.setMetricId(metric.getId());
                        node.setParentMetricId(null);
                        node.setUpstreamType("METRIC");
                        node.setUpstreamId(atomicId);
                        node.setUpstreamName(atomic.getMetricName());
                        node.setLineageType("UPSTREAM");
                        node.setLevel(1);
                        nodes.add(node);
                        if (atomic.getAtomicExt() != null) {
                            LineageNode tblNode = new LineageNode();
                            tblNode.setMetricId(metric.getId());
                            tblNode.setParentMetricId(null);
                            tblNode.setUpstreamType("TABLE");
                            tblNode.setUpstreamId(atomic.getAtomicExt().getTblName());
                            tblNode.setUpstreamName(atomic.getAtomicExt().getTblName());
                            tblNode.setLineageType("UPSTREAM");
                            tblNode.setLevel(2);
                            nodes.add(tblNode);
                            LineageNode colNode = new LineageNode();
                            colNode.setMetricId(metric.getId());
                            colNode.setParentMetricId(null);
                            colNode.setUpstreamType("COLUMN");
                            colNode.setUpstreamId(atomic.getAtomicExt().getColName());
                            colNode.setUpstreamName(atomic.getAtomicExt().getColName());
                            colNode.setLineageType("UPSTREAM");
                            colNode.setLevel(3);
                            nodes.add(colNode);
                        }
                    }
                }
            }
            case COMPOSITE -> {
                if (metric.getCompositeExt() != null && metric.getCompositeExt().getMetricRefs() != null) {
                    for (String refId : metric.getCompositeExt().getMetricRefs()) {
                        Metric ref = metricRepository.findById(refId);
                        if (ref != null) {
                            LineageNode node = new LineageNode();
                            node.setMetricId(metric.getId());
                            node.setParentMetricId(null);
                            node.setUpstreamType("METRIC");
                            node.setUpstreamId(refId);
                            node.setUpstreamName(ref.getMetricName());
                            node.setLineageType("UPSTREAM");
                            node.setLevel(1);
                            nodes.add(node);
                        }
                    }
                }
            }
            default -> {
            }
        }
        if (!nodes.isEmpty()) {
            lineageRepository.saveAll(nodes);
        }
    }

    private LineageTreeBO.LineageNodeBO buildTreeNode(Metric metric, List<LineageNode> nodes, String direction, int maxLevel) {
        LineageTreeBO.LineageNodeBO root = new LineageTreeBO.LineageNodeBO();
        root.setId(metric.getId());
        root.setName(metric.getMetricName());
        root.setNodeType("METRIC");
        root.setChildren(new ArrayList<>());
        Map<String, LineageTreeBO.LineageNodeBO> nodeMap = new HashMap<>();
        nodeMap.put(metric.getId(), root);
        for (LineageNode node : nodes) {
            if (node.getLevel() > maxLevel) continue;
            LineageTreeBO.LineageNodeBO bo = new LineageTreeBO.LineageNodeBO();
            bo.setId(node.getUpstreamId());
            bo.setName(node.getUpstreamName());
            bo.setNodeType(node.getUpstreamType());
            bo.setChildren(new ArrayList<>());
            nodeMap.put(node.getUpstreamId(), bo);
            String parentKey = node.getParentMetricId() == null ? metric.getId() : node.getParentMetricId();
            LineageTreeBO.LineageNodeBO parent = nodeMap.get(parentKey);
            if (parent != null) {
                parent.getChildren().add(bo);
            }
        }
        return root;
    }
}
