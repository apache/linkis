# AISQL任务支持StarRocks引擎类型切换设计文档

## 1. 设计概述

### 1.1 目标
为AISQL类型任务增加StarRocks引擎类型切换支持，通过runtime参数或脚本注释两种方式实现引擎切换，并集成Doctoris服务进行引擎决策。

### 1.2 设计原则
- **最小改动原则**：在现有架构基础上扩展，不修改现有Spark/Hive引擎切换逻辑
- **可配置原则**：通过功能开关控制，开关关闭时相当于回退到上一版本
- **一致性原则**：与现有引擎切换机制保持一致的使用体验

### 1.3 适用范围
- AISQL类型任务提交流程
- 引擎类型切换逻辑
- Doctoris服务调用

## 2. 整体架构

### 2.1 系统架构图
```
用户提交AISQL任务
    ↓
Entrance服务接收
    ↓
AISQLTransformInterceptor拦截器
    ↓
1. 解析runtime参数 (ec.engine.type)
2. 解析脚本注释 (@set ec.engine.type=starrocks)
3. 解析模板配置 (ec.resource.name)
    ↓
判断是否指定StarRocks引擎？
    ↓ Yes
调用Doctoris服务（传递forceEngineType=starrocks）
    ↓
切换EngineTypeLabel为jdbc
    ↓
任务提交到JDBC EngineConn
    ↓
通过StarRocks数据源执行任务
```

### 2.2 处理优先级
```
1. Runtime参数 (ec.engine.type=starrocks)
   ↓ 若未设置
2. 脚本注释 (@set ec.engine.type=starrocks)
   ↓ 若未设置
3. 模板配置 (ec.resource.name包含starrocks关键字)
   ↓ 若未设置
4. Doctoris智能选择（现有逻辑）
   ↓ 若未启用
5. 默认Spark引擎（现有逻辑）
```

## 3. 详细设计

### 3.1 配置设计

#### 3.1.1 新增配置项（EntranceConfiguration.scala）

```scala
// StarRocks引擎切换功能开关
val AISQL_STARROCKS_SWITCH = CommonVars("linkis.aisql.starrocks.switch", false)

// 默认StarRocks引擎类型
val AISQL_DEFAULT_STARROCKS_ENGINE_TYPE =
  CommonVars("linkis.aisql.default.starrocks.engine.type", "jdbc-4")

// StarRocks模板关键字配置
val AISQL_STARROCKS_TEMPLATE_KEYS =
  CommonVars("linkis.aisql.starrocks.template.keys", "starrocks")

// StarRocks数据源名称前缀配置
val AISQL_STARROCKS_DATASOURCE_PREFIX =
  CommonVars("linkis.aisql.starrocks.datasource.prefix", "starrocks_")

// 用户白名单配置
val AISQL_STARROCKS_WHITELIST_USERS =
  CommonVars("linkis.aisql.starrocks.whitelist.users", "")

// 部门白名单配置
val AISQL_STARROCKS_WHITELIST_DEPARTMENTS =
  CommonVars("linkis.aisql.starrocks.whitelist.departments", "")
```

#### 3.1.2 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| linkis.aisql.starrocks.switch | false | StarRocks引擎切换功能开关 |
| linkis.aisql.default.starrocks.engine.type | jdbc-4 | 默认StarRocks引擎类型（jdbc引擎版本） |
| linkis.aisql.starrocks.template.keys | starrocks | 模板关键字，用于识别StarRocks模板 |
| linkis.aisql.starrocks.datasource.prefix | starrocks_ | StarRocks数据源名称前缀 |
| linkis.aisql.starrocks.whitelist.users | 空 | 用户白名单（逗号分隔），为空时所有用户可用 |
| linkis.aisql.starrocks.whitelist.departments | 空 | 部门白名单（逗号分隔），为空时所有部门可用 |

### 3.2 脚本注释解析设计

#### 3.2.1 新增配置键（TemplateConfUtils.scala）

```scala
object TemplateConfUtils {
  // 现有配置
  val confTemplateNameKey = "ec.resource.name"
  val confFixedEngineConnLabelKey = "ec.fixed.sessionId"

  // 新增：引擎类型配置键
  val confEngineTypeKey = "ec.engine.type"
}
```

#### 3.2.2 注释格式支持

支持三种注释格式：
- **SQL/HQL格式**：`---@set ec.engine.type=starrocks`
- **Python/Shell格式**：`##@set ec.engine.type=starrocks`
- **Scala格式**：`///@set ec.engine.type=starrocks`

#### 3.2.3 实现逻辑

利用现有的`getCustomTemplateConfName`方法机制，扩展支持解析`ec.engine.type`配置：

```scala
def getCustomEngineType(code: String, languageType: String): String = {
  val confPattern = languageType.toLowerCase match {
    case x if x.contains("python") || x.contains("shell") =>
      s"##@set\\s+${confEngineTypeKey}\\s*=\\s*([^\\s#]+)".r
    case x if x.contains("scala") =>
      s"///@set\\s+${confEngineTypeKey}\\s*=\\s*([^\\s/]+)".r
    case _ =>
      s"---@set\\s+${confEngineTypeKey}\\s*=\\s*([^\\s-]+)".r
  }

  confPattern.findFirstMatchIn(code) match {
    case Some(m) => m.group(1).trim
    case None => null
  }
}
```

### 3.3 引擎切换逻辑设计

#### 3.3.1 白名单检查设计

在进行引擎切换之前，需要先检查用户是否有权限使用StarRocks引擎：

```scala
/**
 * 检查用户是否在StarRocks白名单中
 * @param submitUser 提交任务的用户
 * @return true表示用户在白名单中或白名单为空（允许所有用户），false表示不在白名单中
 */
private def isUserInStarRocksWhitelist(submitUser: String): Boolean = {
  val whitelistUsers = AISQL_STARROCKS_WHITELIST_USERS.getValue
  val whitelistDepartments = AISQL_STARROCKS_WHITELIST_DEPARTMENTS.getValue

  // 如果白名单都为空，则允许所有用户使用
  if (StringUtils.isBlank(whitelistUsers) && StringUtils.isBlank(whitelistDepartments)) {
    return true
  }

  // 检查用户白名单
  if (StringUtils.isNotBlank(whitelistUsers)) {
    val users = whitelistUsers.split(",").map(_.trim)
    if (users.contains(submitUser)) {
      logger.info(s"User $submitUser is in StarRocks whitelist (user)")
      return true
    }
  }

  // 检查部门白名单
  if (StringUtils.isNotBlank(whitelistDepartments)) {
    val userDepartmentId = EntranceUtils.getUserDepartmentId(submitUser)
    if (StringUtils.isNotBlank(userDepartmentId)) {
      val departments = whitelistDepartments.split(",").map(_.trim)
      if (departments.contains(userDepartmentId)) {
        logger.info(s"User $submitUser (department: $userDepartmentId) is in StarRocks whitelist (department)")
        return true
      }
    }
  }

  logger.warn(s"User $submitUser is not in StarRocks whitelist, will use default engine selection")
  false
}
```

#### 3.3.2 AISQLTransformInterceptor改造

在`AISQLTransformInterceptor.apply()`方法中增加StarRocks引擎处理逻辑：

```scala
override def apply(task: EntranceJob, logAppender: lang.StringBuilder): EntranceJob = {
  // 功能开关检查
  if (!AISQL_STARROCKS_SWITCH.getValue) {
    return applyExistingLogic(task, logAppender) // 现有逻辑
  }

  val jobRequest = task.getJobRequest
  val params = jobRequest.getParams
  val labels = jobRequest.getLabels

  // 1. 检查runtime参数
  val runtimeEngineType = getRuntimeEngineType(params)

  // 2. 检查脚本注释
  val scriptEngineType = if (runtimeEngineType == null) {
    TemplateConfUtils.getCustomEngineType(
      jobRequest.getExecutionCode,
      CodeAndRunTypeUtils.getLanguageTypeByRunType(jobRequest.getRunType)
    )
  } else null

  // 3. 检查模板配置
  val templateEngineType = if (runtimeEngineType == null && scriptEngineType == null) {
    getEngineTypeFromTemplate(jobRequest)
  } else null

  // 确定最终引擎类型
  val targetEngineType = Option(runtimeEngineType)
    .orElse(Option(scriptEngineType))
    .orElse(Option(templateEngineType))
    .orNull

  // 如果指定了starrocks引擎
  if ("starrocks".equalsIgnoreCase(targetEngineType)) {
    // 白名单检查
    if (!isUserInStarRocksWhitelist(jobRequest.getSubmitUser)) {
      logAppender.append(
        LogUtils.generateWarn(
          s"User ${jobRequest.getSubmitUser} is not in StarRocks whitelist, using default engine selection\n"
        )
      )
      // 继续执行现有逻辑（Spark/Hive切换）
      return applyExistingLogic(task, logAppender)
    }

    // 切换到JDBC引擎
    changeToStarRocksEngine(labels, logAppender, params)
  } else {
    // 执行现有逻辑（Spark/Hive切换）
    applyExistingLogic(task, logAppender)
  }

  task
}
```

#### 3.3.2 StarRocks引擎切换实现

```scala
private def changeToStarRocksEngine(
  labels: util.List[Label[_]],
  logAppender: lang.StringBuilder,
  params: util.Map[String, AnyRef]
): Unit = {

  logAppender.append("Switching to StarRocks engine...\n")

  // 1. 移除现有EngineTypeLabel
  val iterator = labels.iterator()
  while (iterator.hasNext) {
    val label = iterator.next()
    if (label.isInstanceOf[EngineTypeLabel]) {
      iterator.remove()
    }
  }

  // 2. 创建JDBC引擎Label
  val jdbcEngineType = AISQL_DEFAULT_STARROCKS_ENGINE_TYPE.getValue
  val Array(engine, version) = jdbcEngineType.split("-", 2)
  val jdbcLabel = new EngineTypeLabel()
  jdbcLabel.setEngineType(engine)
  jdbcLabel.setVersion(version)
  labels.add(jdbcLabel)

  // 3. 添加StarRocks标识到runtime参数（用于后续JDBC引擎识别）
  val runtimeMap = params.getOrDefault(
    JobRequestConstants.JOB_REQUEST_RUNTIME_PARAMS,
    new util.HashMap[String, AnyRef]()
  ).asInstanceOf[util.Map[String, AnyRef]]

  runtimeMap.put("linkis.jdbc.engine.type", "starrocks")
  params.put(JobRequestConstants.JOB_REQUEST_RUNTIME_PARAMS, runtimeMap)

  logAppender.append(s"Engine switched to StarRocks (JDBC engine: $jdbcEngineType)\n")
}
```

#### 3.3.3 Runtime参数获取

```scala
private def getRuntimeEngineType(params: util.Map[String, AnyRef]): String = {
  if (params == null) return null

  val runtimeParams = params.get(JobRequestConstants.JOB_REQUEST_RUNTIME_PARAMS)
  if (runtimeParams == null) return null

  runtimeParams.asInstanceOf[util.Map[String, AnyRef]]
    .get(TemplateConfUtils.confEngineTypeKey) match {
      case null => null
      case value => value.toString
    }
}
```

#### 3.3.4 模板配置获取

```scala
private def getEngineTypeFromTemplate(jobRequest: JobRequest): String = {
  val templateName = TemplateConfUtils.getCustomTemplateConfName(
    jobRequest.getExecutionCode,
    CodeAndRunTypeUtils.getLanguageTypeByRunType(jobRequest.getRunType)
  )

  if (templateName == null) return null

  // 检查模板名称是否包含StarRocks关键字
  val starrocksKeys = AISQL_STARROCKS_TEMPLATE_KEYS.getValue.split(",")
  if (starrocksKeys.exists(key => templateName.toLowerCase.contains(key.toLowerCase))) {
    "starrocks"
  } else {
    null
  }
}
```

### 3.4 Doctoris服务集成设计

#### 3.4.1 接口扩展（EntranceUtils.scala）

修改`getDynamicEngineType`方法，支持传递强制引擎类型参数：

```scala
def getDynamicEngineType(
  sql: String,
  logAppender: lang.StringBuilder,
  forceEngineType: String = null  // 新增：强制引擎类型参数
): String = {

  if (!EntranceConfiguration.AI_SQL_DYNAMIC_ENGINE_SWITCH) {
    if (forceEngineType != null) return forceEngineType
    return defaultEngineType
  }

  val params = new util.HashMap[String, AnyRef]()
  params.put("sql", sql)
  params.put("highStability", "")
  params.put("queueResourceUsage", "")

  // 新增：添加强制引擎类型标识
  if (forceEngineType != null && forceEngineType.nonEmpty) {
    params.put("forceEngineType", forceEngineType)
    logAppender.append(s"Force engine type: $forceEngineType\n")
  }

  val request = DoctorEngineRequest(
    EntranceConfiguration.LINKIS_SYSTEM_NAME,
    EntranceConfiguration.DOCTOR_CLUSTER,
    sql,
    params
  )

  val response = callDoctorService(request, logAppender)
  response.result
}
```

#### 3.4.2 调用时机

在`AISQLTransformInterceptor`中，当检测到需要使用StarRocks引擎时：

```scala
if ("starrocks".equalsIgnoreCase(targetEngineType)) {
  // 调用Doctoris服务，传递强制引擎类型
  val confirmedEngineType = EntranceUtils.getDynamicEngineType(
    jobRequest.getExecutionCode,
    logAppender,
    forceEngineType = "starrocks"  // 传递强制参数
  )

  // 切换到JDBC引擎
  changeToStarRocksEngine(labels, logAppender, params)
}
```

### 3.5 数据流设计

#### 3.5.1 任务提交数据流

```
1. 用户提交任务
   {
     "executionCode": "---@set ec.engine.type=starrocks\nSELECT * FROM table",
     "runType": "aisql",
     "params": {
       "runtime": {}
     }
   }

2. AISQLTransformInterceptor处理
   - 解析脚本注释，提取 ec.engine.type=starrocks
   - 检查功能开关：linkis.aisql.starrocks.switch = true
   - 决定切换到StarRocks引擎

3. 调用Doctoris服务
   POST /api/v1/external/engine/diagnose
   Body: {
     "sql": "SELECT * FROM table",
     "forceEngineType": "starrocks"
   }
   Response: {
     "engine": "starrocks",
     "reason": "Force engine type specified"
   }

4. 修改JobRequest
   - 移除现有EngineTypeLabel
   - 添加新的EngineTypeLabel(engine=jdbc, version=4)
   - 添加runtime参数：linkis.jdbc.engine.type=starrocks

5. 任务路由到JDBC EngineConn
   - JDBC引擎识别linkis.jdbc.engine.type=starrocks
   - 查询用户的StarRocks数据源
   - 通过JDBC连接执行SQL
```

#### 3.5.2 引擎标签变更

```
原始Label：
[EngineTypeLabel(engineType=spark, version=3.4.4)]

↓ 检测到 ec.engine.type=starrocks

新Label：
[EngineTypeLabel(engineType=jdbc, version=4)]

+ Runtime参数：
{
  "linkis.jdbc.engine.type": "starrocks"
}
```

## 4. 接口设计

### 4.1 内部接口

#### 4.1.1 TemplateConfUtils新增方法

```scala
/**
 * 从脚本代码中提取引擎类型配置
 * @param code 脚本代码
 * @param languageType 语言类型
 * @return 引擎类型，如"starrocks"、"spark"、"hive"，未找到返回null
 */
def getCustomEngineType(code: String, languageType: String): String
```

#### 4.1.2 AISQLTransformInterceptor新增私有方法

```scala
/**
 * 从runtime参数中获取引擎类型
 */
private def getRuntimeEngineType(params: util.Map[String, AnyRef]): String

/**
 * 从模板配置中获取引擎类型
 */
private def getEngineTypeFromTemplate(jobRequest: JobRequest): String

/**
 * 切换到StarRocks引擎
 */
private def changeToStarRocksEngine(
  labels: util.List[Label[_]],
  logAppender: lang.StringBuilder,
  params: util.Map[String, AnyRef]
): Unit
```

#### 4.1.3 EntranceUtils方法签名变更

```scala
/**
 * 获取动态引擎类型
 * @param sql SQL语句
 * @param logAppender 日志追加器
 * @param forceEngineType 强制引擎类型（可选），如"starrocks"
 * @return 引擎类型
 */
def getDynamicEngineType(
  sql: String,
  logAppender: lang.StringBuilder,
  forceEngineType: String = null
): String
```

### 4.2 外部接口

#### 4.2.1 任务提交接口（无变更）

保持现有任务提交接口不变，通过扩展参数支持新功能：

```
POST /api/rest_j/v1/entrance/submit

Request Body:
{
  "executionCode": "SELECT * FROM table",
  "runType": "aisql",
  "params": {
    "runtime": {
      "ec.engine.type": "starrocks"  // 新增参数
    }
  }
}
```

#### 4.2.2 Doctoris服务接口

```
POST {DOCTOR_URL}/api/v1/external/engine/diagnose

Request:
{
  "appId": "linkis",
  "cluster": "default",
  "sql": "SELECT * FROM table",
  "params": {
    "forceEngineType": "starrocks"  // 新增参数
  }
}

Response:
{
  "code": 0,
  "data": {
    "engine": "starrocks",
    "reason": "Force engine type specified",
    "duration": 50
  }
}
```

## 5. 异常处理

### 5.1 异常场景

| 异常场景 | 处理策略 |
|----------|----------|
| StarRocks功能开关关闭 | 忽略StarRocks配置，执行现有Spark/Hive切换逻辑 |
| 无效的引擎类型值 | 记录警告日志，使用默认引擎类型 |
| Doctoris服务调用失败 | 记录错误日志，降级到默认引擎类型 |
| JDBC引擎不可用 | 任务提交失败，返回明确错误信息 |
| StarRocks数据源不存在 | 任务执行失败，提示配置数据源 |

### 5.2 日志规范

```scala
// INFO级别：关键流程节点
logger.info(s"AISQL task switches to StarRocks engine for user $username")

// WARN级别：降级处理
logger.warn(s"Invalid engine type specified: $engineType, fallback to default")

// ERROR级别：异常错误
logger.error(s"Failed to switch to StarRocks engine for task $taskId", exception)

// DEBUG级别：详细调试信息
logger.debug(s"Parsing engine type from script: $code")
```

## 6. 测试设计

### 6.1 单元测试

#### 6.1.1 TemplateConfUtils测试

```scala
class TemplateConfUtilsTest {

  test("extract starrocks engine type from SQL comment") {
    val code = "---@set ec.engine.type=starrocks\nSELECT * FROM table"
    val result = TemplateConfUtils.getCustomEngineType(code, "sql")
    assert(result == "starrocks")
  }

  test("extract starrocks engine type from Python comment") {
    val code = "##@set ec.engine.type=starrocks\nSELECT COUNT(*) FROM table"
    val result = TemplateConfUtils.getCustomEngineType(code, "python")
    assert(result == "starrocks")
  }

  test("return null when no engine type specified") {
    val code = "SELECT * FROM table"
    val result = TemplateConfUtils.getCustomEngineType(code, "sql")
    assert(result == null)
  }
}
```

#### 6.1.2 AISQLTransformInterceptor测试

```scala
class AISQLTransformInterceptorTest {

  test("switch to StarRocks via runtime parameter") {
    val jobRequest = createJobRequest(
      code = "SELECT * FROM table",
      runtime = Map("ec.engine.type" -> "starrocks")
    )
    val task = new EntranceJob()
    task.setJobRequest(jobRequest)

    interceptor.apply(task, new StringBuilder())

    val engineLabel = getEngineLabel(task)
    assert(engineLabel.getEngineType == "jdbc")
  }

  test("switch to StarRocks via script comment") {
    val jobRequest = createJobRequest(
      code = "---@set ec.engine.type=starrocks\nSELECT * FROM table"
    )
    val task = new EntranceJob()
    task.setJobRequest(jobRequest)

    interceptor.apply(task, new StringBuilder())

    val engineLabel = getEngineLabel(task)
    assert(engineLabel.getEngineType == "jdbc")
  }

  test("runtime parameter takes precedence over script comment") {
    val jobRequest = createJobRequest(
      code = "---@set ec.engine.type=spark\nSELECT * FROM table",
      runtime = Map("ec.engine.type" -> "starrocks")
    )
    val task = new EntranceJob()
    task.setJobRequest(jobRequest)

    interceptor.apply(task, new StringBuilder())

    val engineLabel = getEngineLabel(task)
    assert(engineLabel.getEngineType == "jdbc") // 使用runtime的starrocks
  }
}
```

### 6.2 集成测试

#### 6.2.1 端到端测试用例

```bash
# 测试1：通过runtime参数切换StarRocks引擎
curl -X POST http://localhost:9001/api/rest_j/v1/entrance/submit \
  -H "Content-Type: application/json" \
  -H "Token-User: testuser" \
  -d '{
    "executionCode": "SELECT * FROM starrocks_table LIMIT 10",
    "runType": "aisql",
    "params": {
      "runtime": {
        "ec.engine.type": "starrocks"
      }
    }
  }'

# 预期结果：任务成功提交，引擎类型为jdbc，执行成功

# 测试2：通过脚本注释切换StarRocks引擎
curl -X POST http://localhost:9001/api/rest_j/v1/entrance/submit \
  -H "Content-Type: application/json" \
  -H "Token-User: testuser" \
  -d '{
    "executionCode": "---@set ec.engine.type=starrocks\nSELECT COUNT(*) FROM user_table",
    "runType": "aisql",
    "params": {}
  }'

# 预期结果：任务成功提交，引擎类型为jdbc，执行成功

# 测试3：功能开关关闭
# 配置：linkis.aisql.starrocks.switch=false
curl -X POST http://localhost:9001/api/rest_j/v1/entrance/submit \
  -H "Content-Type: application/json" \
  -H "Token-User: testuser" \
  -d '{
    "executionCode": "---@set ec.engine.type=starrocks\nSELECT * FROM table",
    "runType": "aisql",
    "params": {}
  }'

# 预期结果：忽略StarRocks配置，使用默认Spark引擎
```

### 6.3 性能测试

测试指标：
- 参数解析耗时 < 10ms
- 引擎切换逻辑耗时 < 5ms
- 任务提交总耗时增加 < 20ms

## 7. 部署方案

### 7.1 部署步骤

1. **编译打包**
   ```bash
   mvn clean package -Dmaven.test.skip=true
   ```

2. **停止Entrance服务**
   ```bash
   sh sbin/linkis-daemon.sh stop entrance
   ```

3. **备份原有文件**
   ```bash
   cp lib/linkis-spring-cloud-services/linkis-entrance.jar \
      lib/linkis-spring-cloud-services/linkis-entrance.jar.bak
   ```

4. **替换新文件**
   ```bash
   cp linkis-computation-governance/linkis-entrance/target/linkis-entrance.jar \
      lib/linkis-spring-cloud-services/
   ```

5. **配置文件修改**（linkis-entrance.properties）
   ```properties
   # 启用StarRocks引擎切换功能
   linkis.aisql.starrocks.switch=true

   # StarRocks引擎类型（jdbc-4表示jdbc引擎版本4）
   linkis.aisql.default.starrocks.engine.type=jdbc-4

   # StarRocks模板关键字
   linkis.aisql.starrocks.template.keys=starrocks
   ```

6. **启动Entrance服务**
   ```bash
   sh sbin/linkis-daemon.sh start entrance
   ```

7. **验证功能**
   ```bash
   # 查看日志确认配置加载
   tail -f logs/linkis-entrance-gc.log | grep "starrocks"

   # 提交测试任务
   sh bin/linkis-cli -engineType aisql -code "---@set ec.engine.type=starrocks\nSELECT 1" -runtimeMap ec.engine.type=starrocks
   ```

### 7.2 回滚方案

如果部署后出现问题，执行以下回滚步骤：

1. **停止服务**
   ```bash
   sh sbin/linkis-daemon.sh stop entrance
   ```

2. **恢复备份文件**
   ```bash
   mv lib/linkis-spring-cloud-services/linkis-entrance.jar.bak \
      lib/linkis-spring-cloud-services/linkis-entrance.jar
   ```

3. **配置文件回滚**
   ```properties
   # 关闭StarRocks功能
   linkis.aisql.starrocks.switch=false
   ```

4. **启动服务**
   ```bash
   sh sbin/linkis-daemon.sh start entrance
   ```

### 7.3 灰度发布方案

1. **阶段1：内部测试环境**（1-2天）
   - 部署到测试环境
   - 开启功能开关
   - 内部人员测试验证

2. **阶段2：生产环境灰度**（3-5天）
   - 仅对特定用户组开启功能
   - 通过用户白名单控制
   - 监控任务成功率和性能指标

3. **阶段3：全量发布**（7天后）
   - 确认无问题后全量开启
   - 持续监控一周

## 8. 监控告警

### 8.1 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| starrocks_engine_switch_count | StarRocks引擎切换次数 | - |
| starrocks_engine_switch_success_rate | 切换成功率 | < 95% |
| starrocks_task_execution_time | 任务执行时间 | > 60s (P95) |
| starrocks_task_fail_count | 任务失败次数 | > 10次/小时 |
| doctoris_call_timeout_count | Doctoris调用超时次数 | > 5次/小时 |

### 8.2 日志监控

关键日志关键字：
- `Switching to StarRocks engine`
- `Force engine type: starrocks`
- `Failed to switch to StarRocks engine`
- `Invalid engine type specified`

## 9. 风险评估与应对

### 9.1 技术风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| StarRocks数据源配置错误 | 任务执行失败 | 中 | 提供详细的错误提示和配置文档 |
| JDBC连接池资源耗尽 | 后续任务阻塞 | 低 | 配置合理的连接池大小和超时时间 |
| Doctoris服务不稳定 | 引擎选择失败 | 中 | 实现降级逻辑，服务异常时使用默认配置 |
| 配置解析性能问题 | 任务提交变慢 | 低 | 优化正则表达式，添加缓存机制 |

### 9.2 业务风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| 用户误配置引擎类型 | 任务失败或结果错误 | 中 | 添加引擎类型有效性校验 |
| 现有任务受影响 | 兼容性问题 | 低 | 功能开关默认关闭，逐步开启 |
| 文档不完善 | 用户使用困难 | 中 | 编写详细使用文档和示例 |

## 10. 兼容性说明

### 10.1 向后兼容
- 功能开关默认关闭（`linkis.aisql.starrocks.switch=false`）
- 不影响现有Spark/Hive引擎切换逻辑
- 不修改现有接口签名和返回结构

### 10.2 版本依赖
- 最低支持版本：Linkis 1.17.0
- JDBC引擎插件版本：jdbc-4
- StarRocks数据源管理模块已部署

### 10.3 升级影响
- 升级时无需修改现有任务配置
- 升级后需手动开启功能开关
- 需要配置StarRocks相关参数

## 11. 文档清单

### 11.1 开发文档
- [x] 需求文档：`docs/1.17.0/requirements/aisql-starrocks-engine-switch.md`
- [x] 设计文档：`docs/1.17.0/design/aisql-starrocks-engine-switch-design.md`

### 11.2 用户文档（待补充）
- [ ] 用户使用指南：如何配置和使用StarRocks引擎
- [ ] 配置参数说明：所有相关配置项的详细说明
- [ ] 常见问题FAQ：常见问题和解决方案

### 11.3 运维文档（待补充）
- [ ] 部署指南：详细部署步骤和验证方法
- [ ] 监控运维手册：监控指标和告警处理
- [ ] 故障排查手册：常见故障和排查方法

## 12. 变更清单

### 12.1 新增文件
- `docs/1.17.0/requirements/aisql-starrocks-engine-switch.md` - 需求文档
- `docs/1.17.0/design/aisql-starrocks-engine-switch-design.md` - 设计文档

### 12.2 修改文件
- `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/conf/EntranceConfiguration.scala` - 新增配置项
- `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/TemplateConfUtils.scala` - 新增引擎类型解析方法
- `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/AISQLTransformInterceptor.scala` - 新增StarRocks切换逻辑
- `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/utils/EntranceUtils.scala` - 扩展Doctoris调用接口

### 12.3 数据库变更
无数据库变更

### 12.4 配置文件变更
- `conf/linkis-entrance.properties` - 新增StarRocks相关配置项

## 13. 质量检查清单

- [ ] 代码符合项目规范（Java/Scala编码规范）
- [ ] 异常处理完整（try-catch、日志记录）
- [ ] 日志记录充分（INFO/WARN/ERROR/DEBUG）
- [ ] 单元测试覆盖（核心逻辑测试覆盖率>80%）
- [ ] 配置开关完整（功能开关、默认值配置）
- [ ] 向后兼容性检查（不影响现有功能）
- [ ] 性能测试通过（满足性能要求）
- [ ] 安全性检查（权限验证、参数校验）
- [ ] 文档完整性（需求、设计、用户、运维文档）

## 14. 附录

### 14.1 相关代码文件路径

| 文件 | 路径 |
|------|------|
| EntranceConfiguration | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/conf/EntranceConfiguration.scala |
| TemplateConfUtils | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/TemplateConfUtils.scala |
| AISQLTransformInterceptor | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/interceptor/impl/AISQLTransformInterceptor.scala |
| EntranceUtils | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/utils/EntranceUtils.scala |
| JDBCConfiguration | linkis-engineconn-plugins/jdbc/src/main/scala/org/apache/linkis/manager/engineplugin/jdbc/conf/JDBCConfiguration.scala |

### 14.2 参考资料
- Apache Linkis官方文档：https://linkis.apache.org
- StarRocks官方文档：https://docs.starrocks.io
- JDBC标准文档：https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/

---

**文档版本**：v1.0
**创建日期**：2025-10-27
**作者**：AI
**审核状态**：待审核
