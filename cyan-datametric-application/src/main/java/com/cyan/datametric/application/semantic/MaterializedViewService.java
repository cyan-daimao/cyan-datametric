package com.cyan.datametric.application.semantic;

import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.semantic.MaterializedView;
import com.cyan.datametric.domain.semantic.repository.MaterializedViewRepository;
import com.cyan.datametric.enums.semantic.MvStatus;
import com.cyan.datametric.enums.semantic.RefreshStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物化视图服务
 * <p>
 * 管理物化视图的生命周期：创建、刷新、删除、状态监控、自动淘汰。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterializedViewService {

    private final MaterializedViewRepository materializedViewRepository;

    @Value("${datametric.mv.evict.minHitCount:10}")
    private long evictMinHitCount;

    @Value("${datametric.mv.evict.idleDays:30}")
    private int evictIdleDays;

    @Value("${datametric.mv.evict.minStorageSize:104857600}")
    private long evictMinStorageSize;

    /**
     * 创建物化视图
     */
    public MaterializedView create(MaterializedView mv) {
        Assert.isNull(materializedViewRepository.findById(mv.getId()), new BusinessException("物化视图已存在"));
        return mv.save(materializedViewRepository);
    }

    /**
     * 更新物化视图
     */
    public MaterializedView update(MaterializedView mv) {
        MaterializedView existing = materializedViewRepository.findById(mv.getId());
        Assert.notNull(existing, new BusinessException("物化视图不存在"));
        return mv.update(materializedViewRepository);
    }

    /**
     * 删除物化视图
     */
    public void delete(String id) {
        MaterializedView mv = materializedViewRepository.findById(id);
        Assert.notNull(mv, new BusinessException("物化视图不存在"));
        mv.delete(materializedViewRepository);
    }

    /**
     * 获取物化视图详情
     */
    public MaterializedView getById(String id) {
        MaterializedView mv = materializedViewRepository.findById(id);
        Assert.notNull(mv, new BusinessException("物化视图不存在"));
        return mv;
    }

    /**
     * 触发刷新
     */
    public void refresh(String id) {
        MaterializedView mv = materializedViewRepository.findById(id);
        Assert.notNull(mv, new BusinessException("物化视图不存在"));
        if (mv.getStatus() == MvStatus.REFRESHING) {
            throw new BusinessException("物化视图正在刷新中，请稍后再试");
        }

        mv.markRefreshing(materializedViewRepository);
        try {
            // TODO: 调用 StarRocks 或数据网关执行刷新 SQL
            // 1. 根据 refreshStrategy 生成刷新 SQL
            // 2. 执行 INSERT OVERWRITE 或 INSERT INTO
            // 3. 更新存储大小统计
            log.info("开始刷新物化视图: id={}, name={}, strategy={}", id, mv.getName(), mv.getRefreshStrategy());

            // 模拟刷新完成
            mv.markActive(materializedViewRepository);
            log.info("物化视图刷新完成: id={}", id);
        } catch (Exception e) {
            log.error("物化视图刷新失败: id={}", id, e);
            mv.markFailed(materializedViewRepository);
            throw new BusinessException("物化视图刷新失败: " + e.getMessage());
        }
    }

    /**
     * 全量刷新（适用于 FULL 策略）
     */
    public void refreshFull(MaterializedView mv) {
        log.info("执行全量刷新: mvId={}, name={}", mv.getId(), mv.getName());
        // INSERT OVERWRITE mv_name AS {definitionSql}
    }

    /**
     * 增量刷新（适用于 INCREMENTAL 策略）
     */
    public void refreshIncremental(MaterializedView mv) {
        log.info("执行增量刷新: mvId={}, name={}, lastRefreshTime={}", mv.getId(), mv.getName(), mv.getLastRefreshTime());
        // INSERT INTO mv_name {definitionSql} WHERE timeColumn > lastRefreshTime
    }

    /**
     * 自动淘汰：扫描并清理长期未命中的物化视图
     */
    public void autoEvict() {
        List<MaterializedView> allMvs = materializedViewRepository.findActiveAll();
        int evictCount = 0;
        for (MaterializedView mv : allMvs) {
            if (mv.shouldEvict(evictMinHitCount, evictIdleDays, evictMinStorageSize)) {
                log.warn("物化视图满足淘汰条件: id={}, name={}, hitCount={}, lastHitTime={}, storageSize={}",
                        mv.getId(), mv.getName(), mv.getHitCount(), mv.getLastHitTime(), mv.getStorageSize());
                // 软删除：标记为 DISABLED，7 天后物理删除
                mv.setStatus(MvStatus.DISABLED);
                mv.setUpdatedAt(LocalDateTime.now());
                materializedViewRepository.update(mv);
                evictCount++;
            }
        }
        log.info("自动淘汰扫描完成: 共扫描 {} 个，淘汰 {} 个", allMvs.size(), evictCount);
    }

    /**
     * 获取所有活跃物化视图（供查询路由使用）
     */
    public List<MaterializedView> listActive() {
        return materializedViewRepository.findActiveAll();
    }
}
