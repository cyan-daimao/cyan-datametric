package com.cyan.datametric.adapter.metric.http.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 血缘树DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class LineageTreeDTO {

    private LineageNodeDTO upstream;
    private LineageNodeDTO downstream;

    @Data
    @Accessors(chain = true)
    public static class LineageNodeDTO {
        private String id;
        private String name;
        private String nodeType;
        private List<LineageNodeDTO> children;
    }
}
