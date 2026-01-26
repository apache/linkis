# 阶段2：设计方案文档

## 1. 总述

### 1.1 需求与目标

**项目背景**：在大模型分析场景中，当前获取用户任务日志接口会返回所有（info、error、warn）任务日志，导致大模型处理文件数量过多。为了优化大模型处理效率，需要对 filesystem 模块的 openLog 接口进行增强，支持根据指定的日志级别返回对应的日志内容。

**设计目标**：
1. 实现 openLog 接口的日志级别过滤功能
2. 支持 all、info、error、warn 四种日志级别
3. 保持向后兼容性，缺省情况下返回全部日志
4. 确保实现的正确性、性能和可靠性

## 2. 技术架构

**技术栈**：
- 开发语言：Java (服务端), Scala (客户端SDK)
- 框架：Spring Boot
- 存储：文件系统

**部署架构**：
与现有 filesystem 模块部署架构一致，无需额外部署组件。

## 3. 核心概念/对象

| 概念/对象 | 描述 |
|-----------|------|
| LogLevel | 日志级别枚举类，定义了 ERROR、WARN、INFO、ALL 四种级别 |
| FsRestfulApi | filesystem 模块的 RESTful 接口实现类 |
| OpenLogAction | 客户端 SDK 中调用 openLog 接口的 Action 类 |
| filterLogByLevel | 新增的日志过滤方法 |

## 4. 处理逻辑设计

### 4.1 接口参数变更

**原接口签名**：
```java
public Message openLog(
    HttpServletRequest req,
    @RequestParam(value = "path", required = false) String path,
    @RequestParam(value = "proxyUser", required = false) String proxyUser)
```

**新接口签名**：
```java
public Message openLog(
    HttpServletRequest req,
    @RequestParam(value = "path", required = false) String path,
    @RequestParam(value = "proxyUser", required = false) String proxyUser,
    @RequestParam(value = "logLevel", required = false, defaultValue = "all") String logLevel)
```

### 4.2 日志过滤逻辑

```
输入: log[4] 数组, logLevel 参数
|
v
logLevel 为空或 "all"? --> 是 --> 返回原始 log[4]
|
v (否)
根据 logLevel 创建新数组 filteredResult[4]，初始化为空字符串
|
v
switch(logLevel.toLowerCase()):
  case "error": filteredResult[0] = log[0]
  case "warn":  filteredResult[1] = log[1]
  case "info":  filteredResult[2] = log[2]
  default:      返回原始 log[4] (向后兼容)
|
v
返回 filteredResult[4]
```

### 4.3 数据结构

日志数组索引与日志级别对应关系：

| 索引 | 日志级别 | LogLevel.Type |
|------|----------|---------------|
| 0 | ERROR | LogLevel.Type.ERROR |
| 1 | WARN | LogLevel.Type.WARN |
| 2 | INFO | LogLevel.Type.INFO |
| 3 | ALL | LogLevel.Type.ALL |

## 5. 代码变更清单

### 5.1 FsRestfulApi.java

**文件路径**: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/filesystem/restful/api/FsRestfulApi.java`

**变更内容**:
1. `openLog` 方法添加 `logLevel` 参数
2. 添加 Swagger API 文档注解
3. 新增 `filterLogByLevel()` 私有方法

### 5.2 OpenLogAction.scala

**文件路径**: `linkis-computation-governance/linkis-client/linkis-computation-client/src/main/scala/org/apache/linkis/ujes/client/request/OpenLogAction.scala`

**变更内容**:
1. Builder 类添加 `logLevel` 属性（默认值 "all"）
2. 添加 `setLogLevel()` 方法
3. `build()` 方法中添加 logLevel 参数设置

## 6. 非功能性设计

### 6.1 安全

- **权限控制**：确保用户只能访问自己有权限的日志文件（复用现有逻辑）
- **参数校验**：对请求参数进行合理处理，无效参数不抛异常

### 6.2 性能

- 日志级别过滤对接口响应时间的影响可忽略不计（< 1ms）
- 过滤逻辑在内存中完成，无额外 I/O 操作

### 6.3 向后兼容

- 缺省情况下返回全部日志，与原有行为一致
- 无效 logLevel 参数返回全部日志，确保服务不中断
- 现有调用方无需修改代码即可继续使用

## 7. 变更历史

| 版本 | 日期 | 变更人 | 变更内容 |
|-----|------|--------|----------|
| 1.0 | 2025-12-26 | AI Assistant | 初始版本 |
