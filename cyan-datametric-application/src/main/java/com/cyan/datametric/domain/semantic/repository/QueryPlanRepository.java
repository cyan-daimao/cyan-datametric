package com.cyan.datametric.domain.semantic.repository;

import com.cyan.datametric.domain.semantic.QueryPlan;

import java.util.List;

/**
 * 查询计划仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface QueryPlanRepository {

    /**
     * 根据ID查询
     */
    QueryPlan findById(String id);

    /**
     * 根据查询哈希查询历史记录
     */
    List<QueryPlan> findByQueryHash(String queryHash);

    /**
     * 保存
     */
    QueryPlan save(QueryPlan queryPlan);

    /**
     * 分页查询最近N条
     */
    List<QueryPlan> findRecent(int limit);
}
