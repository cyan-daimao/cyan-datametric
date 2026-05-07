package com.cyan.datametric.infra.persistence.semantic.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.datametric.domain.semantic.TableRelationship;
import com.cyan.datametric.domain.semantic.repository.TableRelationshipRepository;
import com.cyan.datametric.infra.persistence.semantic.convert.SemanticInfraConvert;
import com.cyan.datametric.infra.persistence.semantic.dos.SemanticTableRelationshipDO;
import com.cyan.datametric.infra.persistence.semantic.mappers.SemanticTableRelationshipMapper;
import com.cyan.datametric.infra.util.SnowflakeIdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表关联关系仓储实现
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Repository
public class TableRelationshipRepositoryImpl implements TableRelationshipRepository {

    private final SemanticTableRelationshipMapper mapper;

    public TableRelationshipRepositoryImpl(SemanticTableRelationshipMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public TableRelationship findById(String id) {
        return SemanticInfraConvert.INSTANCE.toTableRelationship(mapper.selectById(Long.parseLong(id)));
    }

    @Override
    public List<TableRelationship> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(SemanticInfraConvert.INSTANCE::toTableRelationship)
                .toList();
    }

    @Override
    public List<TableRelationship> findByLeftTableId(String leftTableId) {
        LambdaQueryWrapper<SemanticTableRelationshipDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticTableRelationshipDO::getLeftTableId, Long.parseLong(leftTableId));
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toTableRelationship)
                .toList();
    }

    @Override
    public List<TableRelationship> findByRightTableId(String rightTableId) {
        LambdaQueryWrapper<SemanticTableRelationshipDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticTableRelationshipDO::getRightTableId, Long.parseLong(rightTableId));
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toTableRelationship)
                .toList();
    }

    @Override
    public List<TableRelationship> findByTableId(String tableId) {
        Long id = Long.parseLong(tableId);
        LambdaQueryWrapper<SemanticTableRelationshipDO> wrapper = new LambdaQueryWrapper<>()
                .eq(SemanticTableRelationshipDO::getLeftTableId, id)
                .or()
                .eq(SemanticTableRelationshipDO::getRightTableId, id);
        return mapper.selectList(wrapper).stream()
                .map(SemanticInfraConvert.INSTANCE::toTableRelationship)
                .toList();
    }

    @Override
    public TableRelationship save(TableRelationship relationship) {
        long id = SnowflakeIdUtil.nextId();
        relationship.setId(String.valueOf(id));
        SemanticTableRelationshipDO d = SemanticInfraConvert.INSTANCE.toTableRelationshipDO(relationship);
        mapper.insert(d);
        return findById(relationship.getId());
    }

    @Override
    public TableRelationship update(TableRelationship relationship) {
        SemanticTableRelationshipDO d = SemanticInfraConvert.INSTANCE.toTableRelationshipDO(relationship);
        mapper.updateById(d);
        return findById(relationship.getId());
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(Long.parseLong(id));
    }
}
