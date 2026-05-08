package com.cyan.datametric.application.config;

import com.cyan.datametric.application.config.bo.TimePeriodBO;
import com.cyan.datametric.application.config.cmd.TimePeriodCmd;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;

/**
 * 时间周期服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TimePeriodService {

    private final TimePeriodRepository timePeriodRepository;
    private final ConfigAppConvert configAppConvert;


    public TimePeriodBO create(TimePeriodCmd cmd) {
        if (cmd.getPeriodCode() == null || cmd.getPeriodCode().isBlank()) {
            cmd.setPeriodCode("PERIOD_" + System.currentTimeMillis());
        }
        TimePeriod timePeriod = configAppConvert.toTimePeriod(cmd);
        timePeriod = timePeriod.save(timePeriodRepository);
        return configAppConvert.toTimePeriodBO(timePeriod);
    }

    public TimePeriodBO update(String id, TimePeriodCmd cmd) {
        TimePeriod timePeriod = configAppConvert.toTimePeriod(cmd);
        timePeriod.setId(id);
        timePeriod = timePeriod.update(timePeriodRepository);
        return configAppConvert.toTimePeriodBO(timePeriod);
    }

    public void delete(String id) {
        TimePeriod timePeriod = timePeriodRepository.findById(id);
        Assert.notNull(timePeriod, new BusinessException("时间周期不存在"));
        timePeriod.delete(timePeriodRepository);
    }

    public TimePeriodBO detail(String id) {
        TimePeriod timePeriod = timePeriodRepository.findById(id);
        return configAppConvert.toTimePeriodBO(timePeriod);
    }

    public List<TimePeriodBO> listAll() {
        return timePeriodRepository.listAll().stream()
                .map(configAppConvert::toTimePeriodBO)
                .toList();
    }
}
