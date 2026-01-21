# 阶段4：测试用例

## 1. 测试概述

### 1.1 测试范围
- 结果集查看优化功能
- 管理台请求识别和处理
- 非管理台请求处理
- 提示信息动态展示
- 配置变更后的系统表现

### 1.2 测试环境要求
- Linkis服务正常运行
- PublicService组件正常工作
- 配置项可动态修改

## 2. 功能测试用例

### TC-001: 管理台请求查看大结果集

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-001 |
| **用例名称** | 管理台请求查看大结果集 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在包含超过阈值字段的测试文件 |
| **测试步骤** | 1. 发送登录请求，参数中设置`enableLimit=true`<br>2. 调用openFile接口查看大结果集 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&enableLimit=true&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回完整的结果集内容<br>3. 没有截取提示信息 |
| **优先级** | P0 |

### TC-002: 非管理台请求查看大结果集

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-002 |
| **用例名称** | 非管理台请求查看大结果集 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在包含超过阈值字段的测试文件 |
| **测试步骤** | 1. 发送登录请求，参数中设置`enableLimit=false`<br>2. 调用openFile接口查看大结果集 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&enableLimit=false&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回截取后的结果集<br>3. 提示信息中显示配置的实际阈值 |
| **优先级** | P0 |

### TC-003: 非管理台请求未指定enableLimit

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-003 |
| **用例名称** | 非管理台请求未指定enableLimit |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在包含超过阈值字段的测试文件 |
| **测试步骤** | 1. 调用openFile接口查看大结果集，不指定enableLimit参数 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回截取后的结果集<br>3. 提示信息中显示配置的实际阈值 |
| **优先级** | P0 |

### TC-004: 提示信息显示配置阈值10000

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-004 |
| **用例名称** | 提示信息显示配置阈值10000 |
| **前置条件** | `linkis.storage.field.view.max.length=10000`，`linkis.storage.field.truncation.enabled=true` |
| **测试步骤** | 1. 调用openFile接口查看包含超过10000字符字段的文件<br>2. 检查返回的提示信息 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/long-field.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 提示信息中包含"超过10000字符" |
| **优先级** | P0 |

### TC-005: 提示信息显示配置阈值20000

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-005 |
| **用例名称** | 提示信息显示配置阈值20000 |
| **前置条件** | `linkis.storage.field.view.max.length=20000`，`linkis.storage.field.truncation.enabled=true` |
| **测试步骤** | 1. 修改配置文件，设置linkis.storage.field.view.max.length=20000<br>2. 重启服务<br>3. 调用openFile接口查看包含超过20000字符字段的文件<br>4. 检查返回的提示信息 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/very-long-field.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 提示信息中包含"超过20000字符" |
| **优先级** | P0 |

### TC-006: 截取功能开关关闭

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-006 |
| **用例名称** | 截取功能开关关闭 |
| **前置条件** | `linkis.storage.field.truncation.enabled=false` |
| **测试步骤** | 1. 修改配置文件，设置linkis.storage.field.truncation.enabled=false<br>2. 重启服务<br>3. 调用openFile接口查看大结果集 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回完整的结果集<br>3. 没有截取提示信息 |
| **优先级** | P1 |

## 3. 边界测试用例

### TC-007: 字段长度等于阈值

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-007 |
| **用例名称** | 字段长度等于阈值 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在字段长度正好等于阈值的测试文件 |
| **测试步骤** | 1. 调用openFile接口查看字段长度正好等于阈值的文件 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/exact-limit.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回完整的结果集<br>3. 没有截取提示信息 |
| **优先级** | P2 |

### TC-008: 字段长度略大于阈值

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-008 |
| **用例名称** | 字段长度略大于阈值 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在字段长度略大于阈值的测试文件 |
| **测试步骤** | 1. 调用openFile接口查看字段长度略大于阈值的文件 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/slightly-over-limit.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回截取后的结果集<br>3. 提示信息中显示配置的实际阈值 |
| **优先级** | P2 |

### TC-009: enableLimit参数大小写不敏感

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-009 |
| **用例名称** | enableLimit参数大小写不敏感 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在包含超过阈值字段的测试文件 |
| **测试步骤** | 1. 调用openFile接口，参数中设置`enableLimit=TRUE`<br>2. 检查返回结果 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&enableLimit=TRUE&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回完整的结果集<br>3. 没有截取提示信息 |
| **优先级** | P2 |

## 4. 异常场景测试

### TC-010: 无效的enableLimit参数

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-010 |
| **用例名称** | 无效的enableLimit参数 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true`，存在包含超过阈值字段的测试文件 |
| **测试步骤** | 1. 调用openFile接口，参数中设置`enableLimit=invalid`<br>2. 检查返回结果 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&enableLimit=invalid&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 按照非管理台请求处理<br>3. 返回截取后的结果集<br>4. 提示信息中显示配置的实际阈值 |
| **优先级** | P2 |

### TC-011: 配置阈值为0

| 项目 | 内容 |
|------|------|
| **用例ID** | TC-011 |
| **用例名称** | 配置阈值为0 |
| **前置条件** | `linkis.storage.field.truncation.enabled=true` |
| **测试步骤** | 1. 修改配置文件，设置linkis.storage.field.view.max.length=0<br>2. 重启服务<br>3. 调用openFile接口查看结果集 |
| **请求示例** | `curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openFile?path=hdfs:///test/result.txt&page=1&pageSize=5000"` |
| **预期结果** | 1. 接口返回成功<br>2. 返回完整的结果集<br>3. 没有截取提示信息 |
| **优先级** | P2 |

## 5. 测试数据

### 5.1 测试文件

| 文件名 | 描述 | 预期结果 |
|--------|------|----------|
| exact-limit.txt | 字段长度正好等于阈值 | 返回完整结果，无提示 |
| slightly-over-limit.txt | 字段长度略大于阈值 | 返回截取结果，有提示 |
| long-field.txt | 字段长度超过10000 | 返回截取结果，提示超过10000 |
| very-long-field.txt | 字段长度超过20000 | 返回截取结果，提示超过20000 |
| normal-field.txt | 字段长度小于阈值 | 返回完整结果，无提示 |

### 5.2 配置组合

| 配置组合 | 预期行为 |
|----------|----------|
| truncation.enabled=true, view.max.length=10000 | 超过10000字符的字段会被截取，提示超过10000 |
| truncation.enabled=true, view.max.length=20000 | 超过20000字符的字段会被截取，提示超过20000 |
| truncation.enabled=false, view.max.length=10000 | 不进行截取，返回完整结果 |

## 6. 测试执行检查清单

- [ ] TC-001: 管理台请求查看大结果集
- [ ] TC-002: 非管理台请求查看大结果集
- [ ] TC-003: 非管理台请求未指定enableLimit
- [ ] TC-004: 提示信息显示配置阈值10000
- [ ] TC-005: 提示信息显示配置阈值20000
- [ ] TC-006: 截取功能开关关闭
- [ ] TC-007: 字段长度等于阈值
- [ ] TC-008: 字段长度略大于阈值
- [ ] TC-009: enableLimit参数大小写不敏感
- [ ] TC-010: 无效的enableLimit参数
- [ ] TC-011: 配置阈值为0

## 7. 测试建议

1. 建议在测试前准备好各种类型的测试文件，包括不同字段长度的文件
2. 建议测试不同配置组合下的系统表现
3. 建议测试管理台和非管理台请求的不同处理逻辑
4. 建议测试提示信息的动态展示效果
5. 建议测试边界值和异常场景

## 8. 附件

无