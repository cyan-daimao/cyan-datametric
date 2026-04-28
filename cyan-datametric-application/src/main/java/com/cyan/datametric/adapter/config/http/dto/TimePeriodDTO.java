package com.cyan.datametric.adapter.config.http.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
