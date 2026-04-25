package com.cyan.datametric.infra.persistence.metric.subject;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标主题域Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricSubjectMapper extends BaseMapper<MetricSubjectDO> {
}
