package com.cyan.datametric.application.config.convert;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.cmd.ModifierCmd;
import com.cyan.datametric.application.config.cmd.TimePeriodCmd;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.enums.RelativeUnit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 配置应用层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(uses = MapstructConvert.class)
public interface ConfigAppConvert {
    ConfigAppConvert INSTANCE = Mappers.getMapper(ConfigAppConvert.class);

    ModifierBO toModifierBO(Modifier modifier);

    TimePeriodBO toTimePeriodBO(TimePeriod timePeriod);

    DimensionBO toDimensionBO(Dimension dimension);

    default Dimension toDimension(DimensionCmd cmd) {
        if (cmd == null) return null;
        Dimension d = new Dimension();
        d.setDimCode(cmd.getDimCode());
        d.setDimName(cmd.getDimName());
        d.setDsName(cmd.getDsName());
        d.setDbName(cmd.getDbName());
        d.setTblName(cmd.getTblName());
        d.setColName(cmd.getColName());
        d.setDescription(cmd.getDescription());
        d.setCreateBy(cmd.getCreateBy());
        d.setUpdateBy(cmd.getUpdateBy());
        return d;
    }

    default Modifier toModifier(ModifierCmd cmd) {
        if (cmd == null) return null;
        Modifier m = new Modifier();
        m.setModifierCode(cmd.getModifierCode());
        m.setModifierName(cmd.getModifierName());
        m.setFieldName(cmd.getFieldName());
        m.setOperator(cmd.getOperator());
        m.setFieldValues(cmd.getFieldValues());
        m.setDescription(cmd.getDescription());
        m.setCreateBy(cmd.getCreateBy());
        m.setUpdateBy(cmd.getUpdateBy());
        return m;
    }

    default TimePeriod toTimePeriod(TimePeriodCmd cmd) {
        if (cmd == null) return null;
        TimePeriod t = new TimePeriod();
        t.setPeriodCode(cmd.getPeriodCode());
        t.setPeriodName(cmd.getPeriodName());
        t.setPeriodType(PeriodType.valueOf(cmd.getPeriodType()));
        t.setRelativeValue(cmd.getRelativeValue());
        if (cmd.getRelativeUnit() != null) {
            t.setRelativeUnit(RelativeUnit.valueOf(cmd.getRelativeUnit()));
        }
        t.setStartDate(cmd.getStartDate());
        t.setEndDate(cmd.getEndDate());
        t.setCreateBy(cmd.getCreateBy());
        t.setUpdateBy(cmd.getUpdateBy());
        return t;
    }
}
