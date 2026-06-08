package com.cyan.datametric.adapter.config.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.config.http.convert.ConfigAdapterConvert;
import com.cyan.datametric.adapter.config.http.dto.DimensionDTO;
import com.cyan.datametric.adapter.config.http.dto.DimensionFieldBindingDTO;
import com.cyan.datametric.application.config.DimensionService;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.cmd.DimensionFieldBindingCmd;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 公共维度控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/dimensions")
@RequiredArgsConstructor
public class DimensionController {

    private final DimensionService dimensionService;
    private final ConfigAdapterConvert configAdapterConvert;


    @GetMapping("/page")
    public Response<PageResultDTO<DimensionDTO>> page(DimensionPageQuery query) {
        com.cyan.arch.common.api.Page<DimensionBO> page = dimensionService.page(query);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(configAdapterConvert::toDimensionDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/{id}")
    public Response<DimensionDTO> detail(@PathVariable("id") String id) {
        DimensionBO bo = dimensionService.detail(id);
        return Response.success(configAdapterConvert.toDimensionDTO(bo));
    }

    @PostMapping
    public Response<DimensionDTO> create(@RequestBody @Valid DimensionCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        DimensionBO bo = dimensionService.create(cmd);
        return Response.success(configAdapterConvert.toDimensionDTO(bo));
    }

    @PutMapping("/{id}")
    public Response<DimensionDTO> update(@PathVariable("id") String id, @RequestBody @Valid DimensionCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        DimensionBO bo = dimensionService.update(id, cmd);
        return Response.success(configAdapterConvert.toDimensionDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        dimensionService.delete(id);
        return Response.success();
    }

    @GetMapping("/{id}/field-bindings")
    public Response<java.util.List<DimensionFieldBindingDTO>> listFieldBindings(@PathVariable("id") String id) {
        return Response.success(dimensionService.listFieldBindings(id).stream()
                .map(configAdapterConvert::toDimensionFieldBindingDTO)
                .toList());
    }

    @PostMapping("/{id}/field-bindings")
    public Response<DimensionFieldBindingDTO> saveFieldBinding(@PathVariable("id") String id,
                                                               @RequestBody DimensionFieldBindingCmd cmd) {
        String operator = UserContextHolder.getCurrentEmployee().getPassport();
        return Response.success(configAdapterConvert.toDimensionFieldBindingDTO(
                dimensionService.saveFieldBinding(id, cmd, operator)));
    }

    @PutMapping("/{id}/field-bindings/{bindingId}")
    public Response<DimensionFieldBindingDTO> updateFieldBinding(@PathVariable("id") String id,
                                                                 @PathVariable("bindingId") String bindingId,
                                                                 @RequestBody DimensionFieldBindingCmd cmd) {
        String operator = UserContextHolder.getCurrentEmployee().getPassport();
        cmd.setId(bindingId);
        return Response.success(configAdapterConvert.toDimensionFieldBindingDTO(
                dimensionService.saveFieldBinding(id, cmd, operator)));
    }

    @DeleteMapping("/{id}/field-bindings/{bindingId}")
    public Response<Void> deleteFieldBinding(@PathVariable("id") String id,
                                             @PathVariable("bindingId") String bindingId) {
        dimensionService.deleteFieldBinding(id, bindingId);
        return Response.success();
    }

    @PutMapping("/{id}/field-bindings/{bindingId}/primary")
    public Response<Void> setPrimaryFieldBinding(@PathVariable("id") String id,
                                                 @PathVariable("bindingId") String bindingId) {
        dimensionService.setPrimaryFieldBinding(id, bindingId);
        return Response.success();
    }
}
