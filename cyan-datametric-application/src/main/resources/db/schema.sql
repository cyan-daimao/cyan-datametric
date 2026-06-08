-- 指标定义表
CREATE TABLE IF NOT EXISTS metric_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL UNIQUE COMMENT '指标编码',
    metric_name VARCHAR(128) NOT NULL COMMENT '指标名称',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型: ATOMIC/DERIVED/COMPOSITE',
    subject_code VARCHAR(64) COMMENT '关联主题域编码',
    biz_caliber TEXT COMMENT '业务口径',
    tech_caliber TEXT COMMENT '技术口径',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/OFFLINE',
    security_level VARCHAR(8) NOT NULL DEFAULT 'L1' COMMENT '数据密级: L1/L2/L3/L4',
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
    INDEX idx_status (status),
    INDEX idx_security_level (security_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标定义表';

-- 原子指标扩展表
CREATE TABLE IF NOT EXISTS metric_atomic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    stat_func VARCHAR(32) NOT NULL COMMENT '统计函数: SUM/AVG/COUNT/COUNT_DISTINCT/MAX/MIN',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原子指标扩展表';

-- 指标字段绑定表
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

-- 派生指标扩展表
CREATE TABLE IF NOT EXISTS metric_derived (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    atomic_metric_id BIGINT NOT NULL COMMENT '关联原子指标ID',
    time_period_id BIGINT COMMENT '时间周期ID',
    modifier_ids JSON COMMENT '修饰词ID列表JSON',
    dimension_ids JSON COMMENT '维度ID列表JSON',
    group_by_fields JSON COMMENT '分组字段JSON',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='派生指标扩展表';

-- 复合指标扩展表
CREATE TABLE IF NOT EXISTS metric_composite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标定义ID',
    formula TEXT NOT NULL COMMENT '计算公式',
    metric_refs JSON NOT NULL COMMENT '引用的指标ID列表JSON',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    UNIQUE KEY uk_metric_id (metric_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复合指标扩展表';

-- 修饰词表
CREATE TABLE IF NOT EXISTS metric_modifier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '当前指标ID',
    parent_metric_id BIGINT COMMENT '上游指标ID',
    upstream_type VARCHAR(32) NOT NULL COMMENT '上游类型: METRIC/TABLE/COLUMN',
    upstream_id VARCHAR(128) NOT NULL COMMENT '上游节点ID',
    upstream_name VARCHAR(256) COMMENT '上游节点名称',
    lineage_type VARCHAR(32) NOT NULL COMMENT '血缘方向: UPSTREAM/DOWNSTREAM',
    level INT NOT NULL DEFAULT 1 COMMENT '血缘层级',
    create_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_metric_id (metric_id),
    INDEX idx_parent_metric_id (parent_metric_id),
    INDEX idx_lineage_type (lineage_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标血缘表';

-- 公共维度表
CREATE TABLE IF NOT EXISTS metric_dimension (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dim_code VARCHAR(64) NOT NULL UNIQUE COMMENT '维度编码',
    dim_name VARCHAR(128) NOT NULL COMMENT '维度名称',
    dim_type VARCHAR(50) COMMENT '维度类型: ENUM/STRING/DATE/NUMBER/GEO',
    dimension_kind VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '维度实现类型: NORMAL/DEGENERATE/HIERARCHY/DERIVED',
    data_type VARCHAR(50) COMMENT '数据类型: STRING/INT/BIGINT/DECIMAL/DATE/DATETIME',
    dim_values JSON COMMENT '维度可选值（枚举维度时填写）',
    category_id BIGINT COMMENT '维度分类ID',
    hierarchy_code VARCHAR(64) DEFAULT NULL COMMENT '层级编码',
    hierarchy_name VARCHAR(128) DEFAULT NULL COMMENT '层级名称',
    parent_dim_code VARCHAR(64) DEFAULT NULL COMMENT '父级维度编码',
    hierarchy_level INT DEFAULT NULL COMMENT '层级级别',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    description VARCHAR(512) COMMENT '描述',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_dim_name (dim_name),
    INDEX idx_category_id (category_id),
    INDEX idx_dimension_kind (dimension_kind),
    INDEX idx_hierarchy_code (hierarchy_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共维度表';

-- 维度字段绑定表
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

-- 维度分类表
CREATE TABLE IF NOT EXISTS metric_dimension_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父分类ID',
    level INT COMMENT '层级 1-2',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度分类表';

INSERT INTO metric_dimension_category(name, parent_id, level, sort_order) VALUES
('用户属性', null, 1, 1),
('地理位置', null, 1, 2),
('时间周期', null, 1, 3),
('渠道来源', null, 1, 4),
('设备信息', null, 1, 5),
('交易属性', null, 1, 6);

-- 指标收藏表
CREATE TABLE IF NOT EXISTS metric_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_id BIGINT NOT NULL COMMENT '指标ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    UNIQUE KEY uk_metric_user (metric_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标收藏表';

-- 指标主题域表
CREATE TABLE IF NOT EXISTS metric_subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    subject_code VARCHAR(64) NOT NULL UNIQUE COMMENT '主题域编码',
    subject_name VARCHAR(128) NOT NULL COMMENT '主题域名称',
    subject_desc VARCHAR(512) COMMENT '主题域描述',
    parent_id BIGINT COMMENT '父节点ID',
    level INT NOT NULL COMMENT '层级 1-3',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标主题域表';

-- 主题域预置数据
-- 主题域预置数据（一级）
INSERT INTO metric_subject(subject_code, subject_name, subject_desc, parent_id, level, sort_order) VALUES
('SALE', '销售分析', '销售相关指标', null, 1, 1),
('USER', '用户分析', '用户相关指标', null, 1, 2),
('OPERATION', '运营分析', '运营相关指标', null, 1, 3),
('FINANCE', '财务分析', '财务相关指标', null, 1, 4);

-- 主题域预置数据（二级，用 INSERT SELECT 绕过 MySQL 限制）
INSERT INTO metric_subject(subject_code, subject_name, subject_desc, parent_id, level, sort_order)
SELECT 'SALE_OVERVIEW', '销售概览', '销售概览指标', id, 2, 1 FROM metric_subject WHERE subject_code='SALE';
INSERT INTO metric_subject(subject_code, subject_name, subject_desc, parent_id, level, sort_order)
SELECT 'SALE_DETAIL', '销售明细', '销售明细指标', id, 2, 2 FROM metric_subject WHERE subject_code='SALE';
INSERT INTO metric_subject(subject_code, subject_name, subject_desc, parent_id, level, sort_order)
SELECT 'USER_GROWTH', '用户增长', '用户增长指标', id, 2, 1 FROM metric_subject WHERE subject_code='USER';

-- 修饰词预置数据
INSERT INTO metric_modifier(modifier_code, modifier_name, field_name, operator, field_values, description) VALUES
('CHANNEL_APP', 'APP渠道', 'channel', '=', '["APP"]', '仅统计APP渠道'),
('CHANNEL_WEB', 'WEB渠道', 'channel', '=', '["WEB"]', '仅统计WEB渠道'),
('REGION_NORTH', '华北地区', 'region', '=', '["华北"]', '仅统计华北地区'),
('REGION_SOUTH', '华南地区', 'region', '=', '["华南"]', '仅统计华南地区'),
('VIP_YES', 'VIP用户', 'is_vip', '=', '["1"]', '仅统计VIP用户'),
('NEW_USER', '新用户', 'user_type', '=', '["NEW"]', '仅统计新注册用户');

-- 时间周期预置数据
INSERT INTO metric_time_period(period_code, period_name, period_type, relative_value, relative_unit) VALUES
('TODAY', '当天', 'RELATIVE', 0, 'DAY'),
('YESTERDAY', '昨天', 'RELATIVE', -1, 'DAY'),
('LAST_7_DAYS', '近7天', 'RELATIVE', -7, 'DAY'),
('LAST_30_DAYS', '近30天', 'RELATIVE', -30, 'DAY'),
('THIS_MONTH', '本月', 'RELATIVE', 0, 'MONTH'),
('THIS_QUARTER', '本季度', 'RELATIVE', 0, 'MONTH'),
('THIS_YEAR', '本年', 'RELATIVE', 0, 'YEAR');

-- 公共维度预置数据
-- 注意：日/周/月/年/季度/小时/分钟/秒 等时间粒度请优先使用系统内置时间维度
-- （DIM_DATE_DAY、DIM_DATE_HOUR、DIM_TIME_HOUR 等），无需维护 d_date 维表。
-- DIM_DATE 保留作为维表方案的向后兼容。
INSERT INTO metric_dimension(dim_code, dim_name, dim_type, data_type, dim_values, category_id, description) VALUES
('DIM_DATE', '日期', 'DATE', 'DATE', null, (SELECT id FROM metric_dimension_category WHERE name='时间周期'), '日期维度，按天统计。推荐使用系统内置维度 DIM_DATE_DAY。'),
('DIM_PROVINCE', '省份', 'GEO', 'STRING', '["北京","上海","广东","浙江","江苏"]', (SELECT id FROM metric_dimension_category WHERE name='地理位置'), '省级地理维度'),
('DIM_CITY', '城市', 'GEO', 'STRING', '["北京","上海","广州","深圳","杭州"]', (SELECT id FROM metric_dimension_category WHERE name='地理位置'), '城市级地理维度'),
('DIM_CHANNEL', '渠道', 'ENUM', 'STRING', '["APP","WEB","小程序","H5"]', (SELECT id FROM metric_dimension_category WHERE name='渠道来源'), '用户访问渠道'),
('DIM_DEVICE_TYPE', '设备类型', 'ENUM', 'STRING', '["iOS","Android","PC","Mac"]', (SELECT id FROM metric_dimension_category WHERE name='设备信息'), '用户设备类型'),
('DIM_USER_TYPE', '用户类型', 'ENUM', 'STRING', '["新用户","老用户","回流用户"]', (SELECT id FROM metric_dimension_category WHERE name='用户属性'), '用户类型划分'),
('DIM_PAYMENT_METHOD', '支付方式', 'ENUM', 'STRING', '["支付宝","微信支付","银行卡"]', (SELECT id FROM metric_dimension_category WHERE name='交易属性'), '订单支付方式');

INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_date', 'dt', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_DATE';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_province', 'province_name', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_PROVINCE';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_city', 'city_name', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_CITY';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_channel', 'channel_name', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_CHANNEL';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_device', 'device_type', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_DEVICE_TYPE';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_user_type', 'user_type', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_USER_TYPE';
INSERT INTO metric_dimension_binding(dim_id, table_role, catalog_name, schema_name, table_name, column_name, source_type, is_primary, sort_order)
SELECT id, 'DIMENSION', 'iceberg', 'default', 'd_payment_method', 'payment_method', 'COLUMN', 1, 0 FROM metric_dimension WHERE dim_code='DIM_PAYMENT_METHOD';

-- 指标定义历史快照表
CREATE TABLE IF NOT EXISTS metric_definition_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码',
    metric_name VARCHAR(128) NOT NULL COMMENT '指标名称',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型',
    subject_code VARCHAR(64) COMMENT '主题域编码',
    biz_caliber TEXT COMMENT '业务口径',
    tech_caliber TEXT COMMENT '技术口径',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    security_level VARCHAR(8) NOT NULL DEFAULT 'L1' COMMENT '数据密级: L1/L2/L3/L4',
    owner VARCHAR(64) COMMENT '负责人',
    version INT NOT NULL COMMENT '快照时的版本号',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '修改人',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '逻辑删除时间',
    ext_json JSON COMMENT '扩展信息快照（原子/派生/复合）',
    snapshot_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
    INDEX idx_metric_code (metric_code),
    INDEX idx_metric_version (metric_code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标定义历史快照表';
