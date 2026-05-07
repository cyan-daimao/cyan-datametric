package com.cyan.datametric.infra.persistence.semantic.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticLogicalTableDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 逻辑表 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SemanticLogicalTableMapper extends BaseMapper<SemanticLogicalTableDO> {
}
