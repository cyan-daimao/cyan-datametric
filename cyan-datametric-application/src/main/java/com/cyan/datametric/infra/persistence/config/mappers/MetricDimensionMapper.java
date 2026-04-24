package com.cyan.datametric.infra.persistence.config.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.config.dos.MetricDimensionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公共维度Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricDimensionMapper extends BaseMapper<MetricDimensionDO> {
}
