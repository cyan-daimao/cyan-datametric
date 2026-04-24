package com.cyan.datametric.infra.persistence.config.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.arch.common.util.JSON;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.infra.persistence.config.dos.MetricDimensionDO;
import com.cyan.datametric.infra.persistence.config.dos.MetricModifierDO;
import com.cyan.datametric.infra.persistence.config.dos.MetricTimePeriodDO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 配置基础设施层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface ConfigInfraConvert {
    ConfigInfraConvert INSTANCE = Mappers.getMapper(ConfigInfraConvert.class);

    default Modifier toModifier(MetricModifierDO modifierDO) {
        if (modifierDO == null) return null;
        Modifier m = new Modifier();
        m.setId(modifierDO.getId() == null ? null : String.valueOf(modifierDO.getId()));
        m.setModifierCode(modifierDO.getModifierCode());
        m.setModifierName(modifierDO.getModifierName());
        m.setFieldName(modifierDO.getFieldName());
        m.setOperator(modifierDO.getOperator());
        if (modifierDO.getFieldValues() != null && !modifierDO.getFieldValues().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                m.setFieldValues(mapper.readValue(modifierDO.getFieldValues(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                m.setFieldValues(null);
            }
        }
        m.setDescription(modifierDO.getDescription());
        m.setCreateBy(modifierDO.getCreateBy());
        m.setUpdateBy(modifierDO.getUpdateBy());
        m.setCreatedAt(modifierDO.getCreatedAt());
        m.setUpdatedAt(modifierDO.getUpdatedAt());
        return m;
    }

    default MetricModifierDO toModifierDO(Modifier modifier) {
        if (modifier == null) return null;
        MetricModifierDO d = new MetricModifierDO();
        d.setId(modifier.getId() == null ? null : Long.parseLong(modifier.getId()));
        d.setModifierCode(modifier.getModifierCode());
        d.setModifierName(modifier.getModifierName());
        d.setFieldName(modifier.getFieldName());
        d.setOperator(modifier.getOperator());
        if (modifier.getFieldValues() != null) {
            d.setFieldValues(JSON.toJSONString(modifier.getFieldValues()));
        }
        d.setDescription(modifier.getDescription());
        d.setCreateBy(modifier.getCreateBy());
        d.setUpdateBy(modifier.getUpdateBy());
        d.setCreatedAt(modifier.getCreatedAt());
        d.setUpdatedAt(modifier.getUpdatedAt());
        return d;
    }

    TimePeriod toTimePeriod(MetricTimePeriodDO periodDO);

    MetricTimePeriodDO toTimePeriodDO(TimePeriod timePeriod);

    default Dimension toDimension(MetricDimensionDO dimensionDO) {
        if (dimensionDO == null) return null;
        Dimension d = new Dimension();
        d.setId(dimensionDO.getId() == null ? null : String.valueOf(dimensionDO.getId()));
        d.setDimCode(dimensionDO.getDimCode());
        d.setDimName(dimensionDO.getDimName());
        d.setDsName(dimensionDO.getDsName());
        d.setDbName(dimensionDO.getDbName());
        d.setTblName(dimensionDO.getTblName());
        d.setColName(dimensionDO.getColName());
        d.setDescription(dimensionDO.getDescription());
        d.setCreateBy(dimensionDO.getCreateBy());
        d.setUpdateBy(dimensionDO.getUpdateBy());
        d.setCreatedAt(dimensionDO.getCreatedAt());
        d.setUpdatedAt(dimensionDO.getUpdatedAt());
        return d;
    }

    default MetricDimensionDO toDimensionDO(Dimension dimension) {
        if (dimension == null) return null;
        MetricDimensionDO d = new MetricDimensionDO();
        d.setId(dimension.getId() == null ? null : Long.parseLong(dimension.getId()));
        d.setDimCode(dimension.getDimCode());
        d.setDimName(dimension.getDimName());
        d.setDsName(dimension.getDsName());
        d.setDbName(dimension.getDbName());
        d.setTblName(dimension.getTblName());
        d.setColName(dimension.getColName());
        d.setDescription(dimension.getDescription());
        d.setCreateBy(dimension.getCreateBy());
        d.setUpdateBy(dimension.getUpdateBy());
        d.setCreatedAt(dimension.getCreatedAt());
        d.setUpdatedAt(dimension.getUpdatedAt());
        return d;
    }
}
