package com.cyan.datametric.infra.persistence.metric.subject.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Pageable;
import com.cyan.datametric.domain.metric.subject.MetricSubject;
import com.cyan.datametric.domain.metric.subject.query.MetricSubjectQuery;
import com.cyan.datametric.domain.metric.subject.repository.MetricSubjectRepository;
import com.cyan.datametric.infra.persistence.metric.subject.MetricSubjectDO;
import com.cyan.datametric.infra.persistence.metric.subject.MetricSubjectMapper;
import com.cyan.datametric.infra.persistence.metric.subject.convert.MetricSubjectInfraConvert;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标主题域仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class MetricSubjectRepositoryImpl implements MetricSubjectRepository {

    private final MetricSubjectMapper subjectMapper;

    public MetricSubjectRepositoryImpl(MetricSubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    @Override
    public MetricSubject findById(String id) {
        MetricSubjectDO subjectDO = subjectMapper.selectById(Long.parseLong(id));
        return MetricSubjectInfraConvert.INSTANCE.toMetricSubject(subjectDO);
    }

    @Override
    public com.cyan.arch.common.api.Page<MetricSubject> page(MetricSubjectQuery query) {
        Page<MetricSubjectDO> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<MetricSubjectDO> wrapper = new LambdaQueryWrapper<MetricSubjectDO>()
                .like(StringUtils.isNotBlank(query.getSubjectName()), MetricSubjectDO::getSubjectName, query.getSubjectName())
                .orderByAsc(MetricSubjectDO::getLevel)
                .orderByAsc(MetricSubjectDO::getSortOrder);
        Page<MetricSubjectDO> result = subjectMapper.selectPage(page, wrapper);
        List<MetricSubject> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(MetricSubjectInfraConvert.INSTANCE::toMetricSubject)
                .toList();
        return new com.cyan.arch.common.api.Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<MetricSubject> findAll() {
        LambdaQueryWrapper<MetricSubjectDO> wrapper = new LambdaQueryWrapper<MetricSubjectDO>()
                .orderByAsc(MetricSubjectDO::getLevel)
                .orderByAsc(MetricSubjectDO::getSortOrder);
        List<MetricSubjectDO> list = subjectMapper.selectList(wrapper);
        return list.stream().map(MetricSubjectInfraConvert.INSTANCE::toMetricSubject).toList();
    }

    @Override
    public MetricSubject save(MetricSubject subject) {
        long id = SnowflakeIdUtil.nextId();
        subject.setId(String.valueOf(id));
        MetricSubjectDO subjectDO = MetricSubjectInfraConvert.INSTANCE.toMetricSubjectDO(subject);
        subjectMapper.insert(subjectDO);
        return findById(subject.getId());
    }

    @Override
    public MetricSubject update(MetricSubject subject) {
        MetricSubjectDO subjectDO = MetricSubjectInfraConvert.INSTANCE.toMetricSubjectDO(subject);
        subjectMapper.updateById(subjectDO);
        return findById(subject.getId());
    }

    @Override
    public void deleteById(String id) {
        subjectMapper.deleteById(Long.parseLong(id));
    }

    @Override
    public List<MetricSubject> findBySubjectCodes(List<String> subjectCodes) {
        if (subjectCodes == null || subjectCodes.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<MetricSubjectDO> wrapper = new LambdaQueryWrapper<MetricSubjectDO>()
                .in(MetricSubjectDO::getSubjectCode, subjectCodes);
        List<MetricSubjectDO> list = subjectMapper.selectList(wrapper);
        return list.stream().map(MetricSubjectInfraConvert.INSTANCE::toMetricSubject).toList();
    }

    @Override
    public MetricSubject findBySubjectCode(String subjectCode) {
        LambdaQueryWrapper<MetricSubjectDO> wrapper = new LambdaQueryWrapper<MetricSubjectDO>()
                .eq(MetricSubjectDO::getSubjectCode, subjectCode);
        MetricSubjectDO subjectDO = subjectMapper.selectOne(wrapper);
        return MetricSubjectInfraConvert.INSTANCE.toMetricSubject(subjectDO);
    }
}
