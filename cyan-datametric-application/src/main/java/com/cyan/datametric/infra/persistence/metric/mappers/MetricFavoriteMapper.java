package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricFavoriteDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标收藏Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricFavoriteMapper extends BaseMapper<MetricFavoriteDO> {
}
