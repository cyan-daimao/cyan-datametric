package com.cyan.datametric.adapter.metric.subject.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标主题域DTO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class MetricSubjectDTO {

    /**
     * 主键
     */
    private String id;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 主题域名称
     */
    private String subjectName;

    /**
     * 主题域描述
     */
    private String subjectDesc;

    /**
     * 父节点ID
     */
    private String parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 子节点
     */
    private List<MetricSubjectDTO> children;
}
