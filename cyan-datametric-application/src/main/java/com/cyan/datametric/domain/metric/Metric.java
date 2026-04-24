package com.cyan.datametric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.enums.MetricStatus;
import com.cyan.datametric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Metric {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private MetricType metricType;

    /**
     * 关联主题域编码
     */
    private String subjectCode;

    /**
     * 业务口径
     */
    private String bizCaliber;

    /**
     * 技术口径
     */
    private String techCaliber;

    /**
     * 状态
     */
    private MetricStatus status;

    /**
     * 负责人
     */
    private String owner;

    /**
     * 版本号
     */
    private Integer version;

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

    /**
     * 原子指标扩展
     */
    private MetricAtomicExt atomicExt;

    /**
     * 派生指标扩展
     */
    private MetricDerivedExt derivedExt;

    /**
     * 复合指标扩展
     */
    private MetricCompositeExt compositeExt;

    /**
     * 校验基础信息
     */
    private void validate() {
        Assert.notBlank(this.metricName, new BusinessException("指标名称不能为空"));
        Assert.notNull(this.metricType, new BusinessException("指标类型不能为空"));
        Assert.notBlank(this.bizCaliber, new BusinessException("业务口径不能为空"));
        Assert.notBlank(this.subjectCode, new BusinessException("主题域编码不能为空"));
    }

    /**
     * 保存指标
     */
    public Metric save(MetricRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        this.status = MetricStatus.DRAFT;
        this.version = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新指标
     */
    public Metric update(MetricRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        if (this.status == MetricStatus.OFFLINE) {
            throw new BusinessException("已下线的指标不可编辑");
        }
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除指标
     */
    public void delete(MetricRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 发布指标
     */
    public Metric publish(MetricRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        Assert.isTrue(this.status == MetricStatus.DRAFT, new BusinessException("只有草稿状态的指标可发布"));
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 下线指标
     */
    public Metric offline(MetricRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        Assert.isTrue(this.status == MetricStatus.PUBLISHED, new BusinessException("只有已发布状态的指标可下线"));
        this.status = MetricStatus.OFFLINE;
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }
}
