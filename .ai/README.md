# Linkis AI 开发文档导航

> **版本信息**
> - 文档版本: 1.0.0
> - 最后更新: 2025-01-28
> - 适用版本: Apache Linkis 1.17.0+

---

## 🚀 快速开始

### 新手必读（按顺序阅读）
1. **[项目核心规约](./project-context.md)** - 包含技术栈、架构设计、开发规范和模板
2. **[强制性开发规则](./rules.md)** - 必须无条件遵守的开发规则
3. **[模块文档](#模块文档索引)** - 根据你要开发的功能选择对应模块

### 常见开发场景快速跳转
- 🆕 新增 REST 接口 → [REST接口开发模板](#rest接口开发)
- ⚙️ 添加配置项 → [配置管理规范](#配置管理)
- 🗄️ 修改数据库 → [数据库变更规范](#数据库变更)
- 🐛 异常处理 → [异常处理规范](#异常处理)
- 📝 日志记录 → [日志规范](#日志规范)

---

## 📚 核心文档索引

### 🎯 开发规范文档
| 文档 | 用途 | 何时查看 |
|------|------|----------|
| [project-context.md](./project-context.md) | 项目角色定位、技术栈、架构设计、开发模板 | 开始任何开发工作前必读 |
| [rules.md](./rules.md) | 强制性开发规则、需求实现步骤 | 每次开发新需求时参考 |

### 🏗️ 模块文档索引

#### 微服务治理服务（基础设施层）
| 服务 | 文档 | 主要功能 |
|------|------|----------|
| Gateway | [gateway.md](./modules/microservice-governance/gateway.md) | API网关、路由转发、安全认证 |
| Eureka | [eureka.md](./modules/microservice-governance/eureka.md) | 服务注册与发现 |
| 概览 | [README.md](./modules/microservice-governance/README.md) | 微服务治理服务概述 |

#### 计算治理服务（核心业务层）
| 服务 | 文档 | 主要功能 |
|------|------|----------|
| Entrance | [entrance.md](./modules/computation-governance/entrance.md) | 任务提交入口、调度管理 |
| JobHistory | [jobhistory.md](./modules/computation-governance/jobhistory.md) | 任务历史记录查询 |
| Manager | [manager.md](./modules/computation-governance/manager.md) | 资源管理、应用管理 |
| ECM | [ecm.md](./modules/computation-governance/ecm.md) | 引擎连接管理 |
| 概览 | [README.md](./modules/computation-governance/README.md) | 计算治理服务概述 |

#### 公共增强服务（支撑服务层）
| 服务 | 文档 | 主要功能 |
|------|------|----------|
| PublicService | [publicservice.md](./modules/public-enhancements/publicservice.md) | 公共服务、文件管理 |
| Configuration | [configuration.md](./modules/public-enhancements/configuration.md) | 配置管理 |
| BML | [bml.md](./modules/public-enhancements/bml.md) | 大数据物料库 |
| DataSource | [datasource.md](./modules/public-enhancements/datasource.md) | 数据源管理 |
| Context | [context.md](./modules/public-enhancements/context.md) | 上下文服务 |
| Monitor | [monitor.md](./modules/public-enhancements/monitor.md) | 监控服务 |
| 概览 | [README.md](./modules/public-enhancements/README.md) | 公共增强服务概述 |

---

## 🔍 按功能快速查找

### <a name="rest接口开发"></a>REST接口开发
- **开发模板**: [project-context.md - REST接口层](./project-context.md#1-rest接口层)
- **API规范**: [project-context.md - API设计规范](./project-context.md#api设计规范)
- **参考示例**:
  - Entrance接口: [entrance.md - API Interfaces](./modules/computation-governance/entrance.md#api-interfaces)
  - Configuration接口: [configuration.md - API Interfaces](./modules/public-enhancements/configuration.md#api-interfaces)

### <a name="配置管理"></a>配置管理
- **配置规范**: [project-context.md - 配置管理规范](./project-context.md#配置管理规范)
- **配置示例库**: [project-context.md - 常用配置示例库](./project-context.md#常用配置示例库)
- **配置模板**: [project-context.md - 配置类](./project-context.md#4-配置类)
- **参考实现**: linkis-jobhistory/conf/JobhistoryConfiguration

### <a name="数据库变更"></a>数据库变更
- **变更规则**: [rules.md - 数据库修改原则](./rules.md#数据库修改原则)
- **DDL脚本位置**: `linkis-dist/package/db/linkis_ddl.sql`
- **DML脚本位置**: `linkis-dist/package/db/linkis_dml.sql`
- **表结构参考**: 各模块文档的 "Database Table Structures" 章节

### <a name="异常处理"></a>异常处理
- **异常规范**: [project-context.md - 异常处理规范](./project-context.md#异常处理规范)
- **统一异常**: `org.apache.linkis.common.exception.LinkisException`
- **常见错误**: [project-context.md - 常见错误及避免方法](./project-context.md#常见错误及避免方法)

### <a name="日志规范"></a>日志规范
- **日志规范**: [project-context.md - 日志规范](./project-context.md#日志规范)
- **Logger定义**: 必须使用 `LoggerFactory.getLogger(ClassName.class)`
- **日志级别**: ERROR/WARN/INFO/DEBUG 使用场景

---

## 🎨 开发模板快速复制

### 新增功能完整流程
```
1. 查看 rules.md - 需求实现步骤
2. 创建需求文档和设计文档
3. 使用 project-context.md 中的代码模板：
   - REST接口层模板
   - 服务层模板
   - 数据访问层模板
   - 配置类模板
4. 添加功能开关（默认false）
5. 记录数据库变更
6. 编写测试和文档
```

### REST接口模板快速链接
👉 [project-context.md - 新功能开发模板](./project-context.md#新功能开发模板)

### 配置类模板快速链接
👉 [project-context.md - 配置类](./project-context.md#4-配置类)

---

## ⚠️ 重要提醒

### 🚫 禁止操作（来自 rules.md）
- **数据库结构**: 除非明确指定，严禁修改现有表结构
- **第三方依赖**: 不允许引入新的第三方依赖库
- **核心接口**: 不得修改现有公共接口的签名

### ✅ 必须遵守
- **最小改动原则**: 所有功能实现必须遵循最小改动原则
- **功能可配置**: 所有功能必须增加功能开关，默认关闭
- **向后兼容**: 新增功能必须考虑向后兼容性

---

## 💡 开发技巧

### 编程语言选择
- **Java**: REST API、Service层、Entity类、配置类
- **Scala**: 计算逻辑、RPC通信、复杂业务处理、配置对象

### 字符编码
统一使用 `StandardCharsets.UTF_8`，禁止使用字符串 `"UTF-8"`

### 统一返回体
所有REST接口返回 `org.apache.linkis.server.Message`

---

## 📖 如何使用这些文档

### 场景1: 我要在 Entrance 服务中新增一个接口
1. 阅读 [entrance.md](./modules/computation-governance/entrance.md) 了解服务结构
2. 查看 [project-context.md - REST接口层模板](./project-context.md#1-rest接口层)
3. 参考 entrance.md 中现有接口实现
4. 遵循 [rules.md](./rules.md) 中的开发规则
5. 添加功能开关配置

### 场景2: 我需要添加一个新的配置项
1. 查看 [project-context.md - 配置管理规范](./project-context.md#配置管理规范)
2. 参考 [project-context.md - 配置类模板](./project-context.md#4-配置类)
3. 查看 `JobhistoryConfiguration` 实现示例
4. 在当前模块的 conf 目录下的 Configuration 类中添加

### 场景3: 我需要修改数据库表
1. 查看 [rules.md - 数据库修改原则](./rules.md#数据库修改原则)
2. 确认是否能通过新增字段实现（优先选择）
3. 将变更记录到 `linkis-dist/package/db/linkis_ddl.sql`
4. 如有初始化数据，记录到 `linkis-dist/package/db/linkis_dml.sql`

---

## 🔄 文档更新记录

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 1.0.0 | 2025-01-28 | 创建导航文档，优化文档结构 | AI |

---

## 📞 帮助与反馈

如果文档中有不清楚的地方，请：
1. 先查看对应模块的详细文档
2. 查看 project-context.md 中的开发模板和示例
3. 参考现有代码实现

**记住**: 遵循规范比快速开发更重要！
