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
     * 数据源名称
     */
    @TableField("ds_name")
    private String dsName;

    /**
     * 数据库名称
     */
    @TableField("db_name")
    private String dbName;

    /**
     * 表名称
     */
    @TableField("tbl_name")
    private String tblName;

    /**
     * 字段名称
     */
    @TableField("col_name")
    private String colName;

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
