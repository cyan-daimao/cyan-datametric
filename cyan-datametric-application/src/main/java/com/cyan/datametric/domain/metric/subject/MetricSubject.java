package com.cyan.datametric.domain.metric.subject;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标主题域领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MetricSubject {

    /**
     * 主键
     */
    private String id;

    /**
     * 主题域编码
     */
    private String subjectCode;

    /**
     * 主题域名称
     */
    private String subjectName;

    /**
     * 主题域描述
     */
    private String subjectDesc;

    /**
     * 父节点ID
     */
    private String parentId;

    /**
     * 层级 1-3
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;

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
     * 校验基础信息
     */
    private void validate() {
        Assert.notBlank(this.subjectCode, new BusinessException("主题域编码不能为空"));
        Assert.notBlank(this.subjectName, new BusinessException("主题域名称不能为空"));
    }

    /**
     * 计算层级
     */
    private void calculateLevel(MetricSubjectRepository repository) {
        if (this.parentId == null || this.parentId.isBlank()) {
            this.level = 1;
            this.parentId = null;
        } else {
            MetricSubject parent = repository.findById(this.parentId);
            Assert.notNull(parent, new BusinessException("父节点不存在"));
            int newLevel = parent.getLevel() + 1;
            Assert.isTrue(newLevel <= 3, new BusinessException("主题域最多支持3级"));
            this.level = newLevel;
        }
    }

    /**
     * 保存主题域
     */
    public MetricSubject save(MetricSubjectRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        calculateLevel(repository);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新主题域
     */
    public MetricSubject update(MetricSubjectRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        calculateLevel(repository);
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除主题域
     */
    public void delete(MetricSubjectRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }
}
