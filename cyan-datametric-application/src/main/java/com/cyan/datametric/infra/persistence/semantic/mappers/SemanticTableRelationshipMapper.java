package com.cyan.datametric.infra.persistence.semantic.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticTableRelationshipDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表关联关系 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SemanticTableRelationshipMapper extends BaseMapper<SemanticTableRelationshipDO> {
}
