package com.cyan.datametric.application.config;

import com.cyan.arch.common.api.Page;
import com.cyan.datametric.application.config.bo.DimensionBO;
import com.cyan.datametric.application.config.cmd.DimensionCmd;
import com.cyan.datametric.application.config.convert.ConfigAppConvert;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.query.DimensionPageQuery;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import org.springframework.stereotype.Service;

/**
 * 公共维度服务
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
public class DimensionService {

    private final DimensionRepository dimensionRepository;

    public DimensionService(DimensionRepository dimensionRepository) {
        this.dimensionRepository = dimensionRepository;
    }

    public DimensionBO create(DimensionCmd cmd) {
        if (cmd.getDimCode() == null || cmd.getDimCode().isBlank()) {
            cmd.setDimCode("DIM_" + System.currentTimeMillis());
        }
        Dimension dimension = ConfigAppConvert.INSTANCE.toDimension(cmd);
        dimension = dimension.save(dimensionRepository);
        return ConfigAppConvert.INSTANCE.toDimensionBO(dimension);
    }

    public DimensionBO update(String id, DimensionCmd cmd) {
        Dimension dimension = ConfigAppConvert.INSTANCE.toDimension(cmd);
        dimension.setId(id);
        dimension = dimension.update(dimensionRepository);
        return ConfigAppConvert.INSTANCE.toDimensionBO(dimension);
    }

    public void delete(String id) {
        Dimension dimension = new Dimension();
        dimension.setId(id);
        dimension.delete(dimensionRepository);
    }

    public DimensionBO detail(String id) {
        Dimension dimension = dimensionRepository.findById(id);
        return ConfigAppConvert.INSTANCE.toDimensionBO(dimension);
    }

    public Page<DimensionBO> page(DimensionPageQuery query) {
        Page<Dimension> page = dimensionRepository.page(query);
        java.util.List<DimensionBO> list = page.getData().stream()
                .map(ConfigAppConvert.INSTANCE::toDimensionBO)
                .toList();
        return new Page<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }
}
