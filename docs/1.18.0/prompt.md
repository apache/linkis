# 需求开发Prompts合并文档

## 1. openlog-level-filter - 支持更细粒度获取任务日志

### 1.1 需求澄清 Prompt

```
你是一个需求分析专家。用户提出了以下需求：

需求描述：支持更细力度获取任务日志

请分析这个需求：
1. 识别需求类型（新功能/功能增强/Bug修复/优化/重构/集成）
2. 提取关键信息
3. 列出需要澄清的问题
```

### 1.2 需求澄清问答

**问题1**：您希望支持哪些更细粒度的日志获取方式？

**用户回答**：按日志级别获取，比如支持logLevel=all,info,error,warn四种取值

**问题2**：此增强主要用于哪个场景？

**用户回答**：大模型分析场景，减少大模型处理的文件数量

**问题3**：缺省情况下应如何处理？

**用户回答**：缺省情况下返回全部日志，相当于logLevel=all

### 1.3 需求分析 Prompt

```
你是一个软件需求分析师。请基于以下澄清后的需求，生成详细的需求分析文档：

## 需求背景
- 需求类型：功能增强（ENHANCE）
- 目标接口：/filesystem/openLog（任务日志查询）
- 核心需求：支持按日志级别（all/info/error/warn）过滤返回的日志内容

## 当前实现
- 接口返回所有（info、error、warn）任务日志
- 用户无法选择只获取特定级别的日志

## 期望行为
- 新增 logLevel 参数
- 支持 logLevel=all,info,error,warn 四种取值
- 缺省情况下返回全部日志
- 不影响现有调用方的使用

请输出：
1. 功能需求详细描述
2. 非功能需求
3. 验收标准
4. 影响范围分析
```

### 1.4 设计方案 Prompt

```
你是一个软件架构师。请基于以下需求，设计技术实现方案：

## 需求概述
在 filesystem 模块的 openLog 接口中添加 logLevel 参数，支持按日志级别过滤返回的日志内容。

## 现有代码结构
- FsRestfulApi.openLog() - 接口入口
- OpenLogAction - 客户端 SDK Action 类
- LogLevel - 日志级别枚举类

## 现有处理逻辑
返回所有级别的日志，无法进行过滤

请输出：
1. 接口变更设计
2. 新增方法设计
3. 核心逻辑变更
4. 兼容性设计
5. 变更文件清单
```

### 1.5 代码开发 Prompt

```
你是一个 Java/Scala 后端开发工程师。请基于以下设计方案，实现代码变更：

## 变更要求

### 1. 修改 FsRestfulApi.java
- 新增 logLevel 参数：@RequestParam(value = "logLevel", required = false, defaultValue = "all") String logLevel
- 实现日志过滤逻辑：根据 logLevel 参数过滤返回的日志
- 添加 Swagger API 文档注解

### 2. 修改 OpenLogAction.scala
- Builder 类添加 logLevel 属性（默认值 "all"）
- 添加 setLogLevel() 方法
- build() 方法中添加 logLevel 参数设置

### 3. 新增日志过滤方法
- 实现 filterLogByLevel() 方法，根据日志级别过滤日志

## 注意事项
- 保持向后兼容性
- 无效 logLevel 参数返回全部日志
- 确保现有调用方无需修改代码即可继续使用
```

### 1.6 测试用例生成 Prompt

```
你是一个测试工程师。请基于以下功能设计测试用例：

## 功能描述
/filesystem/openLog 接口新增 logLevel 参数，支持按日志级别（all/info/error/warn）过滤日志。

## 参数说明
- logLevel：可选，支持 all/info/error/warn
- 缺省值：all
- 大小写不敏感

## 响应格式
log 数组：[ERROR(0), WARN(1), INFO(2), ALL(3)]

请生成测试用例覆盖：
1. 功能测试（正常场景）
2. 边界测试（缺省情况、无效参数）
3. 兼容性测试（现有调用方式）
4. 异常测试（权限控制）
```

### 1.7 验收标准生成 Prompt

```
你是一个产品经理。请基于以下功能需求，生成验收标准：

## 功能需求
1. 单级别过滤：logLevel=error 仅返回 ERROR 级别日志
2. 全部日志：logLevel=all 或不传返回所有级别日志
3. 向后兼容：现有调用方式不受影响
4. 容错处理：无效 logLevel 值返回全部日志

## 接口规格
GET /api/rest_j/v1/filesystem/openLog?path=xxx&logLevel=xxx

## 响应规格
{
  "status": "success",
  "message": "",
  "data": {
    "log": ["ERROR日志", "WARN日志", "INFO日志", "ALL日志"]
  }
}

请输出：
1. 每个功能点的验收条件
2. 验证方法
3. 检查清单
```

### 1.8 代码审查 Prompt

```
你是一个代码审查专家。请审查以下代码变更：

## 变更文件
1. FsRestfulApi.java
2. OpenLogAction.scala
3. OpenLogFilterTest.java

## 变更内容
1. FsRestfulApi.openLog() 方法新增 logLevel 参数
2. 新增 filterLogByLevel() 方法
3. OpenLogAction 新增 logLevel 属性和方法
4. 新增测试用例

请检查：
1. 代码逻辑正确性
2. 边界情况处理
3. 向后兼容性
4. 性能影响
5. 安全风险
6. 代码风格
```

---

## 2. resultset-view-optimize - 结果集查看优化

### 2.1 需求澄清 Prompt

```
你是一个需求分析专家。用户提出了以下需求：

需求描述：结果集查看优化

请分析这个需求：
1. 识别需求类型（新功能/功能增强/Bug修复/优化/重构/集成）
2. 提取关键信息
3. 列出需要澄清的问题
```

### 2.2 需求澄清问答

**问题1**：您希望优化哪些方面？

**用户回答**：主要有两点：1. 兼容旧逻辑，历史管理台结果集展示不进行拦截；2. 拦截提示展示配置数字

**问题2**：如何识别管理台和非管理台请求？

**用户回答**：通过请求参数enableLimit来识别，enableLimit=true表示管理台请求

**问题3**：配置数字具体指什么？

**用户回答**：指配置文件中设置的字段查看最大长度阈值

### 2.3 需求分析 Prompt

```
你是一个软件需求分析师。请基于以下澄清后的需求，生成详细的需求分析文档：

## 需求背景
- 需求类型：优化（OPTIMIZE）
- 目标功能：结果集查看功能
- 核心需求：
  1. 兼容旧逻辑，历史管理台结果集展示不进行拦截
  2. 拦截提示展示配置数字

## 当前实现
- 所有请求都进行结果集截取
- 提示信息中显示固定的阈值，与配置不一致

## 期望行为
- 管理台请求（enableLimit=true）跳过结果集截取
- 非管理台请求按照原有逻辑处理，但提示信息中动态显示配置的阈值
- 保留原有的功能开关和配置项

请输出：
1. 功能需求详细描述
2. 非功能需求
3. 验收标准
4. 影响范围分析
```

### 2.4 设计方案 Prompt

```
你是一个软件架构师。请基于以下需求，设计技术实现方案：

## 需求概述
对结果集查看功能进行优化，实现管理台请求不进行结果集拦截，非管理台请求动态显示配置的阈值。

## 现有代码结构
- FsRestfulApi.openFile() - 结果集查看接口
- LinkisStorageConf - 存储配置类
- ResultUtils - 结果集处理工具类

## 现有处理逻辑
```java
// 优先截取大字段
if (LinkisStorageConf.FIELD_TRUNCATION_ENABLED()) {
    // 处理逻辑
}
```

请输出：
1. 接口变更设计
2. 新增方法设计
3. 核心逻辑变更
4. 兼容性设计
5. 变更文件清单
```

### 2.5 代码开发 Prompt

```
你是一个 Java 后端开发工程师。请基于以下设计方案，实现代码变更：

## 变更要求

### 1. 修改 FsRestfulApi.java
- 新增管理台请求识别逻辑：根据enableLimit参数判断
- 管理台请求（enableLimit=true）跳过结果集截取
- 非管理台请求按照原有逻辑处理，但提示信息中动态显示配置的阈值

### 2. 修改提示信息生成逻辑
- 将固定的阈值替换为从配置中动态获取的阈值
- 使用LinkisStorageConf.FIELD_VIEW_MAX_LENGTH()获取配置值

## 注意事项
- 保持向后兼容性
- 不影响现有系统的功能和API
- 代码逻辑清晰，易于理解和维护
```

### 2.6 测试用例生成 Prompt

```
你是一个测试工程师。请基于以下功能设计测试用例：

## 功能描述
结果集查看优化，根据请求类型和配置进行不同处理：
- 管理台请求（enableLimit=true）：跳过结果集截取
- 非管理台请求：按照配置阈值进行截取，提示信息动态显示配置的阈值

## 参数说明
- enableLimit：可选，true表示管理台请求，false或未传表示非管理台请求
- linkis.storage.field.view.max.length：配置字段查看最大长度
- linkis.storage.field.truncation.enabled：控制功能开关

## 响应格式
包含metadata、fileContent、oversizedFields、zh_msg、en_msg等字段

请生成测试用例覆盖：
1. 功能测试（正常场景）
2. 边界测试（不同配置值）
3. 兼容性测试（旧版本调用）
4. 异常测试（配置错误）
```

### 2.7 验收标准生成 Prompt

```
你是一个产品经理。请基于以下功能需求，生成验收标准：

## 功能需求
1. 管理台请求处理：enableLimit=true时跳过结果集截取
2. 非管理台请求处理：enableLimit=false时按照配置阈值进行截取
3. 动态提示信息：提示信息中显示配置的实际阈值
4. 功能开关：可通过配置控制功能开启/关闭

## 接口规格
GET /api/rest_j/v1/filesystem/openFile?path=xxx&enableLimit=xxx&page=xxx&pageSize=xxx

## 响应规格
{
  "method": "openFile",
  "status": 0,
  "message": "success",
  "data": {
    "metadata": [...],
    "fileContent": [...],
    "oversizedFields": [...],
    "zh_msg": "结果集存在字段值字符数超过10000，如需查看全部数据请导出文件或使用字符串截取函数...",
    "en_msg": "The result set contains field values exceeding 10000 characters..."
  }
}

请输出：
1. 每个功能点的验收条件
2. 验证方法
3. 检查清单
```

### 2.8 代码审查 Prompt

```
你是一个代码审查专家。请审查以下代码变更：

## 变更文件
1. FsRestfulApi.java

## 变更内容
1. 新增管理台请求识别逻辑
2. 修改结果集截取逻辑，跳过管理台请求
3. 修改提示信息生成逻辑，动态显示配置的阈值

请检查：
1. 代码逻辑正确性
2. 边界情况处理
3. 向后兼容性
4. 性能影响
5. 安全风险
6. 代码风格
```

---

## 3. simplify-dealspark-dynamic-conf - 简化dealsparkDynamicConf方法

### 3.1 需求澄清 Prompt

```
你是一个需求分析专家。用户提出了以下需求：

需求描述：简化dealsparkDynamicConf方法

请分析这个需求：
1. 识别需求类型（新功能/功能增强/Bug修复/优化/重构/集成）
2. 提取关键信息
3. 列出需要澄清的问题
```

### 3.2 需求澄清问答

**问题1**：您希望如何简化这个方法？

**用户回答**：主要有几点：1. 仅强制设置spark.python.version为python3；2. 移除所有其他参数覆盖；3. 信任Spark启动时会自己读取管理台的参数；4. 保留异常处理的兜底逻辑

**问题2**：这个方法主要用于什么场景？

**用户回答**：用于处理Spark3动态资源规划配置

**问题3**：是否需要添加新的工具方法来支持这个简化？

**用户回答**：需要新增isTargetEngine方法，用于检查给定的labels是否对应目标引擎类型和可选版本

### 3.3 需求分析 Prompt

```
你是一个软件需求分析师。请基于以下澄清后的需求，生成详细的需求分析文档：

## 需求背景
- 需求类型：优化（OPTIMIZE）
- 目标方法：dealsparkDynamicConf方法
- 核心需求：
  1. 仅强制设置spark.python.version为python3
  2. 移除所有其他参数覆盖，包括动态资源规划开关
  3. 信任Spark启动时会自己读取管理台的参数
  4. 保留异常处理的兜底逻辑
  5. 新增isTargetEngine方法，用于检查引擎类型和版本

## 当前实现
- 方法复杂，包含大量参数覆盖逻辑
- 包含动态资源规划开关处理
- 代码维护成本高

## 期望行为
- 简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置
- 移除所有其他参数覆盖
- 新增isTargetEngine方法，支持检查引擎类型和版本
- 保留异常处理的兜底逻辑

请输出：
1. 功能需求详细描述
2. 非功能需求
3. 验收标准
4. 影响范围分析
```

### 3.4 设计方案 Prompt

```
你是一个软件架构师。请基于以下需求，设计技术实现方案：

## 需求概述
简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置，移除所有其他参数覆盖，新增isTargetEngine方法。

## 现有代码结构
- EntranceUtils.scala - 包含dealsparkDynamicConf方法
- LabelUtil.scala - 包含引擎标签处理方法
- JobRequest - 作业请求对象
- LogLevel - 日志级别枚举类

## 现有处理逻辑
```scala
def dealsparkDynamicConf(
    jobRequest: JobRequest,
    logAppender: lang.StringBuilder,
    params: util.Map[String, AnyRef]
): Unit = {
  // 复杂的参数处理逻辑
  // 包含大量参数覆盖
  // 包含动态资源规划开关处理
}
```

请输出：
1. 方法简化设计
2. 新增方法设计
3. 核心逻辑变更
4. 兼容性设计
5. 变更文件清单
```

### 3.5 代码开发 Prompt

```
你是一个 Scala 后端开发工程师。请基于以下设计方案，实现代码变更：

## 变更要求

### 1. 简化 EntranceUtils.scala 中的 dealsparkDynamicConf 方法
- 只保留spark.python.version的强制设置
- 移除所有其他参数覆盖，包括动态资源规划开关
- 信任Spark启动时会自己读取管理台的参数
- 保留异常处理的兜底逻辑

### 2. 在 LabelUtil.scala 中新增 isTargetEngine 方法
- 功能：检查给定的labels是否对应目标引擎类型和可选版本
- 参数：labels, engine, version（可选）
- 返回值：布尔值，表示是否匹配

## 注意事项
- 保持向后兼容性
- 兼容现有系统的功能和API
- 代码逻辑清晰，易于理解和维护
- 确保系统稳定性
```

### 3.6 测试用例生成 Prompt

```
你是一个测试工程师。请基于以下功能设计测试用例：

## 功能描述
简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置，新增isTargetEngine方法。

## 参数说明
- dealsparkDynamicConf：
  - jobRequest：作业请求对象
  - logAppender：日志追加器
  - params：参数映射
- isTargetEngine：
  - labels：标签列表
  - engine：目标引擎类型
  - version：可选的目标版本

## 预期行为
- 对于Spark3引擎，强制设置spark.python.version为python3
- 对于非Spark3引擎，不执行任何参数设置
- 异常情况下使用兜底方案
- isTargetEngine方法能正确检查引擎类型和版本

请生成测试用例覆盖：
1. 功能测试（正常场景）
2. 边界测试（空参数、无效引擎类型）
3. 异常测试（方法执行异常）
4. 兼容性测试（现有任务执行）
```

### 3.7 验收标准生成 Prompt

```
你是一个产品经理。请基于以下功能需求，生成验收标准：

## 功能需求
1. 简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置
2. 移除所有其他参数覆盖，包括动态资源规划开关
3. 信任Spark启动时会自己读取管理台的参数
4. 保留异常处理的兜底逻辑
5. 新增isTargetEngine方法，用于检查引擎类型和版本

## 接口规格
该方法为内部方法，无外部接口

## 预期行为
- Spark3作业：只设置spark.python.version为python3
- 非Spark3作业：不执行任何参数设置
- 异常情况下：使用兜底方案，统一由后台配置
- isTargetEngine方法：能正确检查引擎类型和版本

请输出：
1. 每个功能点的验收条件
2. 验证方法
3. 检查清单
```

### 3.8 代码审查 Prompt

```
你是一个代码审查专家。请审查以下代码变更：

## 变更文件
1. EntranceUtils.scala
2. LabelUtil.scala

## 变更内容
1. 简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置
2. 移除所有其他参数覆盖，包括动态资源规划开关
3. 新增isTargetEngine方法，用于检查引擎类型和版本
4. 保留异常处理的兜底逻辑

请检查：
1. 代码逻辑正确性
2. 边界情况处理
3. 向后兼容性
4. 性能影响
5. 异常处理
6. 代码风格
```

---

## 4. spark-task-diagnosis - Spark任务诊断结果持久化

### 4.1 需求澄清 Prompt

```
你是一个需求分析专家。用户提出了以下需求：

需求描述：在jobhistory模块中添加接口，用于将诊断信息更新至linkis_ps_job_history_diagnosis表中

请分析这个需求：
1. 识别需求类型（新功能/功能增强/Bug修复/优化/重构/集成）
2. 提取关键信息
3. 列出需要澄清的问题
```

### 4.2 需求澄清问答

**问题1**：您希望诊断信息包含哪些内容？

**用户回答**：诊断信息存入diagnosisContent字段，diagnosisSource存入doctoris

**问题2**：什么时候调用这个接口？

**用户回答**：在entrance诊断之后调用该接口更新诊断结果

**问题3**：这个接口的调用方式是什么？

**用户回答**：使用RPC接口调用

### 4.3 需求分析 Prompt

```
你是一个软件需求分析师。请基于以下澄清后的需求，生成详细的需求分析文档：

## 需求背景
- 需求类型：新功能（NEW）
- 目标模块：jobhistory模块
- 核心需求：
  1. 添加接口，用于将诊断信息更新至linkis_ps_job_history_diagnosis表
  2. 诊断信息存入diagnosisContent字段
  3. diagnosisSource存入doctoris
  4. 在entrance诊断之后调用该接口更新诊断结果

## 当前实现
- 诊断结果仅存储在日志中，无法持久化存储和查询
- 没有提供诊断结果的更新接口

## 期望行为
- 实现诊断结果的持久化存储
- 提供诊断结果的更新接口
- 支持诊断结果的创建和更新操作
- 在任务诊断完成后自动调用更新接口

请输出：
1. 功能需求详细描述
2. 非功能需求
3. 验收标准
4. 影响范围分析
```

### 4.4 设计方案 Prompt

```
你是一个软件架构师。请基于以下需求，设计技术实现方案：

## 需求概述
在jobhistory模块中添加接口，用于将诊断信息更新至linkis_ps_job_history_diagnosis表中，在entrance诊断之后调用该接口更新诊断结果。

## 现有代码结构
- JobHistoryQueryServiceImpl - JobHistory服务实现类
- EntranceServer - 入口服务，负责任务诊断
- JobDiagnosis - 诊断记录实体类
- JobHistoryDiagnosisService - 诊断记录服务

## 现有处理逻辑
诊断结果仅存储在日志中，无法持久化存储

请输出：
1. 接口设计
2. 数据模型设计
3. 核心逻辑设计
4. 调用流程设计
5. 变更文件清单
```

### 4.5 代码开发 Prompt

```
你是一个 Scala 后端开发工程师。请基于以下设计方案，实现代码变更：

## 变更要求

### 1. 新增 JobReqDiagnosisUpdate 类
- 功能：诊断结果更新请求协议类
- 属性：jobHistoryId, diagnosisContent, diagnosisSource
- 方法：apply方法用于快速创建实例

### 2. 修改 JobHistoryQueryServiceImpl.scala
- 新增updateDiagnosis方法，使用@Receiver注解接收RPC请求
- 实现诊断记录的创建和更新逻辑
- 支持根据jobHistoryId和diagnosisSource查询诊断记录

### 3. 修改 EntranceServer.scala
- 在任务诊断完成后，调用updateDiagnosis接口更新诊断结果
- 构造JobReqDiagnosisUpdate请求，设置diagnosisSource为"doctoris"
- 通过RPC发送请求到jobhistory服务

## 注意事项
- 保持向后兼容性
- 确保接口调用的可靠性和安全性
- 合理处理异常情况
- 记录必要的日志
```

### 4.6 测试用例生成 Prompt

```
你是一个测试工程师。请基于以下功能设计测试用例：

## 功能描述
在jobhistory模块中添加接口，用于将诊断信息更新至linkis_ps_job_history_diagnosis表中。

## 参数说明
- JobReqDiagnosisUpdate：
  - jobHistoryId：任务历史ID
  - diagnosisContent：诊断内容
  - diagnosisSource：诊断来源

## 预期行为
- 当不存在诊断记录时，创建新的诊断记录
- 当存在诊断记录时，更新现有诊断记录
- 诊断结果能正确持久化到数据库中
- 接口调用成功返回正确的响应

请生成测试用例覆盖：
1. 功能测试（正常场景）
2. 边界测试（空参数、无效参数）
3. 异常测试（方法执行异常）
4. 并发测试（并发调用更新接口）
```

### 4.7 验收标准生成 Prompt

```
你是一个产品经理。请基于以下功能需求，生成验收标准：

## 功能需求
1. 诊断结果更新接口：提供RPC接口，用于更新任务诊断结果
2. 诊断记录创建：当不存在诊断记录时，创建新的诊断记录
3. 诊断记录更新：当存在诊断记录时，更新现有诊断记录
4. 自动调用：在entrance诊断之后自动调用更新接口

## 接口规格
RPC接口：JobReqDiagnosisUpdate

## 响应规格
{
  "status": 0,
  "msg": "Update diagnosis success"
}

请输出：
1. 每个功能点的验收条件
2. 验证方法
3. 检查清单
```

### 4.8 代码审查 Prompt

```
你是一个代码审查专家。请审查以下代码变更：

## 变更文件
1. JobReqDiagnosisUpdate.scala
2. JobHistoryQueryServiceImpl.scala
3. EntranceServer.scala

## 变更内容
1. 新增JobReqDiagnosisUpdate RPC协议类
2. JobHistoryQueryServiceImpl新增updateDiagnosis方法
3. EntranceServer在任务诊断完成后调用updateDiagnosis接口

请检查：
1. 代码逻辑正确性
2. 边界情况处理
3. 异常处理
4. 性能影响
5. 安全风险
6. 代码风格
```

---

## 5. system-user-login-block - 系统用户登录拦截

### 5.1 需求澄清 Prompt

```
你是一个需求分析专家。用户提出了以下需求：

需求描述：禁止系统用户和hadoop用户通过Web页面登录Linkis管理台

请分析这个需求：
1. 识别需求类型（新功能/功能增强/Bug修复/优化/重构/集成）
2. 提取关键信息
3. 列出需要澄清的问题
```

### 5.2 需求澄清问答

**问题1**：是否需要影响客户端等其他渠道的登录？

**用户回答**：不影响客户端(client)等其他渠道的登录

**问题2**：如何识别Web页面登录？

**用户回答**：通过HTTP Header的webLogin字段来识别，前端在Web页面调用登录接口时，需要在HTTP请求header中添加webLogin: true

**问题3**：系统用户如何定义？

**用户回答**：通过系统用户前缀列表来定义，配置在linkis.system.user.prohibit.login.prefix参数中

### 5.3 需求分析 Prompt

```
你是一个软件需求分析师。请基于以下澄清后的需求，生成详细的需求分析文档：

## 需求背景
- 需求类型：功能增强（ENHANCE）
- 目标功能：登录拦截功能
- 核心需求：
  1. 禁止系统用户和hadoop用户通过Web页面登录Linkis管理台
  2. 不影响客户端(client)等其他渠道的登录
  3. 提供配置开关和系统用户前缀配置

## 当前实现
- 没有专门针对Web页面登录的拦截机制
- 无法区分不同渠道的登录请求

## 期望行为
- 前端在Web页面调用登录接口时，在HTTP请求header中添加webLogin: true
- 后端从header获取webLogin标识，默认值为false
- 当webLogin=true时，拦截系统用户前缀匹配的用户
- 提供配置开关和系统用户前缀列表

请输出：
1. 功能需求详细描述
2. 非功能需求
3. 验收标准
4. 影响范围分析
```

### 5.4 设计方案 Prompt

```
你是一个软件架构师。请基于以下需求，设计技术实现方案：

## 需求概述
禁止系统用户和hadoop用户通过Web页面登录Linkis管理台，通过HTTP Header的webLogin字段识别Web页面登录请求。

## 现有代码结构
- GatewayConfiguration - 网关配置类
- UserRestful - 用户登录接口实现类
- tryLogin方法 - 处理登录请求
- getRequestSource方法 - 获取请求来源

## 现有处理逻辑
```scala
if (
    GatewayConfiguration.PROHIBIT_LOGIN_SWITCH.getValue &&
    (!getRequestSource(gatewayContext).equals("client"))
) {
  PROHIBIT_LOGIN_PREFIX.split(",").foreach {
    if (userName.toLowerCase().startsWith(prefix)) {
      return Message.error("System users are prohibited from logging in（系统用户禁止登录）！")
    }
  }
}
```

请输出：
1. 接口变更设计
2. 新增方法设计
3. 核心逻辑变更
4. 兼容性设计
5. 变更文件清单
```

### 5.5 代码开发 Prompt

```
你是一个 Scala 后端开发工程师。请基于以下设计方案，实现代码变更：

## 变更要求

### 1. 修改 GatewayConfiguration.scala
- 更新 PROHIBIT_LOGIN_PREFIX 默认值为 hadoop,hduser,shduser
- 新增 WEB_LOGIN_HEADER 常量

### 2. 修改 UserRestful.scala
- 新增 isWebLogin 方法从HTTP Header获取webLogin标识
- 修改 tryLogin 方法的拦截逻辑，使用isWebLogin方法替代原有的getRequestSource方法
- 当webLogin=true时，拦截系统用户前缀匹配的用户

## 注意事项
- 保持向后兼容性
- 默认功能关闭，不影响现有系统
- 可通过配置开关灵活控制
- 系统用户前缀可配置
```

### 5.6 测试用例生成 Prompt

```
你是一个测试工程师。请基于以下功能设计测试用例：

## 功能描述
禁止系统用户和hadoop用户通过Web页面登录Linkis管理台，通过HTTP Header的webLogin字段识别Web页面登录请求。

## 参数说明
- webLogin Header：true表示Web页面登录，false或未传表示其他渠道登录
- linkis.system.user.prohibit.login.switch：功能开关
- linkis.system.user.prohibit.login.prefix：系统用户前缀列表

## 预期行为
- Web页面登录(webLogin=true)：拦截系统用户登录
- 其他渠道登录(webLogin=false或未传)：不拦截系统用户登录
- 功能开关关闭：不拦截任何登录

请生成测试用例覆盖：
1. 功能测试（正常场景）
2. 边界测试（缺省情况、无效参数）
3. 兼容性测试（旧版本调用）
4. 异常测试（配置错误）
```

### 5.7 验收标准生成 Prompt

```
你是一个产品经理。请基于以下功能需求，生成验收标准：

## 功能需求
1. Web页面登录拦截：webLogin=true时拦截系统用户登录
2. 其他渠道放行：webLogin=false或未传时不拦截系统用户登录
3. 功能开关：可通过配置控制功能开启/关闭
4. 系统用户前缀：可配置系统用户前缀列表

## 接口规格
POST /api/rest_j/v1/user/login
Header: webLogin=true

## 响应规格
{
  "method": "/api/rest_j/v1/user/login",
  "status": 1,
  "message": "System users are prohibited from logging in（系统用户禁止登录）！"
}

请输出：
1. 每个功能点的验收条件
2. 验证方法
3. 检查清单
```

### 5.8 代码审查 Prompt

```
你是一个代码审查专家。请审查以下代码变更：

## 变更文件
1. GatewayConfiguration.scala
2. UserRestful.scala

## 变更内容
1. 更新 PROHIBIT_LOGIN_PREFIX 默认值为 hadoop,hduser,shduser
2. 新增 WEB_LOGIN_HEADER 常量
3. 新增 isWebLogin 方法从HTTP Header获取webLogin标识
4. 修改 tryLogin 方法的拦截逻辑

请检查：
1. 代码逻辑正确性
2. 边界情况处理
3. 向后兼容性
4. 性能影响
5. 安全风险
6. 代码风格
```

---

## 总结

本文档合并了5个需求的开发Prompts，每个需求都包含了从需求澄清到代码审查的完整开发流程。这些Prompts将有助于规范和指导各个需求的开发过程，确保开发过程的完整性和质量。