package com.cyan.datametric.application.dimension;

import com.cyan.arch.common.api.BusinessException;
import com.cyan.datametric.domain.config.BuiltinTimeDimension;
import com.cyan.datametric.domain.config.Dimension;
import com.cyan.datametric.domain.config.repository.DimensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 维度解析器
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class DimensionResolver {

    private final DimensionRepository dimensionRepository;

    @Value("${cyan.datametric.default-catalog:iceberg}")
    private String defaultCatalog;

    /**
     * 解析维度编码
     *
     * @param dimCode 维度编码
     * @return 已解析维度
     */
    public ResolvedDimension resolve(String dimCode) {
        if (!StringUtils.hasText(dimCode)) {
            throw new BusinessException("维度编码不能为空");
        }
        BuiltinTimeDimension builtin = BuiltinTimeDimension.of(dimCode);
        if (builtin != null) {
            return resolveBuiltin(builtin);
        }
        Dimension dimension = dimensionRepository.findByDimCode(dimCode);
        if (dimension == null) {
            throw new BusinessException("维度不存在: " + dimCode);
        }
        return resolve(dimension);
    }

    /**
     * 解析维度领域对象
     *
     * @param dimension 维度领域对象
     * @return 已解析维度
     */
    public ResolvedDimension resolve(Dimension dimension) {
        if (dimension == null) {
            throw new BusinessException("维度不存在");
        }
        String kind = normalizeKind(dimension.getDimensionKind());
        String expression = resolveExpression(dimension);
        String tableRef = resolveTableRef(dimension.getSchemaName(), dimension.getTableName());
        String sourceTableRef = StringUtils.hasText(dimension.getSourceTable())
                ? normalizeTableRef(dimension.getSourceTable())
                : null;
        boolean requiresJoin = ("NORMAL".equals(kind) || "HIERARCHY".equals(kind)) && StringUtils.hasText(tableRef);
        String displayExpr = StringUtils.hasText(dimension.getDisplayColumn())
                ? quoteIdentifier(dimension.getDisplayColumn())
                : expression;
        return new ResolvedDimension()
                .setDimCode(dimension.getDimCode())
                .setDimName(dimension.getDimName())
                .setDimensionKind(kind)
                .setSelectExpr(displayExpr)
                .setGroupExpr(displayExpr)
                .setFilterExpr(expression)
                .setTableRef(tableRef)
                .setSourceTableRef(sourceTableRef)
                .setRequiresJoin(requiresJoin)
                .setColumnName(dimension.getColumnName())
                .setDisplayColumn(dimension.getDisplayColumn())
                .setBuiltin(false);
    }

    /**
     * 标准化表引用
     *
     * @param tableRef 表引用
     * @return catalog.schema.table 格式表引用
     */
    public String normalizeTableRef(String tableRef) {
        if (!StringUtils.hasText(tableRef)) {
            return tableRef;
        }
        String[] parts = tableRef.split("\\.");
        if (parts.length == 2) {
            return defaultCatalog + "." + tableRef;
        }
        if (parts.length == 1) {
            throw new BusinessException("表引用格式错误，期望 schema.table 或 catalog.schema.table，实际: " + tableRef);
        }
        return tableRef;
    }

    /**
     * 解析系统内置时间维度
     */
    private ResolvedDimension resolveBuiltin(BuiltinTimeDimension builtin) {
        return new ResolvedDimension()
                .setDimCode(builtin.name())
                .setDimName(builtin.getDimName())
                .setDimensionKind("DERIVED")
                .setSelectExpr(builtin.buildExpr(null))
                .setGroupExpr(builtin.buildExpr(null))
                .setFilterExpr("`dt`")
                .setColumnName("dt")
                .setBuiltin(true);
    }

    /**
     * 标准化维度实现类型
     */
    private String normalizeKind(String kind) {
        return StringUtils.hasText(kind) ? kind : "NORMAL";
    }

    /**
     * 解析维度 SQL 表达式
     */
    private String resolveExpression(Dimension dimension) {
        String sourceType = StringUtils.hasText(dimension.getSourceType()) ? dimension.getSourceType() : "COLUMN";
        return switch (sourceType) {
            case "JSON_PATH" -> "JSON_VALUE(`properties`, '" + escapeSql(resolveJsonPath(dimension)) + "')";
            case "EXPRESSION" -> {
                if (!StringUtils.hasText(dimension.getSourceExpr())) {
                    throw new BusinessException("表达式维度未配置SQL表达式: " + dimension.getDimCode());
                }
                yield dimension.getSourceExpr();
            }
            default -> {
                if (!StringUtils.hasText(dimension.getColumnName())) {
                    throw new BusinessException("字段维度未配置物理字段: " + dimension.getDimCode());
                }
                yield quoteIdentifier(dimension.getColumnName());
            }
        };
    }

    /**
     * 解析 JSON Path
     */
    private String resolveJsonPath(Dimension dimension) {
        if (StringUtils.hasText(dimension.getSourceExpr())) {
            return dimension.getSourceExpr();
        }
        return "$.properties." + dimension.getColumnName();
    }

    /**
     * 解析维表引用
     */
    private String resolveTableRef(String schemaName, String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return null;
        }
        if (tableName.contains(".")) {
            return normalizeTableRef(tableName);
        }
        if (StringUtils.hasText(schemaName)) {
            return normalizeTableRef(schemaName + "." + tableName);
        }
        return normalizeTableRef(tableName);
    }

    /**
     * 转义 SQL 字符串
     */
    private String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    /**
     * 字段加反引号
     */
    private String quoteIdentifier(String value) {
        return "`" + (value == null ? "" : value.replace("`", "``")) + "`";
    }
}
