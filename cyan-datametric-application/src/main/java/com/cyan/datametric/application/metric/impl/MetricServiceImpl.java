package com.cyan.datametric.application.metric.impl;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.application.metric.convert.MetricAppConvert;
import com.cyan.dataman.client.DatamanDsClient;
import com.cyan.dataman.client.dto.SqlExecuteCmd;
import com.cyan.dataman.client.dto.SqlResultDTO;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import com.cyan.datametric.domain.metric.*;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricFavoriteRepository;
import com.cyan.datametric.domain.metric.repository.MetricLineageRepository;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
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
@Service
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;
    private final MetricLineageRepository lineageRepository;
    private final MetricFavoriteRepository favoriteRepository;
    private final ModifierRepository modifierRepository;
    private final TimePeriodRepository timePeriodRepository;
    private final MetricSubjectRepository metricSubjectRepository;
    private final DatamanDsClient datamanDsClient;

    @Value("${datametric.default-datasource:cyan_iceberg}")
    private String defaultDatasource;

    public MetricServiceImpl(MetricRepository metricRepository,
                             MetricLineageRepository lineageRepository,
                             MetricFavoriteRepository favoriteRepository,
                             ModifierRepository modifierRepository,
                             TimePeriodRepository timePeriodRepository,
                             MetricSubjectRepository metricSubjectRepository,
                             DatamanDsClient datamanDsClient) {
        this.metricRepository = metricRepository;
        this.lineageRepository = lineageRepository;
        this.favoriteRepository = favoriteRepository;
        this.modifierRepository = modifierRepository;
        this.timePeriodRepository = timePeriodRepository;
        this.metricSubjectRepository = metricSubjectRepository;
        this.datamanDsClient = datamanDsClient;
    }

    @Override
    public Page<MetricBO> page(MetricPageQuery query, String currentUser) {
        com.cyan.arch.common.api.Page<Metric> page = metricRepository.page(query);
        List<MetricBO> list = page.getData().stream()
                .map(this::toMetricBO)
                .toList();
        fillSubjectName(list);
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public MetricBO detail(String id) {
        Metric metric = metricRepository.findById(id);
        Assert.notNull(metric, new BusinessException("指标不存在"));
        MetricBO bo = toDetailBO(metric);
        fillSubjectName(bo);
        return bo;
    }

    @Override
    @Transactional
    public MetricBO createAtomic(AtomicMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        if (!org.springframework.util.StringUtils.hasText(cmd.getDsName())) {
            cmd.setDsName(defaultDatasource);
        }
        Metric metric = new Metric();
        metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.ATOMIC);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setOwner(cmd.getCreateBy());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setAtomicExt(MetricAppConvert.INSTANCE.toAtomicExt(cmd));
        metric = metric.save(metricRepository);
        return toMetricBO(metric);
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
        Metric metric = new Metric();
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.ATOMIC);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setOwner(existing.getOwner());
        metric.setCreateBy(existing.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric.setAtomicExt(MetricAppConvert.INSTANCE.toAtomicExt(cmd));
        metric = metric.update(metricRepository);
        return toMetricBO(metric);
    }

    @Override
    @Transactional
    public MetricBO createDerived(DerivedMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        Metric metric = new Metric();
        metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.DERIVED);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setOwner(cmd.getCreateBy());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setDerivedExt(MetricAppConvert.INSTANCE.toDerivedExt(cmd));
        metric = metric.save(metricRepository);
        buildLineage(metric);
        return toMetricBO(metric);
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
        Metric metric = new Metric();
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.DERIVED);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setOwner(existing.getOwner());
        metric.setCreateBy(existing.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric.setDerivedExt(MetricAppConvert.INSTANCE.toDerivedExt(cmd));
        metric = metric.update(metricRepository);
        lineageRepository.deleteByMetricId(id);
        buildLineage(metric);
        return toMetricBO(metric);
    }

    @Override
    @Transactional
    public MetricBO createComposite(CompositeMetricCmd cmd) {
        checkNameDuplicate(cmd.getMetricName());
        Metric metric = new Metric();
        metric.setMetricCode("M" + SnowflakeIdUtil.nextId());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.COMPOSITE);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setOwner(cmd.getCreateBy());
        metric.setCreateBy(cmd.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setCompositeExt(MetricAppConvert.INSTANCE.toCompositeExt(cmd));
        metric = metric.save(metricRepository);
        buildLineage(metric);
        return toMetricBO(metric);
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
        Metric metric = new Metric();
        metric.setId(id);
        metric.setMetricCode(existing.getMetricCode());
        metric.setMetricName(cmd.getMetricName());
        metric.setMetricType(MetricType.COMPOSITE);
        metric.setSubjectCode(cmd.getSubjectCode());
        metric.setBizCaliber(cmd.getBizCaliber());
        metric.setTechCaliber(cmd.getTechCaliber());
        metric.setStatus(existing.getStatus() == MetricStatus.PUBLISHED ? MetricStatus.DRAFT : existing.getStatus());
        metric.setVersion(existing.getStatus() == MetricStatus.PUBLISHED ? existing.getVersion() + 1 : existing.getVersion());
        metric.setOwner(existing.getOwner());
        metric.setCreateBy(existing.getCreateBy());
        metric.setUpdateBy(cmd.getUpdateBy());
        metric.setCreatedAt(existing.getCreatedAt());
        metric.setCompositeExt(MetricAppConvert.INSTANCE.toCompositeExt(cmd));
        metric = metric.update(metricRepository);
        lineageRepository.deleteByMetricId(id);
        buildLineage(metric);
        return toMetricBO(metric);
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
        return toMetricBO(metric);
    }

    @Override
    public String previewSql(SqlPreviewCmd cmd) {
        return buildSql(cmd.getMetricType(), cmd.getDefinitionBody());
    }

    @Override
    public SqlTrialResultBO trialSql(SqlTrialCmd cmd) {
        String sql = buildSql(cmd.getMetricType(), cmd.getDefinitionBody());
        int limit = cmd.getLimit() == null ? 100 : Math.min(cmd.getLimit(), 100);
        String limitedSql = sql + " LIMIT " + limit;

        String dsName = cmd.getDefinitionBody().getDsName();
        String dbName = cmd.getDefinitionBody().getDbName();
        if (dsName == null || dbName == null) {
            if (cmd.getDefinitionBody().getAtomicMetricId() != null) {
                Metric atomic = metricRepository.findById(cmd.getDefinitionBody().getAtomicMetricId());
                if (atomic != null && atomic.getAtomicExt() != null) {
                    dsName = atomic.getAtomicExt().getDsName();
                    dbName = atomic.getAtomicExt().getDbName();
                }
            }
        }
        Assert.notBlank(dsName, new BusinessException("数据源名称不能为空"));
        Assert.notBlank(dbName, new BusinessException("数据库名称不能为空"));

        SqlExecuteCmd executeCmd = new SqlExecuteCmd();
        executeCmd.setSql(limitedSql);
        executeCmd.setLimit(limit);

        long start = System.currentTimeMillis();
        com.cyan.arch.common.api.Response<SqlResultDTO> response = datamanDsClient.executeSql(dsName, dbName, executeCmd);
        long cost = System.currentTimeMillis() - start;

        SqlResultDTO result = response.getData();
        SqlTrialResultBO bo = new SqlTrialResultBO();
        bo.setSql(sql);
        bo.setCostTime(cost);

        if (result != null && result.getColumns() != null) {
            bo.setColumns(result.getColumns().stream()
                    .map(c -> new SqlTrialResultBO.ColumnBO().setName(c).setType("STRING"))
                    .toList());
        }
        if (result != null && result.getRows() != null) {
            List<List<Object>> rowList = new ArrayList<>();
            for (java.util.Map<String, Object> r : result.getRows()) {
                rowList.add(new ArrayList<>(r.values()));
            }
            bo.setRows(rowList);
        }
        return bo;
    }

    @Override
    public Page<MetricBO> dictionaryPage(MetricPageQuery query, String currentUser) {
        com.cyan.arch.common.api.Page<Metric> page = metricRepository.page(query);
        List<String> favoriteIds = favoriteRepository.findFavoriteMetricIds(currentUser);
        Set<String> favSet = new HashSet<>(favoriteIds);
        List<MetricBO> list = page.getData().stream()
                .map(m -> {
                    MetricBO bo = toMetricBO(m);
                    bo.setIsFavorite(favSet.contains(m.getId()));
                    return bo;
                })
                .toList();
        fillSubjectName(list);
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

        return toMetricBO(metricRepository.findById(metricId));
    }

    // ==================== 私有方法 ====================

    private void fillSubjectName(List<MetricBO> bos) {
        if (bos == null || bos.isEmpty()) {
            return;
        }
        List<String> subjectCodes = bos.stream()
                .map(MetricBO::getSubjectCode)
                .filter(sc -> sc != null && !sc.isBlank())
                .distinct()
                .toList();
        if (subjectCodes.isEmpty()) {
            return;
        }
        List<MetricSubject> subjects = metricSubjectRepository.findBySubjectCodes(subjectCodes);
        Map<String, String> nameMap = subjects.stream()
                .filter(s -> s.getSubjectCode() != null)
                .collect(Collectors.toMap(MetricSubject::getSubjectCode, MetricSubject::getSubjectName, (a, b) -> a));
        for (MetricBO bo : bos) {
            if (bo.getSubjectCode() != null) {
                bo.setSubjectName(nameMap.get(bo.getSubjectCode()));
            }
        }
    }

    private void fillSubjectName(MetricBO bo) {
        if (bo == null || bo.getSubjectCode() == null || bo.getSubjectCode().isBlank()) {
            return;
        }
        MetricSubject subject = metricSubjectRepository.findBySubjectCode(bo.getSubjectCode());
        if (subject != null) {
            bo.setSubjectName(subject.getSubjectName());
        }
    }

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

    private MetricBO toMetricBO(Metric metric) {
        if (metric == null) return null;
        MetricBO bo = MetricAppConvert.INSTANCE.toMetricBO(metric);
        if (metric.getAtomicExt() != null) {
            MetricAtomicExt ext = metric.getAtomicExt();
            bo.setStatFunc(ext.getStatFunc() == null ? null : ext.getStatFunc().getCode());
            bo.setDsName(ext.getDsName());
            bo.setDbName(ext.getDbName());
            bo.setTblName(ext.getTblName());
            bo.setColName(ext.getColName());
        }
        return bo;
    }

    private MetricBO toDetailBO(Metric metric) {
        MetricBO bo = toMetricBO(metric);
        if (metric.getAtomicExt() != null) {
            MetricAtomicBO atomic = new MetricAtomicBO();
            atomic.setStatFunc(metric.getAtomicExt().getStatFunc() == null ? null : metric.getAtomicExt().getStatFunc().getCode());
            atomic.setDsName(metric.getAtomicExt().getDsName());
            atomic.setDbName(metric.getAtomicExt().getDbName());
            atomic.setTblName(metric.getAtomicExt().getTblName());
            atomic.setColName(metric.getAtomicExt().getColName());
            if (metric.getAtomicExt().getFilterCondition() != null) {
                atomic.setFilterCondition(metric.getAtomicExt().getFilterCondition().stream()
                        .map(f -> new MetricAtomicBO.FilterConditionBO().setField(f.getField()).setOp(f.getOp()).setValue(f.getValue()))
                        .toList());
            }
            bo.setAtomic(atomic);
        }
        if (metric.getDerivedExt() != null) {
            MetricDerivedBO derived = new MetricDerivedBO();
            derived.setAtomicMetricId(metric.getDerivedExt().getAtomicMetricId());
            derived.setTimePeriodId(metric.getDerivedExt().getTimePeriodId());
            derived.setModifierIds(metric.getDerivedExt().getModifierIds());
            derived.setDimensionIds(metric.getDerivedExt().getDimensionIds());
            if (metric.getDerivedExt().getGroupByFields() != null) {
                derived.setGroupByFields(metric.getDerivedExt().getGroupByFields().stream()
                        .map(g -> new MetricDerivedBO.GroupByFieldBO().setCol(g.getCol()))
                        .toList());
            }
            bo.setDerived(derived);
        }
        if (metric.getCompositeExt() != null) {
            MetricCompositeBO composite = new MetricCompositeBO();
            composite.setFormula(metric.getCompositeExt().getFormula());
            composite.setMetricRefs(metric.getCompositeExt().getMetricRefs());
            bo.setComposite(composite);
        }
        return bo;
    }

    private String buildSql(String metricType, SqlPreviewCmd.DefinitionBody body) {
        return switch (MetricType.valueOf(metricType)) {
            case ATOMIC -> buildAtomicSql(body);
            case DERIVED -> buildDerivedSql(body);
            case COMPOSITE -> buildCompositeSql(body);
        };
    }

    private String buildAggExpression(String func, String col) {
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT " + col + ")";
        }
        return func + "(" + col + ")";
    }

    private String buildAtomicSql(SqlPreviewCmd.DefinitionBody body) {
        String func = body.getStatFunc();
        String col = body.getColName();
        String table = body.getDbName() + "." + body.getTblName();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(buildAggExpression(func, col)).append(" as ").append(col).append(" FROM ").append(table);
        List<String> conditions = new ArrayList<>();
        if (body.getFilterCondition() != null) {
            for (AtomicMetricCmd.FilterConditionCmd f : body.getFilterCondition()) {
                conditions.add(f.getField() + " " + f.getOp() + " '" + f.getValue() + "'");
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
        String func = ext.getStatFunc().getCode();
        String col = ext.getColName();
        String table = ext.getDbName() + "." + ext.getTblName();
        StringBuilder sql = new StringBuilder();

        List<String> selectCols = new ArrayList<>();
        if (body.getGroupByFields() != null) {
            for (DerivedMetricCmd.GroupByFieldCmd g : body.getGroupByFields()) {
                selectCols.add(g.getCol());
            }
        }
        selectCols.add(buildAggExpression(func, col) + " as " + col);
        sql.append("SELECT ").append(String.join(", ", selectCols)).append(" FROM ").append(table);

        List<String> conditions = new ArrayList<>();
        if (ext.getFilterCondition() != null) {
            for (MetricAtomicExt.FilterCondition f : ext.getFilterCondition()) {
                conditions.add(f.getField() + " " + f.getOp() + " '" + f.getValue() + "'");
            }
        }
        if (body.getModifierIds() != null && !body.getModifierIds().isEmpty()) {
            List<Modifier> modifiers = modifierRepository.findByIds(body.getModifierIds());
            for (Modifier m : modifiers) {
                if (m.getFieldValues() != null && !m.getFieldValues().isEmpty()) {
                    String values = m.getFieldValues().stream().map(v -> "'" + v + "'")
                            .collect(Collectors.joining(","));
                    conditions.add(m.getFieldName() + " " + m.getOperator() + " (" + values + ")");
                }
            }
        }
        if (body.getTimePeriodId() != null) {
            TimePeriod period = timePeriodRepository.findById(body.getTimePeriodId());
            if (period != null && period.getPeriodType() == PeriodType.RELATIVE) {
                conditions.add(ext.getColName() + " >= date_sub(current_date, " + Math.abs(period.getRelativeValue()) + ")");
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
