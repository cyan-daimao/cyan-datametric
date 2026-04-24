package com.cyan.datametric.domain.config;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修饰词领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Modifier {

    /**
     * 主键
     */
    private String id;

    /**
     * 修饰词编码
     */
    private String modifierCode;

    /**
     * 修饰词名称
     */
    private String modifierName;

    /**
     * 关联字段名
     */
    private String fieldName;

    /**
     * 运算符
     */
    private String operator;

    /**
     * 可选值
     */
    private List<String> fieldValues;

    /**
     * 描述
     */
    private String description;

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
        Assert.notBlank(this.modifierName, new BusinessException("修饰词名称不能为空"));
        Assert.notBlank(this.fieldName, new BusinessException("关联字段名不能为空"));
        Assert.notBlank(this.operator, new BusinessException("运算符不能为空"));
    }

    public Modifier save(ModifierRepository repository) {
        validate();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    public Modifier update(ModifierRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    public void delete(ModifierRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }
}
