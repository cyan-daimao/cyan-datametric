package com.cyan.datametric.infra.persistence.semantic.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.MaterializedView;
import com.cyan.datametric.domain.semantic.repository.MaterializedViewRepository;
import com.cyan.datametric.infra.persistence.semantic.convert.SemanticInfraConvert;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticMaterializedViewDO;
import com.cyan.datametric.infra.persistence.semantic.mappers.SemanticMaterializedViewMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 物化视图仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class MaterializedViewRepositoryImpl implements MaterializedViewRepository {

    private final SemanticMaterializedViewMapper mapper;

    public MaterializedViewRepositoryImpl(SemanticMaterializedViewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MaterializedView findById(String id) {
        return SemanticInfraConvert.INSTANCE.toMaterializedView(mapper.selectById(Long.parseLong(id)));
    }

    @Override
    public List<MaterializedView> findActiveAll() {
        LambdaQueryWrapper<SemanticMaterializedViewDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticMaterializedViewDO::getStatus, "ACTIVE");
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toMaterializedView)
                .toList();
    }

    @Override
    public Page<MaterializedView> page(int pageNum, int pageSize) {
        Page<SemanticMaterializedViewDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SemanticMaterializedViewDO> wrapper = new LambdaQueryWrapper<>()
                .orderByDesc(SemanticMaterializedViewDO::getUpdatedAt);
        Page<SemanticMaterializedViewDO> result = mapper.selectPage(page, wrapper);
        List<MaterializedView> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(SemanticInfraConvert.INSTANCE::toMaterializedView)
                .toList();
        return new Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public MaterializedView save(MaterializedView materializedView) {
        long id = SnowflakeIdUtil.nextId();
        materializedView.setId(String.valueOf(id));
        SemanticMaterializedViewDO d = SemanticInfraConvert.INSTANCE.toMaterializedViewDO(materializedView);
        mapper.insert(d);
        return findById(materializedView.getId());
    }

    @Override
    public MaterializedView update(MaterializedView materializedView) {
        SemanticMaterializedViewDO d = SemanticInfraConvert.INSTANCE.toMaterializedViewDO(materializedView);
        mapper.updateById(d);
        return findById(materializedView.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }
}
