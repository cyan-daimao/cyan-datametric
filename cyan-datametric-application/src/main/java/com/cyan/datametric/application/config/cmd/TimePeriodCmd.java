package com.cyan.datametric.application.config.cmd;

import lombok.Data;

import java.time.LocalDate;

/**
 * 时间周期命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class TimePeriodCmd {

    /**
     * 周期编码
     */
    private String periodCode;

    /**
     * 周期名称
     */
    private String periodName;

    /**
     * 类型
     */
    private String periodType;

    /**
     * 相对偏移值
     */
    private Integer relativeValue;

    /**
     * 相对单位
     */
    private String relativeUnit;

    /**
     * 绝对日期范围-开始
     */
    private LocalDate startDate;

    /**
     * 绝对日期范围-结束
     */
    private LocalDate endDate;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
