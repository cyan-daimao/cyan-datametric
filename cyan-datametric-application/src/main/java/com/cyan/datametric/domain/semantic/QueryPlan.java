package com.cyan.datametric.domain.semantic;

import com.cyan.datametric.enums.semantic.RouteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 查询计划领域对象
 * <p>
 * 记录每次查询的执行计划、路由决策、耗时等信息，用于：
 * 1. 命中率监控与物化视图优化建议
 * 2. 查询性能分析
 * 3. 查询特征缓存（queryHash -> mvId）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class QueryPlan {

    /**
     * 主键
     */
    private String id;

    /**
     * 查询特征哈希（指标编码集合 + 维度字段集合 的摘要）
     */
    private String queryHash;

    /**
     * 最终执行的 SQL
     */
    private String querySql;

    /**
     * 路由类型
     */
    private RouteType routeType;

    /**
     * 命中的物化视图 ID
     */
    private String mvId;

    /**
     * 耗时（毫秒）
     */
    private Long costTimeMs;

    /**
     * 是否命中缓存
     */
    private Boolean hitCache;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
