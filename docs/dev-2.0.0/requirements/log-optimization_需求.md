# Linkis 日志优化需求文档

## 一、功能概述

### 1.1 功能名称
Linkis 日志优化（Token脱敏 + 关键操作日志增强）

### 1.2 功能背景
当前Linkis系统在日志记录方面存在以下问题：
1. **安全隐患**: Token、密码等敏感信息在日志中明文输出，存在安全泄露风险
2. **可追溯性不足**: BML资源操作、引擎管理等关键业务操作缺少完整的操作日志，问题排查困难
3. **日志级别不合理**: 部分重要日志（如Spark告警）级别过低，容易被忽略
4. **审计需求**: 安全审计要求对用户操作、资源访问等关键行为进行完整记录

### 1.3 期望价值
1. **提升安全性**: 通过脱敏处理避免敏感信息泄露
2. **增强可追溯性**: 完善关键操作的日志记录，便于问题定位和审计
3. **优化日志质量**: 调整日志级别，突出重要信息
4. **满足合规要求**: 符合安全审计和数据保护规范

## 二、优化范围

本需求包含5个独立的优化点，按优先级排序：

| 优先级 | 优化点 | 涉及模块 | 复杂度 |
|:------:|--------|---------|:------:|
| P0 | Token脱敏处理 | linkis-module, linkis-engineconn | 高 |
| P1 | BML HDFS路径日志 | linkis-bml-server | 中 |
| P1 | Linkis Manager killEngine日志 | linkis-manager | 低 |
| P1 | 引擎Hadoop客户端日志 | Spark/Hive引擎插件 | 中 |
| P2 | Spark广播表日志级别 | Spark引擎插件 | 低 |

## 三、详细需求

### 3.1 优化点1：Token脱敏处理（P0）

#### 3.1.1 问题描述
当前系统中Token（用户代理Token、服务Token等）在日志中明文输出，存在严重安全隐患。

#### 3.1.2 需求描述
**仅对日志中打印的Token进行脱敏处理**，不修改Token本身的业务逻辑（如传递、验证等）。

> **重要说明**：只修改 `logger.info("token: {}", token)` 这类日志打印语句，不修改服务间Token调用逻辑。

#### 3.1.3 脱敏规则
| Token长度 | 脱敏格式 | 示例 |
|:---------:|---------|------|
| ≤6 | 前{长度-3}位 + *** | `abc123` → `abc***` |
| >6 | 前3位 + *** + 后3位 | `abc123def456` → `abc***456` |

#### 3.1.4 涉及场景（仅日志打印）
1. **用户Token日志**: UJES客户端Token、代理Token在日志中的输出
2. **EngineConnToken日志**: 引擎连接Token在日志中的输出
3. **其他Token日志**: 所有以Token/TokenId命名且在日志中输出的字符串

#### 3.1.5 实现方案
- **方案**: 逐处手动修改日志打印语句（不使用全局拦截）
- **理由**: 避免误脱敏其他字符串，保证精确控制
- **不影响**: 服务间Token调用、Token验证逻辑、Token存储

#### 3.1.6 验收标准
- [ ] 所有输出到日志的Token均已脱敏
- [ ] 脱敏后的Token长度可区分（保留部分原始字符）
- [ ] 不影响Token业务逻辑（传递、验证、存储不受影响）
- [ ] 不影响非Token字符串（避免误脱敏）

### 3.2 优化点2：BML HDFS路径日志（P1）

#### 3.2.1 问题描述
BML（BML资源管理服务）在处理资源上传、下载等操作时，缺少HDFS路径的日志记录，导致资源位置追踪困难。

#### 3.2.2 需求描述
在BML资源操作的关键节点记录HDFS路径信息。

#### 3.2.3 操作范围
| 操作类型 | 说明 |
|---------|------|
| 资源上传 | 记录上传后的HDFS存储路径 |
| 资源下载 | 记录下载的源HDFS路径 |
| 版本更新 | 记录新版本的HDFS路径 |
| 删除全部记录 | 记录被删除的HDFS路径 |

#### 3.2.4 日志规范
- **日志级别**: INFO
- **格式**: 纯文本（便于日志检索）
- **包含信息**:
  - 资源ID（resourceId）
  - 版本号（version）
  - HDFS路径（hdfsPath）
  - 用户名（user）

#### 3.2.5 日志示例
```
INFO [BmlService] Upload resource - resourceId: 10001, version: v001, hdfsPath: hdfs://linkis/bml/resource/10001/v001, user: admin
INFO [BmlService] Download resource - resourceId: 10001, version: v001, hdfsPath: hdfs://linkis/bml/resource/10001/v001, user: admin
```

#### 3.2.6 验收标准
- [ ] 上传操作记录HDFS路径
- [ ] 下载操作记录HDFS路径
- [ ] 版本更新记录HDFS路径
- [ ] 删除操作记录HDFS路径
- [ ] 日志包含resourceId、version、hdfsPath、user四个字段
- [ ] 日志级别为INFO

### 3.3 优化点3：Linkis Manager killEngine日志（P1）

#### 3.3.1 问题描述
Linkis Manager在kill引擎时，日志信息不够详细，无法快速定位被kill的引擎。

#### 3.3.2 需求描述
在killEngine操作时增加关键信息日志。

#### 3.3.3 增强内容
| 信息项 | 说明 | 示例值 |
|-------|------|-------|
| 引擎类型 | Spark、Hive、Python等 | spark |
| 用户名 | 引擎所属用户 | admin |

#### 3.3.4 日志示例
```
INFO [LinkisManagerAMService] Kill engine - engineType: spark, user: admin, engineInstance: {engineInstance}
```

> **注意**：不打印 `engineConnExecId` 和 `ticketId`，避免泄露敏感信息

#### 3.3.5 验收标准
- [ ] killEngine日志包含引擎类型
- [ ] killEngine日志包含用户名
- [ ] killEngine日志不包含敏感信息（engineConnExecId、ticketId等）
- [ ] 日志级别为INFO

### 3.4 优化点4：引擎Hadoop客户端操作日志（P1）

#### 3.4.1 问题描述
Spark、Hive等引擎在执行Hadoop客户端操作（HDFS文件操作、Kerberos认证）时，缺少操作日志，无法追踪底层操作行为。

> **说明**：仅配置 `org.apache.hadoop` 日志级别无法解决此问题，需要在代码层面主动添加业务日志。

#### 3.4.2 需求描述
在核心引擎（Spark、Hive）中，**在调用Hadoop客户端的代码位置增加业务日志**，记录操作类型、路径、用户等信息。

#### 3.4.3 目标引擎
- Spark引擎（linkis-engineconn-plugins/spark）
- Hive引擎（linkis-engineconn-plugins/hive）

#### 3.4.4 操作类型
| 操作类型 | 说明 | 需要添加日志的位置 |
|---------|------|:---------:|
| HDFS文件操作 | 文件读写、目录操作、权限设置 | 调用FileSystem API前/后 |
| Kerberos认证 | Keytab登录、Token刷新 | UserGroupInformation登录方法 |

#### 3.4.5 日志规范
- **日志级别**: INFO（正常操作） / WARN（操作失败）
- **包含信息**: 操作类型、操作路径、用户、操作结果
- **实现方式**: 在代码中添加 `logger.info(...)` 语句

#### 3.4.6 日志示例
```java
// HDFS操作日志
logger.info("HDFS operation - type: mkdir, path: {}, user: {}, result: {}", "mkdir", "/user/admin/tmp", "admin", "success");

// Kerberos认证日志
logger.warn("Kerberos auth - user: {}, keytab: {}, result: {}, error: {}", "admin", "/path/to/keytab", "failed", "Invalid principal");
```

#### 3.4.7 实施步骤
1. 搜索引擎代码中调用 `FileSystem`、`UserGroupInformation` 的位置
2. 在调用前/后添加日志记录
3. 确保日志包含必需信息（操作类型、路径、用户、结果）

#### 3.4.8 验收标准
- [ ] Spark引擎HDFS操作代码中有日志记录
- [ ] Hive引擎HDFS操作代码中有日志记录
- [ ] Kerberos认证代码中有日志记录
- [ ] 日志包含操作类型、路径、用户、结果

### 3.5 优化点5：Spark广播表日志级别（P2）

#### 3.5.1 问题描述
Spark在使用广播表时会产生`FutureWarning: HiveContext is deprecated`等告警信息，通过Python Executor的`appendErrorOutput`方法以ERROR级别输出，导致日志解析时误认为是错误。

**代码位置**：
- 文件：`linkis-engineconn-plugins/spark/src/main/scala/org/apache/linkis/engineplugin/spark/executor/SparkPythonExecutor.scala:377`
- 方法：`appendErrorOutput(message: String)`
- 当前逻辑：`pythonScriptInitialized` 为 true 时使用 `logger.error(message)` 输出

#### 3.5.2 需求描述
通过log4j2.xml配置层面过滤Spark的FutureWarning告警，避免误解析为ERROR。

#### 3.5.3 目标日志
```
FutureWarning: HiveContext is deprecated in Spark 2.0.0. Please use SparkSession.builder().enableHiveSupport().getOrCreate() instead.
```

#### 3.5.4 实现方案
**方案：通过log4j2.xml配置过滤**

**实现方式**：在 `linkis-engineconn-plugins/spark/src/main/resources/log4j2.xml` 中添加过滤器

**配置选项**：

**选项A：按消息内容过滤（RegexFilter）**
```xml
<Logger name="org.apache.linkis.engineplugin.spark.executor.SparkPythonExecutor" level="INFO" additivity="false">
    <AppenderRef ref="RollingFile">
        <RegexFilter regex=".*FutureWarning.*" onMatch="DENY" onMismatch="NEUTRAL"/>
    </AppenderRef>
</Logger>
```

**选项B：按Logger级别过滤（ThresholdFilter）**
```xml
<Logger name="org.apache.linkis.engineplugin.spark.executor.SparkPythonExecutor" level="WARN" additivity="false">
    <AppenderRef ref="RollingFile"/>
</Logger>
```

**推荐方案**：选项A（精确过滤FutureWarning消息）

#### 3.5.5 验收标准
- [ ] FutureWarning消息不在ERROR日志中出现
- [ ] 其他真正的错误消息仍正常输出
- [ ] log4j2.xml配置生效
- [ ] 不影响代码逻辑（仅配置修改）

## 四、影响范围分析

### 4.1 影响模块
| 模块 | 影响点 | 风险等级 |
|-----|--------|:-------:|
| linkis-commons/linkis-module | Token日志输出 | 中 |
| linkis-computation-governance/linkis-manager | killEngine日志 | 低 |
| linkis-public-enhancements/linkis-bml-server | BML资源操作日志 | 低 |
| linkis-engineconn-plugins/spark | Spark引擎日志 | 中 |
| linkis-engineconn-plugins/hive | Hive引擎日志 | 中 |

### 4.2 影响评估
| 影响项 | 说明 |
|-------|------|
| **性能影响** | 极低（仅增加少量日志输出） |
| **兼容性影响** | 无（日志格式调整不影响API） |
| **测试影响** | 需更新日志相关的断言测试 |
| **运维影响** | 日志量略有增加，需关注日志存储 |

### 4.3 风险与缓解
| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| Token误脱敏 | 业务逻辑异常 | 严格限定脱敏场景，充分测试 |
| 日志量过大 | 存储压力 | 仅在关键操作点记录，使用INFO级别 |
| 日志格式变更 | 日志解析失败 | 保持日志格式稳定，纯文本格式 |

## 五、验收标准总览

### 5.1 功能验收
| 优化点 | 验收标准 |
|-------|---------|
| Token脱敏 | 所有Token日志均已脱敏，符合脱敏规则 |
| BML HDFS路径 | 4种操作均记录HDFS路径信息 |
| killEngine日志 | 包含引擎类型、用户名、引擎标识 |
| Hadoop客户端日志 | Spark/Hive的HDFS和Kerberos操作有日志 |
| Spark广播表日志 | FutureWarning级别为WARN |

### 5.2 质量验收
- [ ] 代码通过编译（Maven/Gradle）
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 代码符合项目规范（SonarQube检查）
- [ ] 日志输出不包含敏感信息（人工review）

### 5.3 文档验收
- [ ] 代码注释完整（说明日志记录的目的）
- [ ] 更新配置文档（如有日志级别配置）
- [ ] 更新运维手册（说明新增日志的含义）

## 六、非功能需求

### 6.1 性能要求
- 日志记录操作耗时 < 10ms
- 不影响业务操作的响应时间

### 6.2 可维护性要求
- 使用统一的日志工具类（避免代码重复）
- 日志格式清晰，便于解析和检索

### 6.3 安全性要求
- 敏感信息必须脱敏
- 日志文件访问权限控制

## 七、实施建议

### 7.1 实施顺序
建议按以下顺序实施（从低风险到高风险）：
1. **优化点3**（killEngine日志）- 风险最低
2. **优化点5**（Spark广播表日志）- 风险最低
3. **优化点2**（BML HDFS路径）- 风险低
4. **优化点4**（引擎Hadoop客户端）- 风险中
5. **优化点1**（Token脱敏）- 风险最高，需充分测试

### 7.2 测试策略
1. **单元测试**: 验证脱敏规则、日志格式
2. **集成测试**: 验证日志输出的完整性
3. **安全测试**: 检查是否有敏感信息泄露
4. **回归测试**: 验证业务逻辑不受影响

### 7.3 上线建议
- 建议在测试环境充分验证后再上线生产
- 上线后密切监控日志输出量和系统性能
- 准备回滚方案（日志级别调整可快速回滚）

## 八、附录

### 8.1 参考文档
- Linkis日志规范文档
- Hadoop日志最佳实践
- 安全审计要求文档

### 8.2 相关Issue
- 待补充（如有）

### 8.3 变更历史
| 版本 | 日期 | 变更内容 | 作者 |
|-----|------|---------|------|
| 1.0 | 2026-03-31 | 初始版本 | AI Assistant |
