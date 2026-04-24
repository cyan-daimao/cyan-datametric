package com.cyan.datametric.application.metric.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 血缘树BO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class LineageTreeBO {

    /**
     * 上游血缘
     */
    private LineageNodeBO upstream;

    /**
     * 下游血缘
     */
    private LineageNodeBO downstream;

    @Data
    @Accessors(chain = true)
    public static class LineageNodeBO {
        private String id;
        private String name;
        private String nodeType;
        private List<LineageNodeBO> children;
    }
}
