package com.cyan.datametric.application.config.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间周期业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class TimePeriodBO {

    private String id;
    private String periodCode;
    private String periodName;
    private String periodType;
    private Integer relativeValue;
    private String relativeUnit;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime updatedAt;
}
