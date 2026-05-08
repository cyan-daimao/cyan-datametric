package com.cyan.datametric.adapter.metric.dimension.category;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.metric.dimension.category.convert.DimensionCategoryAdapterConvert;
import com.cyan.datametric.adapter.metric.dimension.category.dto.DimensionCategoryDTO;
import com.cyan.datametric.application.metric.dimension.category.DimensionCategoryService;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import com.cyan.datametric.application.metric.dimension.category.cmd.DimensionCategoryCmd;
import com.cyan.datametric.domain.metric.dimension.category.query.DimensionCategoryQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 维度分类控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/dimension-categories")
@RequiredArgsConstructor
public class DimensionCategoryController {

    private final DimensionCategoryService dimensionCategoryService;
    private final DimensionCategoryAdapterConvert dimensionCategoryAdapterConvert;


    @GetMapping("/tree")
    public Response<List<DimensionCategoryDTO>> tree() {
        List<DimensionCategoryBO> bos = dimensionCategoryService.tree();
        return Response.success(bos.stream()
                .map(dimensionCategoryAdapterConvert::toDimensionCategoryDTO)
                .toList());
    }

    @GetMapping("/page")
    public Response<PageResultDTO<DimensionCategoryDTO>> page(DimensionCategoryQuery query) {
        com.cyan.arch.common.api.Page<DimensionCategoryBO> page = dimensionCategoryService.page(query);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(dimensionCategoryAdapterConvert::toDimensionCategoryDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/{id}")
    public Response<DimensionCategoryDTO> detail(@PathVariable("id") String id) {
        DimensionCategoryBO bo = dimensionCategoryService.detail(id);
        return Response.success(dimensionCategoryAdapterConvert.toDimensionCategoryDTO(bo));
    }

    @PostMapping
    public Response<DimensionCategoryDTO> create(@RequestBody @Valid DimensionCategoryCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        DimensionCategoryBO bo = dimensionCategoryService.create(cmd);
        return Response.success(dimensionCategoryAdapterConvert.toDimensionCategoryDTO(bo));
    }

    @PutMapping("/{id}")
    public Response<DimensionCategoryDTO> update(@PathVariable("id") String id, @RequestBody @Valid DimensionCategoryCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        DimensionCategoryBO bo = dimensionCategoryService.update(id, cmd);
        return Response.success(dimensionCategoryAdapterConvert.toDimensionCategoryDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        dimensionCategoryService.delete(id);
        return Response.success();
    }
}
