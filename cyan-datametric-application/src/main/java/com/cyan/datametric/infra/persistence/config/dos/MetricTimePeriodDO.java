package com.cyan.datametric.infra.persistence.config.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.enums.RelativeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间周期表
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
@TableName("metric_time_period")
public class MetricTimePeriodDO {

    /**
     * 主键
     */
    @TableId("id")
    private Long id;

    /**
     * 周期编码
     */
    @TableField("period_code")
    private String periodCode;

    /**
     * 周期名称
     */
    @TableField("period_name")
    private String periodName;

    /**
     * 类型
     */
    @TableField("period_type")
    private PeriodType periodType;

    /**
     * 相对偏移值
     */
    @TableField("relative_value")
    private Integer relativeValue;

    /**
     * 相对单位
     */
    @TableField("relative_unit")
    private RelativeUnit relativeUnit;

    /**
     * 绝对日期范围-开始
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 绝对日期范围-结束
     */
    @TableField("end_date")
    private LocalDate endDate;

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
