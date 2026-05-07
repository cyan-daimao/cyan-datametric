package com.cyan.datametric.infra.persistence.semantic.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.semantic.QueryPlan;
import com.cyan.datametric.domain.semantic.repository.QueryPlanRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 查询计划仓储实现（简化版，可扩展为真实持久化）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class QueryPlanRepositoryImpl implements QueryPlanRepository {

    @Override
    public QueryPlan findById(String id) {
        return null;
    }

    @Override
    public List<QueryPlan> findByQueryHash(String queryHash) {
        return List.of();
    }

    @Override
    public QueryPlan save(QueryPlan queryPlan) {
        // TODO: 接入真实数据库存储，当前为无操作（不影响主流程）
        return queryPlan;
    }

    @Override
    public List<QueryPlan> findRecent(int limit) {
        return List.of();
    }
}
