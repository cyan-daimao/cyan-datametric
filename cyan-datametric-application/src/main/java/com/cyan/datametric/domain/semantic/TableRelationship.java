package com.cyan.datametric.domain.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.repository.TableRelationshipRepository;
import com.cyan.datametric.enums.semantic.JoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表关联关系领域对象（充血模型）
 * <p>
 * 定义两张逻辑表之间的 JOIN 关系，支持多字段关联条件。
 * 是星型/雪花/星座模型在语义层的直接体现。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class TableRelationship {

    /**
     * 主键
     */
    private String id;

    /**
     * 左表ID（通常为主表/事实表）
     */
    private String leftTableId;

    /**
     * 右表ID（通常为维度表）
     */
    private String rightTableId;

    /**
     * JOIN 类型
     */
    private JoinType joinType;

    /**
     * 关联条件列表（支持复合关联）
     */
    private List<JoinCondition> conditions;

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

    /**
     * 校验
     */
    private void validate() {
        Assert.notBlank(this.leftTableId, new BusinessException("左表ID不能为空"));
        Assert.notBlank(this.rightTableId, new BusinessException("右表ID不能为空"));
        Assert.notNull(this.joinType, new BusinessException("JOIN类型不能为空"));
        Assert.notEmpty(this.conditions, new BusinessException("关联条件不能为空"));
        for (JoinCondition condition : this.conditions) {
            Assert.notBlank(condition.getLeftColumn(), new BusinessException("关联条件左字段不能为空"));
            Assert.notBlank(condition.getRightColumn(), new BusinessException("关联条件右字段不能为空"));
        }
    }

    /**
     * 保存
     */
    public TableRelationship save(TableRelationshipRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新
     */
    public TableRelationship update(TableRelationshipRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除
     */
    public void delete(TableRelationshipRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 生成 JOIN SQL 片段
     *
     * @param leftAlias  左表别名
     * @param rightAlias 右表别名
     * @return 如 "LEFT JOIN dim_user t1 ON t0.user_id = t1.id"
     */
    public String buildJoinSql(String leftAlias, String rightAlias, String rightTableName) {
        String joinKeyword = this.joinType.getCode() + " JOIN ";
        String onClause = this.conditions.stream()
                .map(c -> leftAlias + "." + c.getLeftColumn() + " = " + rightAlias + "." + c.getRightColumn())
                .collect(Collectors.joining(" AND "));
        return joinKeyword + rightTableName + " " + rightAlias + " ON " + onClause;
    }

    /**
     * 关联条件内部类
     */
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinCondition {
        private String leftColumn;
        private String rightColumn;
    }
}
