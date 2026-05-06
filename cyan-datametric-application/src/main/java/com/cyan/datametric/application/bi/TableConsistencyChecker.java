package com.cyan.datametric.application.bi;

import com.cyan.arch.common.api.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事实表一致性检查器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
public class TableConsistencyChecker {

    /**
     * 检查所有基础指标是否来自同一事实表
     *
     * @param resolvedMetrics 已展开的指标列表
     */
    public void check(List<ResolvedMetric> resolvedMetrics) {
        // 扁平化获取所有基础指标
        List<ResolvedMetric> baseMetrics = resolvedMetrics.stream()
                .flatMap(m -> m.flattenBaseMetrics().stream())
                .collect(Collectors.toList());

        if (baseMetrics.isEmpty()) {
            return;
        }

        String firstTable = baseMetrics.get(0).getFullTableName();
        for (ResolvedMetric metric : baseMetrics) {
            String table = metric.getFullTableName();
            if (table == null || !table.equals(firstTable)) {
                throw new BusinessException(MetricBiErrorCode.TABLE_INCONSISTENT.getMessage());
            }
        }
    }

    /**
     * 获取统一的事实表名
     *
     * @param resolvedMetrics 已展开的指标列表
     * @return 事实表全名（dbName.tblName）
     */
    public String getUnifiedTableName(List<ResolvedMetric> resolvedMetrics) {
        return resolvedMetrics.stream()
                .flatMap(m -> m.flattenBaseMetrics().stream())
                .findFirst()
                .map(ResolvedMetric::getFullTableName)
                .orElse(null);
    }
}
