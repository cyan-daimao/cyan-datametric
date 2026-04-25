package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricDefinitionHistoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MetricDefinitionHistoryMapper extends BaseMapper<MetricDefinitionHistoryDO> {
}
