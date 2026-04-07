# Hive表Location路径控制 - 需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-ENHANCE-HIVE-LOCATION-001 |
| 需求名称 | Hive表Location路径控制 |
| 需求类型 | 功能增强（ENHANCE） |
| 优先级 | P1（高优先级） |
| 涉及模块 | linkis-computation-governance/linkis-entrance |
| 文档版本 | v2.0 |
| 创建时间 | 2026-03-25 |
| 最后更新 | 2026-03-25 |

---

## 一、功能概述

### 1.1 功能名称

Hive表Location路径控制

### 1.2 一句话描述

在Entrance层拦截Hive CREATE TABLE语句中的LOCATION参数，防止用户通过指定LOCATION路径创建表，保护数据安全。

### 1.3 功能背景

**当前痛点**：
- 用户可以通过CREATE TABLE语句指定LOCATION参数，将表数据存储在任意HDFS路径
- 可能导致关键业务表数据被误删或恶意修改
- 威胁数据安全性和业务稳定性
- 缺乏统一的安全控制机制

**影响范围**：
- 所有通过Linkis执行的Hive任务（交互式查询和批量任务）
- 生产环境存在数据安全风险

### 1.4 期望价值

**主要价值**：
- 防止用户恶意或误操作通过LOCATION指定路径创建Hive表
- 统一在Entrance层进行拦截，避免用户绕过控制
- 保护核心业务数据安全，提升系统安全性

**次要价值**：
- 提供完整的操作审计日志，满足合规要求
- 简单的配置机制，易于部署和维护
- 清晰的错误提示，提升用户体验

---

## 二、功能范围

### 2.1 核心功能（P0）

| 功能点 | 描述 | 验收标准 |
|-------|------|---------|
| **LOCATION参数拦截** | 在Entrance层拦截CREATE TABLE语句中的LOCATION参数 | 所有包含LOCATION的CREATE TABLE语句被拦截 |
| **功能开关** | 提供配置项开关，允许管理员启用/禁用该功能 | 开关控制生效，禁用时不影响现有功能 |
| **错误提示** | 返回明确的错误信息，说明为什么被拦截 | 错误信息清晰，指导用户正确操作 |

### 2.2 增强功能（P1）

| 功能点 | 描述 | 验收标准 |
|-------|------|---------|
| **审计日志** | 记录所有被拦截的LOCATION操作 | 所有被拦截的操作都有日志记录 |
| **CTAS语句拦截** | 拦截CREATE TABLE AS SELECT中的LOCATION参数 | CTAS语句中的LOCATION被正确拦截 |

### 2.3 不在范围内

- **不拦截ALTER TABLE语句的SET LOCATION操作**（仅拦截CREATE TABLE）
- **不提供任何白名单或豁免机制**（完全禁止指定LOCATION）
- **不影响非LOCATION相关的Hive操作**
- **不涉及跨引擎的控制**（仅限Hive引擎）
- **不拦截CTAS语句**（CREATE TABLE AS SELECT，不指定LOCATION的情况）

---

## 三、详细功能需求

### 3.1 拦截规则

#### 3.1.1 需要拦截的SQL语句

| SQL类型 | 示例 | 是否拦截 |
|---------|------|---------|
| CREATE TABLE ... LOCATION | `CREATE TABLE t ... LOCATION '/path'` | **拦截** |
| CTAS with LOCATION | `CREATE TABLE t AS SELECT ... LOCATION '/path'` | **拦截** |
| CREATE TABLE without LOCATION | `CREATE TABLE t ...` (不指定LOCATION) | **放行** |
| CTAS without LOCATION | `CREATE TABLE t AS SELECT ...` (不指定LOCATION) | **放行** |
| ALTER TABLE ... SET LOCATION | `ALTER TABLE t SET LOCATION '/path'` | **不拦截** |

#### 3.1.2 拦截逻辑

**拦截条件**（同时满足）：
1. 启用了location控制功能（`hive.location.control.enable=true`）
2. SQL语句是CREATE TABLE或CREATE TABLE AS SELECT
3. 语句中包含LOCATION关键字

**拦截动作**：
1. 在Entrance层进行SQL解析
2. 检测到LOCATION关键字时，拒绝执行该SQL
3. 返回明确的错误信息给用户

#### 3.1.3 拦截错误信息

```
错误信息模板：
Location parameter is not allowed in CREATE TABLE statement.
Please remove the LOCATION clause and retry.
SQL: [原始SQL语句]
Reason: To protect data security, specifying LOCATION in CREATE TABLE is disabled.
```

### 3.2 配置项

#### 3.2.1 配置项设计

| 配置项名称 | 类型 | 默认值 | 说明 |
|-----------|------|--------|------|
| `hive.location.control.enable` | Boolean | false | 是否启用location控制（禁止CREATE TABLE指定LOCATION） |

#### 3.2.2 配置示例

```properties
# 启用location控制
hive.location.control.enable=true
```

### 3.3 审计日志

#### 3.3.1 日志内容

| 字段 | 说明 |
|------|------|
| timestamp | 操作时间 |
| user | 执行用户 |
| sql_type | SQL类型（CREATE TABLE / CTAS） |
| location_path | location路径（如果有） |
| is_blocked | 是否被拦截（true） |
| reason | 拦截原因 |

#### 3.3.2 日志示例

```
2026-03-25 10:30:15 | user=zhangsan | sql_type=CREATE TABLE | location=/user/data/test | is_blocked=true | reason=Location parameter not allowed
2026-03-25 10:31:20 | user=lisi | sql_type=CTAS | location=/user/critical/data | is_blocked=true | reason=Location parameter not allowed
```

---

## 四、非功能需求

### 4.1 性能要求

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 解析延迟 | <3% | 对比启用前后的任务执行时间 |
| 吞吐量影响 | <2% | 对比启用前后的任务吞吐量 |
| 内存增加 | <20MB | 测量Entrance进程内存增量 |

### 4.2 可靠性要求

| 指标 | 目标值 |
|------|--------|
| 拦截成功率 | 100% |
| 误报率 | 0%（不误拦截合法操作） |
| 审计日志完整性 | 100% |

### 4.3 可用性要求

| 指标 | 目标值 |
|------|--------|
| 配置生效时间 | 重启后生效 |
| 不影响现有功能 | 100%兼容 |
| 向后兼容性 | 支持Hive 1.x/2.x/3.x |

### 4.4 安全性要求

| 指标 | 目标值 |
|------|--------|
| 绕过拦截 | 0个漏洞 |
| 配置修改权限 | 仅管理员可修改 |
| 审计日志防篡改 | 日志不可修改 |

---

## 五、技术要求

### 5.1 技术栈

| 技术项 | 版本 |
|--------|------|
| Java | 1.8 |
| Scala | 2.11.12 / 2.12.17 |
| Hive | 2.3.3（兼容1.x和3.x） |
| Spring Boot | 2.7.12 |

### 5.2 实现方案

**实现位置**：linkis-computation-governance/linkis-entrance

**实现方式**：在Entrance层的SQL解析阶段进行拦截

**关键组件**：
1. `HiveLocationControlInterceptor`：拦截器，负责检测CREATE TABLE语句中的LOCATION参数
2. `LocationControlConfig`：配置管理器，负责加载配置
3. `LocationAuditLogger`：审计日志记录器

**集成点**：
- 与Linkis配置中心集成
- 与Linkis审计日志集成（统一日志格式）

### 5.3 代码规范

- 遵循Apache Linkis代码规范
- 遵循Scala/Java编码规范
- 单元测试覆盖率 >80%
- 关键逻辑必须有集成测试

---

## 六、验收标准

### 6.1 功能验收

| 场景 | 操作 | 预期结果 |
|------|------|---------|
| 普通建表（无LOCATION） | CREATE TABLE t (id int) | 成功创建 |
| 带LOCATION建表被拦截 | CREATE TABLE t ... LOCATION '/path' | 拒绝执行，返回错误信息 |
| CTAS无LOCATION | CREATE TABLE t AS SELECT ... | 成功创建 |
| CTAS带LOCATION被拦截 | CREATE TABLE t AS SELECT ... LOCATION '/path' | 拒绝执行，返回错误信息 |
| 功能开关禁用 | 禁用location控制后执行带LOCATION的建表 | 成功执行（不拦截） |
| 功能开关启用 | 启用location控制后执行带LOCATION的建表 | 拒绝执行 |
| 审计日志 | 执行被拦截的操作 | 记录审计日志 |

### 6.2 性能验收

| 测试项 | 测试方法 | 通过标准 |
|--------|---------|---------|
| 解析延迟 | 执行1000次建表操作，对比启用前后 | 延迟增加<3% |
| 吞吐量 | 并发执行100个任务，对比吞吐量 | 吞吐量降低<2% |
| 内存占用 | 测量Entrance进程内存 | 内存增加<20MB |

### 6.3 安全验收

| 测试项 | 测试方法 | 通过标准 |
|--------|---------|---------|
| 拦截测试 | 尝试各种带LOCATION的CREATE TABLE语句 | 100%拦截成功 |
| 审计完整性 | 检查所有被拦截操作的日志 | 100%记录完整 |

---

## 七、风险与依赖

### 7.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| SQL解析复杂度 | 复杂SQL可能解析失败 | 使用成熟的SQL解析器 |
| 性能影响 | 频繁解析可能影响性能 | 优化解析逻辑，避免重复解析 |

### 7.2 依赖项

| 依赖 | 类型 | 说明 |
|------|------|------|
| Linkis配置中心 | 功能依赖 | 用于配置管理 |
| Linkis审计日志 | 功能依赖 | 用于统一日志记录 |

### 7.3 限制条件

- 仅支持Hive引擎，不支持其他引擎
- 仅拦截CREATE TABLE语句，不拦截ALTER TABLE
- 不支持任何形式的白名单或豁免

---

## 八、参考文档

- Apache Hive官方文档：https://cwiki.apache.org/confluence/display/Hive
- Linkis官方文档：https://linkis.apache.org/
- Linkis Entrance开发指南：`docs/linkis-entrance-development-guide.md`

---

## 附录

### 附录A：术语表

| 术语 | 定义 |
|------|------|
| Location | Hive表的存储路径，可以是HDFS或本地路径 |
| Entrance | Linkis的任务入口服务，负责接收和调度任务 |
| CTAS | CREATE TABLE AS SELECT，创建表并填充数据 |

### 附录B：配置清单

完整配置项列表见 3.2.1 配置项设计。

### 附录C：测试用例清单

详细测试用例见测试文档：`docs/dev-1.19.0-yarn-tag-update/testing/hive_location_control_测试用例.md`

---

**文档变更记录**

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-03-25 | 初始版本（基于白名单方案） | AI需求分析 |
| v2.0 | 2026-03-25 | 移除白名单机制，简化为Entrance层拦截 | AI需求分析 |
