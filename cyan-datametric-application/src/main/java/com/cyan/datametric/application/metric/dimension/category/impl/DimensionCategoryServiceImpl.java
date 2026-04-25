package com.cyan.datametric.application.metric.dimension.category.impl;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.metric.dimension.category.DimensionCategoryService;
import com.cyan.datametric.application.metric.dimension.category.bo.DimensionCategoryBO;
import com.cyan.datametric.application.metric.dimension.category.cmd.DimensionCategoryCmd;
import com.cyan.datametric.application.metric.dimension.category.convert.DimensionCategoryAppConvert;
import com.cyan.datametric.domain.metric.dimension.category.DimensionCategory;
import com.cyan.datametric.domain.metric.dimension.category.query.DimensionCategoryQuery;
import com.cyan.datametric.domain.metric.dimension.category.repository.DimensionCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 维度分类服务实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
public class DimensionCategoryServiceImpl implements DimensionCategoryService {

    private final DimensionCategoryRepository dimensionCategoryRepository;

    public DimensionCategoryServiceImpl(DimensionCategoryRepository dimensionCategoryRepository) {
        this.dimensionCategoryRepository = dimensionCategoryRepository;
    }

    @Override
    public DimensionCategoryBO create(DimensionCategoryCmd cmd) {
        DimensionCategory category = DimensionCategoryAppConvert.INSTANCE.toDimensionCategory(cmd);
        category = category.save(dimensionCategoryRepository);
        return DimensionCategoryAppConvert.INSTANCE.toDimensionCategoryBO(category);
    }

    @Override
    public DimensionCategoryBO update(String id, DimensionCategoryCmd cmd) {
        DimensionCategory category = DimensionCategoryAppConvert.INSTANCE.toDimensionCategory(cmd);
        category.setId(id);
        category = category.update(dimensionCategoryRepository);
        return DimensionCategoryAppConvert.INSTANCE.toDimensionCategoryBO(category);
    }

    @Override
    public void delete(String id) {
        DimensionCategory category = new DimensionCategory();
        category.setId(id);
        category.delete(dimensionCategoryRepository);
    }

    @Override
    public DimensionCategoryBO detail(String id) {
        DimensionCategory category = dimensionCategoryRepository.findById(id);
        return DimensionCategoryAppConvert.INSTANCE.toDimensionCategoryBO(category);
    }

    @Override
    public Page<DimensionCategoryBO> page(DimensionCategoryQuery query) {
        Page<DimensionCategory> page = dimensionCategoryRepository.page(query);
        List<DimensionCategoryBO> list = page.getData().stream()
                .map(DimensionCategoryAppConvert.INSTANCE::toDimensionCategoryBO)
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public List<DimensionCategoryBO> tree() {
        List<DimensionCategory> all = dimensionCategoryRepository.findAll();
        List<DimensionCategoryBO> bos = all.stream()
                .map(DimensionCategoryAppConvert.INSTANCE::toDimensionCategoryBO)
                .toList();

        Map<String, List<DimensionCategoryBO>> parentMap = bos.stream()
                .filter(b -> b.getParentId() != null && !b.getParentId().isBlank())
                .collect(Collectors.groupingBy(DimensionCategoryBO::getParentId));

        List<DimensionCategoryBO> roots = new ArrayList<>();
        for (DimensionCategoryBO bo : bos) {
            if (bo.getParentId() == null || bo.getParentId().isBlank()) {
                roots.add(bo);
            }
            List<DimensionCategoryBO> children = parentMap.getOrDefault(bo.getId(), new ArrayList<>());
            children.sort(Comparator.comparingInt(DimensionCategoryBO::getSortOrder));
            bo.setChildren(children);
        }
        roots.sort(Comparator.comparingInt(DimensionCategoryBO::getSortOrder));
        return roots;
    }
}
