package com.cyan.datametric.infra.persistence.config.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.config.dos.DimensionFieldBindingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维度字段绑定 Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface DimensionFieldBindingMapper extends BaseMapper<DimensionFieldBindingDO> {
}
