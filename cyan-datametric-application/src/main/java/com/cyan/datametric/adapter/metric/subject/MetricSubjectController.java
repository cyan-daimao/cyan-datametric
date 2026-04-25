package com.cyan.datametric.adapter.metric.subject;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.metric.subject.convert.MetricSubjectAdapterConvert;
import com.cyan.datametric.adapter.metric.subject.dto.MetricSubjectDTO;
import com.cyan.datametric.application.metric.subject.MetricSubjectService;
import com.cyan.datametric.application.metric.subject.bo.MetricSubjectBO;
import com.cyan.datametric.application.metric.subject.cmd.MetricSubjectCmd;
import com.cyan.datametric.domain.metric.subject.query.MetricSubjectQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指标主题域控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/subjects")
public class MetricSubjectController {

    private final MetricSubjectService metricSubjectService;

    public MetricSubjectController(MetricSubjectService metricSubjectService) {
        this.metricSubjectService = metricSubjectService;
    }

    @GetMapping
    public Response<PageResultDTO<MetricSubjectDTO>> page(MetricSubjectQuery query) {
        com.cyan.arch.common.api.Page<MetricSubjectBO> page = metricSubjectService.page(query);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(MetricSubjectAdapterConvert.INSTANCE::toMetricSubjectDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/tree")
    public Response<List<MetricSubjectDTO>> tree() {
        List<MetricSubjectBO> bos = metricSubjectService.tree();
        return Response.success(bos.stream()
                .map(MetricSubjectAdapterConvert.INSTANCE::toMetricSubjectDTO)
                .toList());
    }

    @GetMapping("/{id}")
    public Response<MetricSubjectDTO> detail(@PathVariable("id") String id) {
        MetricSubjectBO bo = metricSubjectService.detail(id);
        return Response.success(MetricSubjectAdapterConvert.INSTANCE.toMetricSubjectDTO(bo));
    }

    @PostMapping
    public Response<MetricSubjectDTO> create(@RequestBody @Valid MetricSubjectCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricSubjectBO bo = metricSubjectService.create(cmd);
        return Response.success(MetricSubjectAdapterConvert.INSTANCE.toMetricSubjectDTO(bo));
    }

    @PutMapping("/{id}")
    public Response<MetricSubjectDTO> update(@PathVariable("id") String id, @RequestBody @Valid MetricSubjectCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricSubjectBO bo = metricSubjectService.update(id, cmd);
        return Response.success(MetricSubjectAdapterConvert.INSTANCE.toMetricSubjectDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        metricSubjectService.delete(id);
        return Response.success();
    }
}
