package com.cyan.datametric.adapter.metric.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.metric.http.convert.MetricAdapterConvert;
import com.cyan.datametric.adapter.metric.http.dto.*;
import com.cyan.datametric.application.metric.MetricService;
import com.cyan.datametric.application.metric.bo.*;
import com.cyan.datametric.application.metric.cmd.*;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.domain.metric.Metric;
import com.cyan.datametric.domain.metric.query.MetricPageQuery;
import com.cyan.datametric.domain.metric.repository.MetricRepository;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final MetricRepository metricRepository;
    private final DimensionRepository dimensionRepository;
    private final MetricSubjectRepository metricSubjectRepository;
    private final DimensionCategoryRepository dimensionCategoryRepository;


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

    // ==================== BI 分析 ====================

    @GetMapping("/bi/list")
    public Response<List<MetricBiListItemDTO>> biList(
            @RequestParam(name = "name", required = false) String name) {
        MetricPageQuery query = new MetricPageQuery();
        query.setStatus("PUBLISHED");
        query.setMetricName(name);
        query.setPageSize(9999);

        com.cyan.arch.common.api.Page<Metric> page = metricRepository.page(query);
        List<Metric> metrics = page.getData();

        List<String> subjectCodes = metrics.stream()
                .map(Metric::getSubjectCode)
                .filter(sc -> sc != null && !sc.isBlank())
                .distinct()
                .toList();

        Map<String, String> subjectNameMap;
        if (!subjectCodes.isEmpty()) {
            subjectNameMap = metricSubjectRepository.findBySubjectCodes(subjectCodes).stream()
                    .filter(s -> s.getSubjectCode() != null)
                    .collect(Collectors.toMap(
                            MetricSubject::getSubjectCode, MetricSubject::getSubjectName, (a, b) -> a));
        } else {
            subjectNameMap = Map.of();
        }

        List<MetricBiListItemDTO> list = metrics.stream()
                .map(m -> {
                    MetricBiListItemDTO dto = new MetricBiListItemDTO();
                    dto.setId(m.getId());
                    dto.setMetricCode(m.getMetricCode());
                    dto.setMetricName(m.getMetricName());
                    dto.setMetricType(m.getMetricType() == null ? null : m.getMetricType().getCode());
                    dto.setSubjectCode(m.getSubjectCode());
                    if (m.getSubjectCode() != null) {
                        dto.setSubjectName(subjectNameMap.get(m.getSubjectCode()));
                    }
                    if (m.getAtomicExt() != null && m.getAtomicExt().getStatFunc() != null) {
                        dto.setStatFunc(m.getAtomicExt().getStatFunc().getCode());
                    }
                    dto.setDataType(null);
                    dto.setDescription(m.getBizCaliber());
                    return dto;
                })
                .toList();

        return Response.success(list);
    }

    @GetMapping("/bi/dimensions")
    public Response<List<DimensionBiListItemDTO>> biDimensions(
            @RequestParam(name = "name", required = false) String name) {
        DimensionPageQuery query = new DimensionPageQuery();
        query.setDimName(name);
        query.setPageSize(9999);

        com.cyan.arch.common.api.Page<Dimension> page = dimensionRepository.page(query);
        List<Dimension> dimensions = page.getData();

        Set<String> categoryIds = dimensions.stream()
                .map(Dimension::getCategoryId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, String> categoryNameMap;
        if (!categoryIds.isEmpty()) {
            categoryNameMap = dimensionCategoryRepository.findAll().stream()
                    .filter(c -> c.getId() != null && categoryIds.contains(c.getId()))
                    .collect(Collectors.toMap(
                            com.cyan.datametric.domain.metric.dimension.category.DimensionCategory::getId,
                            com.cyan.datametric.domain.metric.dimension.category.DimensionCategory::getName,
                            (a, b) -> a));
        } else {
            categoryNameMap = Map.of();
        }

        List<DimensionBiListItemDTO> list = dimensions.stream()
                .map(d -> {
                    DimensionBiListItemDTO dto = new DimensionBiListItemDTO();
                    dto.setId(d.getId());
                    dto.setDimCode(d.getDimCode());
                    dto.setDimName(d.getDimName());
                    dto.setDimType(d.getDimType());
                    dto.setDataType(d.getDataType());
                    dto.setTableName(d.getTableName());
                    dto.setColumnName(d.getColumnName());
                    if (d.getCategoryId() != null) {
                        dto.setCategoryName(categoryNameMap.get(d.getCategoryId()));
                    }
                    return dto;
                })
                .toList();

        return Response.success(list);
    }
}
