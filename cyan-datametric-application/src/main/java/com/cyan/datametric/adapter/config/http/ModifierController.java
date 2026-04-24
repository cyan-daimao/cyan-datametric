package com.cyan.datametric.adapter.config.http;

import com.cyan.arch.common.api.Response;
import com.cyan.datametric.adapter.common.PageResultDTO;
import com.cyan.datametric.adapter.config.http.convert.ConfigAdapterConvert;
import com.cyan.datametric.adapter.config.http.dto.ModifierDTO;
import com.cyan.datametric.application.config.ModifierService;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.cmd.ModifierCmd;
import com.cyan.datametric.domain.config.query.ModifierPageQuery;
import com.cyan.employee.login.filter.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 修饰词控制器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/metrics/modifiers")
public class ModifierController {

    private final ModifierService modifierService;

    public ModifierController(ModifierService modifierService) {
        this.modifierService = modifierService;
    }

    @GetMapping("/page")
    public Response<PageResultDTO<ModifierDTO>> page(ModifierPageQuery query) {
        com.cyan.arch.common.api.Page<ModifierBO> page = modifierService.page(query);
        return Response.success(new PageResultDTO<>(
                page.getData().stream().map(ConfigAdapterConvert.INSTANCE::toModifierDTO).toList(),
                page.getCurrent(), page.getSize(), page.getTotal()));
    }

    @GetMapping("/{id}")
    public Response<ModifierDTO> detail(@PathVariable("id") String id) {
        ModifierBO bo = modifierService.detail(id);
        return Response.success(ConfigAdapterConvert.INSTANCE.toModifierDTO(bo));
    }

    @PostMapping
    public Response<ModifierDTO> create(@RequestBody @Valid ModifierCmd cmd) {
        cmd.setCreateBy(UserContextHolder.getCurrentEmployee().getPassport());
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        ModifierBO bo = modifierService.create(cmd);
        return Response.success(ConfigAdapterConvert.INSTANCE.toModifierDTO(bo));
    }

    @PutMapping("/{id}")
    public Response<ModifierDTO> update(@PathVariable("id") String id, @RequestBody @Valid ModifierCmd cmd) {
        cmd.setUpdateBy(UserContextHolder.getCurrentEmployee().getPassport());
        ModifierBO bo = modifierService.update(id, cmd);
        return Response.success(ConfigAdapterConvert.INSTANCE.toModifierDTO(bo));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable("id") String id) {
        modifierService.delete(id);
        return Response.success();
    }
}
