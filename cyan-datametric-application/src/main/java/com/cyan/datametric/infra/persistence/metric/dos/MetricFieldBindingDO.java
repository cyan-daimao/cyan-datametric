package com.cyan.datametric.infra.persistence.metric.dos;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 指标字段绑定表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_field_binding")
public class MetricFieldBindingDO {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 指标ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * catalog 名称
     */
    @TableField("catalog_name")
    private String catalogName;

    /**
     * schema 名称
     */
    @TableField("schema_name")
    private String schemaName;

    /**
     * 表名称
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 字段名称
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 来源表达式
     */
    @TableField("source_expr")
    private String sourceExpr;

    /**
     * 过滤条件JSON
     */
    @TableField("filter_condition")
    private String filterCondition;

    /**
     * 是否主绑定
     */
    @TableField("is_primary")
    private Boolean primaryBinding;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
     * 删除时间
     */
    @TableField("deleted_at")
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
