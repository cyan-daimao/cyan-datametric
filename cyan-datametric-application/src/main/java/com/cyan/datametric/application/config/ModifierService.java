package com.cyan.datametric.application.config;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.config.bo.ModifierBO;
import com.cyan.datametric.application.config.cmd.ModifierCmd;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.query.ModifierPageQuery;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.cyan.arch.common.api.Assert;
import com.cyan.arch.common.api.BusinessException;

/**
 * 修饰词服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ModifierService {

    private final ModifierRepository modifierRepository;
    private final ConfigAppConvert configAppConvert;


    public ModifierBO create(ModifierCmd cmd) {
        if (cmd.getModifierCode() == null || cmd.getModifierCode().isBlank()) {
            cmd.setModifierCode("MDF_" + System.currentTimeMillis());
        }
        Modifier modifier = configAppConvert.toModifier(cmd);
        modifier = modifier.save(modifierRepository);
        return configAppConvert.toModifierBO(modifier);
    }

    public ModifierBO update(String id, ModifierCmd cmd) {
        Modifier modifier = configAppConvert.toModifier(cmd);
        modifier.setId(id);
        modifier = modifier.update(modifierRepository);
        return configAppConvert.toModifierBO(modifier);
    }

    public void delete(String id) {
        Modifier modifier = modifierRepository.findById(id);
        Assert.notNull(modifier, new BusinessException("修饰词不存在"));
        modifier.delete(modifierRepository);
    }

    public ModifierBO detail(String id) {
        Modifier modifier = modifierRepository.findById(id);
        return configAppConvert.toModifierBO(modifier);
    }

    public Page<ModifierBO> page(ModifierPageQuery query) {
        Page<Modifier> page = modifierRepository.page(query);
        java.util.List<ModifierBO> list = page.getData().stream()
                .map(configAppConvert::toModifierBO)
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }
}
