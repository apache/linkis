# 阶段3：代码开发

## 变更文件列表

| 文件路径 | 变更类型 | 说明 |
|---------|---------|------|
| FsRestfulApi.java | 修改 | 添加 logLevel 参数和过滤逻辑 |
| OpenLogAction.scala | 修改 | 添加 setLogLevel() 方法 |
| OpenLogFilterTest.java | 新增 | 单元测试 |

## 代码变更详情

### 1. FsRestfulApi.java

**文件路径**: `linkis-public-enhancements/linkis-pes-publicservice/src/main/java/org/apache/linkis/filesystem/restful/api/FsRestfulApi.java`

#### 变更1: openLog 方法签名

```java
@ApiOperation(value = "openLog", notes = "open log", response = Message.class)
@ApiImplicitParams({
  @ApiImplicitParam(name = "path", required = false, dataType = "String", value = "path"),
  @ApiImplicitParam(name = "proxyUser", dataType = "String"),
  @ApiImplicitParam(
      name = "logLevel",
      required = false,
      dataType = "String",
      defaultValue = "all",
      value = "Log level filter: all, info, error, warn")
})
@RequestMapping(path = "/openLog", method = RequestMethod.GET)
public Message openLog(
    HttpServletRequest req,
    @RequestParam(value = "path", required = false) String path,
    @RequestParam(value = "proxyUser", required = false) String proxyUser,
    @RequestParam(value = "logLevel", required = false, defaultValue = "all") String logLevel)
    throws IOException, WorkSpaceException {
```

#### 变更2: 调用过滤方法

```java
// Filter logs based on logLevel parameter
String[] filteredLog = filterLogByLevel(log, logLevel);
return Message.ok().data("log", filteredLog);
```

#### 变更3: 新增 filterLogByLevel 方法

```java
/**
 * Filter logs based on the specified log level.
 *
 * @param log The original log array with 4 elements: [ERROR, WARN, INFO, ALL]
 * @param logLevel The log level to filter: all, info, error, warn
 * @return Filtered log array
 */
private String[] filterLogByLevel(StringBuilder[] log, String logLevel) {
  String[] result = Arrays.stream(log).map(StringBuilder::toString).toArray(String[]::new);

  if (StringUtils.isEmpty(logLevel) || "all".equalsIgnoreCase(logLevel)) {
    // Return all logs (default behavior for backward compatibility)
    return result;
  }

  // Create empty array for filtered result
  String[] filteredResult = new String[4];
  Arrays.fill(filteredResult, "");

  switch (logLevel.toLowerCase()) {
    case "error":
      filteredResult[LogLevel.Type.ERROR.ordinal()] = result[LogLevel.Type.ERROR.ordinal()];
      break;
    case "warn":
      filteredResult[LogLevel.Type.WARN.ordinal()] = result[LogLevel.Type.WARN.ordinal()];
      break;
    case "info":
      filteredResult[LogLevel.Type.INFO.ordinal()] = result[LogLevel.Type.INFO.ordinal()];
      break;
    default:
      // Invalid logLevel, return all logs for backward compatibility
      LOGGER.warn("Invalid logLevel: {}, returning all logs", logLevel);
      return result;
  }

  return filteredResult;
}
```

---

### 2. OpenLogAction.scala

**文件路径**: `linkis-computation-governance/linkis-client/linkis-computation-client/src/main/scala/org/apache/linkis/ujes/client/request/OpenLogAction.scala`

```scala
object OpenLogAction {
  def newBuilder(): Builder = new Builder

  class Builder private[OpenLogAction] () {
    private var proxyUser: String = _
    private var logPath: String = _
    private var logLevel: String = "all"

    def setProxyUser(user: String): Builder = {
      this.proxyUser = user
      this
    }

    def setLogPath(path: String): Builder = {
      this.logPath = path
      this
    }

    def setLogLevel(level: String): Builder = {
      this.logLevel = level
      this
    }

    def build(): OpenLogAction = {
      val openLogAction = new OpenLogAction
      openLogAction.setUser(proxyUser)
      openLogAction.setParameter("path", logPath)
      if (logLevel != null && logLevel.nonEmpty) {
        openLogAction.setParameter("logLevel", logLevel)
      }
      openLogAction
    }
  }
}
```

---

### 3. OpenLogFilterTest.java (新增)

**文件路径**: `linkis-public-enhancements/linkis-pes-publicservice/src/test/java/org/apache/linkis/filesystem/restful/api/OpenLogFilterTest.java`

单元测试文件已创建，包含以下测试用例：
- testFilterLogByLevelAll
- testFilterLogByLevelError
- testFilterLogByLevelWarn
- testFilterLogByLevelInfo
- testFilterLogByLevelNull
- testFilterLogByLevelEmpty
- testFilterLogByLevelInvalid
- testFilterLogByLevelCaseInsensitive
- testLogLevelTypeOrdinal
