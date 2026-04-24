package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标定义Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricDefinitionMapper extends BaseMapper<MetricDefinitionDO> {
}
