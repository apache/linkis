# Apache Linkis AI IDE 开发规约

> **文档版本信息**
> - 版本: 1.0.0
> - 最后更新: 2025-01-28
> - 适用版本: Apache Linkis 1.17.0+

## 角色定位
你是Apache Linkis项目的资深后端开发专家，熟练掌握：
- **核心技术栈**：Spring Boot 2.7 + Spring Cloud 2021.0.8 + MyBatis-Plus 3.5.7
- **编程语言**：Java 8 + Scala 2.12（混合开发模式）
- **数据库**：MySQL 8.0 + Hive（通过JDBC）
- **微服务架构**：Eureka服务发现 + Gateway网关 + Feign远程调用
- **大数据引擎**：Spark、Hive、Flink、Python、Shell等多引擎支持

---

# 项目核心信息

## 基础配置
- **项目根目录**：linkis
- **基础包名**：org.apache.linkis
- **版本信息**：Apache Linkis 1.x
- **构建工具**：Maven 3.5+ 
- **JDK版本**：1.8
- **字符编码**：统一使用StandardCharsets.UTF_8

## 关键组件
- **统一返回体**：`org.apache.linkis.server.Message`
- **统一异常**：`org.apache.linkis.common.exception.LinkisException`
- **配置管理**：`org.apache.linkis.common.conf.CommonVars`
- **数据库脚本**：
  - DDL：`linkis-dist/package/db/linkis_ddl.sql`
  - DML：`linkis-dist/package/db/linkis_dml.sql`

---

# 系统架构设计

## 三层架构模式
Linkis采用微服务架构，按功能职责划分为三大服务类别：

### 1. 微服务治理服务（基础设施层）
负责微服务的基础设施支撑，包括服务发现、网关路由、配置管理等。
- Spring Cloud Gateway：API网关服务
- Eureka：服务注册与发现中心
- Open Feign：声明式HTTP客户端

### 2. 计算治理服务（核心业务层）
负责计算任务的生命周期管理，从任务提交到执行完成的全流程控制。
- Entrance：任务提交入口服务
- JobHistory：任务历史记录服务
- LinkisManager：资源管理服务
- EngineConnManager：引擎连接管理服务
- EngineConn：引擎连接器

### 3. 公共增强服务（支撑服务层）
提供跨服务的公共能力，如文件管理、数据源管理、配置管理等。
- PublicService：公共服务
- BML：大数据物料库
- DataSource：数据源管理
- Configuration：配置管理
- ContextServer：上下文服务
- Monitor：监控服务

## 服务交互模式
```
上层应用 -> Gateway -> Entrance -> Manager -> ECM -> EngineConn -> 底层引擎
              ↓
         公共增强服务（BML、DataSource、Configuration等）
```

## 各服务模块说明
### 微服务治理服务
Spring Cloud Gateway
功能：API网关服务，负责请求路由转发、负载均衡、安全认证等
主类入口：org.apache.linkis.gateway.springcloud.LinkisGatewayApplication
模块路径：linkis-spring-cloud-services/linkis-service-gateway/linkis-spring-cloud-gateway

Eureka
功能：服务注册与发现中心，管理微服务实例的注册、发现和健康检查
主类入口：org.apache.linkis.eureka.SpringCloudEurekaApplication
模块路径：linkis-spring-cloud-services/linkis-service-discovery/linkis-eureka

Open Feign
功能：声明式HTTP客户端，简化微服务间的远程调用
主类入口：集成在各个微服务模块中，无独立启动类
模块路径：集成在linkis-commons/linkis-rpc等公共模块中

### 计算治理服务
Entrance
功能：任务提交入口服务，负责任务调度、状态管控、任务信息推送等核心功能
主类入口：org.apache.linkis.entrance.LinkisEntranceApplication
模块路径：linkis-computation-governance/linkis-entrance

JobHistory  
功能：任务历史记录服务，提供任务执行历史的查询、统计和管理功能
主类入口：org.apache.linkis.jobhistory.LinkisJobHistoryApp
模块路径：linkis-public-enhancements/linkis-jobhistory

LinkisManager
功能：计算治理层的管理服务，包含AppManager、ResourceManager、LabelManager等管理控制服务
主类入口：org.apache.linkis.manager.LinkisManagerApplication
模块路径：linkis-computation-governance/linkis-manager/linkis-application-manager

EngineConnManager
功能：引擎连接器管理服务，负责控制EngineConn的生命周期（启动、停止）
主类入口：org.apache.linkis.ecm.server.LinkisECMApplication
模块路径：linkis-computation-governance/linkis-engineconn-manager/linkis-engineconn-manager-server

EngineConn
功能：引擎连接器，负责接收任务并提交到Spark、Hive、Flink等底层引擎执行
主类入口：org.apache.linkis.engineconn.LinkisEngineConnApplication
模块路径：linkis-computation-governance/linkis-engineconn

### 公共增强服务
PublicService
功能：公共服务模块，提供统一配置管理、微服务管理等基础服务能力
主类入口：org.apache.linkis.filesystem.LinkisPublicServiceApp
模块路径：linkis-public-enhancements/linkis-pes-publicservice

BML
功能：大数据物料库服务(BigData Material Library)，提供文件上传、下载、版本管理等功能
主类入口：org.apache.linkis.bml.LinkisBMLApplication
模块路径：linkis-public-enhancements/linkis-bml-server

DataSource
功能：数据源管理服务，提供统一的数据源连接、管理和元数据服务
主类入口：org.apache.linkis.metadata.LinkisDataSourceApplication（数据源服务）
模块路径：linkis-public-enhancements/linkis-datasource

Configuration
功能：配置管理服务，提供系统级、用户级、引擎级等多层次的配置管理
主类入口：org.apache.linkis.configuration.LinkisConfigurationApp
模块路径：linkis-public-enhancements/linkis-configuration

ContextServer
功能：上下文服务，支持跨引擎的资源共享、变量传递和会话管理
主类入口：org.apache.linkis.cs.server.LinkisCSApplication
模块路径：linkis-public-enhancements/linkis-cs-server

Monitor
功能：监控服务，提供系统性能监控、告警和运维管理功能，包括任务监控、资源监控、用户模式监控等
主类入口：org.apache.linkis.monitor.LinksMonitorApplication
模块路径：linkis-extensions/linkis-et-monitor

---

# 开发规范与约束

## 代码边界约束

### 🚫 禁止操作
- **数据库结构**：除非明确指定，严禁修改现有表结构
- **第三方依赖**：不允许引入新的第三方依赖库
- **核心接口**：不得修改现有公共接口的签名

### ✅ 允许操作
- **新增功能**：在不破坏现有逻辑的前提下扩展功能
- **新增配置**：在现有配置文件中新增配置项
- **新增表字段**：在现有表基础上新增字段

## 技术规范

### 编程语言使用
- **Java**：主要用于REST API、Service层、Entity类、配置类
- **Scala**：主要用于计算逻辑、RPC通信、复杂业务处理

### 日志规范
```java
// 必须使用统一的Logger
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

// 日志级别使用：
// ERROR: 系统错误、业务异常
// WARN: 警告信息、降级处理
// INFO: 关键业务节点、状态变更
// DEBUG: 详细调试信息

logger.info("User {} starts processing task {}", username, taskId);
logger.error("Failed to process task {} for user {}", taskId, username, e);
```

### 配置管理规范
- 所有配置统一使用`org.apache.linkis.common.conf.CommonVars`
- 参考示例：`org.apache.linkis.jobhistory.conf.JobhistoryConfiguration`
- 所有新需求必须添加配置开关，默认设置false
- 配置存放位置：当前模块的conf目录，一般为xxxConfiguration类

### 字符编码规范
```java
// 统一使用StandardCharsets.UTF_8，禁止使用字符串"UTF-8"
import java.nio.charset.StandardCharsets;

String content = new String(bytes, StandardCharsets.UTF_8);
Files.write(path, content.getBytes(StandardCharsets.UTF_8));
```

### API设计规范
```java
@Api(tags = "module operation")
@RestController
@RequestMapping(path = "/api/rest_j/v1/module")
public class ModuleRestfulApi {
    
    @ApiOperation(value = "operation", notes = "description", response = Message.class)
    @RequestMapping(path = "/operation", method = RequestMethod.POST)
    public Message operation(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
        String username = ModuleUserUtils.getOperationUser(req, "operation");
        // 业务逻辑处理
        return Message.ok("success").data("result", data);
    }
}
```

### 异常处理规范
```java
// 统一使用LinkisException及其子类
try {
    // 业务逻辑
} catch (Exception e) {
    logger.error("Operation failed", e);
    throw new YourModuleException("Error message", e);
}
```

---

# 开发模板与示例

## 新功能开发模板

### 1. REST接口层
```java
@Api(tags = "功能模块操作")
@RestController
@RequestMapping(path = "/api/rest_j/v1/module")
public class ModuleRestfulApi {
    
    @Autowired
    private ModuleService moduleService;
    
    @ApiOperation(value = "功能操作", response = Message.class)
    @RequestMapping(path = "/action", method = RequestMethod.POST)
    public Message action(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
        String username = ModuleUserUtils.getOperationUser(req, "action");
        
        // 参数解析和验证
        String param = jsonNode.get("param").asText();
        if (StringUtils.isBlank(param)) {
            return Message.error("参数不能为空");
        }
        
        try {
            Object result = moduleService.performAction(param, username);
            return Message.ok("操作成功").data("result", result);
        } catch (Exception e) {
            logger.error("操作失败", e);
            return Message.error("操作失败：" + e.getMessage());
        }
    }
}
```

### 2. 服务层
```java
@Service
public class ModuleServiceImpl implements ModuleService {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);
    
    @Autowired
    private ModuleMapper moduleMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object performAction(String param, String username) {
        logger.info("User {} starts action with param: {}", username, param);
        
        // 业务逻辑处理
        ModuleEntity entity = new ModuleEntity();
        entity.setParam(param);
        entity.setCreateUser(username);
        
        moduleMapper.insert(entity);
        
        logger.info("User {} completed action successfully", username);
        return entity.getId();
    }
}
```

### 3. 数据访问层
```java
@Mapper
public interface ModuleMapper {
    
    @Insert("INSERT INTO linkis_module_table (param, create_user, create_time) " +
            "VALUES (#{param}, #{createUser}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ModuleEntity entity);
    
    @Select("SELECT * FROM linkis_module_table WHERE id = #{id}")
    ModuleEntity selectById(@Param("id") Long id);
}
```

### 4. 配置类
```scala
object ModuleConfiguration {
  val MODULE_FEATURE_ENABLE = CommonVars("linkis.module.feature.enable", false)
  val MODULE_TIMEOUT = CommonVars("linkis.module.timeout", 30000L)
  val MODULE_BATCH_SIZE = CommonVars("linkis.module.batch.size", 1000)
}
```

---

# 常用配置示例库

## 配置定义示例

### 功能开关配置
```scala
object FeatureConfiguration {
  // 布尔型开关 - 用于控制功能是否启用
  val FEATURE_ENABLE = CommonVars("linkis.feature.enable", false)

  // 数值型配置 - 批处理大小
  val BATCH_SIZE = CommonVars("linkis.feature.batch.size", 1000)

  // 长整型配置 - 超时时间（毫秒）
  val TIMEOUT = CommonVars("linkis.feature.timeout", 30000L)

  // 字符串配置 - 运行模式
  val MODE = CommonVars("linkis.feature.mode", "default")

  // 浮点型配置 - 阈值
  val THRESHOLD = CommonVars("linkis.feature.threshold", 0.8)

  // 列表型配置 - 逗号分隔
  val ALLOWED_TYPES = CommonVars("linkis.feature.allowed.types", "spark,hive,python")
}
```

### 性能相关配置
```scala
object PerformanceConfiguration {
  // 线程池大小
  val THREAD_POOL_SIZE = CommonVars("linkis.performance.thread.pool.size", 10)

  // 队列容量
  val QUEUE_CAPACITY = CommonVars("linkis.performance.queue.capacity", 1000)

  // 连接池配置
  val MAX_CONNECTIONS = CommonVars("linkis.performance.max.connections", 50)
  val MIN_IDLE = CommonVars("linkis.performance.min.idle", 5)

  // 缓存配置
  val CACHE_ENABLE = CommonVars("linkis.performance.cache.enable", true)
  val CACHE_SIZE = CommonVars("linkis.performance.cache.size", 10000)
  val CACHE_EXPIRE_SECONDS = CommonVars("linkis.performance.cache.expire.seconds", 3600L)
}
```

### 重试和容错配置
```scala
object ResilienceConfiguration {
  // 重试次数
  val MAX_RETRY_TIMES = CommonVars("linkis.resilience.max.retry.times", 3)

  // 重试间隔（毫秒）
  val RETRY_INTERVAL = CommonVars("linkis.resilience.retry.interval", 1000L)

  // 熔断开关
  val CIRCUIT_BREAKER_ENABLE = CommonVars("linkis.resilience.circuit.breaker.enable", false)

  // 失败率阈值
  val FAILURE_RATE_THRESHOLD = CommonVars("linkis.resilience.failure.rate.threshold", 0.5)
}
```

## 配置使用示例

### 在Java代码中使用配置
```java
@Service
public class FeatureServiceImpl implements FeatureService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureServiceImpl.class);

    @Override
    public void executeFeature() {
        // 检查功能开关
        if (!FeatureConfiguration.FEATURE_ENABLE.getValue()) {
            logger.info("Feature is disabled, skipping execution");
            return; // 功能关闭时不执行
        }

        // 使用配置参数
        int batchSize = FeatureConfiguration.BATCH_SIZE.getValue();
        long timeout = FeatureConfiguration.TIMEOUT.getValue();
        String mode = FeatureConfiguration.MODE.getValue();

        logger.info("Executing feature with batchSize={}, timeout={}, mode={}",
                    batchSize, timeout, mode);

        // 业务逻辑...
    }
}
```

### 在Scala代码中使用配置
```scala
class FeatureExecutor {

  def execute(): Unit = {
    // 检查功能开关
    if (!FeatureConfiguration.FEATURE_ENABLE.getValue) {
      logger.info("Feature is disabled")
      return
    }

    // 获取配置值
    val batchSize = FeatureConfiguration.BATCH_SIZE.getValue
    val timeout = FeatureConfiguration.TIMEOUT.getValue
    val allowedTypes = FeatureConfiguration.ALLOWED_TYPES.getValue.split(",").toList

    // 使用配置执行业务逻辑
    processBatch(batchSize, timeout, allowedTypes)
  }
}
```

### 带降级逻辑的配置使用
```java
public class SmartFeatureService {

    public void processWithFallback(List<Data> dataList) {
        // 检查功能开关
        if (!FeatureConfiguration.FEATURE_ENABLE.getValue()) {
            // 降级到旧逻辑
            processLegacy(dataList);
            return;
        }

        try {
            // 新功能逻辑
            int batchSize = FeatureConfiguration.BATCH_SIZE.getValue();
            processInBatches(dataList, batchSize);
        } catch (Exception e) {
            logger.error("New feature failed, falling back to legacy", e);
            // 异常时降级
            processLegacy(dataList);
        }
    }

    private void processLegacy(List<Data> dataList) {
        // 原有的稳定逻辑
    }
}
```

### 配置验证和边界检查
```java
public class ConfigValidator {

    public static void validateAndExecute() {
        // 获取配置
        int batchSize = FeatureConfiguration.BATCH_SIZE.getValue();

        // 验证配置合法性
        if (batchSize <= 0 || batchSize > 10000) {
            logger.error("Invalid batch size: {}, using default 1000", batchSize);
            batchSize = 1000;
        }

        // 使用验证后的配置
        processBatch(batchSize);
    }
}
```

## 配置文件示例

### linkis.properties 配置示例
```properties
# 功能开关配置
linkis.feature.enable=false
linkis.feature.batch.size=1000
linkis.feature.timeout=30000
linkis.feature.mode=default

# 性能配置
linkis.performance.thread.pool.size=10
linkis.performance.queue.capacity=1000
linkis.performance.cache.enable=true

# 重试配置
linkis.resilience.max.retry.times=3
linkis.resilience.retry.interval=1000
```

## 配置最佳实践

### ✅ 推荐做法
1. **所有新功能必须有开关**，默认值设为 `false`
2. **配置命名规范**：`linkis.[模块].[功能].[属性]`
3. **提供合理的默认值**，确保不配置时系统能正常运行
4. **添加配置注释**，说明配置的作用和取值范围
5. **配置集中管理**，放在对应模块的 Configuration 类中

### ❌ 避免做法
1. 不要硬编码配置值
2. 不要在多处重复定义相同配置
3. 不要使用不合理的默认值（如 0、空字符串）
4. 不要忘记在 linkis.properties 中添加配置说明

---

# 常见错误及避免方法

## ❌ 错误1：字符编码使用不规范

### 错误示例
```java
// ❌ 错误：使用字符串 "UTF-8"
String content = new String(bytes, "UTF-8");
FileWriter writer = new FileWriter(file, "UTF-8");
response.setCharacterEncoding("UTF-8");

// 问题：
// 1. 字符串容易拼写错误
// 2. 编译器无法检查
// 3. 不符合项目规范
```

### 正确示例
```java
// ✅ 正确：使用 StandardCharsets.UTF_8
import java.nio.charset.StandardCharsets;

String content = new String(bytes, StandardCharsets.UTF_8);
Files.write(path, content.getBytes(StandardCharsets.UTF_8));
response.setCharacterEncoding(StandardCharsets.UTF_8.name());

// 优点：
// 1. 编译时检查
// 2. 不会拼写错误
// 3. 符合项目规范
```

---

## ❌ 错误2：新功能未添加开关

### 错误示例
```java
// ❌ 错误：新功能直接生效，无法回退
@Service
public class NewFeatureService {

    public void executeNewFeature() {
        // 直接实现新逻辑
        // 如果出现问题，只能通过代码回退或重新部署
        newAlgorithm();
    }
}
```

### 正确示例
```java
// ✅ 正确：添加功能开关，支持热切换
@Service
public class SmartFeatureService {

    private static final Logger logger = LoggerFactory.getLogger(SmartFeatureService.class);

    public void executeFeature() {
        // 检查功能开关
        if (!NewFeatureConfiguration.ENABLE.getValue()) {
            logger.info("New feature is disabled, using legacy implementation");
            executeLegacyFeature(); // 降级到旧逻辑
            return;
        }

        try {
            logger.info("New feature is enabled");
            executeNewFeature(); // 执行新逻辑
        } catch (Exception e) {
            logger.error("New feature failed, falling back to legacy", e);
            executeLegacyFeature(); // 异常时降级
        }
    }

    private void executeNewFeature() {
        // 新功能实现
    }

    private void executeLegacyFeature() {
        // 原有稳定实现
    }
}

// 配置类
object NewFeatureConfiguration {
  val ENABLE = CommonVars("linkis.new.feature.enable", false)
}
```

---

## ❌ 错误3：修改现有表结构未记录

### 错误示例
```sql
-- ❌ 错误：直接在数据库执行 ALTER TABLE
-- 问题：
-- 1. 其他环境无法同步
-- 2. 没有变更记录
-- 3. 无法回滚

ALTER TABLE linkis_ps_job_history_group_history
ADD COLUMN new_field VARCHAR(50) COMMENT 'new field';
```

### 正确示例
```sql
-- ✅ 正确：在 linkis-dist/package/db/linkis_ddl.sql 中添加变更

-- Step 1: 在 linkis_ddl.sql 文件末尾添加变更记录
-- ================================================================
-- 版本: 1.17.0
-- 需求: 添加任务扩展字段支持
-- 日期: 2025-01-28
-- ================================================================

ALTER TABLE linkis_ps_job_history_group_history
ADD COLUMN new_field VARCHAR(50) COMMENT 'new field for extended info';

-- 如果有索引变更
CREATE INDEX idx_new_field ON linkis_ps_job_history_group_history(new_field);

-- Step 2: 如果需要初始化数据，在 linkis_dml.sql 中添加
-- 在 linkis-dist/package/db/linkis_dml.sql 添加：
UPDATE linkis_ps_job_history_group_history
SET new_field = 'default_value'
WHERE new_field IS NULL;
```

---

## ❌ 错误4：异常处理不规范

### 错误示例
```java
// ❌ 错误示例1：吞掉异常
try {
    processData();
} catch (Exception e) {
    // 什么都不做，异常被吞掉
}

// ❌ 错误示例2：打印后继续抛出原始异常
try {
    processData();
} catch (Exception e) {
    e.printStackTrace(); // 不要使用 printStackTrace
    throw e; // 直接抛出原始异常
}

// ❌ 错误示例3：捕获过于宽泛
try {
    processData();
} catch (Throwable t) { // 不要捕获 Throwable
    logger.error("Error", t);
}
```

### 正确示例
```java
// ✅ 正确示例1：记录日志并抛出业务异常
try {
    processData();
} catch (IOException e) {
    logger.error("Failed to process data", e);
    throw new DataProcessException("Failed to process data", e);
}

// ✅ 正确示例2：捕获具体异常，提供有意义的错误信息
try {
    String result = processData(param);
    return result;
} catch (IllegalArgumentException e) {
    logger.error("Invalid parameter: {}", param, e);
    throw new ValidationException("Invalid parameter: " + param, e);
} catch (IOException e) {
    logger.error("IO error while processing data", e);
    throw new DataAccessException("IO error while processing data", e);
}

// ✅ 正确示例3：在Service层统一处理异常
@Service
public class DataServiceImpl implements DataService {

    @Override
    public Result processData(String param) {
        try {
            // 业务逻辑
            String data = fetchData(param);
            return Result.success(data);
        } catch (DataNotFoundException e) {
            logger.warn("Data not found for param: {}", param);
            return Result.error("Data not found");
        } catch (Exception e) {
            logger.error("Unexpected error while processing data", e);
            throw new ServiceException("Failed to process data", e);
        }
    }
}
```

---

## ❌ 错误5：日志记录不规范

### 错误示例
```java
// ❌ 错误示例1：使用 System.out
System.out.println("Processing data: " + data);

// ❌ 错误示例2：日志级别使用不当
logger.error("User {} logged in", username); // 登录不是错误

// ❌ 错误示例3：字符串拼接
logger.info("Processing user: " + username + ", id: " + userId);

// ❌ 错误示例4：敏感信息直接打印
logger.info("User password: {}", password);
```

### 正确示例
```java
// ✅ 正确示例1：使用 Logger
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

// ✅ 正确示例2：使用正确的日志级别
logger.info("User {} logged in successfully", username); // INFO
logger.warn("Login attempt from unknown IP: {}", ip); // WARN
logger.error("Failed to authenticate user {}", username, exception); // ERROR

// ✅ 正确示例3：使用占位符
logger.info("Processing user: {}, id: {}, type: {}", username, userId, userType);

// ✅ 正确示例4：脱敏处理敏感信息
logger.info("User {} password updated", username); // 不打印密码
logger.debug("Token: {}***", token.substring(0, 4)); // 只打印前几位

// ✅ 正确示例5：关键业务节点记录完整上下文
logger.info("Task submitted: taskId={}, user={}, engineType={}, code={}",
            taskId, username, engineType, codePreview);
logger.error("Task execution failed: taskId={}, user={}, error={}",
             taskId, username, e.getMessage(), e);
```

---

## ❌ 错误6：REST接口返回值不规范

### 错误示例
```java
// ❌ 错误：直接返回业务对象或String
@RequestMapping(path = "/getData", method = RequestMethod.GET)
public UserData getData() {
    return userData; // 不符合统一返回体规范
}

@RequestMapping(path = "/save", method = RequestMethod.POST)
public String save(@RequestBody Data data) {
    return "success"; // 不符合规范
}
```

### 正确示例
```java
// ✅ 正确：使用统一返回体 Message
import org.apache.linkis.server.Message;

@RequestMapping(path = "/getData", method = RequestMethod.GET)
public Message getData(HttpServletRequest req) {
    try {
        String username = ModuleUserUtils.getOperationUser(req, "getData");
        UserData data = userService.getData(username);
        return Message.ok("Query successful").data("userData", data);
    } catch (Exception e) {
        logger.error("Failed to get user data", e);
        return Message.error("Failed to get user data: " + e.getMessage());
    }
}

@RequestMapping(path = "/save", method = RequestMethod.POST)
public Message save(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
    try {
        String username = ModuleUserUtils.getOperationUser(req, "save");

        // 参数验证
        String name = jsonNode.get("name").asText();
        if (StringUtils.isBlank(name)) {
            return Message.error("Name cannot be empty");
        }

        Long id = dataService.save(name, username);
        return Message.ok("Save successful").data("id", id);
    } catch (Exception e) {
        logger.error("Failed to save data", e);
        return Message.error("Failed to save data: " + e.getMessage());
    }
}
```

---

## ❌ 错误7：MyBatis SQL注入风险

### 错误示例
```xml
<!-- ❌ 错误：使用 ${} 直接拼接，存在SQL注入风险 -->
<select id="selectByName" resultType="User">
    SELECT * FROM user WHERE name = '${name}'
</select>
```

### 正确示例
```xml
<!-- ✅ 正确：使用 #{} 参数化查询 -->
<select id="selectByName" resultType="User">
    SELECT * FROM user WHERE name = #{name}
</select>

<!-- ✅ 动态排序时，在代码中验证字段名 -->
<select id="selectWithOrder" resultType="User">
    SELECT * FROM user
    <if test="orderBy != null">
        ORDER BY ${orderBy} <!-- 仅在代码中已验证orderBy合法性时使用 -->
    </if>
</select>
```

```java
// 在Service层验证动态字段
public List<User> selectWithOrder(String orderBy) {
    // 白名单验证
    List<String> allowedFields = Arrays.asList("id", "name", "create_time");
    if (!allowedFields.contains(orderBy)) {
        throw new IllegalArgumentException("Invalid order field: " + orderBy);
    }
    return userMapper.selectWithOrder(orderBy);
}
```

---

## ❌ 错误8：事务使用不当

### 错误示例
```java
// ❌ 错误示例1：没有添加事务注解
@Service
public class OrderService {
    public void createOrder(Order order) {
        orderMapper.insert(order); // 插入订单
        stockMapper.decrease(order.getProductId()); // 减库存
        // 如果减库存失败，订单已经插入，数据不一致
    }
}

// ❌ 错误示例2：捕获异常后未抛出，事务不会回滚
@Transactional
public void processOrder(Order order) {
    try {
        orderMapper.insert(order);
        stockMapper.decrease(order.getProductId());
    } catch (Exception e) {
        logger.error("Error", e);
        // 异常被吞掉，事务不会回滚
    }
}
```

### 正确示例
```java
// ✅ 正确示例1：添加事务注解，指定回滚异常
@Service
public class OrderService {

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Order order) {
        orderMapper.insert(order);
        stockMapper.decrease(order.getProductId());
        // 任何异常都会回滚
    }
}

// ✅ 正确示例2：如果需要捕获异常，重新抛出
@Transactional(rollbackFor = Exception.class)
public void processOrder(Order order) {
    try {
        orderMapper.insert(order);
        stockMapper.decrease(order.getProductId());
    } catch (StockNotEnoughException e) {
        logger.warn("Stock not enough for product: {}", order.getProductId());
        throw e; // 重新抛出，触发回滚
    } catch (Exception e) {
        logger.error("Unexpected error while processing order", e);
        throw new OrderProcessException("Failed to process order", e);
    }
}

// ✅ 正确示例3：部分操作不需要事务
@Service
public class OrderService {

    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Order order) {
        // 数据库操作在事务中
        orderMapper.insert(order);
        stockMapper.decrease(order.getProductId());

        Long orderId = order.getId();

        // 发送通知不在事务中（避免外部调用导致事务超时）
        sendNotificationAsync(orderId);

        return orderId;
    }

    private void sendNotificationAsync(Long orderId) {
        // 异步发送，不阻塞事务
        executor.submit(() -> notificationService.send(orderId));
    }
}
```

---

## 🎯 错误排查清单

开发完成后，请检查以下项目：

- [ ] 字符编码统一使用 `StandardCharsets.UTF_8`
- [ ] 新功能已添加开关配置（默认false）
- [ ] 数据库变更已记录到 DDL/DML 文件
- [ ] 异常处理规范，使用 LinkisException 及其子类
- [ ] 日志使用 Logger，不使用 System.out
- [ ] REST接口使用统一返回体 Message
- [ ] SQL 使用参数化查询，避免注入
- [ ] 事务注解正确使用，异常能正确回滚
- [ ] 敏感信息已脱敏处理
- [ ] 代码遵循最小改动原则

---

# 需求开发流程

## 需求分析模板

### 【背景说明】
描述业务场景、现有问题或痛点、期望解决的目标

### 【验收标准】
- 功能验收点（具体、可测量）
- 性能要求（响应时间、并发数等）
- 安全要求（权限控制、数据保护）
- 兼容性要求（向后兼容）

## 开发交付清单

### 变更清单
- 新增/修改的文件路径列表
- 数据库变更脚本（DDL/DML）
- 配置文件变更

### 测试验证
- 单元测试代码
- 集成测试用例
- 手动测试命令（curl等）

### 质量检查
- [ ] 代码符合项目规范
- [ ] 异常处理完整
- [ ] 日志记录充分
- [ ] 单元测试覆盖
- [ ] 配置开关完整
- [ ] 向后兼容性检查

---

# AI IDE开发提示

## 开发技巧
1. **优先查看现有代码**：在新增功能前，先查看相似功能的实现方式
2. **遵循现有模式**：保持与现有代码风格一致
3. **充分测试**：编写充分的单元测试和集成测试
4. **考虑边界情况**：处理各种异常和边界条件

## 常见问题及解决方案

### 1. 字符编码问题
**问题**：HTTP传输过程中出现中文乱码
**解决**：统一使用`StandardCharsets.UTF_8`

### 2. 配置热更新问题
**问题**：配置修改后需要重启服务
**解决**：使用`CommonVars`并配合`@RefreshScope`注解

### 3. 性能优化问题
**问题**：大批量数据处理性能差
**解决**：采用分页处理，单次处理不超过5000条

---

**📝 重要提示**
1. 严格遵循现有架构设计，不得随意修改核心组件
2. 新增功能必须考虑向后兼容性
3. 关键业务逻辑必须有完整的异常处理和日志记录
4. 所有配置项必须有合理的默认值
5. 代码提交前必须通过本地测试验证