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
 * 物化视图 DO
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("semantic_materialized_view")
public class SemanticMaterializedViewDO {

    @TableId("id")
    private Long id;

    @TableField("name")
    private String name;

    @TableField("definition_sql")
    private String definitionSql;

    @TableField("source_tables_json")
    private String sourceTablesJson;

    @TableField("dimensions_json")
    private String dimensionsJson;

    @TableField("metrics_json")
    private String metricsJson;

    @TableField("refresh_strategy")
    private String refreshStrategy;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("last_refresh_time")
    private LocalDateTime lastRefreshTime;

    @TableField("status")
    private String status;

    @TableField("hit_count")
    private Long hitCount;

    @TableField("last_hit_time")
    private LocalDateTime lastHitTime;

    @TableField("storage_size")
    private Long storageSize;

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
