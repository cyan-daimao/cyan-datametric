package com.cyan.datametric.domain.metric.subject.repository;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.query.MetricSubjectQuery;

import java.util.List;

/**
 * 指标主题域仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface MetricSubjectRepository {

    /**
     * 根据ID查询
     */
    MetricSubject findById(String id);

    /**
     * 分页查询
     */
    Page<MetricSubject> page(MetricSubjectQuery query);

    /**
     * 查询全部
     */
    List<MetricSubject> findAll();

    /**
     * 保存
     */
    MetricSubject save(MetricSubject subject);

    /**
     * 更新
     */
    MetricSubject update(MetricSubject subject);

    /**
     * 删除
     */
    void deleteById(String id);

    /**
     * 根据主题域编码列表查询
     */
    List<MetricSubject> findBySubjectCodes(List<String> subjectCodes);

    /**
     * 根据主题域编码查询
     */
    MetricSubject findBySubjectCode(String subjectCode);
}
