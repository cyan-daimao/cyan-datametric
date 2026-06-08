package com.cyan.datametric.application.collection.convert;

import com.cyan.datametric.application.collection.cmd.CollectionDimensionUpsertCmd;
import com.cyan.datametric.application.collection.cmd.CollectionMetricUpsertCmd;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.metric.cmd.AtomicMetricCmd;
import org.mapstruct.Mapper;

import java.util.Optional;

/**
 * 采集映射应用层转换
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface CollectionMappingAppConvert {

    /**
     * 转换维度命令
     *
     * @param cmd 采集属性维度同步命令
     * @return 公共维度命令
     */
    default DimensionCmd toDimensionCmd(CollectionDimensionUpsertCmd cmd) {
        if (cmd == null) {
            return null;
        }
        DimensionCmd result = new DimensionCmd();
        result.setDimCode(defaultString(cmd.getDimCode(), "dim_" + cmd.getPropertyCode()));
        result.setDimName(defaultString(cmd.getDimName(), cmd.getPropertyName()));
        result.setDimType(defaultString(cmd.getDimType(), "STRING"));
        result.setDimensionKind("DEGENERATE");
        result.setDataType(defaultString(cmd.getDataType(), "STRING"));
        result.setDimValues(cmd.getDimValues());
        result.setCategoryId(resolveNumericCategoryId(cmd.getCategoryId()));
        result.setTableName(null);
        result.setColumnName(defaultString(cmd.getColumnName(), cmd.getPropertyCode()));
        result.setDisplayColumn(defaultString(cmd.getColumnName(), cmd.getPropertyCode()));
        result.setSourceType(defaultString(cmd.getSourceType(), "JSON_PATH"));
        result.setSourceExpr(defaultString(cmd.getSourceExpr(), "$.properties." + cmd.getPropertyCode()));
        result.setSourceTable(cmd.getSourceTable());
        result.setDescription(defaultString(cmd.getDescription(), "采集属性 " + cmd.getPropertyCode() + " 自动同步维度"));
        result.setCreateBy(defaultString(cmd.getOperator(), "system"));
        result.setUpdateBy(defaultString(cmd.getOperator(), "system"));
        return result;
    }

    /**
     * 转换原子指标命令
     *
     * @param cmd 采集事件指标同步命令
     * @return 原子指标命令
     */
    default AtomicMetricCmd toAtomicMetricCmd(CollectionMetricUpsertCmd cmd) {
        if (cmd == null) {
            return null;
        }
        AtomicMetricCmd result = new AtomicMetricCmd();
        result.setMetricCode(defaultString(cmd.getMetricCode(), cmd.getEventCode() + "_count"));
        result.setMetricName(defaultString(cmd.getMetricName(), cmd.getEventName() + "次数"));
        result.setSubjectCode(defaultString(cmd.getSubjectCode(), "data_collection"));
        result.setStatFunc(defaultString(cmd.getStatFunc(), "COUNT"));
        result.setDsName(defaultString(cmd.getDsName(), "iceberg"));
        result.setDbName(defaultString(cmd.getDbName(), "ods"));
        result.setTblName(cmd.getTblName());
        result.setColName(defaultString(cmd.getColName(), "request_id"));
        result.setBizCaliber(defaultString(cmd.getBizCaliber(), "统计事件 " + cmd.getEventCode() + " 的触发次数"));
        result.setTechCaliber(defaultString(cmd.getTechCaliber(),
                "从 " + result.getDbName() + "." + result.getTblName() + " 按 event_code='" + cmd.getEventCode() + "' 过滤后 COUNT(" + result.getColName() + ")"));
        result.setSecurityLevel(defaultString(cmd.getSecurityLevel(), "L1"));
        result.setOwner(defaultString(cmd.getOwner(), "system"));
        result.setCreateBy(defaultString(cmd.getOperator(), "system"));
        result.setUpdateBy(defaultString(cmd.getOperator(), "system"));
        result.setFilterCondition(Optional.ofNullable(cmd.getFilterCondition()).orElseGet(java.util.List::of).stream()
                .map(item -> {
                    AtomicMetricCmd.FilterConditionCmd filter = new AtomicMetricCmd.FilterConditionCmd();
                    filter.setField(item.getField());
                    filter.setOp(item.getOp());
                    filter.setValue(item.getValue());
                    return filter;
                })
                .toList());
        return result;
    }

    /**
     * 默认字符串
     */
    default String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 仅保留数字分类ID，兼容调用方传入业务编码
     */
    default String resolveNumericCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        return categoryId.chars().allMatch(Character::isDigit) ? categoryId : null;
    }
}
