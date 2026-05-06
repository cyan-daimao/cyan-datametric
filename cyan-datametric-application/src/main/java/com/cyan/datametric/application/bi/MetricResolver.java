package com.cyan.datametric.application.bi;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.adapter.bi.http.dto.MetricBiAnalysisCmd;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.MetricAtomicExt;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指标展开器：将各类指标（原子/派生/复合）展开为可生成SQL的基础形式
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MetricResolver {

    private final MetricRepository metricRepository;
    private final ModifierRepository modifierRepository;
    private final TimePeriodRepository timePeriodRepository;

    private static final Pattern COMPOSITE_REF_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 展开指标列表
     *
     * @param metricRefs 前端传入的指标引用列表
     * @return 展开后的指标列表
     */
    public List<ResolvedMetric> resolve(List<MetricBiAnalysisCmd.MetricRef> metricRefs) {
        List<ResolvedMetric> result = new ArrayList<>();
        for (MetricBiAnalysisCmd.MetricRef ref : metricRefs) {
            ResolvedMetric resolved = resolveMetric(ref.getMetricId(), ref.getAlias());
            result.add(resolved);
        }
        return result;
    }

    private ResolvedMetric resolveMetric(String metricId, String alias) {
        Metric metric = metricRepository.findById(metricId);
        Assert.notNull(metric, new BusinessException(MetricBiErrorCode.METRIC_NOT_FOUND.getMessage()));
        Assert.isTrue(metric.getStatus() != MetricStatus.OFFLINE,
                new BusinessException(MetricBiErrorCode.METRIC_NOT_FOUND.getMessage()));

        ResolvedMetric resolved = new ResolvedMetric();
        resolved.setMetricId(metricId);
        resolved.setAlias(alias != null && !alias.isBlank() ? alias : metric.getMetricName());
        resolved.setOriginalType(metric.getMetricType());

        switch (metric.getMetricType()) {
            case ATOMIC -> resolveAtomic(metric, resolved);
            case DERIVED -> resolveDerived(metric, resolved);
            case COMPOSITE -> resolveComposite(metric, resolved);
        }

        return resolved;
    }

    private void resolveAtomic(Metric metric, ResolvedMetric resolved) {
        Assert.notNull(metric.getAtomicExt(), new BusinessException("原子指标扩展信息缺失"));
        MetricAtomicExt ext = metric.getAtomicExt();
        resolved.setDbName(ext.getDbName());
        resolved.setTblName(ext.getTblName());
        resolved.setColName(ext.getColName());
        resolved.setStatFunc(ext.getStatFunc());
        resolved.setAtomicFilters(ext.getFilterCondition());
    }

    private void resolveDerived(Metric metric, ResolvedMetric resolved) {
        Assert.notNull(metric.getDerivedExt(), new BusinessException("派生指标扩展信息缺失"));
        String atomicMetricId = metric.getDerivedExt().getAtomicMetricId();
        Metric atomicMetric = metricRepository.findById(atomicMetricId);
        Assert.notNull(atomicMetric, new BusinessException("派生指标关联的原子指标不存在"));
        Assert.notNull(atomicMetric.getAtomicExt(), new BusinessException("原子指标扩展信息缺失"));

        MetricAtomicExt ext = atomicMetric.getAtomicExt();
        resolved.setDbName(ext.getDbName());
        resolved.setTblName(ext.getTblName());
        resolved.setColName(ext.getColName());
        resolved.setStatFunc(ext.getStatFunc());
        resolved.setAtomicFilters(ext.getFilterCondition());

        // 加载修饰词
        List<String> modifierIds = metric.getDerivedExt().getModifierIds();
        if (modifierIds != null && !modifierIds.isEmpty()) {
            resolved.setModifiers(modifierRepository.findByIds(modifierIds));
        }

        // 加载时间周期
        String timePeriodId = metric.getDerivedExt().getTimePeriodId();
        if (timePeriodId != null && !timePeriodId.isBlank()) {
            resolved.setTimePeriod(timePeriodRepository.findById(timePeriodId));
        }
    }

    private void resolveComposite(Metric metric, ResolvedMetric resolved) {
        Assert.notNull(metric.getCompositeExt(), new BusinessException("复合指标扩展信息缺失"));
        String formula = metric.getCompositeExt().getFormula();
        Assert.notBlank(formula, new BusinessException(MetricBiErrorCode.COMPOSITE_PARSE_FAILED.getMessage()));

        resolved.setFormula(formula);

        List<String> refIds = metric.getCompositeExt().getMetricRefs();
        Assert.notEmpty(refIds, new BusinessException(MetricBiErrorCode.COMPOSITE_PARSE_FAILED.getMessage()));

        List<ResolvedMetric> refMetrics = new ArrayList<>();
        // 按公式中出现的顺序解析引用指标
        Matcher matcher = COMPOSITE_REF_PATTERN.matcher(formula);
        while (matcher.find()) {
            String refId = matcher.group(1);
            ResolvedMetric refResolved = resolveMetric(refId, null);
            refMetrics.add(refResolved);
        }

        resolved.setRefMetrics(refMetrics);
    }
}
