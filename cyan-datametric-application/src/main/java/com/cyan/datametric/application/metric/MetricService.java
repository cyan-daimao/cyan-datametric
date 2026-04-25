package com.cyan.datametric.application.metric;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;

import java.util.List;

/**
 * 指标服务接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricService {

    /**
     * 分页查询指标
     */
    Page<MetricBO> page(MetricPageQuery query, String currentUser);

    /**
     * 查询指标详情
     */
    MetricBO detail(String id);

    /**
     * 创建原子指标
     */
    MetricBO createAtomic(AtomicMetricCmd cmd);

    /**
     * 更新原子指标
     */
    MetricBO updateAtomic(String id, AtomicMetricCmd cmd);

    /**
     * 创建派生指标
     */
    MetricBO createDerived(DerivedMetricCmd cmd);

    /**
     * 更新派生指标
     */
    MetricBO updateDerived(String id, DerivedMetricCmd cmd);

    /**
     * 创建复合指标
     */
    MetricBO createComposite(CompositeMetricCmd cmd);

    /**
     * 更新复合指标
     */
    MetricBO updateComposite(String id, CompositeMetricCmd cmd);

    /**
     * 删除指标
     */
    void delete(String id);

    /**
     * 更新状态
     */
    MetricBO updateStatus(String id, UpdateStatusCmd cmd);

    /**
     * SQL预览
     */
    String previewSql(SqlPreviewCmd cmd);

    /**
     * SQL试算
     */
    SqlTrialResultBO trialSql(SqlTrialCmd cmd);

    /**
     * 指标字典分页
     */
    Page<MetricBO> dictionaryPage(MetricPageQuery query, String currentUser);

    /**
     * 收藏指标
     */
    void favorite(String id, String userId);

    /**
     * 取消收藏
     */
    void unfavorite(String id, String userId);

    /**
     * 查询血缘
     */
    LineageTreeBO lineage(String id, String direction, int maxLevel);

    /**
     * Dashboard统计
     */
    DashboardStatsBO dashboardStats();

    /**
     * 主题域下钻
     */
    List<SubjectDrilldownBO> subjectDrilldown(String subjectCode);

    /**
     * 查询指标版本历史
     */
    List<MetricVersionBO> listVersions(String metricId);

    /**
     * 回退到指定版本
     */
    MetricBO rollback(String metricId, Integer version);
}
