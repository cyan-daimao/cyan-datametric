package com.cyan.datametric.adapter.config.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间周期DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class TimePeriodDTO {

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
