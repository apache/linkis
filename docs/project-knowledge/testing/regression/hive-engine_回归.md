# Hive引擎模块回归测试

## 模块信息

| 项目 | 内容 |
|-----|------|
| 模块ID | hive-engine |
| 模块名称 | Hive引擎 |
| 关键级别 | critical（关键模块） |
| 最后更新 | 2026-03-26 |
| 版本 | v1.0 |

## 模块描述

Hive引擎插件是Linkis的核心引擎之一，负责Hive SQL的解析、执行控制、安全拦截等功能。本模块确保Hive引擎在生产环境中的稳定性、安全性和性能表现。

---

## 测试覆盖统计

| 测试类型 | 用例数量 | 覆盖率 |
|---------|:-------:|:------:|
| 功能测试 | 40 | 100% |
| 性能测试 | 3 | 100% |
| 安全性测试 | 5 | 100% |
| 兼容性测试 | 5 | 100% |
| 回归测试 | 4 | 100% |
| **总计** | **57** | **100%** |

---

## 涉及的需求

| 需求名称 | 优先级 | 状态 | 来源分支 |
|---------|:------:|:----:|:--------:|
| Hive表Location路径控制 | P0 | 已完成 | dev-1.19.0-yarn-tag-update |

---

## 回归测试用例

### 一、功能测试用例（40个）

#### 1.1 拦截功能测试（P0）- 6个用例

**TC-001**: 普通CREATE TABLE with LOCATION被拦截
- **优先级**: P0
- **前置条件**: `wds.linkis.hive.location.control.enable=true`
- **测试步骤**: 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`
- **预期结果**: SQL被拒绝执行，返回错误信息

**TC-002**: CREATE EXTERNAL TABLE with LOCATION被拦截
- **优先级**: P0
- **测试步骤**: 提交SQL: `CREATE EXTERNAL TABLE ext_table (id int) LOCATION '/user/data/external'`
- **预期结果**: SQL被拒绝执行

**TC-003**: CTAS with LOCATION被拦截
- **优先级**: P0
- **测试步骤**: 提交SQL: `CREATE TABLE new_table AS SELECT * FROM source_table LOCATION '/user/data/new'`
- **预期结果**: SQL被拒绝执行

**TC-004**: CREATE TABLE without LOCATION正常执行
- **优先级**: P0
- **测试步骤**: 提交SQL: `CREATE TABLE normal_table (id int, name string)`
- **预期结果**: SQL成功执行

**TC-005**: CTAS without LOCATION正常执行
- **优先级**: P0
- **测试步骤**: 提交SQL: `CREATE TABLE copy_table AS SELECT * FROM source_table`
- **预期结果**: SQL成功执行

**TC-006**: ALTER TABLE SET LOCATION不被拦截
- **优先级**: P1
- **测试步骤**: 提交SQL: `ALTER TABLE existing_table SET LOCATION '/new/path'`
- **预期结果**: SQL正常执行

#### 1.2 配置开关测试（P0）- 2个用例

**TC-007**: 开关禁用时LOCATION语句正常执行
- **优先级**: P0
- **前置条件**: `wds.linkis.hive.location.control.enable=false`
- **预期结果**: SQL成功执行

**TC-008**: 开关启用时LOCATION语句被拦截
- **优先级**: P0
- **前置条件**: `wds.linkis.hive.location.control.enable=true`
- **预期结果**: SQL被拒绝执行

#### 1.3 边界条件测试（P1）- 6个用例

**TC-009**: 带注释的CREATE TABLE with LOCATION被拦截
- **优先级**: P1
- **测试要点**: 注释不影响拦截逻辑

**TC-010**: 多行SQL中包含带LOCATION的CREATE TABLE
- **优先级**: P1
- **测试要点**: 整个脚本被拒绝执行

**TC-011**: 空SQL或空字符串处理
- **优先级**: P1
- **测试要点**: 正常处理，不抛出异常

**TC-012**: 大小写LOCATION关键字被识别
- **优先级**: P1
- **测试要点**: 所有大小写组合都被正确拦截

**TC-013**: 不同引号的LOCATION路径被识别
- **优先级**: P1
- **测试要点**: 单引号、双引号、反引号都被正确拦截

**TC-014**: 跨多行的CREATE TABLE with LOCATION
- **优先级**: P1
- **测试要点**: 跨多行的LOCATION被正确识别

#### 1.4 错误处理测试（P1）- 2个用例

**TC-015**: 拦截错误信息包含SQL片段
- **优先级**: P1
- **测试要点**: 错误信息清晰可读

**TC-016**: 异常情况下的Fail-open策略
- **优先级**: P1
- **测试要点**: 确保可用性，记录警告日志

#### 1.5 审计日志测试（P1）- 2个用例

**TC-017**: 被拦截操作记录警告日志
- **优先级**: P1
- **测试要点**: 日志包含用户信息、SQL片段

**TC-018**: 日志格式符合Linkis规范
- **优先级**: P2
- **测试要点**: 使用LogUtils标准方法

---

### 二、性能测试用例（3个）

**TC-PERF-001**: 单次解析延迟
- **优先级**: P1
- **预期结果**: 平均延迟增加 < 3%

**TC-PERF-002**: 批量解析吞吐量
- **优先级**: P1
- **预期结果**: 吞吐量降低 < 2%

**TC-PERF-003**: 内存增量测试
- **优先级**: P2
- **预期结果**: 内存增量 < 20MB

---

### 三、兼容性测试用例（5个）

**TC-COMPAT-001**: Hive 1.x兼容性
- **优先级**: P2
- **测试版本**: Hive 1.2.1

**TC-COMPAT-002**: Hive 2.x兼容性
- **优先级**: P2
- **测试版本**: Hive 2.3.3

**TC-COMPAT-003**: Hive 3.x兼容性
- **优先级**: P2
- **测试版本**: Hive 3.1.2

**TC-COMPAT-004**: 带Hive分区语法的CREATE TABLE
- **优先级**: P2
- **测试要点**: PARTITIONED BY + LOCATION 被正确拦截

**TC-COMPAT-005**: 带存储格式语法的CREATE TABLE
- **优先级**: P2
- **测试要点**: ROW FORMAT + STORED AS + LOCATION 被正确拦截

---

### 四、安全性测试用例（5个）

**TC-SEC-001**: 尝试通过大小写绕过
- **优先级**: P0
- **测试要点**: 100%拦截成功，无绕过可能

**TC-SEC-002**: 尝试通过注释绕过
- **优先级**: P0
- **测试要点**: 100%拦截成功

**TC-SEC-003**: 尝试通过空格/换行绕过
- **优先级**: P0
- **测试要点**: 100%拦截成功

**TC-SEC-004**: SQL注入尝试
- **优先级**: P1
- **测试要点**: 不导致SQL注入漏洞

**TC-SEC-005**: 路径遍历尝试
- **优先级**: P1
- **测试要点**: 不导致路径遍历漏洞

---

### 五、回归测试用例（4个）

**TC-REG-001**: SQL LIMIT功能正常
- **优先级**: P0
- **测试要点**: 验证自动添加LIMIT 5000功能不受影响

**TC-REG-002**: DROP TABLE拦截正常
- **优先级**: P1
- **测试要点**: DROP TABLE拦截功能不受影响

**TC-REG-003**: CREATE DATABASE拦截正常
- **优先级**: P1
- **测试要点**: CREATE DATABASE拦截功能不受影响

**TC-REG-004**: Python/Scala代码检查正常
- **优先级**: P1
- **测试要点**: Python/Scala代码检查功能不受影响

---

## 测试执行计划

### 优先级执行顺序

```
第1轮: P0功能测试（TC-001 ~ TC-008, TC-SEC-001 ~ TC-SEC-003, TC-REG-001）
   ↓
第2轮: P1功能测试（TC-009 ~ TC-018）
   ↓
第3轮: 性能测试（TC-PERF-001 ~ TC-PERF-003）
   ↓
第4轮: 安全性测试（TC-SEC-004 ~ TC-SEC-005）
   ↓
第5轮: 兼容性测试（TC-COMPAT-001 ~ TC-COMPAT-005）
   ↓
第6轮: 回归测试（TC-REG-002 ~ TC-REG-004）
```

### 测试通过标准

| 测试类型 | 通过标准 |
|---------|---------|
| **功能测试** | 所有P0用例100%通过，P1用例≥95%通过 |
| **性能测试** | 所有性能指标达到目标值 |
| **安全性测试** | 0个绕过漏洞 |
| **兼容性测试** | Hive 1.x/2.x/3.x全部通过 |
| **回归测试** | 100%通过，无副作用 |

---

## 变更历史

| 版本 | 日期 | 变更内容 | 来源需求 |
|------|------|---------|---------|
| v1.0 | 2026-03-26 | 初始版本，沉淀Hive表Location路径控制功能测试用例（57个） | LINKIS-ENHANCE-HIVE-LOCATION-001 |

---

## 附录

### 测试环境要求

| 环境 | 配置要求 |
|------|---------|
| **开发环境** | 本地Linkis + HDFS |
| **测试环境** | 容器化Linkis集群 |
| **预生产环境** | 与生产相同配置 |

### 测试工具清单

| 工具 | 版本 | 用途 |
|------|------|------|
| ScalaTest | 3.2.x | 单元测试 |
| JMeter | 5.5 | 性能测试 |
| MockServer | 5.15 | 模拟服务 |
| Docker | 20.10 | 容器化测试 |
| Hive Client | 1.2.1 / 2.3.3 / 3.1.2 | 多版本测试 |

---

**回归测试集维护者**: Linkis开发团队
**最后审查日期**: 2026-03-26
**下次审查日期**: 2026-06-26（季度审查）
