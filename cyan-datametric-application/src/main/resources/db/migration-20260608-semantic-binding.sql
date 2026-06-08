-- 指标语义对象与物理绑定 1:n 重构迁移脚本
-- 执行顺序：先创建绑定表并迁移旧字段数据，再删除旧物理字段列。

CREATE TABLE IF NOT EXISTS metric_field_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    catalog_name VARCHAR(64) DEFAULT 'iceberg' COMMENT 'catalog 名称',
    schema_name VARCHAR(128) NOT NULL COMMENT 'schema 名称',
    table_name VARCHAR(128) NOT NULL COMMENT '表名称',
    column_name VARCHAR(128) DEFAULT NULL COMMENT '字段名称',
    source_expr VARCHAR(512) DEFAULT NULL COMMENT '来源表达式',
    filter_condition JSON COMMENT '过滤条件JSON',
    is_primary TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主绑定',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_metric_id (metric_id),
    INDEX idx_metric_table (catalog_name, schema_name, table_name),
    INDEX idx_metric_primary (metric_id, is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标字段绑定表';

CREATE TABLE IF NOT EXISTS metric_dimension_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dim_id BIGINT NOT NULL COMMENT '维度ID',
    table_role VARCHAR(32) NOT NULL COMMENT '表角色: FACT/DIMENSION',
    catalog_name VARCHAR(64) DEFAULT 'iceberg' COMMENT 'catalog 名称',
    schema_name VARCHAR(128) NOT NULL COMMENT 'schema 名称',
    table_name VARCHAR(128) NOT NULL COMMENT '表名称',
    column_name VARCHAR(128) DEFAULT NULL COMMENT '字段名称',
    display_column VARCHAR(128) DEFAULT NULL COMMENT '显示字段',
    source_type VARCHAR(32) DEFAULT 'COLUMN' COMMENT '来源类型: COLUMN/JSON_PATH/EXPRESSION',
    source_expr VARCHAR(512) DEFAULT NULL COMMENT '来源表达式',
    is_primary TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主绑定',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_dim_id (dim_id),
    INDEX idx_dim_table (catalog_name, schema_name, table_name),
    INDEX idx_dim_primary (dim_id, is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度字段绑定表';

INSERT INTO metric_field_binding(metric_id, catalog_name, schema_name, table_name, column_name, filter_condition, is_primary, sort_order, created_at, updated_at)
SELECT metric_id,
       COALESCE(NULLIF(ds_name, ''), 'iceberg'),
       db_name,
       tbl_name,
       col_name,
       filter_condition,
       1,
       0,
       NOW(),
       NOW()
FROM metric_atomic
WHERE deleted_at IS NULL
  AND db_name IS NOT NULL
  AND tbl_name IS NOT NULL
  AND col_name IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM metric_field_binding b
      WHERE b.metric_id = metric_atomic.metric_id
        AND b.schema_name = metric_atomic.db_name
        AND b.table_name = metric_atomic.tbl_name
        AND b.column_name = metric_atomic.col_name
        AND b.deleted_at IS NULL
  );

INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, display_column, source_type, source_expr, is_primary, sort_order, created_at, updated_at)
SELECT id,
       CASE WHEN source_table IS NOT NULL AND source_table <> '' THEN 'FACT' ELSE 'DIMENSION' END,
       CASE
           WHEN source_table LIKE '%.%.%' THEN SUBSTRING_INDEX(source_table, '.', 1)
           ELSE 'iceberg'
       END,
       CASE
           WHEN source_table LIKE '%.%.%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(source_table, '.', 2), '.', -1)
           WHEN source_table LIKE '%.%' THEN SUBSTRING_INDEX(source_table, '.', 1)
           WHEN schema_name IS NOT NULL AND schema_name <> '' THEN schema_name
           ELSE 'default'
       END,
       CASE
           WHEN source_table LIKE '%.%' THEN SUBSTRING_INDEX(source_table, '.', -1)
           WHEN source_table IS NOT NULL AND source_table <> '' THEN source_table
           ELSE table_name
       END,
       column_name,
       display_column,
       COALESCE(source_type, 'COLUMN'),
       source_expr,
       1,
       0,
       NOW(),
       NOW()
FROM metric_dimension
WHERE deleted_at IS NULL
  AND (table_name IS NOT NULL OR source_table IS NOT NULL)
  AND NOT EXISTS (
      SELECT 1 FROM metric_dimension_binding b
      WHERE b.dim_id = metric_dimension.id
        AND b.deleted_at IS NULL
  );

ALTER TABLE metric_atomic
    DROP COLUMN ds_name,
    DROP COLUMN db_name,
    DROP COLUMN tbl_name,
    DROP COLUMN col_name,
    DROP COLUMN filter_condition;

ALTER TABLE metric_dimension
    DROP COLUMN schema_name,
    DROP COLUMN table_name,
    DROP COLUMN column_name,
    DROP COLUMN display_column,
    DROP COLUMN source_type,
    DROP COLUMN source_expr,
    DROP COLUMN source_table;
