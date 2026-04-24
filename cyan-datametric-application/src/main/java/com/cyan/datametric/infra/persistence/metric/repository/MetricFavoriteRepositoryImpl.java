package com.cyan.datametric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.metric.repository.MetricFavoriteRepository;
import com.cyan.datametric.infra.persistence.metric.dos.MetricFavoriteDO;
import com.cyan.datametric.infra.persistence.metric.mappers.MetricFavoriteMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标收藏仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class MetricFavoriteRepositoryImpl implements MetricFavoriteRepository {

    private final MetricFavoriteMapper favoriteMapper;

    public MetricFavoriteRepositoryImpl(MetricFavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public void favorite(String metricId, String userId) {
        if (isFavorite(metricId, userId)) {
            return;
        }
        MetricFavoriteDO favoriteDO = new MetricFavoriteDO();
        favoriteDO.setId(SnowflakeIdUtil.nextId());
        favoriteDO.setMetricId(Long.parseLong(metricId));
        favoriteDO.setUserId(userId);
        favoriteMapper.insert(favoriteDO);
    }

    @Override
    public void unfavorite(String metricId, String userId) {
        LambdaQueryWrapper<MetricFavoriteDO> wrapper = new LambdaQueryWrapper<MetricFavoriteDO>()
                .eq(MetricFavoriteDO::getMetricId, Long.parseLong(metricId))
                .eq(MetricFavoriteDO::getUserId, userId);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public boolean isFavorite(String metricId, String userId) {
        LambdaQueryWrapper<MetricFavoriteDO> wrapper = new LambdaQueryWrapper<MetricFavoriteDO>()
                .eq(MetricFavoriteDO::getMetricId, Long.parseLong(metricId))
                .eq(MetricFavoriteDO::getUserId, userId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<String> findFavoriteMetricIds(String userId) {
        LambdaQueryWrapper<MetricFavoriteDO> wrapper = new LambdaQueryWrapper<MetricFavoriteDO>()
                .eq(MetricFavoriteDO::getUserId, userId);
        List<MetricFavoriteDO> list = favoriteMapper.selectList(wrapper);
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(d -> String.valueOf(d.getMetricId()))
                .toList();
    }
}
