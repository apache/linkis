# 阶段4：测试用例

## 1. 测试概述

### 1.1 测试范围
- 系统用户Web登录拦截功能
- 配置开关有效性验证
- 非Web渠道登录不受影响

### 1.2 测试环境要求
- Gateway服务正常运行
- 配置项可动态修改

## 2. 功能测试用例

### TC-001: 普通用户Web登录成功

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-001 |
| **用例名称** | 普通用户Web登录成功 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`testuser`（非系统用户） |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"testuser","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录成功，返回状态码0 |
| **优先级** | P0 |

### TC-002: hadoop用户Web登录被拦截

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-002 |
| **用例名称** | hadoop用户Web登录被拦截 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录失败，返回"System users are prohibited from logging in（系统用户禁止登录）！" |
| **优先级** | P0 |

### TC-003: hduser前缀用户Web登录被拦截

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-003 |
| **用例名称** | hduser前缀用户Web登录被拦截 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`hduser01` |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"hduser01","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录失败，返回"System users are prohibited from logging in（系统用户禁止登录）！" |
| **优先级** | P0 |

### TC-004: hadoop用户Client登录成功

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-004 |
| **用例名称** | hadoop用户Client登录成功（无webLogin header） |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中不设置`webLogin`<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录成功（webLogin默认为false，不拦截） |
| **优先级** | P0 |

### TC-005: hadoop用户显式设置webLogin=false登录成功

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-005 |
| **用例名称** | hadoop用户显式设置webLogin=false登录成功 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=false`<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -H "webLogin: false" -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录成功 |
| **优先级** | P1 |

### TC-006: 功能开关关闭时hadoop用户Web登录成功

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-006 |
| **用例名称** | 功能开关关闭时hadoop用户Web登录成功 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=false` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录成功（功能开关关闭，不进行拦截） |
| **优先级** | P0 |

### TC-007: shduser前缀用户Web登录被拦截

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-007 |
| **用例名称** | shduser前缀用户Web登录被拦截 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`shduser_test` |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"shduser_test","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录失败，返回"System users are prohibited from logging in（系统用户禁止登录）！" |
| **优先级** | P1 |

## 3. 边界测试用例

### TC-008: webLogin大小写不敏感

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-008 |
| **用例名称** | webLogin值大小写不敏感 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=TRUE`<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -H "webLogin: TRUE" -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录失败，拦截生效 |
| **优先级** | P2 |

### TC-009: 用户名大小写不敏感

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-009 |
| **用例名称** | 用户名大小写不敏感 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=true`<br>2. 用户名设置为`HADOOP` |
| **请求示例** | `curl -X POST -H "webLogin: true" -d '{"userName":"HADOOP","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录失败，拦截生效（用户名会转小写后匹配） |
| **优先级** | P2 |

### TC-010: webLogin为空字符串

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-010 |
| **用例名称** | webLogin为空字符串 |
| **前置条件** | `linkis.system.user.prohibit.login.switch=true` |
| **测试步骤** | 1. 发送登录请求，Header中设置`webLogin=`（空）<br>2. 用户名设置为`hadoop` |
| **请求示例** | `curl -X POST -H "webLogin: " -d '{"userName":"hadoop","password":"xxx"}' http://gateway/api/rest_j/v1/user/login` |
| **预期结果** | 登录成功（空字符串不等于"true"） |
| **优先级** | P2 |

## 4. 测试数据

### 4.1 系统用户前缀
```
hadoop,hduser,shduser
```

### 4.2 测试用户

| 用户名 | 类型 | webLogin=true时预期 |
|--------|------|---------------------|
| hadoop | 系统用户 | 拦截 |
| hduser01 | 系统用户(前缀匹配) | 拦截 |
| shduser_test | 系统用户(前缀匹配) | 拦截 |
| testuser | 普通用户 | 放行 |
| admin | 普通用户 | 放行 |
| hadooptest | 系统用户(前缀匹配) | 拦截 |

## 5. 测试执行检查清单

- [ ] TC-001: 普通用户Web登录成功
- [ ] TC-002: hadoop用户Web登录被拦截
- [ ] TC-003: hduser前缀用户Web登录被拦截
- [ ] TC-004: hadoop用户Client登录成功
- [ ] TC-005: hadoop用户显式设置webLogin=false登录成功
- [ ] TC-006: 功能开关关闭时hadoop用户Web登录成功
- [ ] TC-007: shduser前缀用户Web登录被拦截
- [ ] TC-008: webLogin大小写不敏感
- [ ] TC-009: 用户名大小写不敏感
- [ ] TC-010: webLogin为空字符串
