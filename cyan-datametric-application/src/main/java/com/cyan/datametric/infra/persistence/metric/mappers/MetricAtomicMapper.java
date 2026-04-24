package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricAtomicDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 原子指标扩展Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricAtomicMapper extends BaseMapper<MetricAtomicDO> {
}
