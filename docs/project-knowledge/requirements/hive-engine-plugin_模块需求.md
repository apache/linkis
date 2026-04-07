# Hive Engine Plugin 模块需求

## 模块概述

| 属性 | 值 |
|-----|---|
| 模块ID | hive-engine-plugin |
| 创建日期 | 2026-03-27 |
| 最后更新 | 2026-03-27 |
| 需求数量 | 1 |

### 模块描述

Hive引擎插件模块，负责Linkis与Hive引擎的集成，包括Hive任务的执行、配置管理和YARN标签设置等功能。

---

## 需求列表

### 功能增强（ENHANCE）

| 需求ID | 标题 | 类型 | 状态 | 日期 |
|--------|-----|------|:----:|------|
| REQ-HIVE-001 | Hive传递给YARN的tag加上用户名 | ENHANCE | 设计中 | 2026-03-27 |

---

### REQ-HIVE-001: Hive传递给YARN的tag加上用户名

**类型**：功能增强（ENHANCE）
**状态**：设计中
**日期**：2026-03-27
**关联模块**：linkis-engineconn-plugins/hive
**版本**：1.19.0

**需求描述**：

在日常Hive任务调度和监控中，运维和开发人员需要能够快速定位YARN集群上运行的任务来源。当前Linkis在向YARN提交Hive任务时，已经设置了`mapreduce.job.tags`参数来标记任务ID，格式为 `LINKIS_{jobId}`，但缺少用户名信息，导致在YARN界面上难以区分是哪个用户提交的任务。

本需求旨在增强YARN任务标签，在现有的`mapreduce.job.tags`参数中增加用户名信息，标签格式为：
```
LINKIS_{jobId},USER_{username}
```

**业务价值**：
1. **提升运维效率**：运维人员可以在YARN界面上快速识别任务所属用户
2. **便于故障排查**：当某个用户的任务出现问题时，可以快速定位并处理
3. **增强任务可追溯性**：通过YARN标签即可知道任务提交者，无需查看Linkis日志

**量化目标**：
- 运维人员定位任务来源的时间从 **5分钟** 降低到 **10秒**
- 任务识别准确率达到 **100%**

**核心功能**：

1. **用户名标签添加**
   - 在HiveEngineConnExecutor.scala（第165-176行）中添加用户名标签
   - 在HiveEngineConcurrentConnExecutor.scala（第144-148行）中添加用户名标签
   - 用户名来源：`engineExecutorContext.getProperties.get("execUser")`

2. **向后兼容性保证**
   - 完全向后兼容现有功能
   - 当无法获取用户名时，保持原有标签格式
   - 不影响现有任务执行

3. **特殊字符处理**
   - 用户名中的特殊字符保持原样，不进行转义或过滤
   - YARN标签本身支持特殊字符

**验收标准**：

| 用例编号 | 测试场景 | 输入条件 | 预期结果 |
|---------|---------|----------|----------|
| TC001 | 正常用户名 | execUser="zhangsan", jobId="123" | `LINKIS_123,USER_zhangsan` |
| TC002 | 用户名为空字符串 | execUser="", jobId="123" | `LINKIS_123` |
| TC003 | 用户名为null | execUser=null, jobId="123" | `LINKIS_123` |
| TC004 | jobId为空 | jobId="" | 不设置标签 |
| TC005 | 特殊字符用户名 | execUser="user@example.com", jobId="123" | `LINKIS_123,USER_user@example.com` |
| TC006 | 结合jobTags使用 | jobId="123", jobTags="EMR", execUser="zhangsan" | `LINKIS_123,EMR,USER_zhangsan` |

**影响范围**：
- 影响模块：`linkis-engineconn-plugins/hive`
- 不影响模块：其他引擎插件（Spark、Flink等）、Linkis公共服务模块

**原始需求文档**：`docs/dev-1.19.0-hive-tag-username/requirements/hive_yarn_tag_username_需求.md`

---

## 统计信息

| 统计项 | 数量 |
|--------|-----:|
| 总需求数 | 1 |
| NEW | 0 |
| ENHANCE | 1 |
| FIX | 0 |
| OPTIMIZE | 0 |
| REFACTOR | 0 |
| INTEGRATE | 0 |

---

**模块文档版本**：1.0
**最后更新时间**：2026-03-27
