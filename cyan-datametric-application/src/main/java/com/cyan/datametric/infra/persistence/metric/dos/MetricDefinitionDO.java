package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标定义表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_definition")
public class MetricDefinitionDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 指标编码
     */
    @TableField("metric_code")
    private String metricCode;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 指标类型
     */
    @TableField("metric_type")
    private MetricType metricType;

    /**
     * 关联主题域编码
     */
    @TableField("subject_code")
    private String subjectCode;

    /**
     * 业务口径
     */
    @TableField("biz_caliber")
    private String bizCaliber;

    /**
     * 技术口径
     */
    @TableField("tech_caliber")
    private String techCaliber;

    /**
     * 状态
     */
    @TableField("status")
    private MetricStatus status;

    /**
     * 负责人
     */
    @TableField("owner")
    private String owner;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 修改人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
