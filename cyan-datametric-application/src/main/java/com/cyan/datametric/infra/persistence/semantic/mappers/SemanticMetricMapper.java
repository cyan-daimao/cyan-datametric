package com.cyan.datametric.infra.persistence.semantic.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticMetricDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 语义指标 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SemanticMetricMapper extends BaseMapper<SemanticMetricDO> {
}
