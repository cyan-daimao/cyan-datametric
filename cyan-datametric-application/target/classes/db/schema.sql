-- 指标定义表
CREATE TABLE IF NOT EXISTS metric_definition (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL UNIQUE COMMENT '指标编码',
    metric_name VARCHAR(128) NOT NULL COMMENT '指标名称',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型: ATOMIC/DERIVED/COMPOSITE',
    subject_code VARCHAR(64) COMMENT '关联主题域编码',
    biz_caliber TEXT COMMENT '业务口径',
    tech_caliber TEXT COMMENT '技术口径',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/OFFLINE',
    owner VARCHAR(64) COMMENT '负责人',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_metric_name (metric_name),
    INDEX idx_metric_type (metric_type),
    INDEX idx_subject_code (subject_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标定义表';

-- 原子指标扩展表
CREATE TABLE IF NOT EXISTS metric_atomic (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    stat_func VARCHAR(32) NOT NULL COMMENT '统计函数: SUM/AVG/COUNT/COUNT_DISTINCT/MAX/MIN',
    ds_name VARCHAR(64) NOT NULL COMMENT '数据源名称',
    db_name VARCHAR(64) NOT NULL COMMENT '数据库名称',
    tbl_name VARCHAR(128) NOT NULL COMMENT '表名称',
    col_name VARCHAR(128) NOT NULL COMMENT '字段名称',
    filter_condition JSON COMMENT '过滤条件JSON',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原子指标扩展表';

-- 派生指标扩展表
CREATE TABLE IF NOT EXISTS metric_derived (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    atomic_metric_id BIGINT NOT NULL COMMENT '关联原子指标ID',
    time_period_id BIGINT COMMENT '时间周期ID',
    modifier_ids JSON COMMENT '修饰词ID列表JSON',
    dimension_ids JSON COMMENT '维度ID列表JSON',
    group_by_fields JSON COMMENT '分组字段JSON',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='派生指标扩展表';

-- 复合指标扩展表
CREATE TABLE IF NOT EXISTS metric_composite (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    formula TEXT NOT NULL COMMENT '计算公式',
    metric_refs JSON NOT NULL COMMENT '引用的指标ID列表JSON',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合指标扩展表';

-- 修饰词表
CREATE TABLE IF NOT EXISTS metric_modifier (
    id BIGINT PRIMARY KEY COMMENT '主键',
    modifier_code VARCHAR(64) NOT NULL UNIQUE COMMENT '修饰词编码',
    modifier_name VARCHAR(128) NOT NULL COMMENT '修饰词名称',
    field_name VARCHAR(128) NOT NULL COMMENT '关联字段名',
    operator VARCHAR(32) NOT NULL COMMENT '运算符: =/!=/IN/NOT_IN/>/< 等',
    field_values JSON COMMENT '可选值JSON',
    description VARCHAR(512) COMMENT '描述',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_modifier_name (modifier_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修饰词表';

-- 时间周期表
CREATE TABLE IF NOT EXISTS metric_time_period (
    id BIGINT PRIMARY KEY COMMENT '主键',
    period_code VARCHAR(64) NOT NULL UNIQUE COMMENT '周期编码',
    period_name VARCHAR(128) NOT NULL COMMENT '周期名称',
    period_type VARCHAR(32) NOT NULL COMMENT '类型: RELATIVE/ABSOLUTE',
    relative_value INT COMMENT '相对偏移值，如 -7',
    relative_unit VARCHAR(32) COMMENT '相对单位: DAY/WEEK/MONTH/YEAR',
    start_date DATE COMMENT '绝对日期范围-开始',
    end_date DATE COMMENT '绝对日期范围-结束',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间周期表';

-- 指标血缘表
CREATE TABLE IF NOT EXISTS metric_lineage (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '当前指标ID',
    parent_metric_id BIGINT COMMENT '上游指标ID',
    upstream_type VARCHAR(32) NOT NULL COMMENT '上游类型: METRIC/TABLE/COLUMN',
    upstream_id VARCHAR(128) NOT NULL COMMENT '上游节点ID',
    upstream_name VARCHAR(256) COMMENT '上游节点名称',
    lineage_type VARCHAR(32) NOT NULL COMMENT '血缘方向: UPSTREAM/DOWNSTREAM',
    level INT NOT NULL DEFAULT 1 COMMENT '血缘层级',
    create_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric_id (metric_id),
    INDEX idx_parent_metric_id (parent_metric_id),
    INDEX idx_lineage_type (lineage_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标血缘表';

-- 公共维度表
CREATE TABLE IF NOT EXISTS metric_dimension (
    id BIGINT PRIMARY KEY COMMENT '主键',
    dim_code VARCHAR(64) NOT NULL UNIQUE COMMENT '维度编码',
    dim_name VARCHAR(128) NOT NULL COMMENT '维度名称',
    ds_name VARCHAR(64) NOT NULL COMMENT '数据源名称',
    db_name VARCHAR(64) NOT NULL COMMENT '数据库名称',
    tbl_name VARCHAR(128) NOT NULL COMMENT '表名称',
    col_name VARCHAR(128) NOT NULL COMMENT '字段名称',
    description VARCHAR(512) COMMENT '描述',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_dim_name (dim_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共维度表';

-- 指标收藏表
CREATE TABLE IF NOT EXISTS metric_favorite (
    id BIGINT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_metric_user (metric_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标收藏表';
