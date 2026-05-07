package com.cyan.datametric.infra.persistence.semantic.dos;

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
 * 表关联关系 DO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("semantic_table_relationship")
public class SemanticTableRelationshipDO {

    @TableId("id")
    private Long id;

    @TableField("left_table_id")
    private Long leftTableId;

    @TableField("right_table_id")
    private Long rightTableId;

    @TableField("join_type")
    private String joinType;

    @TableField("conditions_json")
    private String conditionsJson;

    @TableField("description")
    private String description;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
