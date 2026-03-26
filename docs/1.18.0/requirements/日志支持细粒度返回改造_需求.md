# 阶段1：需求分析文档

## 一、需求背景

在大模型分析场景中，当前获取用户任务日志接口会返回所有（info、error、warn）任务日志，导致大模型处理文件数量过多。为了优化大模型处理效率，需要对 filesystem 模块的 openLog 接口进行增强，支持根据指定的日志级别返回对应的日志内容。

## 二、需求描述

### 2.1 需求详细描述

| 模块 | 功能点 | 功能描述 | UI设计及细节 | 功能关注点 |
|-----|--------|----------|--------------|------------|
| filesystem | 日志级别过滤 | 在 openLog 接口中添加 logLevel 参数，支持指定返回的日志级别 | 不涉及 | 确保参数类型正确，默认值设置合理 |
| filesystem | 多种日志级别支持 | 支持 logLevel=all,info,error,warn 四种取值 | 不涉及 | 确保所有取值都能正确处理 |
| filesystem | 默认值处理 | 缺省情况下返回全部日志（相当于 logLevel=all） | 不涉及 | 确保向后兼容性 |
| filesystem | 向后兼容 | 不影响现有调用方的使用 | 不涉及 | 现有调用方无需修改代码即可继续使用 |

### 2.2 需求交互步骤

1. 用户调用 `/openLog` 接口，指定 `path` 参数和可选的 `logLevel` 参数
2. 系统解析请求参数，获取日志文件路径和日志级别
3. 系统读取日志文件内容，根据指定的日志级别过滤日志
4. 系统返回过滤后的日志内容给用户

### 2.3 模块交互步骤

```
用户 → filesystem模块 → openLog接口 → 日志文件 → 日志过滤 → 返回结果
```

**关键步骤说明**：
1. 用户调用 openLog 接口，传入 path 和 logLevel 参数
2. openLog 接口验证参数合法性，解析日志级别
3. 系统读取指定路径的日志文件
4. 系统根据日志级别过滤日志内容
5. 系统将过滤后的日志内容封装为响应对象返回给用户

**关注点**：
- 需关注无效 logLevel 参数的处理，应返回默认日志（全部日志）
- 需关注日志文件过大的情况，应返回合理的错误信息
- 需关注权限控制，确保用户只能访问自己有权限的日志文件

## 三、接口文档

### 3.1 接口基本信息

| 项 | 说明 |
|----|------|
| 接口URL | /api/rest_j/v1/filesystem/openLog |
| 请求方法 | GET |
| 接口描述 | 获取指定路径的日志文件内容，支持按日志级别过滤 |

### 3.2 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| path | String | 是 | 无 | 日志文件路径 |
| proxyUser | String | 否 | 无 | 代理用户，仅管理员可使用 |
| logLevel | String | 否 | all | 日志级别，取值为 all,info,error,warn |

### 3.3 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | String | 响应状态，success 表示成功，error 表示失败 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| data.log | String[] | 日志内容数组，按以下顺序排列：<br>1. 第0位：ERROR 级别的日志<br>2. 第1位：WARN 级别的日志<br>3. 第2位：INFO 级别的日志<br>4. 第3位：ALL 级别的日志（所有日志） |

### 3.4 请求示例

```bash
# 请求所有日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log"

# 请求特定级别的日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log&logLevel=error"
```

### 3.5 响应示例

**请求所有日志的响应**：
```json
{
  "status": "success",
  "message": "",
  "data": {
    "log": [
      "2025-12-26 10:00:02.000 ERROR This is an error log\n",
      "2025-12-26 10:00:01.000 WARN  This is a warn log\n",
      "2025-12-26 10:00:00.000 INFO  This is an info log\n",
      "2025-12-26 10:00:00.000 INFO  This is an info log\n2025-12-26 10:00:01.000 WARN  This is a warn log\n2025-12-26 10:00:02.000 ERROR This is an error log\n"
    ]
  }
}
```

**请求 ERROR 级别日志的响应**：
```json
{
  "status": "success",
  "message": "",
  "data": {
    "log": [
      "2025-12-26 10:00:02.000 ERROR This is an error log\n",
      "",
      "",
      ""
    ]
  }
}
```

## 四、关联影响分析

- **对存量功能的影响**：无，该功能是对现有接口的增强，不会影响其他功能
- **对第三方组件的影响**：无，该功能仅涉及 filesystem 模块内部逻辑

## 五、测试关注点

- 验证不同日志级别参数的处理是否正确
- 验证缺省情况下是否返回全部日志
- 验证无效日志级别参数的处理是否正确
- 验证大小写不敏感是否正确
- 验证权限控制是否有效
