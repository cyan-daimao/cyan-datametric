package com.cyan.datametric.domain.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 系统内置时间维度枚举
 * <p>
 * 提供年/季度/月/周/日/小时/分钟/秒 等常用时间粒度维度，
 * 无需维护外部日期维表，SQL 生成时直接通过 DATE_FORMAT 等函数转换。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BuiltinTimeDimension {

    // ==================== 含日期部分（保留年月日，按具体日期时间分组） ====================

    /**
     * 日期-按年
     */
    DIM_DATE_YEAR("日期-按年", "DATE_FORMAT(dt, '%Y')"),

    /**
     * 日期-按季度
     */
    DIM_DATE_QUARTER("日期-按季度", "CONCAT(YEAR(dt), '-Q', QUARTER(dt))"),

    /**
     * 日期-按月
     */
    DIM_DATE_MONTH("日期-按月", "DATE_FORMAT(dt, '%Y-%m')"),

    /**
     * 日期-按周
     */
    DIM_DATE_WEEK("日期-按周", "DATE_FORMAT(dt, '%Y-%u')"),

    /**
     * 日期-按天
     */
    DIM_DATE_DAY("日期-按天", "DATE_FORMAT(dt, '%Y-%m-%d')"),

    /**
     * 日期-按小时（保留日期）
     */
    DIM_DATE_HOUR("日期-按小时", "DATE_FORMAT(dt, '%Y-%m-%d %H:00:00')"),

    /**
     * 日期-按分钟（保留日期）
     */
    DIM_DATE_MINUTE("日期-按分钟", "DATE_FORMAT(dt, '%Y-%m-%d %H:%i:00')"),

    /**
     * 日期-按秒（保留日期）
     */
    DIM_DATE_SECOND("日期-按秒", "DATE_FORMAT(dt, '%Y-%m-%d %H:%i:%s')"),

    // ==================== 仅时间部分（去掉日期，跨天聚合） ====================

    /**
     * 时间-按小时（仅小时数字，跨天聚合）
     */
    DIM_TIME_HOUR("时间-按小时", "DATE_FORMAT(dt, '%H')"),

    /**
     * 时间-按分钟（仅时分，跨天聚合）
     */
    DIM_TIME_MINUTE("时间-按分钟", "DATE_FORMAT(dt, '%H:%i')"),

    /**
     * 时间-按秒（仅时分秒，跨天聚合）
     */
    DIM_TIME_SECOND("时间-按秒", "DATE_FORMAT(dt, '%H:%i:%s')");

    /**
     * 维度名称
     */
    private final String dimName;

    /**
     * SQL 表达式模板，默认使用 dt 作为日期字段
     */
    private final String exprTemplate;

    /**
     * 根据编码获取内置时间维度
     *
     * @param dimCode 维度编码
     * @return 内置时间维度，若不存在则返回 null
     */
    public static BuiltinTimeDimension of(String dimCode) {
        if (dimCode == null || dimCode.isBlank()) {
            return null;
        }
        for (BuiltinTimeDimension value : values()) {
            if (value.name().equals(dimCode)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取所有内置时间维度
     *
     * @return 内置时间维度列表
     */
    public static List<BuiltinTimeDimension> listAll() {
        return Arrays.asList(values());
    }

    /**
     * 构建 SQL 表达式
     *
     * @param dateColumn 日期字段名，若为 null 则使用默认的 dt
     * @return 可执行的 SQL 表达式
     */
    public String buildExpr(String dateColumn) {
        String col = dateColumn != null && !dateColumn.isBlank() ? dateColumn : "dt";
        return exprTemplate.replace("dt", col);
    }
}
