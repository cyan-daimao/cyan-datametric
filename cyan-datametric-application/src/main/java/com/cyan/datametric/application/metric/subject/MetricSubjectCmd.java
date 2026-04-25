package com.cyan.datametric.application.metric.subject.cmd;

import lombok.Data;

/**
 * 指标主题域命令
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
public class MetricSubjectCmd {

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
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
