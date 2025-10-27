# Apache Linkis AI IDE 开发规约

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