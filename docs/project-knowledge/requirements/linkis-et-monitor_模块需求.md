# linkis-et-monitor 模块需求

## 模块概述

| 属性 | 值 |
|-----|---|
| 模块ID | linkis-et-monitor |
| 创建日期 | 2026-03-23 |
| 最后更新 | 2026-03-23 |
| 需求数量 | 1 |

### 模块描述

Apache Linkis的Monitor模块（linkis-et-monitor），负责系统监控、事后诊断、告警发布等功能。该模块包含定时任务扫描、失败任务诊断、连接池管理等核心能力。

## 需求列表

### OPTIMIZE（优化）

| 需求ID | 标题 | 类型 | 状态 | 日期 | 分支 |
|--------|-----|------|:----:|------|------|
| MON-OPT-001 | Monitor模块优化（诊断日志清理 + 诊断功能拆分 + 连接池扩容） | OPTIMIZE | 待评审 | 2026-03-23 | dev-1.19.0-monitor-update |

#### MON-OPT-001: Monitor模块优化（诊断日志清理 + 诊断功能拆分 + 连接池扩容）

**类型**：OPTIMIZE（综合优化）
**状态**：待评审
**日期**：2026-03-23
**分支**：dev-1.19.0-monitor-update

**需求描述**：
Monitor模块（linkis-et-monitor）在运行过程中遇到以下问题：

1. **诊断日志占用磁盘空间**：Monitor模块存在定时任务扫描前20分钟的任务，当任务失败时触发事后诊断功能，诊断日志保存在服务器磁盘，长期累积不清理会导致磁盘空间浪费，缺少自动清理机制。

2. **诊断功能无法按需关闭**：诊断功能和job扫描任务耦合在一起执行，某些环境不需要诊断功能，但无法单独关闭，造成资源浪费。

3. **数据库连接池配置过小**：ThreadUtils中alert连接池配置为5个线程，当诊断任务较多时，连接池成为性能瓶颈，导致诊断任务排队等待，影响监控及时性。

本次优化包含三个子项：
- 需求子项1：诊断日志自动清理（NEW，P0）
- 需求子项2：诊断功能配置化拆分（REFACTOR，P1）
- 需求子项3：Alert连接池扩容（FIX，P0）

**优化内容**：

| 序号 | 优化项 | 类型 | 优先级 |
|-----|-------|------|:------:|
| 1 | 诊断日志自动清理 | NEW | P0 |
| 2 | 诊断功能配置化拆分 | REFACTOR | P1 |
| 3 | Alert连接池扩容 | FIX | P0 |

**验收标准**：

| ID | 功能点 | 验收标准 | 优先级 |
|----|-------|---------|:------:|
| F1.1 | 定时清理任务 | 定时任务能按时执行 | P0 |
| F1.2 | 配置化保留策略 | 能正确删除超过保留期的日志文件 | P0 |
| F1.3 | 诊断日志目录识别 | 不会误删非诊断日志文件 | P0 |
| F1.4 | 删除过期日志 | 配置参数生效，支持动态调整 | P0 |
| F1.5 | 清理审计日志 | 清理日志包含删除文件数量和释放空间信息 | P1 |
| F1.6 | 功能开关 | 支持配置启用/禁用日志清理 | P1 |
| F2.1 | 配置化开关 | 配置true时，诊断功能正常工作 | P0 |
| F2.2 | 条件执行 | 配置false时，跳过诊断扫描逻辑 | P0 |
| F2.3 | 日志输出 | 诊断功能禁用时输出明确提示日志 | P1 |
| F2.4 | 向后兼容 | 默认值为true，保持现有行为 | P1 |
| F3.1 | 连接池参数调整 | 代码已修改，线程数为20 | P0 |
| F3.2 | 单元测试更新（如有） | 编译通过，无语法错误 | P0 |

**配置参数清单**：

| 参数名 | 说明 | 默认值 | 作用范围 |
|-------|------|:------:|---------|
| linkis.monitor.diagnosis.log.enabled | 是否启用日志清理 | true | F1.6 |
| linkis.monitor.diagnosis.log.retention.days | 日志保留天数 | 7 | F1.2 |
| linkis.monitor.diagnosis.log.path | 诊断日志路径 | 待确定 | F1.3 |
| linkis.monitor.jobHistory.diagnosis.enabled | 是否启用诊断功能 | true | F2.1 |

**相关文件**：
- linkis-et-monitor/src/main/java/org/apache/linkis/monitor/until/ThreadUtils.java
- linkis-et-monitor/src/main/java/org/apache/linkis/monitor/scheduled/JobHistoryMonitor.java
- linkis-et-monitor/src/main/java/org/apache/linkis/monitor/scheduled/DiagnosisLogClear.java（新增）
- linkis-et-monitor/src/main/resources/linkis-et-monitor.properties

---

## 统计信息

| 统计项 | 数量 |
|--------|-----:|
| 总需求数 | 1 |
| NEW | 1 |
| ENHANCE | 0 |
| FIX | 0 |
| OPTIMIZE | 1 |
| REFACTOR | 0 |
| INTEGRATE | 0 |

---

**同步信息**：
- 同步时间：2026-03-23
- 来源文档：docs/dev-1.19.0-monitor-update/requirements/monitor优化_需求.md
- 同步状态：已完成
