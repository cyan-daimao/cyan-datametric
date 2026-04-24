package com.cyan.datametric.infra.persistence.config.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 修饰词表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_modifier")
public class MetricModifierDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 修饰词编码
     */
    @TableField("modifier_code")
    private String modifierCode;

    /**
     * 修饰词名称
     */
    @TableField("modifier_name")
    private String modifierName;

    /**
     * 关联字段名
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 运算符
     */
    @TableField("operator")
    private String operator;

    /**
     * 可选值JSON
     */
    @TableField("field_values")
    private String fieldValues;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

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
