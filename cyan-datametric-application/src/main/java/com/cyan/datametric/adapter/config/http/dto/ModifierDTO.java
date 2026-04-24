package com.cyan.datametric.adapter.config.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修饰词DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class ModifierDTO {

    private String id;
    private String modifierCode;
    private String modifierName;
    private String fieldName;
    private String operator;
    private List<String> fieldValues;
    private String description;
    private LocalDateTime updatedAt;
}
