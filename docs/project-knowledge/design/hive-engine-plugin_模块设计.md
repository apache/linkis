# Hive引擎插件 模块设计

## 模块概述

| 属性 | 值 |
|-----|---|
| 模块ID | hive-engine-plugin |
| 创建日期 | 2026-03-27 |
| 最后更新 | 2026-03-27 |
| 设计数量 | 1 |

### 设计范围

本模块负责 Apache Linkis 的 Hive 引擎插件实现，包括：
- Hive 任务执行器（单连接和并发连接）
- YARN 任务标签设置
- HiveConf 配置管理
- 与 YARN ResourceManager 的集成

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│              Linkis Hive Engine Plugin                  │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  HiveEngineConnExecutor (单连接执行器)            │    │
│  │  HiveEngineConcurrentConnExecutor (并发执行器)    │    │
│  │                                                   │    │
│  │  职责：                                            │    │
│  │  - 接收并执行Hive SQL语句                          │    │
│  │  - 管理HiveConf配置                                │    │
│  │  - 设置YARN任务标签                                │    │
│  │  - 与YARN通信                                      │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                                │
│                          ▼                                │
│  ┌─────────────────────────────────────────────────┐    │
│  │              YARN ResourceManager                 │    │
│  │                                                   │    │
│  │  任务标签显示:                                    │    │
│  │  - LINKIS_{jobId}                                │    │
│  │  - USER_{username} (增强功能)                     │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 模块划分

| 子模块 | 职责 | 对外接口 |
|-------|------|---------|
| **HiveEngineConnExecutor** | Hive引擎单连接执行器 | `execute(line: String): Unit` |
| **HiveEngineConcurrentConnExecutor** | Hive引擎并发执行器 | `execute(line: String): Unit` |
| **JobUtils** | 任务工具类 | `getJobIdFromMap`, `getJobSourceTagsFromObjectMap` |
| **StringUtils** | 字符串工具类 | `isNotBlank`, `isAsciiPrintable` |

### 依赖关系

```
hive-engine-plugin
  ├─ linkis-common (公共通用模块)
  ├─ linkis-engineconn (引擎连接基础模块)
  ├─ linkis-rpc (RPC通信)
  ├─ Apache Commons Lang3 (字符串工具)
  └─ Hadoop Configuration (配置管理)
```

---

## API设计

### 内部接口列表

| 接口名 | 类名 | 方法 | 描述 |
|--------|-----|------|------|
| 任务执行 | HiveEngineConnExecutor | execute(line: String): Unit | 执行Hive SQL语句 |
| 任务执行 | HiveEngineConcurrentConnExecutor | execute(line: String): Unit | 并发执行Hive SQL语句 |
| 获取任务ID | JobUtils | getJobIdFromMap(properties): String | 从属性中获取任务ID |
| 获取任务标签 | JobUtils | getJobSourceTagsFromObjectMap(properties): String | 获取任务源标签（如EMR） |

### 配置接口

| 配置项 | 类型 | 描述 | 示例值 |
|-------|------|------|--------|
| mapreduce.job.tags | String | YARN任务标签 | `LINKIS_123456789,USER_zhangsan` |

---

## 数据模型

### 内存数据结构

**engineExecutorContext.getProperties**：

| 字段名 | 类型 | 说明 | 约束 | 示例值 |
|-------|------|------|------|--------|
| jobId | String | 任务ID | 可选 | "123456789" |
| jobTags | String | 任务源标签 | 可选 | "EMR" |
| execUser | String | 执行用户名 | 可选 | "zhangsan" |

---

## 安全设计

| 安全关注点 | 措施 | 说明 |
|-----------|------|------|
| 用户名隐私 | 用户名已在YARN中暴露，无新增风险 | execUser从现有context获取，未新增泄露点 |
| 权限控制 | 无变更 | 标签设置无额外权限要求 |
| 数据安全 | 无变更 | 仅修改标签内容，不涉及数据处理 |

---

## 监控与告警

### 关键指标

| 指标 | 阈值 | 告警级别 | 说明 |
|-----|------|---------|------|
| 标签构建时间 | <1ms | P1 | 标签构建耗时监控 |
| execUser为空率 | <5% | P2 | execUser为空的任务占比，异常升高告警 |
| YARN标签解析失败 | 0次/天 | P1 | YARN无法解析标签的情况 |

### 关键日志

```scala
LOG.info(s"set mapreduce.job.tags=$tags")
```

---

## 变更历史

| 日期 | 设计类型 | 功能描述 | 设计文档 |
|------|---------|---------|---------|
| 2026-03-27 | ENHANCE | 在YARN任务标签中增加用户名信息，增强任务可追溯性 | [hive_yarn_tag_username_设计.md](../../dev-1.19.0-hive-tag-username/design/hive_yarn_tag_username_设计.md) |

---

## 统计信息

| 统计项 | 数量 |
|--------|-----:|
| 总设计数 | 1 |
| new-feature | 0 |
| enhance-feature | 1 |
| fix-bug | 0 |
| optimize | 0 |
| refactor | 0 |
| integrate | 0 |

---

## 设计决策记录 (ADR) 索引

| ADR编号 | 决策主题 | 状态 | 相关设计 |
|--------|---------|------|---------|
| ADR-001 | 用户名标签前缀选择 USER_ | 已采纳 | hive_yarn_tag_username |
| ADR-002 | 标签顺序固定为 LINKIS,jobTags,USER | 已采纳 | hive_yarn_tag_username |
| ADR-003 | 用户名获取失败时保持原格式 | 已采纳 | hive_yarn_tag_username |
| ADR-004 | 不存储用户名标签到数据库 | 已采纳 | hive_yarn_tag_username |

---

## 附录

### 相关代码路径

| 功能 | 文件路径 |
|-----|---------|
| 单连接执行器 | `linkis-engineconn-plugins/hive/src/main/scala/org/apache/linkis/engineplugin/hive/executor/HiveEngineConnExecutor.scala` |
| 并发执行器 | `linkis-engineconn-plugins/hive/src/main/scala/org/apache/linkis/engineplugin/hive/executor/HiveEngineConcurrentConnExecutor.scala` |
| 任务工具类 | `linkis-engineconn-plugins/hive/src/main/scala/org/apache/linkis/engineplugin/hive/utils/JobUtils.scala` |

### 依赖版本

| 依赖 | 版本 |
|-----|------|
| Scala | 2.11.12 / 2.12.17 |
| Hadoop | 3.3.4 |
| Hive | 2.3.3 |
| Apache Commons Lang3 | 已存在 |
