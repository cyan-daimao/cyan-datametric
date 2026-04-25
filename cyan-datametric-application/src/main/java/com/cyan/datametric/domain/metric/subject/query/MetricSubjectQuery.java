package com.cyan.datametric.domain.metric.subject.query;

import com.cyan.arch.common.api.Pageable;
import lombok.Data;

/**
 * 指标主题域查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class MetricSubjectQuery implements Pageable {

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页大小
     */
    private long pageSize = 20;

    /**
     * 主题域名称（模糊）
     */
    private String subjectName;

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
