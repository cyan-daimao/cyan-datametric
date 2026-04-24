package com.cyan.datametric.domain.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 血缘节点
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class LineageNode {

    /**
     * 节点ID
     */
    private String id;

    /**
     * 当前指标ID
     */
    private String metricId;

    /**
     * 上游指标ID
     */
    private String parentMetricId;

    /**
     * 上游类型
     */
    private String upstreamType;

    /**
     * 上游节点ID
     */
    private String upstreamId;

    /**
     * 上游节点名称
     */
    private String upstreamName;

    /**
     * 血缘方向
     */
    private String lineageType;

    /**
     * 血缘层级
     */
    private Integer level;

    /**
     * 子节点
     */
    private List<LineageNode> children = new ArrayList<>();
}
