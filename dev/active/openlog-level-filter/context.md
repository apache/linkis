# 任务上下文状态文件

## 基本信息

| 属性 | 值 |
|-----|-----|
| 任务名称 | openlog-level-filter |
| 需求类型 | ENHANCE (功能增强) |
| 创建时间 | 2025-12-26 |
| 当前阶段 | stage-4 (测试用例) |
| 执行模式 | 快速模式 |
| 状态 | 已完成 |

## 需求摘要

支持更细力度获取任务日志 - 为 filesystem 模块的 openLog 接口添加 logLevel 参数，支持按日志级别（all/info/error/warn）过滤返回的日志内容。

## 阶段进度

| 阶段 | 状态 | 完成时间 |
|-----|------|---------|
| stage-0 需求澄清 | ✅ 已完成 | 2025-12-26 |
| stage-1 需求分析 | ✅ 已完成 | 2025-12-26 |
| stage-2 设计方案 | ✅ 已完成 | 2025-12-26 |
| stage-3 代码开发 | ✅ 已完成 | 2025-12-26 |
| stage-4 测试用例 | ✅ 已完成 | 2025-12-26 |

## 变更文件

| 文件路径 | 变更类型 |
|---------|---------|
| linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/filesystem/restful/api/FsRestfulApi.java | 修改 |
| linkis-computation-governance/linkis-client/linkis-computation-client/src/main/scala/org/apache/linkis/ujes/client/request/OpenLogAction.scala | 修改 |
| linkis-public-enhancements/linkis-pes-publicservice/src/test/java/org/apache/linkis/filesystem/restful/api/OpenLogFilterTest.java | 新增 |
