package com.cyan.datametric.domain.metric.repository;

import java.util.List;

/**
 * 指标收藏仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricFavoriteRepository {

    /**
     * 收藏
     */
    void favorite(String metricId, String userId);

    /**
     * 取消收藏
     */
    void unfavorite(String metricId, String userId);

    /**
     * 是否已收藏
     */
    boolean isFavorite(String metricId, String userId);

    /**
     * 查询用户收藏的指标ID列表
     */
    List<String> findFavoriteMetricIds(String userId);
}
