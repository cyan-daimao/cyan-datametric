package com.cyan.datametric.domain.config.repository;

import com.cyan.datametric.domain.config.TimePeriod;

import java.util.List;

/**
 * 时间周期仓储接口
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface TimePeriodRepository {

    /**
     * 根据ID查询
     */
    TimePeriod findById(String id);

    /**
     * 查询全部
     */
    List<TimePeriod> listAll();

    /**
     * 保存
     */
    TimePeriod save(TimePeriod timePeriod);

    /**
     * 更新
     */
    TimePeriod update(TimePeriod timePeriod);

    /**
     * 删除
     */
    void deleteById(String id);
}
