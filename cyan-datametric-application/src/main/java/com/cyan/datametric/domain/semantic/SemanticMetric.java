package com.cyan.datametric.domain.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.repository.SemanticMetricRepository;
import com.cyan.datametric.enums.MetricType;
import com.cyan.datametric.enums.StatFunc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义指标领域对象（充血模型）
 * <p>
 * Phase 3 统一语义层的核心概念：指标在语义层中不再仅绑定到物理字段，
 * 而是绑定到逻辑表（LogicalTable）+ 逻辑字段，支持跨表自动 JOIN 分析。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class SemanticMetric {

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
     * 指标类型：ATOMIC / DERIVED / COMPOSITE
     */
    private MetricType metricType;

    /**
     * 来源逻辑表ID
     */
    private String sourceTableId;

    /**
     * 来源字段
     */
    private String sourceColumn;

    /**
     * 聚合函数
     */
    private StatFunc statFunc;

    /**
     * 复合指标公式（如 "${M001} / ${M002}"）
     */
    private String formula;

    /**
     * 修饰词 ID 列表
     */
    private List<String> modifiers;

    /**
     * 时间周期配置ID
     */
    private String timePeriodId;

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

    private static final Pattern COMPOSITE_REF_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 校验
     */
    private void validate() {
        Assert.notBlank(this.metricCode, new BusinessException("指标编码不能为空"));
        Assert.notBlank(this.metricName, new BusinessException("指标名称不能为空"));
        Assert.notNull(this.metricType, new BusinessException("指标类型不能为空"));
        Assert.notBlank(this.sourceTableId, new BusinessException("来源逻辑表ID不能为空"));
        if (this.metricType == MetricType.COMPOSITE) {
            Assert.notBlank(this.formula, new BusinessException("复合指标公式不能为空"));
        } else {
            Assert.notBlank(this.sourceColumn, new BusinessException("来源字段不能为空"));
            Assert.notNull(this.statFunc, new BusinessException("聚合函数不能为空"));
        }
    }

    /**
     * 保存
     */
    public SemanticMetric save(SemanticMetricRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新
     */
    public SemanticMetric update(SemanticMetricRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除
     */
    public void delete(SemanticMetricRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 是否为基础指标（原子或派生）
     */
    public boolean isBaseMetric() {
        return this.metricType == MetricType.ATOMIC || this.metricType == MetricType.DERIVED;
    }

    /**
     * 是否为复合指标
     */
    public boolean isComposite() {
        return this.metricType == MetricType.COMPOSITE;
    }

    /**
     * 构建聚合表达式 SQL 片段
     *
     * @param tableAlias 表别名
     * @return 如 "SUM(t0.amount)"
     */
    public String buildAggExpression(String tableAlias) {
        if (!isBaseMetric()) {
            throw new BusinessException("复合指标不支持直接构建聚合表达式");
        }
        String col = tableAlias != null ? tableAlias + "." + this.sourceColumn : this.sourceColumn;
        String func = this.statFunc == null ? "SUM" : this.statFunc.getCode();
        if ("COUNT_DISTINCT".equals(func)) {
            return "COUNT(DISTINCT " + col + ")";
        }
        return func + "(" + col + ")";
    }

    /**
     * 提取复合指标引用的指标编码列表
     */
    public List<String> extractRefMetricCodes() {
        List<String> refs = new ArrayList<>();
        if (this.formula == null) {
            return refs;
        }
        Matcher matcher = COMPOSITE_REF_PATTERN.matcher(this.formula);
        while (matcher.find()) {
            refs.add(matcher.group(1));
        }
        return refs;
    }
}
