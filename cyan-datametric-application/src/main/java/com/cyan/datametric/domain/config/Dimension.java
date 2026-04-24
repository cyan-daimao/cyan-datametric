package com.cyan.datametric.domain.config;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

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
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tblName;

    /**
     * 字段名称
     */
    private String colName;

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
        Assert.notBlank(this.dsName, new BusinessException("数据源名称不能为空"));
        Assert.notBlank(this.dbName, new BusinessException("数据库名称不能为空"));
        Assert.notBlank(this.tblName, new BusinessException("表名称不能为空"));
        Assert.notBlank(this.colName, new BusinessException("字段名称不能为空"));
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
