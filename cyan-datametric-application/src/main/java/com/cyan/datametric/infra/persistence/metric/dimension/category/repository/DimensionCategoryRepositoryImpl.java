package com.cyan.datametric.infra.persistence.metric.dimension.category.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Pageable;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.query.DimensionCategoryQuery;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import com.cyan.datametric.infra.persistence.metric.dimension.category.DimensionCategoryDO;
import com.cyan.datametric.infra.persistence.metric.dimension.category.DimensionCategoryMapper;
import com.cyan.datametric.infra.persistence.metric.dimension.category.convert.DimensionCategoryInfraConvert;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 维度分类仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class DimensionCategoryRepositoryImpl implements DimensionCategoryRepository {

    private final DimensionCategoryMapper categoryMapper;

    public DimensionCategoryRepositoryImpl(DimensionCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public DimensionCategory findById(String id) {
        DimensionCategoryDO categoryDO = categoryMapper.selectById(Long.parseLong(id));
        return DimensionCategoryInfraConvert.INSTANCE.toDimensionCategory(categoryDO);
    }

    @Override
    public com.cyan.arch.common.api.Page<DimensionCategory> page(DimensionCategoryQuery query) {
        Page<DimensionCategoryDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<DimensionCategoryDO> wrapper = new LambdaQueryWrapper<DimensionCategoryDO>()
                .like(StringUtils.isNotBlank(query.getName()), DimensionCategoryDO::getName, query.getName())
                .orderByAsc(DimensionCategoryDO::getLevel)
                .orderByAsc(DimensionCategoryDO::getSortOrder);
        Page<DimensionCategoryDO> result = categoryMapper.selectPage(page, wrapper);
        List<DimensionCategory> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(DimensionCategoryInfraConvert.INSTANCE::toDimensionCategory)
                .toList();
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<DimensionCategory> findAll() {
        LambdaQueryWrapper<DimensionCategoryDO> wrapper = new LambdaQueryWrapper<DimensionCategoryDO>()
                .orderByAsc(DimensionCategoryDO::getLevel)
                .orderByAsc(DimensionCategoryDO::getSortOrder);
        List<DimensionCategoryDO> list = categoryMapper.selectList(wrapper);
        return list.stream().map(DimensionCategoryInfraConvert.INSTANCE::toDimensionCategory).toList();
    }

    @Override
    public DimensionCategory save(DimensionCategory category) {
        long id = SnowflakeIdUtil.nextId();
        category.setId(String.valueOf(id));
        DimensionCategoryDO categoryDO = DimensionCategoryInfraConvert.INSTANCE.toDimensionCategoryDO(category);
        categoryMapper.insert(categoryDO);
        return findById(category.getId());
    }

    @Override
    public DimensionCategory update(DimensionCategory category) {
        DimensionCategoryDO categoryDO = DimensionCategoryInfraConvert.INSTANCE.toDimensionCategoryDO(category);
        categoryMapper.updateById(categoryDO);
        return findById(category.getId());
    }

    @Override
    public void deleteById(String id) {
        categoryMapper.deleteById(Long.parseLong(id));
    }
}
