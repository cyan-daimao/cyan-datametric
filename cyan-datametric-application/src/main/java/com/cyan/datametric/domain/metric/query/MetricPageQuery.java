package com.cyan.datametric.domain.metric.query;

import com.cyan.arch.common.api.Pageable;
import lombok.Data;

/**
 * 指标分页查询
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class MetricPageQuery implements Pageable {

    /**
     * 页码
     */
    private long pageNum = 1;

    /**
     * 页大小
     */
    private long pageSize = 20;

    /**
     * 指标名称（模糊）
     */
    private String metricName;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 状态
     */
    private String status;

    /**
     * 只看收藏
     */
    private Boolean favorite;

    @Override
    public long current() {
        return pageNum;
    }

    @Override
    public long size() {
        return pageSize;
    }
}
