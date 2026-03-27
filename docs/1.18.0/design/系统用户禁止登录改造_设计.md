# 阶段2：技术设计方案

## 1. 设计概述

### 1.1 设计目标
在现有登录拦截逻辑基础上进行增强，将登录来源判断方式从 request body 的 `source` 字段改为 HTTP Header 的 `webLogin` 字段。

### 1.2 设计原则
- **最小改动**: 复用现有拦截逻辑，仅修改来源判断方式
- **向后兼容**: 默认功能关闭，不影响现有系统
- **可配置性**: 支持配置开关和系统用户前缀列表

## 2. 架构设计

### 2.1 组件关系图

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Web Frontend  │────>│ Gateway Server  │────>│   Backend API   │
│                 │     │                 │     │                 │
│ Header:         │     │ UserRestful     │     │                 │
│ webLogin=true   │     │ ↓               │     │                 │
└─────────────────┘     │ tryLogin()      │     └─────────────────┘
                        │ ↓               │
                        │ isWebLogin()    │
                        │ ↓               │
                        │ checkSystemUser │
                        └─────────────────┘
```

### 2.2 处理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                         登录请求处理流程                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐    ┌───────────────┐    ┌────────────────────┐    │
│  │ 接收请求  │───>│ 获取用户名密码  │───>│ 检查功能开关是否开启 │    │
│  └──────────┘    └───────────────┘    └─────────┬──────────┘    │
│                                                  │               │
│                                    ┌─────────────┴─────────────┐ │
│                                    │ 开关状态?                  │ │
│                                    └─────────────┬─────────────┘ │
│                               关闭 │             │ 开启          │
│                                    ▼             ▼               │
│                          ┌─────────────┐  ┌─────────────────┐   │
│                          │ 继续正常登录 │  │ 从Header获取     │   │
│                          └─────────────┘  │ webLogin标识     │   │
│                                           └────────┬────────┘   │
│                                                    │             │
│                                      ┌─────────────┴───────────┐ │
│                                      │ webLogin == "true"?     │ │
│                                      └─────────────┬───────────┘ │
│                                  false │           │ true        │
│                                        ▼           ▼             │
│                              ┌─────────────┐ ┌───────────────┐  │
│                              │ 继续正常登录 │ │ 检查用户名前缀 │  │
│                              └─────────────┘ └───────┬───────┘  │
│                                                      │           │
│                                      ┌───────────────┴─────────┐ │
│                                      │ 匹配系统用户前缀?        │ │
│                                      └───────────────┬─────────┘ │
│                                     否 │             │ 是        │
│                                        ▼             ▼           │
│                              ┌─────────────┐  ┌─────────────┐   │
│                              │ 继续正常登录 │  │ 返回错误信息 │   │
│                              └─────────────┘  │ 拒绝登录    │   │
│                                               └─────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 3. 详细设计

### 3.1 配置项修改

**文件**: `GatewayConfiguration.scala`

| 配置项 | 当前值 | 修改后 |
|--------|--------|--------|
| PROHIBIT_LOGIN_PREFIX | `hduser,shduser` | `hadoop,hduser,shduser` |

**新增配置项**: 无需新增，复用现有配置

### 3.2 代码修改

**文件**: `UserRestful.scala`

#### 3.2.1 新增方法: isWebLogin

```scala
private val WEB_LOGIN_HEADER = "webLogin"

private def isWebLogin(gatewayContext: GatewayContext): Boolean = {
  val headers = gatewayContext.getRequest.getHeaders
  val webLoginValues = headers.get(WEB_LOGIN_HEADER)
  if (webLoginValues != null && webLoginValues.nonEmpty) {
    "true".equalsIgnoreCase(webLoginValues.head)
  } else {
    false  // 默认为false
  }
}
```

#### 3.2.2 修改tryLogin方法

**现有代码**:
```scala
if (
    GatewayConfiguration.PROHIBIT_LOGIN_SWITCH.getValue &&
    (!getRequestSource(gatewayContext).equals("client"))
) {
  PROHIBIT_LOGIN_PREFIX.split(",").foreach { prefix =>
    if (userName.toLowerCase().startsWith(prefix)) {
      return Message.error("System users are prohibited from logging in（系统用户禁止登录）！")
    }
  }
}
```

**修改后**:
```scala
if (
    GatewayConfiguration.PROHIBIT_LOGIN_SWITCH.getValue &&
    isWebLogin(gatewayContext)
) {
  PROHIBIT_LOGIN_PREFIX.split(",").foreach { prefix =>
    if (userName.toLowerCase().startsWith(prefix)) {
      return Message.error("System users are prohibited from logging in（系统用户禁止登录）！")
    }
  }
}
```

## 4. 接口设计

### 4.1 登录接口变更

**接口**: POST /api/rest_j/v1/user/login

**新增Header**:
| Header | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| webLogin | String | 否 | false | Web页面登录标识 |

**请求示例**:
```http
POST /api/rest_j/v1/user/login HTTP/1.1
Host: gateway.linkis.com
Content-Type: application/json
webLogin: true

{
  "userName": "testuser",
  "password": "xxx"
}
```

**错误响应** (系统用户被拦截):
```json
{
  "method": "/api/rest_j/v1/user/login",
  "status": 1,
  "message": "System users are prohibited from logging in（系统用户禁止登录）！"
}
```

## 5. 前端配合要求

前端在Web页面调用登录接口时，需要在HTTP请求header中添加:
```javascript
headers: {
  'webLogin': 'true'
}
```

## 6. 配置示例

### 6.1 linkis.properties

```properties
# 开启系统用户禁止登录功能
linkis.system.user.prohibit.login.switch=true

# 系统用户前缀列表（逗号分隔）
linkis.system.user.prohibit.login.prefix=hadoop,hduser,shduser
```

## 7. 兼容性说明

| 场景 | 行为 |
|------|------|
| 旧前端(无webLogin header) | 默认webLogin=false，不拦截，正常登录 |
| 客户端登录(无webLogin header) | 默认webLogin=false，不拦截，正常登录 |
| 新前端(webLogin=true) + 普通用户 | 正常登录 |
| 新前端(webLogin=true) + 系统用户 | 拦截，返回错误 |
