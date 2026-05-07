package com.cyan.datametric.adapter.metric.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.metric.http.convert.MetricAdapterConvert;
import com.cyan.datametric.adapter.metric.http.dto.*;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指标控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;


    // ==================== 指标定义 ====================

    @GetMapping("/page")
    public Response<PageResultDTO<MetricDTO>> page(MetricPageQuery query) {
        String currentUser = UserContextHolder.getCurrentEmployee().getPassport();
        com.cyan.arch.common.api.Page<MetricBO> page = metricService.page(query, currentUser);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(MetricAdapterConvert.INSTANCE::toMetricDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/{id}")
    public Response<MetricDetailDTO> detail(@PathVariable("id") String id) {
        MetricBO bo = metricService.detail(id);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDetailDTO(bo));
    }

    @PostMapping("/atomic")
    public Response<MetricDTO> createAtomic(@RequestBody @Valid AtomicMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createAtomic(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/atomic/{id}")
    public Response<MetricDTO> updateAtomic(@PathVariable("id") String id, @RequestBody @Valid AtomicMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateAtomic(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PostMapping("/derived")
    public Response<MetricDTO> createDerived(@RequestBody @Valid DerivedMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createDerived(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/derived/{id}")
    public Response<MetricDTO> updateDerived(@PathVariable("id") String id, @RequestBody @Valid DerivedMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateDerived(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PostMapping("/composite")
    public Response<MetricDTO> createComposite(@RequestBody @Valid CompositeMetricCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.createComposite(cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @PutMapping("/composite/{id}")
    public Response<MetricDTO> updateComposite(@PathVariable("id") String id, @RequestBody @Valid CompositeMetricCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        MetricBO bo = metricService.updateComposite(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        metricService.delete(id);
        return Response.success();
    }

    @PutMapping("/{id}/status")
    public Response<MetricDTO> updateStatus(@PathVariable("id") String id, @RequestBody UpdateStatusCmd cmd) {
        MetricBO bo = metricService.updateStatus(id, cmd);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDTO(bo));
    }

    // ==================== SQL 预览与试算 ====================

    @PostMapping("/preview-sql")
    public Response<String> previewSql(@RequestBody SqlPreviewCmd cmd) {
        String sql = metricService.previewSql(cmd);
        return Response.success(sql);
    }

    // ==================== 指标字典 ====================

    @GetMapping("/dictionary/page")
    public Response<PageResultDTO<DictionaryMetricDTO>> dictionaryPage(MetricPageQuery query) {
        String currentUser = UserContextHolder.getCurrentEmployee().getPassport();
        com.cyan.arch.common.api.Page<MetricBO> page = metricService.dictionaryPage(query, currentUser);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(MetricAdapterConvert.INSTANCE::toDictionaryMetricDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @PostMapping("/{id}/favorite")
    public Response<Void> favorite(@PathVariable("id") String id) {
        String userId = UserContextHolder.getCurrentEmployee().getPassport();
        metricService.favorite(id, userId);
        return Response.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Response<Void> unfavorite(@PathVariable("id") String id) {
        String userId = UserContextHolder.getCurrentEmployee().getPassport();
        metricService.unfavorite(id, userId);
        return Response.success();
    }

    // ==================== 血缘 ====================

    @GetMapping("/{id}/lineage")
    public Response<LineageTreeDTO> lineage(
            @PathVariable("id") String id,
            @RequestParam(name = "direction", defaultValue = "BOTH") String direction,
            @RequestParam(name = "maxLevel", defaultValue = "3") int maxLevel) {
        LineageTreeBO bo = metricService.lineage(id, direction, maxLevel);
        return Response.success(MetricAdapterConvert.INSTANCE.toLineageTreeDTO(bo));
    }

    // ==================== 版本管理 ====================

    @GetMapping("/{id}/versions")
    public Response<List<MetricVersionDTO>> listVersions(@PathVariable("id") String id) {
        List<MetricVersionBO> list = metricService.listVersions(id);
        return Response.success(list.stream()
                .map(MetricAdapterConvert.INSTANCE::toMetricVersionDTO)
                .toList());
    }

    @PostMapping("/{id}/rollback/{version}")
    public Response<MetricDetailDTO> rollback(
            @PathVariable("id") String id,
            @PathVariable("version") Integer version) {
        MetricBO bo = metricService.rollback(id, version);
        return Response.success(MetricAdapterConvert.INSTANCE.toMetricDetailDTO(bo));
    }
}
