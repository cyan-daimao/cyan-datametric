# cyan-datametric — 数据资产指标平台

> 本文档面向 AI Coding Agent。如果你刚拿到这个项目且对它一无所知，请先完整阅读本文档。

---

## 1. 项目概述

`cyan-datametric` 是 Cyan 公司内部的数据资产指标平台后端服务，用于统一管理数据指标体系。核心能力包括：

- **三种指标类型**：原子指标（ATOMIC）、派生指标（DERIVED）、复合指标（COMPOSITE）。
- **指标生命周期管理**：草稿（DRAFT）→ 已发布（PUBLISHED）→ 已下线（OFFLINE），支持版本快照与回滚。
- **血缘追踪**：自动构建指标与上游表/字段/指标之间的血缘关系。
- **SQL 预览与试算**：根据指标定义动态生成可执行的 SQL。
- **指标字典与收藏**：支持按主题域检索、个人收藏。
- **公共维度与修饰词管理**：为派生指标提供下钻维度与过滤条件。

项目采用 **Maven 多模块** 结构，主模块为可执行的 Spring Boot 应用，client 模块对外暴露枚举与 Feign 客户端能力。

---

## 2. 技术栈与运行环境

| 组件 | 版本 / 选型 | 说明 |
|------|------------|------|
| Java | 21 | 编译与运行版本 |
| Spring Boot | 3.3.13 | 主框架 |
| Spring Cloud | — | 使用 Nacos 做配置中心与服务发现；使用 OpenFeign 做 RPC |
| MyBatis-Plus | 3.5.7 | ORM 与分页 |
| MySQL | 8.3.0 | 业务数据库（驱动 `com.mysql.cj.jdbc.Driver`） |
| MapStruct | — | 层间对象转换（配合 `arch-common` 的 `MapstructConvert`） |
| Lombok | 1.18.42 | 简化样板代码 |
| Maven | — | 构建工具 |

### 外部依赖的中间件
- **Nacos**：配置中心与服务注册中心（地址见 `bootstrap-dev.yml`）。
- **MySQL**：业务库名 `cyan_datametric`。

### 内部依赖的 Cyan 公共组件
- `arch` / `arch-common` / `arch-base`：公共响应体、分页、断言、雪花 ID、MapStruct 增强等。
- `cyan-employee-login`：员工登录认证与 `UserContextHolder`。
- `cyan-dataman-client`：数据管理相关客户端（在 application 模块引入）。

---

## 3. 项目结构（Maven 多模块）

```
cyan-datametric/
├── pom.xml                          # 父 POM：统一依赖管理与插件配置
├── cyan-datametric-client/          # 客户端模块（JAR）
│   └── src/main/java/com/cyan/datametric/enums/
│       ├── MetricStatus.java        # 指标状态枚举
│       ├── MetricType.java          # 指标类型枚举
│       ├── PeriodType.java          # 时间周期类型枚举
│       ├── RelativeUnit.java        # 相对时间单位枚举
│       └── StatFunc.java            # 统计函数枚举
│
└── cyan-datametric-application/     # 主应用模块（可执行 Spring Boot JAR）
    ├── pom.xml
    └── src/main/java/com/cyan/datametric/
        ├── Application.java         # 启动类（@SpringBootApplication + @EnableDiscoveryClient）
        │
        ├── adapter/                 # 【适配层】HTTP 入口
        │   ├── analysis/http/       # 指标分析相关 Controller
        │   ├── config/http/         # 维度/修饰词/时间周期 Controller
        │   ├── dashboard/http/      # 看板统计 Controller
        │   └── metric/http/         # 指标 CRUD、SQL 预览、血缘、版本 Controller
        │       ├── convert/         # AdapterConvert（BO → DTO）
        │       └── dto/             # DTO（返回前端）
        │
        ├── application/             # 【应用层】流程编排、业务组装
        │   ├── config/              # 维度/修饰词/时间周期 Service
        │   └── metric/              # 指标 Service
        │       ├── bo/              # BO（跨领域聚合对象）
        │       ├── cmd/             # Cmd（写操作入参）
        │       ├── convert/         # AppConvert（Domain ↔ BO/Cmd）
        │       └── impl/            # Service 实现类
        │
        ├── domain/                  # 【领域层】充血模型 + 仓储接口
        │   ├── config/              # 维度/修饰词/时间周期领域对象与仓储接口
        │   └── metric/              # 指标领域对象与仓储接口
        │       ├── repository/      # 仓储接口（只定义，不实现）
        │       └── query/           # 领域查询对象
        │
        └── infra/                   # 【基础设施层】数据存取、仓储实现
            ├── config/              # MyBatis-Plus 配置等
            ├── persistence/         # Mapper + DO + RepositoryImpl
            │   ├── config/          # 维度/修饰词/时间周期持久化
            │   └── metric/          # 指标持久化
            │       ├── convert/     # InfraConvert（DO ↔ Domain）
            │       ├── dos/         # DO（与数据库表一一对应）
            │       ├── mappers/     # MyBatis-Plus Mapper 接口
            │       └── repository/  # 仓储实现类
            └── util/
                └── SnowflakeIdUtil.java   # 雪花 ID 生成工具
```

> 数据库表初始化脚本位于 `cyan-datametric-application/src/main/resources/db/schema.sql`，内含预置种子数据。

---

## 4. 架构规范（DDBD 四层架构）

本项目严格遵循 **DDBD 简化版 DDD 规范**。AI Agent 生成或修改代码时必须遵守以下铁律：

### 4.1 四层职责

| 层级 | 职责 | 返回类型 |
|------|------|---------|
| **Adapter** | HTTP/RPC 入口；接收参数、调用 Application、返回 DTO | DTO |
| **Application** | 流程编排、业务组装、业务校验；**不直接操作数据库** | BO |
| **Domain** | 充血模型：实体属性 + 业务行为；定义仓储接口与查询对象 | Domain |
| **Infrastructure** | 数据持久化、仓储实现、第三方调用、工具类 | DO |

### 4.2 对象规范

| 对象 | 定义层级 | 用途 | 关键规则 |
|------|---------|------|---------|
| DO | Infra | 与数据库表一一对应 | 字段类型与数据库列一致；枚举可直接映射 `varchar` |
| Domain | Domain | 封装属性 + 行为 | `id` 必须为 **String**；充血模型，禁止贫血 |
| BO | App | 多领域聚合 / 接口返回中间对象 | 由 Application 层按需组装 |
| DTO | Adapter | 返回给前端 / RPC | 禁止暴露内部结构 |
| Cmd | App | 写操作入参 | — |
| Query | Domain | 查询入参 | — |

### 4.3 转换链路（不可跳过、不可逆序）

```
读：DO → Domain → BO → DTO
写：Cmd → Domain → (仓储) → DO
查：Query 透传 Adapter → App → Domain → Infra
```

每层转换类命名规范：`XxxAdapterConvert`、`XxxAppConvert`、`XxxInfraConvert`。

### 4.4 关键约定

1. **禁止贫血模型**：领域对象必须包含业务方法（如 `save()`、`update()`、`publish()`、`offline()`、`delete()`）。
2. **Controller 不调 Mapper**：Controller 只能调用 Application Service；禁止跳过 Service 直接查库。
3. **UserContextHolder 只在 Controller 使用**：通过 `UserContextHolder.getCurrentEmployee().getPassport()` 获取当前登录人，以参数形式透传给下层。
4. **所有查询必须经过领域层**：禁止在 Application 层直接构造 MyBatis Wrapper。
5. **每个接口有独立的 DTO/Request**：禁止复用。
6. **禁止用 DO/Domain 直接返回前端**：必须经过 DTO 转换。
7. **ID 类型**：Domain 层 `String`，DO 层与数据库一致（`bigint` → `Long`）。原因：前端 JS 对 2^53 以上 Long 精度丢失。
8. **枚举格式**：`ENUM_NAME("code", "描述")`，并提供 `of(String code)` 工厂方法。
9. **断言风格**：`Assert.notBlank(name, new BusinessException("msg"))` — 不满足立即抛异常。
10. **响应格式**：统一使用 `Response<T> { code, message, data }`（来自 `arch-common`）。
11. **分页格式**：`PageQuery { pageNum, pageSize }` → `PageResult<T> { list, total, pageNum, pageSize }`。
12. **中文注释**：每个类的属性与方法都必须有中文 Javadoc/KDoc 注释。

---

## 5. 构建与运行

### 5.1 本地编译打包

```bash
# 在项目根目录执行
mvn clean package
```

主模块会生成可执行 JAR：`cyan-datametric-application/target/cyan-datametric.jar`。

### 5.2 运行主应用

```bash
# 方式一：直接运行 Spring Boot
mvn -pl cyan-datametric-application spring-boot:run -Dspring-boot.run.profiles=dev

# 方式二：运行打包后的 JAR
java -jar cyan-datametric-application/target/cyan-datametric.jar --spring.profiles.active=dev
```

### 5.3 本地启动前置条件

1. **MySQL**：创建数据库 `cyan_datametric`，执行 `schema.sql`。
2. **Nacos**（可选，若只本地联调可跳过配置中心）：确保 `bootstrap-dev.yml` 中的 Nacos 地址可达；或直接在 `bootstrap-dev.yml` 中覆盖所需配置。

### 5.4 部署发布

- 通过 `mvn deploy` 发布到内部 Nexus 仓库：
  - Release：`http://nexus.cyan.com/repository/maven-releases/`
  - Snapshot：`http://nexus.cyan.com/repository/maven-snapshots/`

---

## 6. 配置文件说明

| 文件 | 作用 |
|------|------|
| `bootstrap.yml` | 基础配置：应用名 `cyan-datametric`，默认 profile `dev`，暴露所有 Actuator 端点 |
| `bootstrap-dev.yml` | 开发环境：Nacos 配置中心、MySQL 数据源、服务端口 8080 |
| `bootstrap-pre.yml` | 预发环境配置 |
| `bootstrap-pro.yml` | 生产环境配置 |

> 配置中心通过 Nacos 导入 `arch-base.yaml` 与 `cyan-datametric.yaml`（`DEFAULT_GROUP`，`dev` 命名空间）。

---

## 7. 核心领域说明

### 7.1 指标（Metric）

- **原子指标**：直接映射到物理表的聚合字段（SUM/AVG/COUNT/COUNT_DISTINCT/MAX/MIN）。
- **派生指标**：基于原子指标 + 时间周期 + 修饰词 + 维度分组。
- **复合指标**：通过公式引用其他指标计算得出。

### 7.2 状态机

指标生命周期状态：
- `DRAFT`（草稿）→ `PUBLISHED`（已发布）
- `PUBLISHED` → `OFFLINE`（已下线）
- `OFFLINE` → `PUBLISHED`
- 编辑已发布指标时会自动创建历史快照，并生成新版本草稿。

### 7.3 关键数据库表

| 表名 | 说明 |
|------|------|
| `metric_definition` | 指标主表（通用字段） |
| `metric_atomic` | 原子指标扩展 |
| `metric_derived` | 派生指标扩展 |
| `metric_composite` | 复合指标扩展 |
| `metric_definition_history` | 指标版本历史快照 |
| `metric_lineage` | 血缘关系 |
| `metric_favorite` | 用户收藏 |
| `metric_subject` | 主题域 |
| `metric_dimension` | 公共维度 |
| `metric_dimension_category` | 维度分类 |
| `metric_modifier` | 修饰词 |
| `metric_time_period` | 时间周期 |

---

## 8. 测试策略

> ⚠️ **当前项目暂无测试目录**。如需新增测试，建议遵循以下规范：

- 单元测试：针对 Domain 层充血模型的业务方法进行测试（纯 Java，无 Spring 上下文）。
- 集成测试：针对 Application Service 使用 `@SpringBootTest`，配合 H2 或测试环境 MySQL。
- 禁止在单元测试中启动完整的 Spring 上下文来测试简单的工具类。

---

## 9. 安全与注意事项

1. **认证**：依赖 `cyan-employee-login` 模块，所有接口默认在网关层鉴权；Controller 中通过 `UserContextHolder` 获取当前登录人护照号（passport）。
2. **配置安全**：`bootstrap-dev.yml` 中明文存放了开发环境 Nacos 与 MySQL 密码，**禁止将此类文件提交到生产仓库**。生产环境应通过环境变量或配置中心加密能力注入。
3. **SQL 注入风险**：`MetricServiceImpl.previewSql` 与 `buildAtomicSql` / `buildDerivedSql` 中通过字符串拼接生成 SQL。当前实现仅用于预览/试算，若未来直接执行，必须引入参数化查询或 SQL 构建器。
4. **数据权限**：当前未做行级数据权限隔离，所有登录用户可见全量指标；如需扩展，应在 Application 层根据用户角色注入过滤条件。

---

## 10. 代码风格速查

```java
// 1. Controller 示例
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {
    private final MetricService metricService;

    @GetMapping("/page")
    public Response<PageResultDTO<MetricDTO>> page(MetricPageQuery query) {
        String currentUser = UserContextHolder.getCurrentEmployee().getPassport();
        Page<MetricBO> page = metricService.page(query, currentUser);
        return Response.success(new PageResultDTO<>(
            page.getData().stream().map(MetricAdapterConvert.INSTANCE::toMetricDTO).toList(),
            page.getCurrent(), page.getSize(), page.getTotal()));
    }
}

// 2. 充血模型示例
@Data
@Accessors(chain = true)
public class Metric {
    private String id;
    private MetricStatus status;
    // ... 其他字段

    public Metric publish(MetricRepository repository) {
        Assert.notBlank(this.id, new BusinessException("ID不能为空"));
        Assert.isTrue(this.status == MetricStatus.DRAFT || this.status == MetricStatus.OFFLINE,
            new BusinessException("只有草稿或已下线状态的指标可发布"));
        this.status = MetricStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
        return repository.update(this);
    }
}

// 3. 枚举示例
@Getter
@AllArgsConstructor
public enum MetricStatus {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    OFFLINE("OFFLINE", "已下线");

    private final String code;
    private final String desc;

    public static MetricStatus of(String code) {
        for (MetricStatus value : values()) {
            if (value.code.equals(code)) return value;
        }
        return null;
    }
}
```

---

## 11. 常用命令汇总

```bash
# 编译
mvn clean compile

# 打包
mvn clean package

# 仅打包 application 模块
mvn -pl cyan-datametric-application clean package

# 安装到本地 Maven 仓库
mvn clean install

# 发布到 Nexus
mvn clean deploy

# 运行
mvn -pl cyan-datametric-application spring-boot:run
```

---

> 最后更新：基于当前代码库实际内容生成，未做任何假设性推断。
