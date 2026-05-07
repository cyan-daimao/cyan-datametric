package com.cyan.datametric.domain.config;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公共维度领域对象（充血模型）
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Dimension {

    /**
     * 主键
     */
    private String id;

    /**
     * 维度编码
     */
    private String dimCode;

    /**
     * 维度名称
     */
    private String dimName;

    /**
     * 维度类型
     */
    private String dimType;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 维度可选值
     */
    private List<String> dimValues;

    /**
     * 维度分类ID
     */
    private String categoryId;

    /**
     * 数仓维表所在 schema
     */
    private String schema;

    /**
     * 关联数仓维表名
     */
    private String tableName;

    /**
     * 关联维表字段名
     */
    private String columnName;

    /**
     * 显示字段名（BI展示用）
     */
    private String displayColumn;

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
        Assert.notBlank(this.dimCode, new BusinessException("维度编码不能为空"));
        Assert.notBlank(this.dimName, new BusinessException("维度名称不能为空"));
    }

    public Dimension save(DimensionRepository repository) {
        validate();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    public Dimension update(DimensionRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    public void delete(DimensionRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }
}
