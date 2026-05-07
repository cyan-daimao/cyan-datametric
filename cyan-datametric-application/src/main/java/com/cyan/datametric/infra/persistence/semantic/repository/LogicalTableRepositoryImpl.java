package com.cyan.datametric.infra.persistence.semantic.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.arch.common.api.Page;
import com.cyan.datametric.domain.semantic.LogicalTable;
import com.cyan.datametric.domain.semantic.repository.LogicalTableRepository;
import com.cyan.datametric.enums.semantic.TableType;
import com.cyan.datametric.infra.persistence.semantic.convert.SemanticInfraConvert;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticLogicalTableDO;
import com.cyan.datametric.infra.persistence.semantic.mappers.SemanticLogicalTableMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 逻辑表仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class LogicalTableRepositoryImpl implements LogicalTableRepository {

    private final SemanticLogicalTableMapper mapper;

    public LogicalTableRepositoryImpl(SemanticLogicalTableMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LogicalTable findById(String id) {
        SemanticLogicalTableDO d = mapper.selectById(Long.parseLong(id));
        return SemanticInfraConvert.INSTANCE.toLogicalTable(d);
    }

    @Override
    public LogicalTable findByTableName(String tableName) {
        LambdaQueryWrapper<SemanticLogicalTableDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticLogicalTableDO::getTableName, tableName);
        SemanticLogicalTableDO d = mapper.selectOne(wrapper);
        return SemanticInfraConvert.INSTANCE.toLogicalTable(d);
    }

    @Override
    public Page<LogicalTable> page(int pageNum, int pageSize) {
        Page<SemanticLogicalTableDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SemanticLogicalTableDO> wrapper = new LambdaQueryWrapper<>()
                .orderByDesc(SemanticLogicalTableDO::getUpdatedAt);
        Page<SemanticLogicalTableDO> result = mapper.selectPage(page, wrapper);
        List<LogicalTable> list = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(SemanticInfraConvert.INSTANCE::toLogicalTable)
                .toList();
        return new Page<>(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<LogicalTable> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(SemanticInfraConvert.INSTANCE::toLogicalTable)
                .toList();
    }

    @Override
    public List<LogicalTable> findByTableType(String tableType) {
        LambdaQueryWrapper<SemanticLogicalTableDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticLogicalTableDO::getTableType, tableType);
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toLogicalTable)
                .toList();
    }

    @Override
    public LogicalTable save(LogicalTable logicalTable) {
        long id = SnowflakeIdUtil.nextId();
        logicalTable.setId(String.valueOf(id));
        SemanticLogicalTableDO d = SemanticInfraConvert.INSTANCE.toLogicalTableDO(logicalTable);
        mapper.insert(d);
        return findById(logicalTable.getId());
    }

    @Override
    public LogicalTable update(LogicalTable logicalTable) {
        SemanticLogicalTableDO d = SemanticInfraConvert.INSTANCE.toLogicalTableDO(logicalTable);
        mapper.updateById(d);
        return findById(logicalTable.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }
}
