package com.cyan.datametric.infra.persistence.semantic.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticMaterializedViewDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物化视图 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface SemanticMaterializedViewMapper extends BaseMapper<SemanticMaterializedViewDO> {
}
