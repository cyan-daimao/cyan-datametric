package com.cyan.datametric.application.config;

import com.cyan.datametric.application.config.bo.TimePeriodBO;
import com.cyan.datametric.application.config.cmd.TimePeriodCmd;
import com.cyan.datametric.domain.config.TimePeriod;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.repository.TimePeriodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 时间周期服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
public class TimePeriodService {

    private final TimePeriodRepository timePeriodRepository;

    public TimePeriodService(TimePeriodRepository timePeriodRepository) {
        this.timePeriodRepository = timePeriodRepository;
    }

    public TimePeriodBO create(TimePeriodCmd cmd) {
        if (cmd.getPeriodCode() == null || cmd.getPeriodCode().isBlank()) {
            cmd.setPeriodCode("PERIOD_" + System.currentTimeMillis());
        }
        TimePeriod timePeriod = ConfigAppConvert.INSTANCE.toTimePeriod(cmd);
        timePeriod = timePeriod.save(timePeriodRepository);
        return ConfigAppConvert.INSTANCE.toTimePeriodBO(timePeriod);
    }

    public TimePeriodBO update(String id, TimePeriodCmd cmd) {
        TimePeriod timePeriod = ConfigAppConvert.INSTANCE.toTimePeriod(cmd);
        timePeriod.setId(id);
        timePeriod = timePeriod.update(timePeriodRepository);
        return ConfigAppConvert.INSTANCE.toTimePeriodBO(timePeriod);
    }

    public void delete(String id) {
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.setId(id);
        timePeriod.delete(timePeriodRepository);
    }

    public TimePeriodBO detail(String id) {
        TimePeriod timePeriod = timePeriodRepository.findById(id);
        return ConfigAppConvert.INSTANCE.toTimePeriodBO(timePeriod);
    }

    public List<TimePeriodBO> listAll() {
        return timePeriodRepository.listAll().stream()
                .map(ConfigAppConvert.INSTANCE::toTimePeriodBO)
                .toList();
    }
}
