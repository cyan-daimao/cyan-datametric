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
 * 逻辑表 DO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("semantic_logical_table")
public class SemanticLogicalTableDO {

    @TableId("id")
    private Long id;

    @TableField("table_name")
    private String tableName;

    @TableField("display_name")
    private String displayName;

    @TableField("table_type")
    private String tableType;

    @TableField("primary_key")
    private String primaryKey;

    @TableField("time_column")
    private String timeColumn;

    @TableField("schema_json")
    private String schemaJson;

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
