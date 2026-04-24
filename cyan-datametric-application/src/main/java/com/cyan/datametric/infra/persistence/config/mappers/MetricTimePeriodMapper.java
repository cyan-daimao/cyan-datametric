package com.cyan.datametric.infra.persistence.config.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.config.dos.MetricTimePeriodDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时间周期Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricTimePeriodMapper extends BaseMapper<MetricTimePeriodDO> {
}
