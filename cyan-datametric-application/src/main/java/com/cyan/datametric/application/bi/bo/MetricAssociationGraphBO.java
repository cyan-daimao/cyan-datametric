package com.cyan.datametric.application.bi.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 指标可关联图谱业务对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricAssociationGraphBO {

    /**
     * 中心节点
     */
    private Node center;

    /**
     * 图谱节点列表
     */
    private List<Node> nodes;

    /**
     * 图谱边列表
     */
    private List<Edge> edges;

    /**
     * 图谱节点
     */
    @Data
    @Accessors(chain = true)
    public static class Node {

        /**
         * 节点ID
         */
        private String id;

        /**
         * 节点编码
         */
        private String code;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点类型：METRIC/DIMENSION
         */
        private String nodeType;

        /**
         * 指标类型
         */
        private String metricType;

        /**
         * 表引用
         */
        private String tableRef;
    }

    /**
     * 图谱边
     */
    @Data
    @Accessors(chain = true)
    public static class Edge {

        /**
         * 来源节点ID
         */
        private String source;

        /**
         * 目标节点ID
         */
        private String target;

        /**
         * 关系类型
         */
        private String relationType;

        /**
         * JOIN 类型
         */
        private String joinType;

        /**
         * 来源字段
         */
        private String sourceColumn;

        /**
         * 目标字段
         */
        private String targetColumn;

        /**
         * 来源表
         */
        private String sourceTable;

        /**
         * 目标表
         */
        private String targetTable;

        /**
         * 关系描述
         */
        private String description;
    }
}
