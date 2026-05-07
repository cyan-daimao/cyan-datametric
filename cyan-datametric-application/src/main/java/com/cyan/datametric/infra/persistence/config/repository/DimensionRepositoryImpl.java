package com.cyan.datametric.infra.persistence.config.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import com.cyan.datametric.infra.persistence.config.convert.ConfigInfraConvert;
import com.cyan.datametric.infra.persistence.config.dos.MetricDimensionDO;
import com.cyan.datametric.infra.persistence.config.mappers.MetricDimensionMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 公共维度仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class DimensionRepositoryImpl implements DimensionRepository {

    private final MetricDimensionMapper dimensionMapper;

    public DimensionRepositoryImpl(MetricDimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public Dimension findById(String id) {
        MetricDimensionDO dimensionDO = dimensionMapper.selectById(Long.parseLong(id));
        return ConfigInfraConvert.INSTANCE.toDimension(dimensionDO);
    }

    @Override
    public Dimension findByDimCode(String dimCode) {
        LambdaQueryWrapper<MetricDimensionDO> wrapper = new LambdaQueryWrapper<MetricDimensionDO>()
                .eq(MetricDimensionDO::getDimCode, dimCode);
        MetricDimensionDO dimensionDO = dimensionMapper.selectOne(wrapper);
        return ConfigInfraConvert.INSTANCE.toDimension(dimensionDO);
    }

    @Override
    public com.cyan.arch.common.api.Page<Dimension> page(DimensionPageQuery query) {
        Page<MetricDimensionDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<MetricDimensionDO> wrapper = new LambdaQueryWrapper<MetricDimensionDO>()
                .like(StringUtils.isNotBlank(query.getDimName()), MetricDimensionDO::getDimName, query.getDimName())
                .orderByDesc(MetricDimensionDO::getUpdatedAt);
        if (StringUtils.isNotBlank(query.getCategoryId())) {
            wrapper.eq(MetricDimensionDO::getCategoryId, Long.parseLong(query.getCategoryId()));
        }
        Page<MetricDimensionDO> result = dimensionMapper.selectPage(page, wrapper);
        List<Dimension> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(ConfigInfraConvert.INSTANCE::toDimension)
                .toList();
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public Dimension save(Dimension dimension) {
        long id = SnowflakeIdUtil.nextId();
        dimension.setId(String.valueOf(id));
        MetricDimensionDO dimensionDO = ConfigInfraConvert.INSTANCE.toDimensionDO(dimension);
        dimensionMapper.insert(dimensionDO);
        return findById(dimension.getId());
    }

    @Override
    public Dimension update(Dimension dimension) {
        MetricDimensionDO dimensionDO = ConfigInfraConvert.INSTANCE.toDimensionDO(dimension);
        dimensionMapper.updateById(dimensionDO);
        return findById(dimension.getId());
    }

    @Override
    public void deleteById(String id) {
        dimensionMapper.deleteById(Long.parseLong(id));
    }
}
