# AISQL任务支持StarRocks引擎类型切换需求

## 需求概述

为AISQL类型任务增加StarRocks引擎类型切换支持，允许用户通过多种方式指定任务使用StarRocks引擎执行。

## 需求背景

### 当前现状
1. AISQL任务目前支持Spark和Hive两种引擎类型的切换
2. 引擎切换通过以下两种方式实现：
   - 通过模板配置（ec.resource.name）中的关键字匹配自动选择引擎
   - 通过调用Doctoris服务进行智能引擎选择
3. StarRocks引擎已通过JDBC引擎实现，可通过查询用户数据源名称的方式提交任务

### 存在问题
- 当前AISQL任务无法切换到StarRocks引擎执行
- 用户无法通过配置参数或脚本注释指定使用StarRocks引擎

### 业务价值
- 支持用户灵活选择StarRocks引擎执行AISQL任务
- 与现有Spark/Hive引擎切换机制保持一致
- 扩展AISQL任务的引擎支持能力

## 功能需求

### 功能点1：Runtime参数方式指定StarRocks引擎

**需求描述**：
用户可以在任务提交时，通过runtime参数指定当前AISQL任务使用StarRocks引擎执行。

**实现方式**：
- 新增runtime参数键：`ec.engine.type`
- 参数值：`starrocks`
- 当检测到该参数时，优先切换到StarRocks引擎

**示例**：
```json
{
  "executionCode": "SELECT * FROM table",
  "runType": "aisql",
  "params": {
    "runtime": {
      "ec.engine.type": "starrocks"
    }
  }
}
```

### 功能点2：脚本注释方式指定StarRocks引擎

**需求描述**：
用户可以在AISQL脚本中通过注释配置参数，指定当前任务使用StarRocks引擎执行。

**实现方式**：
- 参考TemplateConfUtils中ec.resource.name的实现方式
- 支持在脚本注释中添加`@set ec.engine.type=starrocks`配置
- 支持多种注释格式（SQL/Python/Scala）

**示例**：
```sql
---@set ec.engine.type=starrocks
SELECT * FROM starrocks_table WHERE dt = '2024-01-01'
```

```python
##@set ec.engine.type=starrocks
SELECT COUNT(*) FROM user_table
```

### 功能点3：Doctoris服务集成

**需求描述**：
当指定使用StarRocks引擎时，需要调用Doctoris服务，传递标识参数表明当前任务必须使用StarRocks引擎。

**实现要求**：
- 扩展现有Doctoris服务调用接口
- 新增参数标识：`forceEngineType` 或 `fixedEngineType`
- 参数值：`starrocks`
- Doctoris服务根据该标识强制返回StarRocks引擎

## 验收标准

### 功能验收
1. 通过runtime参数`ec.engine.type=starrocks`可成功切换到StarRocks引擎
2. 通过脚本注释`@set ec.engine.type=starrocks`可成功切换到StarRocks引擎
3. Runtime参数方式的优先级高于脚本注释方式
4. 调用Doctoris服务时正确传递StarRocks标识参数
5. 任务执行记录中正确记录使用的引擎类型为JDBC
6. 白名单功能正常工作：
   - 白名单为空时，所有用户可以使用StarRocks引擎
   - 白名单配置用户后，只有白名单用户可以使用
   - 白名单配置部门后，只有白名单部门的用户可以使用
   - 不在白名单的用户指定StarRocks引擎时，系统忽略该配置并使用默认引擎

### 性能要求
- 引擎切换逻辑不影响现有任务提交性能
- 参数解析耗时不超过10ms

### 兼容性要求
- 不影响现有Spark和Hive引擎的切换功能
- 功能开关关闭时，行为与上一版本保持一致
- 向后兼容，不修改现有接口签名

### 安全要求
- 验证用户是否有对应StarRocks数据源的访问权限
- 引擎切换不绕过现有权限校验机制

## 功能点4：用户和部门白名单控制

**需求描述**：
为了更安全地控制StarRocks引擎切换功能的使用范围，需要增加用户和部门白名单机制。只有配置在白名单中的用户或部门才能使用StarRocks引擎切换功能。

**实现方式**：
- 新增配置项：`linkis.aisql.starrocks.whitelist.users`，配置允许使用的用户列表（逗号分隔）
- 新增配置项：`linkis.aisql.starrocks.whitelist.departments`，配置允许使用的部门ID列表（逗号分隔）
- 白名单检查逻辑：
  - 如果白名单配置为空，则所有用户都可以使用（兼容现有行为）
  - 如果白名单配置不为空，则只有白名单中的用户或部门才能使用
  - 用户检查：检查提交任务的用户是否在用户白名单中
  - 部门检查：获取提交任务用户的部门ID，检查是否在部门白名单中
  - 满足任一条件即可使用StarRocks引擎

**示例配置**：
```properties
# 允许使用StarRocks引擎的用户（逗号分隔）
linkis.aisql.starrocks.whitelist.users=user1,user2,admin

# 允许使用StarRocks引擎的部门ID（逗号分隔）
linkis.aisql.starrocks.whitelist.departments=dept001,dept002

# 如果两个配置都为空，则所有用户都可以使用
```

**行为说明**：
- 当用户不在白名单中时，即使指定了`ec.engine.type=starrocks`，系统也会忽略该配置，继续使用默认的Spark/Hive引擎选择逻辑
- 日志中会记录白名单检查结果，方便问题排查

## 配置开关

新增配置项：
- `linkis.aisql.starrocks.switch`：StarRocks引擎切换功能开关，默认值：`false`
- `linkis.aisql.default.starrocks.engine.type`：默认StarRocks引擎类型，默认值：`jdbc-4`
- `linkis.aisql.starrocks.template.keys`：StarRocks模板关键字，默认值：`starrocks`
- `linkis.aisql.starrocks.whitelist.users`：用户白名单（逗号分隔），默认值：空（所有用户可用）
- `linkis.aisql.starrocks.whitelist.departments`：部门白名单（逗号分隔），默认值：空（所有部门可用）

## 依赖关系

### 前置依赖
- JDBC引擎插件已支持StarRocks
- StarRocks数据源管理功能已实现

### 影响模块
- linkis-entrance：AISQL任务拦截器
- linkis-entrance-conf：配置管理
- linkis-computation-governance-common：任务协议

## 风险评估

### 技术风险
- **风险**：StarRocks引擎可能不支持某些AISQL语法
- **应对**：在文档中明确说明语法限制，引擎切换失败时给出明确提示

### 兼容性风险
- **风险**：新增配置可能与现有配置冲突
- **应对**：使用独立的配置键名，遵循现有配置命名规范

## 参考资料

- TemplateConfUtils实现：`linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/TemplateConfUtils.scala`
- AISQLTransformInterceptor实现：`linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/AISQLTransformInterceptor.scala`
- JDBCConfiguration配置：`linkis-engineconn-plugins/jdbc/src/main/scala/org/apache/linkis/manager/engineplugin/jdbc/conf/JDBCConfiguration.scala`

## 更新记录

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| v1.0 | 2025-10-27 | AI | 初始版本 |
