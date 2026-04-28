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
 * 公共维度表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_dimension")
public class MetricDimensionDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 维度编码
     */
    @TableField("dim_code")
    private String dimCode;

    /**
     * 维度名称
     */
    @TableField("dim_name")
    private String dimName;

    /**
     * 维度类型
     */
    @TableField("dim_type")
    private String dimType;

    /**
     * 数据类型
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 维度可选值
     */
    @TableField("dim_values")
    private String dimValues;

    /**
     * 维度分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 关联数仓维表名
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 关联维表字段名
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 显示字段名（BI展示用）
     */
    @TableField("display_column")
    private String displayColumn;

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
