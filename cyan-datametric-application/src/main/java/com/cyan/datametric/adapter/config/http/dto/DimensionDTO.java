package com.cyan.datametric.adapter.config.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 公共维度DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionDTO {

    private String id;
    private String dimCode;
    private String dimName;
    private String dsName;
    private String dbName;
    private String tblName;
    private String colName;
    private String description;
    private LocalDateTime updatedAt;
}
