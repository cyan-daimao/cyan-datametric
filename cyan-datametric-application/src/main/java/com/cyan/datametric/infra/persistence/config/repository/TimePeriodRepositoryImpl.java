package com.cyan.datametric.infra.persistence.config.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import com.cyan.datametric.infra.persistence.config.convert.ConfigInfraConvert;
import com.cyan.datametric.infra.persistence.config.dos.MetricTimePeriodDO;
import com.cyan.datametric.infra.persistence.config.mappers.MetricTimePeriodMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 时间周期仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class TimePeriodRepositoryImpl implements TimePeriodRepository {

    private final MetricTimePeriodMapper timePeriodMapper;

    public TimePeriodRepositoryImpl(MetricTimePeriodMapper timePeriodMapper) {
        this.timePeriodMapper = timePeriodMapper;
    }

    @Override
    public TimePeriod findById(String id) {
        MetricTimePeriodDO periodDO = timePeriodMapper.selectById(Long.parseLong(id));
        return ConfigInfraConvert.INSTANCE.toTimePeriod(periodDO);
    }

    @Override
    public List<TimePeriod> listAll() {
        List<MetricTimePeriodDO> list = timePeriodMapper.selectList(new LambdaQueryWrapper<>());
        return Optional.ofNullable(list).orElse(List.of()).stream()
                .map(ConfigInfraConvert.INSTANCE::toTimePeriod)
                .toList();
    }

    @Override
    public TimePeriod save(TimePeriod timePeriod) {
        long id = SnowflakeIdUtil.nextId();
        timePeriod.setId(String.valueOf(id));
        MetricTimePeriodDO periodDO = ConfigInfraConvert.INSTANCE.toTimePeriodDO(timePeriod);
        timePeriodMapper.insert(periodDO);
        return findById(timePeriod.getId());
    }

    @Override
    public TimePeriod update(TimePeriod timePeriod) {
        MetricTimePeriodDO periodDO = ConfigInfraConvert.INSTANCE.toTimePeriodDO(timePeriod);
        timePeriodMapper.updateById(periodDO);
        return findById(timePeriod.getId());
    }

    @Override
    public void deleteById(String id) {
        timePeriodMapper.deleteById(Long.parseLong(id));
    }
}
