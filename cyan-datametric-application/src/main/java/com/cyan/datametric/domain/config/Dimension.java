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
     * 维度实现类型：NORMAL/DEGENERATE/HIERARCHY/DERIVED
     */
    private String dimensionKind;

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
    private String schemaName;

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
     * 来源类型：COLUMN/JSON_PATH/EXPRESSION
     */
    private String sourceType;

    /**
     * 来源表达式
     */
    private String sourceExpr;

    /**
     * 来源事实表
     */
    private String sourceTable;

    /**
     * 层级编码
     */
    private String hierarchyCode;

    /**
     * 层级名称
     */
    private String hierarchyName;

    /**
     * 父级维度编码
     */
    private String parentDimCode;

    /**
     * 层级级别
     */
    private Integer hierarchyLevel;

    /**
     * 排序号
     */
    private Integer sortOrder;

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
        if (this.dimensionKind == null || this.dimensionKind.isBlank()) {
            this.dimensionKind = "NORMAL";
        }
        switch (this.dimensionKind) {
            case "NORMAL" -> {
                Assert.notBlank(this.tableName, new BusinessException("普通维度必须配置关联维表"));
                Assert.notBlank(this.columnName, new BusinessException("普通维度必须配置关联字段"));
            }
            case "DEGENERATE" -> {
                Assert.notBlank(this.sourceTable, new BusinessException("退化维度必须配置来源事实表"));
                Assert.isTrue(hasColumnOrExpression(), new BusinessException("退化维度必须配置物理字段或来源表达式"));
            }
            case "HIERARCHY" -> {
                Assert.notBlank(this.tableName, new BusinessException("层级维度必须配置关联维表"));
                Assert.notBlank(this.columnName, new BusinessException("层级维度必须配置关联字段"));
                Assert.notBlank(this.hierarchyCode, new BusinessException("层级维度必须配置层级编码"));
                Assert.notNull(this.hierarchyLevel, new BusinessException("层级维度必须配置层级级别"));
            }
            case "DERIVED" -> {
                Assert.isTrue("EXPRESSION".equals(this.sourceType), new BusinessException("派生维度来源类型必须为 EXPRESSION"));
                Assert.notBlank(this.sourceExpr, new BusinessException("派生维度必须配置来源表达式"));
                Assert.isTrue((this.sourceTable != null && !this.sourceTable.isBlank())
                                || (this.tableName != null && !this.tableName.isBlank()),
                        new BusinessException("派生维度必须配置来源事实表或关联维表"));
            }
            default -> throw new BusinessException("不支持的维度实现类型: " + this.dimensionKind);
        }
    }

    /**
     * 是否配置了字段或表达式
     */
    private boolean hasColumnOrExpression() {
        return (this.columnName != null && !this.columnName.isBlank())
                || (this.sourceExpr != null && !this.sourceExpr.isBlank());
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
