# Hive表Location路径控制 - 测试用例文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-ENHANCE-HIVE-LOCATION-001 |
| 需求名称 | Hive表Location路径控制 |
| 需求类型 | 功能增强（ENHANCE） |
| 测试类型 | 功能测试 |
| 文档版本 | v1.0 |
| 创建时间 | 2026-03-26 |
| 最后更新 | 2026-03-26 |

---

## 一、测试概述

### 1.1 测试目标

本测试文档旨在验证Hive表Location路径控制功能的完整性和正确性，确保：

1. **功能完整性**：所有包含LOCATION的CREATE TABLE语句被正确拦截
2. **配置有效性**：功能开关正确控制拦截行为
3. **错误提示清晰**：用户收到明确的错误信息
4. **性能合规**：拦截逻辑不影响系统性能
5. **无副作用**：不影响其他合法的SQL操作

### 1.2 测试范围

| 测试域 | 包含 | 不包含 |
|-------|------|--------|
| **拦截功能** | CREATE TABLE with LOCATION<br>CREATE EXTERNAL TABLE with LOCATION<br>CTAS with LOCATION | ALTER TABLE SET LOCATION<br>CREATE TABLE without LOCATION<br>CTAS without LOCATION |
| **配置管理** | 开关启用/禁用 | 配置持久化（由Linkis配置中心负责） |
| **错误处理** | 拦截错误信息<br>异常情况处理 | - |
| **性能** | 解析延迟<br>吞吐量影响<br>内存占用 | - |
| **兼容性** | Hive 1.x/2.x/3.x<br>不同SQL方言 | - |

### 1.3 测试策略

| 测试类型 | 测试方法 | 工具 |
|---------|---------|------|
| **单元测试** | ScalaTest | ScalaTest框架 |
| **集成测试** | 模拟Entrance环境 | MockServer |
| **性能测试** | JMeter基准测试 | JMeter |
| **兼容性测试** | 多Hive版本验证 | Docker容器 |

---

## 二、功能测试用例

### 2.1 拦截功能测试（P0）

#### TC-001: 普通CREATE TABLE with LOCATION被拦截

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 返回错误信息: `CREATE TABLE with LOCATION clause is not allowed. Please remove the LOCATION clause and retry.`
- 日志记录包含此次拦截

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-002: CREATE EXTERNAL TABLE with LOCATION被拦截

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE EXTERNAL TABLE ext_table (id int) LOCATION '/user/data/external'`
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 返回错误信息包含"LOCATION clause is not allowed"

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-003: CTAS with LOCATION被拦截

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE new_table AS SELECT * FROM source_table LOCATION '/user/data/new'`
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 返回错误信息

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-004: CREATE TABLE without LOCATION正常执行

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE normal_table (id int, name string)`
2. 观察执行结果

**预期结果**:
- SQL成功执行
- 表创建成功

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-005: CTAS without LOCATION正常执行

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE copy_table AS SELECT * FROM source_table`
2. 观察执行结果

**预期结果**:
- SQL成功执行
- 表创建成功并填充数据

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-006: ALTER TABLE SET LOCATION不被拦截

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `ALTER TABLE existing_table SET LOCATION '/new/path'`
2. 观察执行结果

**预期结果**:
- SQL正常执行（不被拦截）
- 表位置成功修改

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 2.2 配置开关测试（P0）

#### TC-007: 开关禁用时LOCATION语句正常执行

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=false`
**测试步骤**:
1. 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`
2. 观察执行结果

**预期结果**:
- SQL成功执行
- 表创建成功，LOCATION生效

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-008: 开关启用时LOCATION语句被拦截

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 返回错误信息

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 2.3 边界条件测试（P1）

#### TC-009: 带注释的CREATE TABLE with LOCATION被拦截

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL:
```sql
-- This is a test table
CREATE TABLE test_table (
    id int,
    name string
)
-- This is the location
LOCATION '/user/data/test'
```
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 注释不影响拦截逻辑

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-010: 多行SQL中包含带LOCATION的CREATE TABLE

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL:
```sql
CREATE TABLE table1 (id int);
CREATE TABLE table2 (id int) LOCATION '/user/data/table2';
CREATE TABLE table3 (id int);
```
2. 观察执行结果

**预期结果**:
- 整个脚本被拒绝执行
- 返回错误信息指出第2个语句包含LOCATION

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-011: 空SQL或空字符串处理

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交空SQL: `""`
2. 提交纯空格SQL: `"   "`
3. 提交纯注释SQL: `"-- Just a comment"`
4. 观察执行结果

**预期结果**:
- 所有情况正常处理，不抛出异常
- 返回适当的响应（成功或空结果）

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-012: 大写LOCATION关键字被识别

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`（大写）
2. 提交SQL: `CREATE TABLE test_table (id int) location '/user/data/test'`（小写）
3. 提交SQL: `CREATE TABLE test_table (id int) LoCaTiOn '/user/data/test'`（混合大小写）
4. 观察执行结果

**预期结果**:
- 所有大小写组合都被正确拦截

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-013: 不同引号的LOCATION路径被识别

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL: `CREATE TABLE test_table (id int) LOCATION '/user/data/test'`（单引号）
2. 提交SQL: `CREATE TABLE test_table (id int) LOCATION "/user/data/test"`（双引号）
3. 提交SQL: `CREATE TABLE test_table (id int) LOCATION \`/user/data/test\``（反引号）
4. 观察执行结果

**预期结果**:
- 所有引号类型都被正确拦截

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-014: 跨多行的CREATE TABLE with LOCATION

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL:
```sql
CREATE TABLE test_table (
    id int COMMENT 'ID column',
    name string COMMENT 'Name column'
)
COMMENT 'This is a test table'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '/user/hive/warehouse/test_table'
```
2. 观察执行结果

**预期结果**:
- SQL被拒绝执行
- 跨多行的LOCATION被正确识别

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 2.4 错误处理测试（P1）

#### TC-015: 拦截错误信息包含SQL片段

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交超长SQL: `CREATE TABLE test_table (id int, very_long_column_name_that_exceeds_normal_length string) LOCATION '/user/data/test'`
2. 观察错误信息

**预期结果**:
- 错误信息包含SQL片段（截断到100字符）
- 错误信息清晰可读

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-016: 异常情况下的Fail-open策略

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 模拟SQL解析异常（如注入特殊字符）
2. 提交可能导致解析异常的SQL
3. 观察执行结果

**预期结果**:
- 异常情况下返回true（放行），确保可用性
- 记录警告日志

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 2.5 审计日志测试（P1）

#### TC-017: 被拦截操作记录警告日志

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交带LOCATION的CREATE TABLE语句
2. 检查Entrance日志文件
3. 搜索警告日志

**预期结果**:
- 日志包含警告信息: `Failed to check LOCATION in SQL`
- 日志包含用户信息、SQL片段

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-018: 日志格式符合Linkis规范

**优先级**: P2
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 触发拦截操作
2. 检查日志格式

**预期结果**:
- 日志使用LogUtils.generateWarn()或类似标准方法
- 日志包含时间戳、日志级别、类名、线程信息

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

## 三、性能测试用例

### 3.1 解析延迟测试（P1）

#### TC-PERF-001: 单次解析延迟

**优先级**: P1
**测试方法**:
1. 准备1000条不同复杂度的CREATE TABLE语句
2. 启用location控制
3. 记录每条语句的解析时间
4. 计算平均延迟

**预期结果**:
- 平均延迟增加 < 3%（对比禁用时）

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-PERF-002: 批量解析吞吐量

**优先级**: P1
**测试方法**:
1. 准备10000条CREATE TABLE语句（10%包含LOCATION）
2. 启用location控制
3. 记录总处理时间
4. 计算吞吐量降低比例

**预期结果**:
- 吞吐量降低 < 2%

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 3.2 内存占用测试（P2）

#### TC-PERF-003: 内存增量测试

**优先级**: P2
**测试方法**:
1. 启动Entrance服务，记录初始内存
2. 启用location控制
3. 执行1000次SQL解析
4. 记录最终内存
5. 计算内存增量

**预期结果**:
- 内存增量 < 20MB

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

## 四、兼容性测试用例

### 4.1 多版本Hive兼容性（P2）

#### TC-COMPAT-001: Hive 1.x兼容性

**优先级**: P2
**测试方法**:
1. 使用Hive 1.2.1版本
2. 执行TC-001至TC-006
3. 验证功能正常

**预期结果**:
- 所有测试用例通过

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-COMPAT-002: Hive 2.x兼容性

**优先级**: P2
**测试方法**:
1. 使用Hive 2.3.3版本
2. 执行TC-001至TC-006
3. 验证功能正常

**预期结果**:
- 所有测试用例通过

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-COMPAT-003: Hive 3.x兼容性

**优先级**: P2
**测试方法**:
1. 使用Hive 3.1.2版本
2. 执行TC-001至TC-006
3. 验证功能正常

**预期结果**:
- 所有测试用例通过

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 4.2 特殊SQL方言兼容性（P2）

#### TC-COMPAT-004: 带Hive分区语法的CREATE TABLE

**优先级**: P2
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL:
```sql
CREATE TABLE partitioned_table (
    id int,
    name string,
    dt string
)
PARTITIONED BY (dt)
LOCATION '/user/data/partitioned'
```
2. 观察执行结果

**预期结果**:
- SQL被正确拦截（包含LOCATION）

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-COMPAT-005: 带存储格式语法的CREATE TABLE

**优先级**: P2
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交SQL:
```sql
CREATE TABLE formatted_table (
    id int
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION '/user/data/formatted'
```
2. 观察执行结果

**预期结果**:
- SQL被正确拦截

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

## 五、安全性测试用例

### 5.1 绕过测试（P0）

#### TC-SEC-001: 尝试通过大小写绕过

**优先级**: P0
**测试方法**:
1. 尝试各种大小写组合: `LOCATION`, `location`, `LoCaTiOn`, `lOcAtIoN`
2. 验证所有组合都被拦截

**预期结果**:
- 100%拦截成功，无绕过可能

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-SEC-002: 尝试通过注释绕过

**优先级**: P0
**测试方法**:
1. 尝试在LOCATION关键字中插入注释: `LOC/**/ATION`
2. 尝试在引号中插入注释
3. 验证都被拦截

**预期结果**:
- 100%拦截成功

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-SEC-003: 尝试通过空格/换行绕过

**优先级**: P0
**测试方法**:
1. 尝试多余空格: `LOCATION  '/path'`
2. 尝试换行: `LOCATION\n'/path'`
3. 尝试Tab: `LOCATION\t'/path'`
4. 验证都被拦截

**预期结果**:
- 100%拦截成功

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

### 5.2 注入攻击测试（P1）

#### TC-SEC-004: SQL注入尝试

**优先级**: P1
**测试方法**:
1. 尝试在LOCATION路径中注入SQL: `LOCATION '/path'; DROP TABLE other_table; --'`
2. 验证系统安全性

**预期结果**:
- 拦截逻辑正常工作
- 不导致SQL注入漏洞

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-SEC-005: 路径遍历尝试

**优先级**: P1
**测试方法**:
1. 尝试路径遍历: `LOCATION '../../../etc/passwd'`
2. 验证系统安全性

**预期结果**:
- 拦截逻辑正常工作
- 不导致路径遍历漏洞

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

## 六、回归测试用例

### 6.1 现有功能不受影响（P0）

#### TC-REG-001: SQL LIMIT功能正常

**优先级**: P0
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交无LIMIT的SELECT语句
2. 验证自动添加LIMIT 5000
3. 提交LIMIT超过5000的SELECT语句
4. 验证LIMIT被修改为5000

**预期结果**:
- SQL LIMIT功能不受影响

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-REG-002: DROP TABLE拦截正常

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交DROP TABLE语句
2. 验证被正确拦截

**预期结果**:
- DROP TABLE拦截功能不受影响

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-REG-003: CREATE DATABASE拦截正常

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交CREATE DATABASE语句
2. 验证被正确拦截

**预期结果**:
- CREATE DATABASE拦截功能不受影响

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

#### TC-REG-004: Python/Scala代码检查正常

**优先级**: P1
**前置条件**: `wds.linkis.hive.location.control.enable=true`
**测试步骤**:
1. 提交Python代码（包含sys模块导入尝试）
2. 提交Scala代码（包含System.exit尝试）
3. 验证被正确拦截

**预期结果**:
- Python/Scala代码检查功能不受影响

**实际结果**: _____________
**测试状态**: [ ] 通过 [ ] 失败

---

## 七、测试执行计划

### 7.1 测试环境

| 环境 | 配置 | 用途 |
|------|------|------|
| **开发环境** | 本地Linkis + HDFS | 单元测试、集成测试 |
| **测试环境** | 容器化Linkis集群 | 功能测试、性能测试 |
| **预生产环境** | 与生产相同配置 | 回归测试、兼容性测试 |

### 7.2 测试执行顺序

```
第1轮: P0功能测试（TC-001 ~ TC-008）
   ↓
第2轮: P1功能测试（TC-009 ~ TC-018）
   ↓
第3轮: 性能测试（TC-PERF-001 ~ TC-PERF-003）
   ↓
第4轮: 兼容性测试（TC-COMPAT-001 ~ TC-COMPAT-005）
   ↓
第5轮: 安全性测试（TC-SEC-001 ~ TC-SEC-005）
   ↓
第6轮: 回归测试（TC-REG-001 ~ TC-REG-004）
```

### 7.3 测试通过标准

| 测试类型 | 通过标准 |
|---------|---------|
| **功能测试** | 所有P0用例100%通过，P1用例≥95%通过 |
| **性能测试** | 所有性能指标达到目标值 |
| **兼容性测试** | Hive 1.x/2.x/3.x全部通过 |
| **安全性测试** | 0个绕过漏洞 |
| **回归测试** | 100%通过，无副作用 |

---

## 八、附录

### 8.1 测试数据准备

**表1**: source_table（用于CTAS测试）
```sql
CREATE TABLE source_table (
    id int,
    name string,
    age int
);
INSERT INTO source_table VALUES (1, 'Alice', 25);
INSERT INTO source_table VALUES (2, 'Bob', 30);
```

**表2**: existing_table（用于ALTER TABLE测试）
```sql
CREATE TABLE existing_table (
    id int,
    value string
);
```

### 8.2 测试工具清单

| 工具 | 版本 | 用途 |
|------|------|------|
| ScalaTest | 3.2.x | 单元测试 |
| JMeter | 5.5 | 性能测试 |
| MockServer | 5.15 | 模拟服务 |
| Docker | 20.10 | 容器化测试 |
| Hive Client | 1.2.1 / 2.3.3 / 3.1.2 | 多版本测试 |

### 8.3 术语表

| 术语 | 定义 |
|------|------|
| LOCATION | Hive表的存储路径，可以是HDFS或本地路径 |
| CTAS | CREATE TABLE AS SELECT，创建表并填充数据 |
| P0/P1/P2 | 优先级等级，P0最高，P2最低 |

### 8.4 变更记录

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-03-26 | 初始版本 | AI测试生成 |

---

**测试用例总数**: 40个
- P0: 8个
- P1: 18个
- P2: 14个

**预计测试时间**: 2-3个工作日
