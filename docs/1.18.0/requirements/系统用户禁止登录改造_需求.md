# 阶段1：需求分析文档

## 1. 需求概述

### 1.1 背景
根据安全要求，Linkis管理台需要禁止系统用户（如hadoop、hduser、shduser等）通过Web页面登录，以降低安全风险。

### 1.2 目标
- 拦截系统用户的Web页面登录请求
- 不影响客户端(client)及其他渠道的登录
- 提供配置开关和系统用户前缀配置

## 2. 功能需求

### 2.1 登录拦截逻辑

| 编号 | 功能点 | 描述 | 优先级 |
|------|--------|------|--------|
| FR-001 | webLogin标识传递 | 前端在HTTP header中传递`webLogin`标识 | P0 |
| FR-002 | webLogin标识获取 | 后端从header获取标识，默认值为`false` | P0 |
| FR-003 | 系统用户拦截 | 当webLogin=true时，拦截系统用户前缀匹配的用户 | P0 |
| FR-004 | 非Web渠道放行 | webLogin=false或未传时不进行拦截 | P0 |

### 2.2 错误提示

| 编号 | 功能点 | 描述 | 优先级 |
|------|--------|------|--------|
| FR-005 | 统一错误信息 | 拦截时返回"系统用户禁止登录" | P0 |

### 2.3 配置管理

| 编号 | 功能点 | 描述 | 优先级 |
|------|--------|------|--------|
| FR-006 | 功能开关 | `linkis.system.user.prohibit.login.switch` 控制功能开启/关闭 | P0 |
| FR-007 | 系统用户前缀 | `linkis.system.user.prohibit.login.prefix` 配置系统用户前缀列表 | P0 |

## 3. 非功能需求

### 3.1 兼容性
- 现有客户端登录方式不受影响
- 配置项需向后兼容

### 3.2 安全性
- 拦截逻辑不可绕过
- webLogin标识仅用于识别登录来源，不用于认证

### 3.3 可配置性
- 功能可通过配置开关完全关闭
- 系统用户前缀列表可动态配置

## 4. 数据字典

### 4.1 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| linkis.system.user.prohibit.login.switch | Boolean | false | 禁止系统用户登录功能开关 |
| linkis.system.user.prohibit.login.prefix | String | hadoop,hduser,shduser | 系统用户前缀列表，逗号分隔 |

### 4.2 HTTP Header

| Header名称 | 类型 | 默认值 | 说明 |
|------------|------|--------|------|
| webLogin | String | false | Web页面登录标识，true表示来自Web页面 |

## 5. 用例分析

### 5.1 正常场景

#### UC-001: 普通用户Web登录
- **前置条件**: 功能开关开启
- **输入**: 用户名=testuser, webLogin=true
- **预期**: 登录成功

#### UC-002: 系统用户Client登录
- **前置条件**: 功能开关开启
- **输入**: 用户名=hadoop, webLogin=false
- **预期**: 登录成功

### 5.2 异常场景

#### UC-003: 系统用户Web登录
- **前置条件**: 功能开关开启
- **输入**: 用户名=hadoop, webLogin=true
- **预期**: 登录失败，返回"系统用户禁止登录"

#### UC-004: hduser用户Web登录
- **前置条件**: 功能开关开启
- **输入**: 用户名=hduser01, webLogin=true
- **预期**: 登录失败，返回"系统用户禁止登录"

### 5.3 边界场景

#### UC-005: 功能开关关闭
- **前置条件**: 功能开关关闭
- **输入**: 用户名=hadoop, webLogin=true
- **预期**: 登录成功（不进行拦截）

#### UC-006: webLogin未传递
- **前置条件**: 功能开关开启
- **输入**: 用户名=hadoop, header中无webLogin
- **预期**: 登录成功（默认webLogin=false）

## 6. 影响范围分析

### 6.1 代码改动范围

| 文件 | 改动类型 | 改动内容 |
|------|---------|---------|
| GatewayConfiguration.scala | 修改 | 更新PROHIBIT_LOGIN_PREFIX默认值 |
| UserRestful.scala | 修改 | 修改登录拦截逻辑，从header获取webLogin |

### 6.2 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| 影响正常用户登录 | 低 | 功能开关默认关闭 |
| 前端未传webLogin | 低 | 默认值为false，不拦截 |
| 配置错误导致无法登录 | 中 | 提供配置示例和文档 |
