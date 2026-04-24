package com.cyan.datametric.infra.persistence.metric.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.datametric.infra.persistence.metric.dos.MetricLineageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 指标血缘Mapper
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Mapper
public interface MetricLineageMapper extends BaseMapper<MetricLineageDO> {

    /**
     * 查询上游血缘
     */
    @Select("SELECT * FROM metric_lineage WHERE metric_id = #{metricId} AND lineage_type = 'UPSTREAM' ORDER BY level")
    List<MetricLineageDO> selectUpstream(@Param("metricId") Long metricId);

    /**
     * 查询下游血缘
     */
    @Select("SELECT * FROM metric_lineage WHERE parent_metric_id = #{metricId} AND lineage_type = 'DOWNSTREAM' ORDER BY level")
    List<MetricLineageDO> selectDownstream(@Param("metricId") Long metricId);
}
