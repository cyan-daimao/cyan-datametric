package com.cyan.datametric.infra.persistence.config.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Pageable;
import com.cyan.datametric.domain.config.Modifier;
import com.cyan.datametric.domain.config.query.ModifierPageQuery;
import com.cyan.datametric.domain.config.repository.ModifierRepository;
import com.cyan.datametric.infra.persistence.config.convert.ConfigInfraConvert;
import com.cyan.datametric.infra.persistence.config.dos.MetricModifierDO;
import com.cyan.datametric.infra.persistence.config.mappers.MetricModifierMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 修饰词仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class ModifierRepositoryImpl implements ModifierRepository {

    private final MetricModifierMapper modifierMapper;

    public ModifierRepositoryImpl(MetricModifierMapper modifierMapper) {
        this.modifierMapper = modifierMapper;
    }

    @Override
    public Modifier findById(String id) {
        MetricModifierDO modifierDO = modifierMapper.selectById(Long.parseLong(id));
        return ConfigInfraConvert.INSTANCE.toModifier(modifierDO);
    }

    @Override
    public com.cyan.arch.common.api.Page<Modifier> page(ModifierPageQuery query) {
        Page<MetricModifierDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<MetricModifierDO> wrapper = new LambdaQueryWrapper<MetricModifierDO>()
                .like(StringUtils.isNotBlank(query.getModifierName()), MetricModifierDO::getModifierName, query.getModifierName())
                .orderByDesc(MetricModifierDO::getUpdatedAt);
        Page<MetricModifierDO> result = modifierMapper.selectPage(page, wrapper);
        List<Modifier> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(ConfigInfraConvert.INSTANCE::toModifier)
                .toList();
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Modifier save(Modifier modifier) {
        long id = SnowflakeIdUtil.nextId();
        modifier.setId(String.valueOf(id));
        MetricModifierDO modifierDO = ConfigInfraConvert.INSTANCE.toModifierDO(modifier);
        modifierMapper.insert(modifierDO);
        return findById(modifier.getId());
    }

    @Override
    public Modifier update(Modifier modifier) {
        MetricModifierDO modifierDO = ConfigInfraConvert.INSTANCE.toModifierDO(modifier);
        modifierMapper.updateById(modifierDO);
        return findById(modifier.getId());
    }

    @Override
    public void deleteById(String id) {
        modifierMapper.deleteById(Long.parseLong(id));
    }

    @Override
    public List<Modifier> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> longIds = ids.stream().map(Long::parseLong).toList();
        LambdaQueryWrapper<MetricModifierDO> wrapper = new LambdaQueryWrapper<MetricModifierDO>()
                .in(MetricModifierDO::getId, longIds);
        List<MetricModifierDO> list = modifierMapper.selectList(wrapper);
        return list.stream().map(ConfigInfraConvert.INSTANCE::toModifier).toList();
    }
}
