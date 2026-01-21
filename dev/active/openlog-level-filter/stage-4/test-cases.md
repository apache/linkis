# 阶段4：测试用例文档

## 一、测试范围

| 测试类型 | 测试内容 |
|---------|---------|
| 单元测试 | filterLogByLevel 方法的各种输入场景 |
| 接口测试 | openLog 接口的 logLevel 参数处理 |
| 兼容性测试 | 向后兼容性验证 |

## 二、单元测试用例

### 2.1 filterLogByLevel 方法测试

| 用例编号 | 用例名称 | 输入 | 预期结果 |
|---------|---------|------|---------|
| UT-001 | logLevel=all | logLevel="all" | 返回所有4个位置的日志 |
| UT-002 | logLevel=error | logLevel="error" | 仅 log[0] 有内容，其余为空 |
| UT-003 | logLevel=warn | logLevel="warn" | 仅 log[1] 有内容，其余为空 |
| UT-004 | logLevel=info | logLevel="info" | 仅 log[2] 有内容，其余为空 |
| UT-005 | logLevel=null | logLevel=null | 返回所有日志（向后兼容） |
| UT-006 | logLevel="" | logLevel="" | 返回所有日志（向后兼容） |
| UT-007 | logLevel=invalid | logLevel="xxx" | 返回所有日志（向后兼容） |
| UT-008 | 大小写不敏感 | logLevel="ERROR" | 与 "error" 结果相同 |

### 2.2 测试代码

```java
@Test
@DisplayName("Test filterLogByLevel with logLevel=error")
public void testFilterLogByLevelError() throws Exception {
  FsRestfulApi api = new FsRestfulApi();
  Method method = FsRestfulApi.class.getDeclaredMethod(
      "filterLogByLevel", StringBuilder[].class, String.class);
  method.setAccessible(true);

  StringBuilder[] logs = createTestLogs();
  String[] result = (String[]) method.invoke(api, logs, "error");

  // Only ERROR logs should be returned
  assertEquals(4, result.length);
  assertTrue(result[LogLevel.Type.ERROR.ordinal()].contains("ERROR log"));
  assertEquals("", result[LogLevel.Type.WARN.ordinal()]);
  assertEquals("", result[LogLevel.Type.INFO.ordinal()]);
  assertEquals("", result[LogLevel.Type.ALL.ordinal()]);
}
```

## 三、接口测试用例

### 3.1 正常场景

| 用例编号 | 用例名称 | 请求参数 | 预期结果 |
|---------|---------|---------|---------|
| IT-001 | 获取所有日志 | path=/path/to/log | data.log 数组4个位置都有内容 |
| IT-002 | 获取ERROR日志 | path=/path/to/log&logLevel=error | 仅 data.log[0] 有内容 |
| IT-003 | 获取WARN日志 | path=/path/to/log&logLevel=warn | 仅 data.log[1] 有内容 |
| IT-004 | 获取INFO日志 | path=/path/to/log&logLevel=info | 仅 data.log[2] 有内容 |
| IT-005 | 不传logLevel | path=/path/to/log | data.log 数组4个位置都有内容 |

### 3.2 异常场景

| 用例编号 | 用例名称 | 请求参数 | 预期结果 |
|---------|---------|---------|---------|
| IT-101 | 无效logLevel | path=/path/to/log&logLevel=invalid | data.log 数组4个位置都有内容 |
| IT-102 | 空path参数 | path= | 返回错误信息 |
| IT-103 | 文件不存在 | path=/not/exist | 返回错误信息 |

### 3.3 请求示例

```bash
# IT-001: 获取所有日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log"

# IT-002: 获取ERROR日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log&logLevel=error"

# IT-003: 获取WARN日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log&logLevel=warn"

# IT-004: 获取INFO日志
curl -X GET "http://localhost:8080/api/rest_j/v1/filesystem/openLog?path=/path/to/test.log&logLevel=info"
```

## 四、兼容性测试用例

| 用例编号 | 用例名称 | 测试场景 | 预期结果 |
|---------|---------|---------|---------|
| CT-001 | 旧客户端兼容 | 不传logLevel参数 | 返回所有日志，与原接口行为一致 |
| CT-002 | 返回结构兼容 | 任意logLevel | data.log 始终为4元素数组 |
| CT-003 | SDK向后兼容 | 使用旧SDK调用 | 正常返回所有日志 |

## 五、测试执行

### 5.1 运行单元测试

```bash
cd linkis-public-enhancements/linkis-pes-publicservice
mvn test -Dtest=OpenLogFilterTest
```

### 5.2 测试文件位置

```
linkis-public-enhancements/
└── linkis-pes-publicservice/
    └── src/
        └── test/
            └── java/
                └── org/apache/linkis/filesystem/restful/api/
                    └── OpenLogFilterTest.java
```

## 六、测试结论

| 测试类型 | 用例数 | 通过数 | 状态 |
|---------|-------|-------|------|
| 单元测试 | 9 | - | 待执行 |
| 接口测试 | 8 | - | 待执行 |
| 兼容性测试 | 3 | - | 待执行 |
