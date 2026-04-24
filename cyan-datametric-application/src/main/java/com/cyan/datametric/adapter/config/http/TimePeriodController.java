package com.cyan.datametric.adapter.config.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.config.http.convert.ConfigAdapterConvert;
import com.cyan.datametric.adapter.config.http.dto.TimePeriodDTO;
import com.cyan.datametric.application.config.TimePeriodService;
import com.cyan.datametric.application.config.bo.TimePeriodBO;
import com.cyan.datametric.application.config.cmd.TimePeriodCmd;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 时间周期控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/time-periods")
public class TimePeriodController {

    private final TimePeriodService timePeriodService;

    public TimePeriodController(TimePeriodService timePeriodService) {
        this.timePeriodService = timePeriodService;
    }

    @GetMapping
    public Response<List<TimePeriodDTO>> listAll() {
        List<TimePeriodBO> list = timePeriodService.listAll();
        return Response.success(list.stream()
                .map(ConfigAdapterConvert.INSTANCE::toTimePeriodDTO)
                .toList());
    }

    @GetMapping("/{id}")
    public Response<TimePeriodDTO> detail(@PathVariable("id") String id) {
        TimePeriodBO bo = timePeriodService.detail(id);
        return Response.success(ConfigAdapterConvert.INSTANCE.toTimePeriodDTO(bo));
    }

    @PostMapping
    public Response<TimePeriodDTO> create(@RequestBody @Valid TimePeriodCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        TimePeriodBO bo = timePeriodService.create(cmd);
        return Response.success(ConfigAdapterConvert.INSTANCE.toTimePeriodDTO(bo));
    }

    @PutMapping("/{id}")
    public Response<TimePeriodDTO> update(@PathVariable("id") String id, @RequestBody @Valid TimePeriodCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        TimePeriodBO bo = timePeriodService.update(id, cmd);
        return Response.success(ConfigAdapterConvert.INSTANCE.toTimePeriodDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        timePeriodService.delete(id);
        return Response.success();
    }
}
