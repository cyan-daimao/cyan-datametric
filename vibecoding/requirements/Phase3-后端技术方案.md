# Phase 3：统一语义层 + 智能物化加速 — 后端技术方案

## 1. 架构设计

### 1.1 总体架构图（文字描述）

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                接入层 (Adapter)                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ 语义模型管理   │  │ 自助分析 API │  │ 物化中心 API │  │ 血缘与影响分析 API│ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘ │
└─────────┼─────────────────┼─────────────────┼───────────────────┼───────────┘
          │                 │                 │                   │
┌─────────┼─────────────────┼─────────────────┼───────────────────┼───────────┐
│         ▼                 ▼                 ▼                   ▼           │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                         应用层 (Application)                          │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │  │
│  │  │ SemanticQueryEngine│  │MaterializedViewService│  │ LineageEngine   │   │  │
│  │  │  ├─JoinPathResolver│  │  ├─QueryRouter      │  │  ├─ImpactAnalyzer│   │  │
│  │  │  ├─SemanticSqlBuilder│ │  ├─RefreshScheduler │  │  └─LineageGraph  │   │  │
│  │  │  └─SmartRouter     │  │  └─AutoEvictor      │  │                  │   │  │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
          │                 │                 │
┌─────────┼─────────────────┼─────────────────┼───────────────────────────────┐
│         ▼                 ▼                 ▼                               │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                         领域层 (Domain)                               │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────────┐  │  │
│  │  │LogicalTable│ │TableRelation│ │SemanticMetric│ │MaterializedView │  │  │
│  │  └────────────┘ └────────────┘ └────────────┘ └──────────────────┘  │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐                       │  │
│  │  │JoinCondition│ │ QueryPlan  │ │SemanticModel│                      │  │
│  │  └────────────┘ └────────────┘ └────────────┘                       │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
          │                 │                 │
┌─────────┼─────────────────┼─────────────────┼───────────────────────────────┐
│         ▼                 ▼                 ▼                               │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                      基础设施层 (Infrastructure)                       │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────────┐  │  │
│  │  │ MyBatis-Plus│ │ StarRocks  │ │  Scheduler │ │   Kafka (可选)    │  │  │
│  │  │   Mapper   │ │   Client   │ │  (XxlJob)  │ │   Binlog Sync    │  │  │
│  │  └────────────┘ └────────────┘ └────────────┘ └──────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 核心模块划分

| 模块 | 职责 | 关键类 |
|------|------|--------|
| **语义层服务** | 管理逻辑表、关联关系、语义指标定义 | `LogicalTable`, `TableRelationship`, `SemanticMetric` |
| **查询引擎** | 接收指标+维度组合，自动计算 JOIN 路径，生成可执行 SQL | `SemanticQueryEngine`, `JoinPathResolver`, `SemanticSqlBuilder` |
| **物化引擎** | 物化视图生命周期管理、刷新调度、命中率监控与自动淘汰 | `MaterializedViewService`, `QueryRouter`, `AutoEvictor` |
| **血缘引擎** | 指标血缘图构建、变更影响推演 | `LineageEngine`, `ImpactAnalyzer` |

### 1.3 与现有系统的集成点

1. **兼容 Phase 1/2 维度-事实关联配置**：
   - 现有 `Dimension` 中的 `tableName` + `columnName` 在导入语义模型时自动转换为 `LogicalTable`（类型为 DIMENSION）
   - 现有 `MetricAtomicExt` 中的 `dbName` + `tblName` 自动转换为 `LogicalTable`（类型为 FACT）
   - 当 `Dimension` 与 `Metric` 关联到同一张事实表时，自动创建 `TableRelationship`（`Dimension.tableName == MetricAtomicExt.tblName` 的直接关联）
   - 通过 `SemanticModelMigrationService`（一期可选实现）批量导入现有配置

2. **平滑迁移 BiAnalysisServiceImpl / MetricSqlBuilder**：
   - `SemanticQueryEngine` 提供 `execute(SemanticQueryCmd)` 接口，内部替代 `MetricResolver` + `TableConsistencyChecker` + `MetricSqlBuilder` 的组合
   - 保留原有 `BiAnalysisServiceImpl` 作为兼容层，当查询未命中语义模型时回退到旧逻辑
   - `SemanticSqlBuilder` 复用 `MetricSqlBuilder` 的聚合表达式生成逻辑（提取为 `AggExpressionBuilder` 工具类）

---

## 2. 数据模型设计（充血模型）

### 2.1 领域对象清单

#### LogicalTable（逻辑表）
```
- id: String
- tableName: String          // 物理表全名（db.table）
- displayName: String        // 展示名称
- tableType: TableType       // FACT / DIMENSION / BRIDGE
- primaryKey: String         // 主键字段
- timeColumn: String         // 时间字段（用于增量刷新）
- schema: List<ColumnSchema> // 字段 schema
- description: String
- createBy / updateBy / createdAt / updatedAt
```

#### TableRelationship（表关联关系）
```
- id: String
- leftTableId: String        // 左表（通常为事实表或维度表）
- rightTableId: String       // 右表（通常为维度表）
- joinType: JoinType         // INNER / LEFT / RIGHT / FULL
- conditions: List<JoinCondition>
- description: String
```

#### JoinCondition（关联条件）
```
- leftColumn: String
- rightColumn: String
```

#### SemanticMetric（语义指标）
```
- id: String
- metricCode: String
- metricName: String
- metricType: MetricType     // ATOMIC / DERIVED / COMPOSITE
- sourceTableId: String      // 关联 LogicalTable.id
- sourceColumn: String       // 来源字段
- statFunc: StatFunc         // 聚合函数
- formula: String            // 复合指标公式
- modifiers: List<String>    // 修饰词 ID 列表
- timePeriod: TimePeriod     // 时间周期
- description: String
```

#### MaterializedView（物化视图）
```
- id: String
- name: String
- definitionSql: String      // 定义 SQL（含 GROUP BY 维度）
- sourceTables: List<String> // 来源逻辑表 ID 列表
- dimensions: List<String>   // 物化维度字段列表
- metrics: List<String>      // 物化指标编码列表
- refreshStrategy: RefreshStrategy  // FULL / INCREMENTAL / REALTIME / ON_DEMAND
- cronExpression: String
- lastRefreshTime: LocalDateTime
- status: MvStatus           // ACTIVE / REFRESHING / FAILED / DISABLED
- hitCount: Long
- lastHitTime: LocalDateTime
- storageSize: Long          // 存储大小（字节）
- createBy / updateBy / createdAt / updatedAt
```

#### QueryPlan（查询计划）
```
- id: String
- queryHash: String          // 查询特征哈希（指标+维度组合）
- querySql: String           // 最终执行 SQL
- routeType: RouteType       // MATERIALIZED / REALTIME / FALLBACK
- mvId: String              // 命中的物化视图 ID
- costTimeMs: Long
- hitCache: Boolean
- createdAt: LocalDateTime
```

### 2.2 充血模型行为设计

- `LogicalTable`：提供 `validate()`, `save()`, `update()`, `delete()`, `isFactTable()`, `isDimensionTable()` 方法
- `TableRelationship`：提供 `validate()`, `save()`, `update()`, `getJoinSql(leftAlias, rightAlias)` 方法
- `SemanticMetric`：提供 `validate()`, `save()`, `update()`, `buildAggExpression()`, `isComposite()` 方法
- `MaterializedView`：提供 `validate()`, `save()`, `markRefreshing()`, `markActive()`, `recordHit()`, `shouldEvict()` 方法

---

## 3. SQL 引擎改造

### 3.1 JOIN 路径计算

**算法核心**：基于图遍历的最短 JOIN 路径（BFS）

```
输入：
  - metrics: List<SemanticMetric>    // 每个指标关联到 sourceTableId
  - dimensions: List<LogicalTable>   // 每个维度关联到 logicalTable

输出：
  - List<JoinEdge>                   // JOIN 顺序和条件

步骤：
1. 收集所有涉及的事实表（metrics 的 sourceTableId）
2. 收集所有涉及的维度表（dimensions 的 logicalTableId，或 dimension 直接关联的 tableId）
3. 构建表关系图 G = (V, E)，V 为所有逻辑表，E 为 TableRelationship
4. 若只有一张事实表：
   a. 对该事实表，BFS 查找每个维度表的最短路径
   b. 路径上的所有中间维度表自动加入 JOIN 链
5. 若有多张事实表（星座模型）：
   a. 选择一张中心事实表（指标数最多或数据量最小）
   b. 其他事实表通过共享维度间接 JOIN 到中心事实表
   c. 合并所有路径，消除重复边
6. 生成 JoinEdge 列表，每张表分配别名 t0, t1, t2...
```

**星型模型示例**：
```
事实表 orders (t0) → INNER JOIN 维度表 dim_user (t1) ON t0.user_id = t1.id
                  → INNER JOIN 维度表 dim_product (t2) ON t0.product_id = t2.id
```

**雪花模型示例**：
```
事实表 orders (t0) → INNER JOIN 维度表 dim_user (t1) ON t0.user_id = t1.id
                  → INNER JOIN 维度表 dim_region (t2) ON t1.region_id = t2.id
```

**星座模型示例**：
```
事实表 orders (t0) → INNER JOIN 维度表 dim_user (t1) ON t0.user_id = t1.id
事实表 refunds (t2) → INNER JOIN 维度表 dim_user (t1) ON t2.user_id = t1.id
（共享维度 dim_user 作为桥接）
```

### 3.2 SQL 生成

基于 JOIN 路径生成最终 SQL：
1. **SELECT**：维度列（含别名）+ 指标聚合表达式
2. **FROM**：起始事实表
3. **JOIN**：按路径顺序展开 JOIN 链
4. **WHERE**：指标自带过滤 + 用户过滤 + 时间周期过滤
5. **GROUP BY**：所有维度列（事实表级别 + 维度表级别）
6. **ORDER BY / LIMIT**：用户传入

### 3.3 与现有 BiAnalysisServiceImpl / MetricSqlBuilder 的集成

- 新增 `SemanticQueryEngine` 作为 Phase 3 主入口
- 保留 `BiAnalysisServiceImpl` 作为降级策略（`FallbackBiAnalysisService`）
- 提取公共的聚合表达式生成逻辑到 `AggExpressionBuilder`
- `SemanticSqlBuilder` 在构建指标表达式时，调用 `ResolvedMetric` / `SemanticMetric` 的 `buildAggExpression()`

---

## 4. 物化加速设计

### 4.1 物化视图管理

**生命周期状态机**：
```
CREATED → ACTIVE → REFRESHING → ACTIVE
   ↓         ↓         ↓
DISABLED  DISABLED   FAILED → ACTIVE（手动恢复）
```

**创建流程**：
1. 用户或系统自动生成 `definitionSql`
2. 在 StarRocks 中执行 `CREATE MATERIALIZED VIEW`（或异步物化视图）
3. 记录元数据到 `materialized_view` 表
4. 首次全量刷新

### 4.2 刷新策略

| 策略 | 适用场景 | 实现方式 |
|------|----------|----------|
| FULL | 小表（< 1000万行） | `INSERT OVERWRITE` 全量覆盖 |
| INCREMENTAL | 大表，有时间字段 | 基于 `timeColumn` 的 `WHERE timeColumn > lastRefreshTime` 增量插入 |
| REALTIME | 实时要求高的场景 | Kafka → Flink/StarRocks Routine Load → 物化视图（设计预留） |
| ON_DEMAND | 低频查询 | 用户手动触发或首次查询时触发 |

### 4.3 查询路由

**三级路由策略**：

1. **精确匹配路由**：
   - 查询的 `metrics` + `dimensions` 组合与物化视图完全一致
   - 直接 `SELECT * FROM mv_name WHERE ...`

2. **上卷路由（Roll-up）**：
   - 物化视图粒度更粗（如日级），查询粒度更细（如周级）但可通过二次聚合满足
   - 例：物化到 `dim_date` 日级，查询周级 → `SELECT WEEK(dim_date), SUM(metric) FROM mv GROUP BY WEEK(dim_date)`
   - 需要 `statFunc` 为 `SUM` / `COUNT` / `COUNT_DISTINCT` 等可上卷聚合函数

3. **部分匹配路由**：
   - 物化视图包含查询所需全部字段，但可能包含额外维度
   - 在物化视图基础上增加 `WHERE` 过滤即可满足
   - 例：物化视图按 `province, city` 聚合，查询仅需 `province` → 加 `GROUP BY province` 二次聚合

**路由决策流程**：
```
输入查询 Q(metrics, dimensions, filters)
1. 精确匹配：查找 mv.metrics == Q.metrics && mv.dimensions == Q.dimensions
   → 命中则返回 mv
2. 上卷匹配：查找 mv.metrics ⊇ Q.metrics && mv.dimensions ⊃ Q.dimensions
   → 检查 Q.dimensions 是否为 mv.dimensions 的上卷维度
   → 检查 mv.metrics 的 statFunc 是否支持上卷
   → 命中则返回 mv + 二次聚合 SQL
3. 部分匹配：查找 mv.metrics ⊇ Q.metrics && mv.dimensions ⊇ Q.dimensions
   → 命中则返回 mv + 过滤 SQL
4. 未命中 → 走实时 JOIN 计算
```

### 4.4 自动淘汰

- 条件：`hitCount < 10` 且 `lastHitTime < now() - 30 days` 且 `storageSize > 100MB`
- 动作：将状态置为 `DISABLED`，发送通知，7 天后物理删除
- 可调参数：通过配置中心调整阈值

---

## 5. 血缘与影响分析设计

### 5.1 指标血缘图构建

**节点类型**：`METRIC` / `TABLE` / `COLUMN` / `DIMENSION` / `MODIFIER`

**构建方式**：
1. 解析 `SemanticMetric`：
   - 原子指标 → 关联到 `LogicalTable` + `sourceColumn`
   - 派生指标 → 关联到原子指标 + `Modifier` + `TimePeriod`
   - 复合指标 → 关联到引用的指标列表
2. 解析 `TableRelationship`：
   - 事实表 → 维度表 → 维度字段
3. 血缘图以 Neo4j / 内存图（JGraphT）存储

### 5.2 变更影响推演算法

```
输入：变更节点 N（如某 LogicalTable 的 sourceColumn 变更）
输出：受影响节点列表 + 影响路径

步骤：
1. 从 N 出发，DFS 遍历血缘图
2. 记录所有可达的 METRIC 节点
3. 按层级排序（直接依赖 → 间接依赖）
4. 生成影响报告：
   - 直接影响：引用该字段的原子指标
   - 间接影响：引用这些原子指标的派生/复合指标
   - 下游影响：使用这些指标的 BI 图表、看板、数据加工任务
```

---

## 6. 数据库 Schema 变更（DDL）

```sql
-- ============================================
-- 逻辑表管理
-- ============================================
CREATE TABLE semantic_logical_table (
    id              BIGINT          PRIMARY KEY COMMENT '主键',
    table_name      VARCHAR(255)    NOT NULL COMMENT '物理表全名（db.table）',
    display_name    VARCHAR(255)    NOT NULL COMMENT '展示名称',
    table_type      VARCHAR(32)     NOT NULL COMMENT 'FACT/DIMENSION/BRIDGE',
    primary_key     VARCHAR(128)    COMMENT '主键字段',
    time_column     VARCHAR(128)    COMMENT '时间字段（增量刷新用）',
    schema_json     TEXT            COMMENT '字段 schema JSON',
    description     VARCHAR(1024)   COMMENT '描述',
    create_by       VARCHAR(64)     COMMENT '创建人',
    update_by       VARCHAR(64)     COMMENT '修改人',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at      DATETIME        COMMENT '逻辑删除时间',
    INDEX idx_table_name (table_name),
    INDEX idx_table_type (table_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层-逻辑表';

-- ============================================
-- 表关联关系
-- ============================================
CREATE TABLE semantic_table_relationship (
    id              BIGINT          PRIMARY KEY COMMENT '主键',
    left_table_id   BIGINT          NOT NULL COMMENT '左表ID',
    right_table_id  BIGINT          NOT NULL COMMENT '右表ID',
    join_type       VARCHAR(32)     NOT NULL COMMENT 'INNER/LEFT/RIGHT/FULL',
    conditions_json TEXT            NOT NULL COMMENT '关联条件 JSON 数组',
    description     VARCHAR(1024)   COMMENT '描述',
    create_by       VARCHAR(64)     COMMENT '创建人',
    update_by       VARCHAR(64)     COMMENT '修改人',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at      DATETIME        COMMENT '逻辑删除时间',
    INDEX idx_left_table (left_table_id),
    INDEX idx_right_table (right_table_id),
    UNIQUE KEY uk_rel (left_table_id, right_table_id, join_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层-表关联关系';

-- ============================================
-- 语义指标（与 metric_definition 互补，增加语义层专属字段）
-- ============================================
CREATE TABLE semantic_metric (
    id              BIGINT          PRIMARY KEY COMMENT '主键',
    metric_code     VARCHAR(128)    NOT NULL COMMENT '指标编码',
    metric_name     VARCHAR(255)    NOT NULL COMMENT '指标名称',
    metric_type     VARCHAR(32)     NOT NULL COMMENT 'ATOMIC/DERIVED/COMPOSITE',
    source_table_id BIGINT          NOT NULL COMMENT '来源逻辑表ID',
    source_column   VARCHAR(128)    COMMENT '来源字段',
    stat_func       VARCHAR(64)     COMMENT '聚合函数',
    formula         TEXT            COMMENT '复合指标公式',
    modifiers_json  TEXT            COMMENT '修饰词ID列表 JSON',
    time_period_id  BIGINT          COMMENT '时间周期配置ID',
    description     VARCHAR(1024)   COMMENT '描述',
    create_by       VARCHAR(64)     COMMENT '创建人',
    update_by       VARCHAR(64)     COMMENT '修改人',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at      DATETIME        COMMENT '逻辑删除时间',
    UNIQUE KEY uk_metric_code (metric_code),
    INDEX idx_source_table (source_table_id),
    INDEX idx_metric_type (metric_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层-语义指标';

-- ============================================
-- 物化视图
-- ============================================
CREATE TABLE semantic_materialized_view (
    id                  BIGINT          PRIMARY KEY COMMENT '主键',
    name                VARCHAR(255)    NOT NULL COMMENT '物化视图名称',
    definition_sql      TEXT            NOT NULL COMMENT '定义SQL',
    source_tables_json  TEXT            NOT NULL COMMENT '来源逻辑表ID列表 JSON',
    dimensions_json     TEXT            COMMENT '物化维度字段列表 JSON',
    metrics_json        TEXT            COMMENT '物化指标编码列表 JSON',
    refresh_strategy    VARCHAR(32)     NOT NULL COMMENT 'FULL/INCREMENTAL/REALTIME/ON_DEMAND',
    cron_expression     VARCHAR(128)    COMMENT 'Cron表达式',
    last_refresh_time   DATETIME        COMMENT '上次刷新时间',
    status              VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REFRESHING/FAILED/DISABLED',
    hit_count           BIGINT          DEFAULT 0 COMMENT '命中次数',
    last_hit_time       DATETIME        COMMENT '上次命中时间',
    storage_size        BIGINT          DEFAULT 0 COMMENT '存储大小（字节）',
    create_by           VARCHAR(64)     COMMENT '创建人',
    update_by           VARCHAR(64)     COMMENT '修改人',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at          DATETIME        COMMENT '逻辑删除时间',
    INDEX idx_status (status),
    INDEX idx_refresh_strategy (refresh_strategy)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层-物化视图';

-- ============================================
-- 查询计划（用于命中监控与查询分析）
-- ============================================
CREATE TABLE semantic_query_plan (
    id              BIGINT          PRIMARY KEY COMMENT '主键',
    query_hash      VARCHAR(64)     NOT NULL COMMENT '查询特征哈希',
    query_sql       TEXT            NOT NULL COMMENT '最终执行SQL',
    route_type      VARCHAR(32)     NOT NULL COMMENT 'MATERIALIZED/REALTIME/FALLBACK',
    mv_id           BIGINT          COMMENT '命中物化视图ID',
    cost_time_ms    BIGINT          COMMENT '耗时（毫秒）',
    hit_cache       TINYINT(1)      DEFAULT 0 COMMENT '是否命中缓存',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_query_hash (query_hash),
    INDEX idx_mv_id (mv_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语义层-查询计划';
```

---

## 7. 性能目标与保障措施

| 目标 | 措施 |
|------|------|
| 命中物化视图 P99 < 200ms | 物化视图存储在 StarRocks，利用其列存 + MPP 优势；查询计划缓存 |
| 实时 JOIN P99 < 3s（单事实表+2维度） | JOIN 路径优化（最短路径 + 小表广播）；StarRocks 分布式 JOIN；限制返回行数（LIMIT 10000） |
| 支持 100+ 逻辑表、1000+ 指标 | 元数据缓存（Caffeine Local Cache）；表关系图内存化 |
| 支持 100+ 物化视图 | 自动淘汰 + 命中率监控；优先级队列调度刷新 |

---

## 8. 风险评估与回退策略

| 风险 | 应对措施 |
|------|----------|
| JOIN 路径计算错误导致 SQL 错误 | 增加 SQL 语法校验（StarRocks `EXPLAIN`）；灰度发布 |
| 物化视图数据延迟 | 默认走实时 JOIN，物化视图仅用于加速；关键看板强制实时 |
| 元数据与物理表不一致 | 定时校验任务（对比 StarRocks `INFORMATION_SCHEMA`） |
| 自动淘汰误删高频 MV | 淘汰前 7 天通知 + 软删除；可一键恢复 |
