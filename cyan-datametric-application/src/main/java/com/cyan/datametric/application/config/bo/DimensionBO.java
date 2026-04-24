package com.cyan.datametric.application.config.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 公共维度业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class DimensionBO {

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
