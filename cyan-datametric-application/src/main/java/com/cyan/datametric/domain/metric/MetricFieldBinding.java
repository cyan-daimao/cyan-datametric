package com.cyan.datametric.domain.metric;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.metric.repository.MetricFieldBindingRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标物理字段绑定领域对象
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MetricFieldBinding {

    /**
     * 主键
     */
    private String id;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * catalog 名称
     */
    private String catalogName;

    /**
     * schema 名称
     */
    private String schemaName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 过滤条件
     */
    private List<MetricAtomicExt.FilterCondition> filterCondition;

    /**
     * 是否主绑定
     */
    private Boolean primaryBinding;

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
     * 完整表引用
     */
    public String tableRef(String defaultCatalog) {
        String catalog = hasText(catalogName) ? catalogName : defaultCatalog;
        return catalog + "." + schemaName + "." + tableName;
    }

    /**
     * 保存绑定
     */
    public MetricFieldBinding save(MetricFieldBindingRepository repository) {
        validate();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新绑定
     */
    public MetricFieldBinding update(MetricFieldBindingRepository repository) {
        Assert.notBlank(this.id, new BusinessException("绑定ID不能为空"));
        validate();
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除绑定
     */
    public void delete(MetricFieldBindingRepository repository) {
        Assert.notBlank(this.id, new BusinessException("绑定ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 校验绑定
     */
    private void validate() {
        Assert.notBlank(this.metricId, new BusinessException("指标ID不能为空"));
        Assert.notBlank(this.schemaName, new BusinessException("指标绑定必须配置 schema"));
        Assert.notBlank(this.tableName, new BusinessException("指标绑定必须配置表名"));
        Assert.isTrue(hasText(this.columnName) || hasText(this.sourceExpr),
                new BusinessException("指标绑定必须配置字段或表达式"));
        if (this.primaryBinding == null) {
            this.primaryBinding = false;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
