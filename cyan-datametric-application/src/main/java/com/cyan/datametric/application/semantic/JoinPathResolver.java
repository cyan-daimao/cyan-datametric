package com.cyan.datametric.application.semantic;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.LogicalTable;
import com.cyan.datametric.domain.semantic.TableRelationship;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * JOIN 路径计算器
 * <p>
 * 基于图遍历（BFS）的最短 JOIN 路径算法：
 * 1. 将逻辑表视为图的节点，TableRelationship 视为有向边
 * 2. 从事实表出发，BFS 查找到达每个维度表的最短路径
 * 3. 支持星型（维度直连事实表）、雪花（维度间接连事实表）、星座（多事实表共享维度）模型
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Component
public class JoinPathResolver {

    /**
     * 计算 JOIN 路径
     *
     * @param factTables      事实表列表（来自指标）
     * @param dimensionTables 维度表列表（来自用户选择的维度）
     * @param relationships   全量表关联关系
     * @return JOIN 路径列表（按执行顺序排列）
     */
    public List<JoinEdge> resolve(List<LogicalTable> factTables,
                                   List<LogicalTable> dimensionTables,
                                   List<TableRelationship> relationships) {
        if (factTables == null || factTables.isEmpty()) {
            throw new BusinessException("事实表不能为空");
        }

        // 构建邻接表（无向图，因为 JOIN 是双向的）
        Map<String, List<GraphEdge>> graph = buildGraph(relationships);

        // 选择主事实表（指标数最多的，或仅有一张时直接选）
        LogicalTable primaryFact = selectPrimaryFactTable(factTables);
        String startNode = primaryFact.getId();

        Set<String> targetNodes = new HashSet<>();
        for (LogicalTable dim : dimensionTables) {
            if (!dim.getId().equals(startNode)) {
                targetNodes.add(dim.getId());
            }
        }

        // 多事实表场景：其他事实表也需要加入目标
        for (LogicalTable fact : factTables) {
            if (!fact.getId().equals(startNode)) {
                targetNodes.add(fact.getId());
            }
        }

        if (targetNodes.isEmpty()) {
            return List.of();
        }

        // BFS 计算从主事实表到所有目标节点的最短路径
        Map<String, PathResult> shortestPaths = bfsShortestPaths(startNode, targetNodes, graph);

        // 合并所有路径为 JOIN 边列表
        List<JoinEdge> joinEdges = new ArrayList<>();
        Set<String> visitedNodes = new HashSet<>();
        visitedNodes.add(startNode);

        int aliasIndex = 1;
        Map<String, String> tableAliasMap = new HashMap<>();
        tableAliasMap.put(startNode, "t0");

        for (PathResult path : shortestPaths.values()) {
            if (path == null || path.getEdges().isEmpty()) {
                continue;
            }
            String currentFrom = startNode;
            for (GraphEdge edge : path.getEdges()) {
                String nextNode = edge.to;
                if (visitedNodes.contains(nextNode)) {
                    continue;
                }
                TableRelationship rel = edge.relationship;
                // 确定方向：如果当前 from 是左表，则直接用；否则交换
                boolean reverse = !currentFrom.equals(rel.getLeftTableId());
                String leftTableId = reverse ? rel.getRightTableId() : rel.getLeftTableId();
                String rightTableId = reverse ? rel.getLeftTableId() : rel.getRightTableId();

                String leftAlias = tableAliasMap.get(leftTableId);
                if (leftAlias == null) {
                    leftAlias = "t" + aliasIndex++;
                    tableAliasMap.put(leftTableId, leftAlias);
                }
                String rightAlias = "t" + aliasIndex++;
                tableAliasMap.put(rightTableId, rightAlias);

                JoinEdge joinEdge = new JoinEdge();
                joinEdge.setLeftTableId(leftTableId);
                joinEdge.setRightTableId(rightTableId);
                joinEdge.setLeftAlias(leftAlias);
                joinEdge.setRightAlias(rightAlias);
                joinEdge.setRelationship(rel);
                joinEdge.setReverse(reverse);
                joinEdges.add(joinEdge);

                visitedNodes.add(nextNode);
                currentFrom = nextNode;
            }
        }

        return joinEdges;
    }

    /**
     * 构建邻接表（无向图）
     */
    private Map<String, List<GraphEdge>> buildGraph(List<TableRelationship> relationships) {
        Map<String, List<GraphEdge>> graph = new HashMap<>();
        for (TableRelationship rel : relationships) {
            GraphEdge edge1 = new GraphEdge(rel.getRightTableId(), rel);
            GraphEdge edge2 = new GraphEdge(rel.getLeftTableId(), rel);
            graph.computeIfAbsent(rel.getLeftTableId(), k -> new ArrayList<>()).add(edge1);
            graph.computeIfAbsent(rel.getRightTableId(), k -> new ArrayList<>()).add(edge2);
        }
        return graph;
    }

    /**
     * 选择主事实表（简单策略：选第一张，可扩展为数据量最小或指标数最多）
     */
    private LogicalTable selectPrimaryFactTable(List<LogicalTable> factTables) {
        return factTables.get(0);
    }

    /**
     * BFS 计算从起点到多个目标点的最短路径
     */
    private Map<String, PathResult> bfsShortestPaths(String startNode,
                                                      Set<String> targetNodes,
                                                      Map<String, List<GraphEdge>> graph) {
        Map<String, PathResult> result = new HashMap<>();
        Set<String> remaining = new HashSet<>(targetNodes);

        Queue<PathState> queue = new LinkedList<>();
        queue.offer(new PathState(startNode, List.of(), new HashSet<>(Set.of(startNode))));

        while (!queue.isEmpty() && !remaining.isEmpty()) {
            PathState current = queue.poll();
            List<GraphEdge> edges = graph.getOrDefault(current.node, List.of());

            for (GraphEdge edge : edges) {
                String nextNode = edge.to;
                if (current.visited.contains(nextNode)) {
                    continue;
                }
                List<GraphEdge> newPath = new ArrayList<>(current.path);
                newPath.add(edge);
                Set<String> newVisited = new HashSet<>(current.visited);
                newVisited.add(nextNode);

                if (remaining.contains(nextNode)) {
                    result.put(nextNode, new PathResult(newPath));
                    remaining.remove(nextNode);
                }

                queue.offer(new PathState(nextNode, newPath, newVisited));
            }
        }

        // 未找到路径的目标节点记录为 null
        for (String target : targetNodes) {
            result.putIfAbsent(target, null);
        }
        return result;
    }

    // ==================== 内部数据结构 ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinEdge {
        private String leftTableId;
        private String rightTableId;
        private String leftAlias;
        private String rightAlias;
        private TableRelationship relationship;
        private boolean reverse;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class GraphEdge {
        private String to;
        private TableRelationship relationship;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PathState {
        private String node;
        private List<GraphEdge> path;
        private Set<String> visited;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PathResult {
        private List<GraphEdge> edges;
    }
}
