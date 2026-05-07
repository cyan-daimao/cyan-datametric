package com.cyan.datametric.domain.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.repository.MaterializedViewRepository;
import com.cyan.datametric.enums.semantic.MvStatus;
import com.cyan.datametric.enums.semantic.RefreshStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物化视图领域对象（充血模型）
 * <p>
 * 定义预计算结果的元数据，包含刷新策略、命中统计、状态管理。
 * 支持自动淘汰：长期未命中且占用存储空间较大的视图会被标记为 DISABLED。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MaterializedView {

    /**
     * 主键
     */
    private String id;

    /**
     * 物化视图名称
     */
    private String name;

    /**
     * 定义 SQL（含 SELECT / FROM / JOIN / GROUP BY）
     */
    private String definitionSql;

    /**
     * 来源逻辑表 ID 列表
     */
    private List<String> sourceTables;

    /**
     * 物化维度字段列表（用于路由匹配）
     */
    private List<String> dimensions;

    /**
     * 物化指标编码列表（用于路由匹配）
     */
    private List<String> metrics;

    /**
     * 刷新策略
     */
    private RefreshStrategy refreshStrategy;

    /**
     * Cron 表达式（定时刷新用）
     */
    private String cronExpression;

    /**
     * 上次刷新时间
     */
    private LocalDateTime lastRefreshTime;

    /**
     * 状态
     */
    private MvStatus status;

    /**
     * 命中次数
     */
    private Long hitCount;

    /**
     * 上次命中时间
     */
    private LocalDateTime lastHitTime;

    /**
     * 存储大小（字节）
     */
    private Long storageSize;

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
        Assert.notBlank(this.name, new BusinessException("物化视图名称不能为空"));
        Assert.notBlank(this.definitionSql, new BusinessException("定义SQL不能为空"));
        Assert.notNull(this.refreshStrategy, new BusinessException("刷新策略不能为空"));
        Assert.notEmpty(this.sourceTables, new BusinessException("来源逻辑表不能为空"));
    }

    /**
     * 保存
     */
    public MaterializedView save(MaterializedViewRepository repository) {
        validate();
        Assert.isBlank(this.id, new BusinessException("新增时ID必须为空"));
        if (this.status == null) {
            this.status = MvStatus.ACTIVE;
        }
        if (this.hitCount == null) {
            this.hitCount = 0L;
        }
        if (this.storageSize == null) {
            this.storageSize = 0L;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.save(this);
    }

    /**
     * 更新
     */
    public MaterializedView update(MaterializedViewRepository repository) {
        validate();
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 删除
     */
    public void delete(MaterializedViewRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        repository.deleteById(this.id);
    }

    /**
     * 标记为刷新中
     */
    public MaterializedView markRefreshing(MaterializedViewRepository repository) {
        this.status = MvStatus.REFRESHING;
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 标记为可用
     */
    public MaterializedView markActive(MaterializedViewRepository repository) {
        this.status = MvStatus.ACTIVE;
        this.lastRefreshTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 标记为失败
     */
    public MaterializedView markFailed(MaterializedViewRepository repository) {
        this.status = MvStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }

    /**
     * 记录一次命中
     */
    public void recordHit(MaterializedViewRepository repository) {
        this.hitCount = (this.hitCount == null ? 0L : this.hitCount) + 1;
        this.lastHitTime = LocalDateTime.now();
        repository.update(this);
    }

    /**
     * 判断是否应自动淘汰
     *
     * @param minHitCount   最小命中次数阈值
     * @param idleDays      空闲天数阈值
     * @param minStorageSize 最小存储大小阈值（字节）
     * @return true 表示应淘汰
     */
    public boolean shouldEvict(long minHitCount, int idleDays, long minStorageSize) {
        if (this.status != MvStatus.ACTIVE) {
            return false;
        }
        if (this.hitCount != null && this.hitCount >= minHitCount) {
            return false;
        }
        if (this.storageSize == null || this.storageSize < minStorageSize) {
            return false;
        }
        if (this.lastHitTime == null) {
            return true;
        }
        return this.lastHitTime.plusDays(idleDays).isBefore(LocalDateTime.now());
    }
}
