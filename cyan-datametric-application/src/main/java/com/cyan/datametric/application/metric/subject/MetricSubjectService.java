package com.cyan.datametric.application.metric.subject;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import com.cyan.datametric.application.metric.subject.cmd.MetricSubjectCmd;
import com.cyan.datametric.domain.metric.subject.query.MetricSubjectQuery;

import java.util.List;

/**
 * 指标主题域服务接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricSubjectService {

    /**
     * 创建主题域
     */
    MetricSubjectBO create(MetricSubjectCmd cmd);

    /**
     * 更新主题域
     */
    MetricSubjectBO update(String id, MetricSubjectCmd cmd);

    /**
     * 删除主题域
     */
    void delete(String id);

    /**
     * 详情
     */
    MetricSubjectBO detail(String id);

    /**
     * 分页查询
     */
    Page<MetricSubjectBO> page(MetricSubjectQuery query);

    /**
     * 树形结构查询
     */
    List<MetricSubjectBO> tree();
}
