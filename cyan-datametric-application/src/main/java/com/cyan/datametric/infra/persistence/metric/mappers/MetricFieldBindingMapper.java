package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricFieldBindingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标字段绑定 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricFieldBindingMapper extends BaseMapper<MetricFieldBindingDO> {
}
