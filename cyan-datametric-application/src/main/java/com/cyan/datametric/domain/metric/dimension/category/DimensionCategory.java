package com.cyan.datametric.domain.metric.dimension.category;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 维度分类领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class DimensionCategory {

    /**
     * 主键
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private String parentId;

    /**
     * 层级 1-2
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
        Assert.notBlank(this.name, new BusinessException("分类名称不能为空"));
    }

    /**
     * 计算层级
     */
    private void calculateLevel(DimensionCategoryRepository repository) {
        if (this.parentId == null || this.parentId.isBlank()) {
            this.level = 1;
            this.parentId = null;
        } else {
            DimensionCategory parent = repository.findById(this.parentId);
            Assert.notNull(parent, new BusinessException("父分类不存在"));
            int newLevel = parent.getLevel() + 1;
            Assert.isTrue(newLevel <= 2, new BusinessException("维度分类最多支持2级"));
            this.level = newLevel;
        }
    }

    /**
     * 保存维度分类
     */
    public DimensionCategory save(DimensionCategoryRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        calculateLevel(repository);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新维度分类
     */
    public DimensionCategory update(DimensionCategoryRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        calculateLevel(repository);
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除维度分类
     */
    public void delete(DimensionCategoryRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }
}
