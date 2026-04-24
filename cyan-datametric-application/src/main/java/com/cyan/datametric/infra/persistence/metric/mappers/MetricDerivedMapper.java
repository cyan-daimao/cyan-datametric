package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDerivedDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 派生指标扩展Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricDerivedMapper extends BaseMapper<MetricDerivedDO> {
}
