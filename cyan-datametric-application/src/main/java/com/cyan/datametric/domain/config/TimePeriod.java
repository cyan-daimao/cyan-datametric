package com.cyan.datametric.domain.config;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import com.cyan.datametric.enums.PeriodType;
import com.cyan.datametric.enums.RelativeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间周期领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class TimePeriod {

    /**
     * 主键
     */
    private String id;

    /**
     * 周期编码
     */
    private String periodCode;

    /**
     * 周期名称
     */
    private String periodName;

    /**
     * 类型
     */
    private PeriodType periodType;

    /**
     * 相对偏移值
     */
    private Integer relativeValue;

    /**
     * 相对单位
     */
    private RelativeUnit relativeUnit;

    /**
     * 绝对日期范围-开始
     */
    private LocalDate startDate;

    /**
     * 绝对日期范围-结束
     */
    private LocalDate endDate;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    private void validate() {
        Assert.notBlank(this.periodName, new BusinessException("周期名称不能为空"));
        Assert.notNull(this.periodType, new BusinessException("周期类型不能为空"));
        if (this.periodType == PeriodType.RELATIVE) {
            Assert.notNull(this.relativeValue, new BusinessException("相对偏移值不能为空"));
            Assert.notNull(this.relativeUnit, new BusinessException("相对单位不能为空"));
        }
    }

    public TimePeriod save(TimePeriodRepository repository) {
        validate();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    public TimePeriod update(TimePeriodRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    public void delete(TimePeriodRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }
}
