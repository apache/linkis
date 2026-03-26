# Linkis结果集下载和导出功能支持敏感字段屏蔽需求文档

## 文档信息

| 项目 | 信息 |
|-----|------|
| 文档版本 | v1.1 (已实现) |
| 创建日期 | 2025-10-27 |
| 更新日期 | 2025-10-30 |
| 当前版本 | Linkis 1.17.0 |
| 负责模块 | linkis-pes-publicservice (Filesystem) + pipeline |
| 开发分支 | feature/1.17.0-resultset-sensitive-field-masking |
| 状态 | ✅ 开发完成，已测试 |

---

## 实施总结

### 代码修改统计

```bash
7 files changed, 2698 insertions(+), 163 deletions(-)
```

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `FsRestfulApi.java` | 功能增强 | 添加maskedFieldNames参数支持 |
| `ResultUtils.java` | 新增工具类 | 提取公共字段过滤逻辑 |
| `PipelineEngineConnExecutor.scala` | 语法扩展 | 支持without子句解析 |
| `CSVExecutor.scala` | 功能增强 | 实现CSV导出字段屏蔽 |
| `ExcelExecutor.scala` | 功能增强 | 实现Excel导出字段屏蔽 |
| `resultset-sensitive-field-masking.md` | 新增文档 | 需求文档 |
| `resultset-sensitive-field-masking-design.md` | 新增文档 | 设计文档 |

### 核心改进点

1. **代码复用**: 将字段过滤逻辑提取到`ResultUtils`工具类，实现Java和Scala代码共享
2. **简化实现**: 使用`ResultUtils.dealMaskedField()`统一处理字段屏蔽，减少重复代码
3. **更好的架构**: 将通用逻辑放在`linkis-storage`模块，提高可维护性

---

## 1. 需求背景

### 1.1 现状说明

Linkis当前在结果集查看功能(`/api/rest_j/v1/filesystem/openFile`)中已经实现了敏感字段屏蔽机制,通过`maskedFieldNames`参数支持动态指定需要屏蔽的字段列表。该功能在前端展示结果集时可以有效保护敏感数据。

### 1.2 存在的安全风险

虽然结果集查看时支持屏蔽敏感字段,但用户仍然可以通过以下接口**绕过屏蔽机制**获取完整的敏感数据:

1. **单结果集下载接口**: `/api/rest_j/v1/filesystem/resultsetToExcel`
2. **多结果集下载接口**: `/api/rest_j/v1/filesystem/resultsetsToExcel`
3. **其他导出接口**(如存在)

这导致敏感字段屏蔽功能形同虚设,存在数据泄露风险。

### 1.3 需求来源

- 数据安全合规要求
- 敏感信息保护策略的全链路落地
- 用户权限管理的完善

---

## 2. 功能现状分析

### 2.1 结果集查看功能 (已支持屏蔽)

#### 接口信息

```
接口路径: /api/rest_j/v1/filesystem/openFile
请求方法: GET
Controller类: org.apache.linkis.filesystem.restful.api.FsRestfulApi
实现方法: openFile() (行625-777)
```

#### 敏感字段屏蔽参数

| 参数名 | 类型 | 是否必填 | 说明 | 示例 |
|-------|------|---------|------|------|
| maskedFieldNames | String | 否 | 需要屏蔽的字段名,多个字段用逗号分隔(不区分大小写) | password,apikey,secret_token |

#### 屏蔽实现机制

**实现位置**: FsRestfulApi.java 行735-858

```java
// 1. 解析屏蔽字段列表
Set<String> maskedFields =
    new HashSet<>(Arrays.asList(maskedFieldNames.toLowerCase().split(",")));

// 2. 过滤元数据
Map[] metadata = filterMaskedFieldsFromMetadata(resultmap, maskedFields);

// 3. 移除数据内容中的对应列
List<String[]> fileContent =
    removeFieldsFromContent(resultmap, result.getSecond(), maskedFields);
```

**关键方法**:

1. `filterMaskedFieldsFromMetadata()` (行841-858): 从元数据中过滤屏蔽字段
2. `removeFieldsFromContent()` (行787-838): 从内容数据中移除屏蔽字段列

**特性**:
- 不区分大小写匹配字段名
- 从后向前删除列索引,避免索引变化问题
- 同时处理元数据和内容数据

---

### 2.2 单结果集下载功能 (不支持屏蔽)

#### 接口信息

```
接口路径: /api/rest_j/v1/filesystem/resultsetToExcel
请求方法: GET
Controller类: org.apache.linkis.filesystem.restful.api.FsRestfulApi
实现方法: resultsetToExcel() (行972-1084)
```

#### 核心参数

| 参数名 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| path | String | - | 结果集文件路径(必填) |
| outputFileType | String | csv | 导出格式: csv 或 xlsx |
| csvSeparator | String | , | CSV分隔符 |
| outputFileName | String | downloadResultset | 输出文件名 |
| sheetName | String | result | Excel sheet名称 |
| nullValue | String | NULL | null值替换字符串 |
| limit | Integer | 0 | 行数限制(0表示使用配置值) |
| autoFormat | Boolean | false | 是否自动格式化 |
| keepNewline | Boolean | false | 是否保留换行符 |

#### 实现流程

```
用户请求 → 权限验证 → 文件系统操作 → 格式判断(CSV/XLSX)
→ Writer初始化 → 数据写入 → 响应流输出
```

**问题**: 当前实现直接将完整的结果集数据写入输出流,**没有任何字段过滤或屏蔽逻辑**。

---

### 2.3 多结果集下载功能 (不支持屏蔽)

#### 接口信息

```
接口路径: /api/rest_j/v1/filesystem/resultsetsToExcel
请求方法: GET
Controller类: org.apache.linkis.filesystem.restful.api.FsRestfulApi
实现方法: resultsetsToExcel() (行1105-1189)
```

#### 核心参数

| 参数名 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| path | String | - | 结果集目录路径(必填) |
| outputFileName | String | downloadResultset | 输出文件名 |
| nullValue | String | NULL | null值替换字符串 |
| limit | Integer | 0 | 每个结果集的行数限制 |
| autoFormat | Boolean | false | 是否自动格式化 |

#### 特殊说明

- **仅支持XLSX格式**
- path参数为目录路径,包含多个结果集文件
- 使用`StorageMultiExcelWriter`将多个结果集合并到单个Excel的不同Sheet
- 自动按文件序号排序: `ResultSetUtils.sortByNameNum()`

**问题**: 与单结果集下载类似,**没有任何字段过滤或屏蔽逻辑**。

---

### 2.4 结果集导出功能 (不支持屏蔽)

#### 功能说明

结果集导出功能与下载功能**不同**,它使用**Pipeline引擎**将dolphin结果集文件导出到服务器共享目录,而非直接下载到客户端。

#### 实现方式

**核心引擎**: Pipeline引擎
**实现语言**: Scala
**代码路径**: `linkis-engineconn-plugins/pipeline/`

#### 工作流程

```
用户操作流程:
1. 用户在前端点击"导出"按钮
2. 前端弹出导出配置对话框 (resultsExport.vue)
3. 用户选择:
   - 导出文件名
   - 导出格式 (CSV/Excel)
   - 目标路径 (服务器共享目录)
   - 是否导出全部结果集 (多结果集时)
4. 前端生成Pipeline代码: from <源路径> to <目标路径>
5. 提交Pipeline脚本到引擎执行
6. Pipeline引擎读取dolphin文件 → 转换格式 → 写入目标目录
```

#### 前端实现

**文件路径**: `linkis-web/src/components/consoleComponent/resultsExport.vue`

**导出配置参数**:

| 参数 | 类型 | 说明 | 验证规则 |
|-----|------|------|---------|
| name | String | 导出文件名 | 1-200字符,仅支持英文/数字/中文 |
| path | String | 目标目录路径 | 必填,从目录树选择 |
| format | String | 导出格式 | 1=CSV, 2=Excel |
| isAll | Boolean | 是否导出全部结果集 | 仅多结果集且Excel格式时可选 |

**Pipeline代码生成逻辑**:

```javascript
// resultsExport.vue 导出确认方法
exportConfirm() {
  // 生成临时脚本名称
  const tabName = `new_stor_${Date.now()}.out`;

  // 确定源路径
  let temPath = this.currentPath;  // 当前结果集路径
  if (this.isAll) {
    // 导出全部时,源路径为目录(不带文件名)
    temPath = temPath.substring(0, temPath.lastIndexOf('/'));
  }

  // 根据格式添加扩展名
  const exportOptionName = this.exportOption.format === '2'
    ? `${this.exportOption.name}.xlsx`
    : `${this.exportOption.name}.csv`;

  // 生成Pipeline执行代码
  const code = `from ${temPath} to ${this.exportOption.path}/${exportOptionName}`;

  // 添加临时脚本并自动执行
  this.dispatch('Workbench:add', { id: md5Path, code, saveAs: true }, (f) => {
    this.$nextTick(() => {
      this.dispatch('Workbench:run', { id: md5Path });
    });
  });
}
```

#### Pipeline引擎实现

**执行入口**: `PipelineEngineConnExecutor.scala` (行69-89)

```scala
// 正则解析Pipeline语法
val regex = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?".r

code match {
  case regex(sourcePath, destPath) =>
    // 选择合适的执行器
    PipelineExecutorSelector
      .select(sourcePath, destPath, options)
      .execute(sourcePath, destPath, engineExecutorContext)
}
```

**执行器选择逻辑**: `PipelineExecutorSelector.scala`

```scala
def select(sourcePath: String, destPath: String, options: Map[String, String]): PipeLineExecutor = {
  // 根据目标文件扩展名选择执行器
  getSuffix(destPath) match {
    case ".csv" => CSVExecutor           // CSV导出
    case ".xlsx" => ExcelExecutor        // Excel导出
    case _ if sameFileName => CopyExecutor  // 文件复制
    case _ => throw UnsupportedOutputTypeException
  }
}
```

#### 三大执行器实现

##### 1. CSVExecutor - CSV格式导出

**文件**: `CSVExecutor.scala`

**执行流程**:

```scala
override def execute(sourcePath: String, destPath: String, context: EngineExecutionContext): ExecuteResponse = {
  // 1. 验证源文件是否为结果集
  if (!FileSource.isResultSet(sourcePath)) {
    throw NotAResultSetFileException
  }

  // 2. 创建文件系统
  val sourceFs = FSFactory.getFs(new FsPath(sourcePath))
  val destFs = FSFactory.getFs(new FsPath(destPath))

  // 3. 创建FileSource读取结果集
  val fileSource = FileSource.create(new FsPath(sourcePath), sourceFs)

  // 4. 获取配置参数
  val nullValue = options.getOrDefault("pipeline.output.shuffle.null.type", "NULL")
  val charset = options.getOrDefault("pipeline.output.charset", "UTF-8")
  val separator = options.getOrDefault("pipeline.field.split", ",")
  val quoteRetouchEnable = options.getOrDefault("pipeline.field.quote.retoch.enable", false)

  // 5. 创建CSV Writer
  val outputStream = destFs.write(new FsPath(destPath), isOverwrite = true)
  val csvWriter = CSVFsWriter.getCSVFSWriter(charset, separator, quoteRetouchEnable, outputStream)

  // 6. 写入数据 (仅处理nullValue参数)
  fileSource.addParams("nullValue", nullValue).write(csvWriter)

  // 7. 清理资源
  IOUtils.closeQuietly(csvWriter)
  IOUtils.closeQuietly(fileSource)
}
```

**问题**: ❌ **没有任何敏感字段屏蔽逻辑**

##### 2. ExcelExecutor - Excel格式导出

**文件**: `ExcelExecutor.scala`

**执行流程**:

```scala
override def execute(sourcePath: String, destPath: String, context: EngineExecutionContext): ExecuteResponse = {
  val sourceFs = FSFactory.getFs(new FsPath(sourcePath))
  val destFs = FSFactory.getFs(new FsPath(destPath))

  val outputStream = destFs.write(new FsPath(destPath), isOverwrite = true)

  // 支持两种模式:
  // 模式1: 单个结果集文件 (sourcePath包含".")
  if (sourcePath.contains(".")) {
    val fileSource = FileSource.create(new FsPath(sourcePath), sourceFs)
    val excelWriter = ExcelFsWriter.getExcelFsWriter(
      charset = "utf-8",
      sheetName = "result",
      dateFormat = "yyyy-MM-dd HH:mm:ss",
      outputStream,
      autoFormat = false
    )
    fileSource.addParams("nullValue", nullValue).write(excelWriter)
  }
  // 模式2: 多个结果集 (sourcePath为目录)
  else {
    val fsPathList = sourceFs.listPathWithError(new FsPath(sourcePath)).getFsPaths
    ResultSetUtils.sortByNameNum(fsPathList)  // 按序号排序
    val fileSource = FileSource.create(fsPathList.toArray, sourceFs)
    val multiExcelWriter = new StorageMultiExcelWriter(outputStream, autoFormat)
    fileSource.addParams("nullValue", nullValue).write(multiExcelWriter)
  }
}
```

**问题**: ❌ **同样没有敏感字段屏蔽逻辑**

##### 3. CopyExecutor - 文件复制

**文件**: `CopyExecutor.scala`

```scala
override def execute(sourcePath: String, destPath: String, context: EngineExecutionContext): ExecuteResponse = {
  val sourceFs = FSFactory.getFs(new FsPath(sourcePath))
  val destFs = FSFactory.getFs(new FsPath(destPath))

  val inputStream = sourceFs.read(new FsPath(sourcePath))
  val outputStream = destFs.write(new FsPath(destPath), isOverwrite = true)

  // 直接流复制,不做任何处理
  IOUtils.copy(inputStream, outputStream)
}
```

**问题**: ❌ **直接复制文件,完全绕过所有检查**

#### 关键配置项

**文件**: `PipelineEngineConfiguration.scala`

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| pipeline.output.charset | UTF-8 | 输出字符集 |
| pipeline.field.split | , | CSV字段分隔符 |
| pipeline.output.shuffle.null.type | NULL | 空值替换标记 |
| pipeline.field.quote.retoch.enable | false | 引号处理开关 |
| pipeline.output.isoverwrite | true | 是否覆盖已存在文件 |
| wds.linkis.pipeline.export.excel.auto_format.enable | false | Excel自动格式化 |

#### Dolphin结果集文件格式

**文件**: `Dolphin.scala`

```scala
object Dolphin {
  val MAGIC = "dolphin"                    // 文件头魔数 (7字节)
  val DOLPHIN_FILE_SUFFIX = ".dolphin"     // 文件后缀
  val COL_SPLIT = ","                      // 列分隔符
  val NULL = "NULL"                        // 空值标记
  val INT_LEN = 10                         // 整数字段长度(固定10字节)
}
```

**文件结构**:
1. 文件头 (7字节): "dolphin"
2. 类型标识 (10字节): TABLE/PICTURE/TEXT等
3. 元数据区 (变长): 列名、数据类型、注释等
4. 数据区 (变长): 按行存储的数据记录

#### 结果集读取流程

**FileSource.scala** → **ResultsetFileSource.scala** → **StorageCSVWriter.scala**

```scala
// ResultsetFileSource.scala - 结果集字段处理
class ResultsetFileSource(fileSplits: Array[FileSplit]) extends AbstractFileSource(fileSplits) {
  // 应用shuffle变换 (仅处理NULL值和Double格式)
  shuffle({
    case t: TableRecord =>
      new TableRecord(t.row.map {
        case null | "NULL" =>
          val nullValue = getParams.getOrDefault("nullValue", "NULL")
          nullValue
        case value: Double => StorageUtils.doubleToString(value)
        case rvalue => rvalue
      })
  })
}
```

**关键发现**:
- ✅ 有NULL值处理
- ✅ 有数值格式化
- ❌ **没有字段级别的过滤或屏蔽**
- ❌ **没有敏感字段检查**
- ❌ **没有数据脱敏处理**

#### 导出目标路径

根据配置项 `wds.linkis.filesystem.root.path`:

| 文件系统类型 | 默认根路径 | 说明 |
|------------|----------|------|
| 本地文件系统 | file:///tmp/linkis/ | LOCAL_USER_ROOT_PATH |
| HDFS | hdfs:///tmp/{user}/linkis/ | HDFS_USER_ROOT_PATH_PREFIX + user + SUFFIX |

**用户可选择的导出路径**:
- 个人工作目录
- 共享目录(如配置允许)
- 项目目录

#### 核心安全问题

##### 问题1: dolphin源文件包含所有字段

```
执行SQL: SELECT name, password, email FROM users;
      ↓
生成结果集: /user/hadoop/linkis/result_001.dolphin
      ↓
dolphin文件内容:
  - 元数据: [name, password, email]
  - 数据: ["Alice", "pwd123", "alice@example.com"]
           ["Bob", "secret456", "bob@example.com"]
```

**问题**: 结果集文件已包含所有敏感字段

##### 问题2: 导出时未进行字段屏蔽

```
用户执行导出:
  from /user/hadoop/linkis/result_001.dolphin
  to /shared/exports/users.csv
      ↓
CSVExecutor.execute() 流程:
  1. FileSource.create() - 读取dolphin文件
  2. fileSource.addParams("nullValue", "NULL")
  3. fileSource.write(csvWriter)
      ↓
输出文件 /shared/exports/users.csv:
  name,password,email
  Alice,pwd123,alice@example.com
  Bob,secret456,bob@example.com
```

**问题**: ❌ **password字段未被屏蔽,直接导出**

##### 问题3: 导出文件存在数据泄露风险

```
导出后的文件位置:
  - 服务器共享目录 (/shared/exports/)
  - 其他用户可能有读权限
  - 文件未加密
  - 没有访问审计
```

**风险**:
- 敏感数据以明文形式存储在共享目录
- 可被其他有权限的用户访问
- 可被复制或传播
- 难以追踪数据流向

#### 与下载功能的对比

| 维度 | 下载功能 (resultsetToExcel) | 导出功能 (Pipeline) |
|-----|------------------------|------------------|
| **触发方式** | REST API调用 | Pipeline脚本执行 |
| **数据流向** | 服务器 → 客户端浏览器 | 服务器 → 服务器目录 |
| **格式转换** | FsRestfulApi中实现 | Pipeline执行器实现 |
| **敏感字段屏蔽** | ❌ 不支持 | ❌ 不支持 |
| **行数限制** | ✅ 支持 (默认5000) | ❌ 不限制 |
| **权限检查** | ✅ checkIsUsersDirectory() | ⚠️ 仅文件系统级别 |
| **审计日志** | ✅ 有日志记录 | ⚠️ 仅引擎执行日志 |
| **文件访问控制** | ✅ 单次下载后用户控制 | ⚠️ 服务器文件系统权限 |

#### 完整的Pipeline导出执行链路

```
前端 resultsExport.vue
    ↓ [生成Pipeline代码]
from /user/hadoop/linkis/result.dolphin to /shared/export/file.csv
    ↓ [提交到Workbench执行]
PipelineEngineConnExecutor.executeLine()
    ↓ [正则解析]
sourcePath = /user/hadoop/linkis/result.dolphin
destPath = /shared/export/file.csv
    ↓ [选择执行器]
PipelineExecutorSelector.select() → CSVExecutor
    ↓ [执行导出]
CSVExecutor.execute()
    ├─ FSFactory.getFs(sourcePath)
    ├─ FileSource.create(sourcePath, fs)
    │   └─ ResultSetFactory.getResultSetByPath()
    │       └─ ResultSetReader.getResultSetReader()
    │           └─ 读取dolphin文件 (含所有字段)
    ├─ CSVFsWriter.getCSVFSWriter()
    ├─ fileSource.addParams("nullValue", "NULL")
    └─ fileSource.write(csvWriter)
        ├─ ResultsetFileSource.shuffle() [仅NULL值处理]
        ├─ StorageCSVWriter.addMetaData() [写入所有列名]
        └─ StorageCSVWriter.addRecord() [写入所有数据]
            ↓
输出文件: /shared/export/file.csv (包含所有敏感字段)
```

**关键发现**:
- 整个链路中**没有任何一个环节**检查或过滤敏感字段
- 所有字段从dolphin文件原样转换到目标格式
- 用户可以轻松绕过任何前置的敏感数据检查

---

## 3. 需求详细说明

### 3.1 核心需求

**在结果集下载和导出时支持敏感字段屏蔽功能,与查看功能保持一致的安全策略,全面堵塞敏感数据泄露渠道。**

**涉及的三个功能模块**:
1. ✅ **结果集查看** (`/api/rest_j/v1/filesystem/openFile`) - 已支持屏蔽
2. ❌ **结果集下载** (`/api/rest_j/v1/filesystem/resultsetToExcel`, `resultsetsToExcel`) - 需要支持
3. ❌ **结果集导出** (Pipeline引擎: `CSVExecutor`, `ExcelExecutor`) - 需要支持

### 3.2 功能要求

#### 3.2.1 参数设计

##### (1) 下载接口参数扩展

在`resultsetToExcel`和`resultsetsToExcel`两个接口中**新增可选参数**:

| 参数名 | 类型 | 是否必填 | 默认值 | 说明 |
|-------|------|---------|-------|------|
| maskedFieldNames | String | 否 | null | 需要屏蔽的字段名,多个字段用逗号分隔 |

**示例请求**:

```
GET /api/rest_j/v1/filesystem/resultsetToExcel?path=/user/result.dolphin&outputFileType=csv&maskedFieldNames=password,apikey,ssn
```

##### (2) Pipeline导出语法扩展 ⭐

**职责划分**:
- **其他团队**: 负责前端交互和Pipeline代码生成（包含屏蔽字段）
- **我们团队**: 负责Pipeline引擎执行（解析语法并应用屏蔽逻辑）

**新增Pipeline语法**:

```
from <源路径> to <目标路径> without "<字段名1,字段名2,...>"
```

**语法规则**:
- `without` 关键字后跟屏蔽字段列表
- 字段名用**双引号**包裹
- 多个字段用**逗号分隔**（不区分大小写）
- 双引号内可包含空格

**示例**:

```sql
-- 示例1: 屏蔽单个字段
from /user/result.dolphin to /export/file.csv without "password"

-- 示例2: 屏蔽多个字段
from /user/result.dolphin to /export/users.xlsx without "password,apikey,credit_card"

-- 示例3: 字段名包含空格
from /user/result.dolphin to /export/data.csv without "user password, api key, credit card"

-- 示例4: 不屏蔽（保持原语法）
from /user/result.dolphin to /export/file.csv
```

**语法兼容性**:
- ✅ 向后兼容：不使用`without`子句时，保持原有行为
- ✅ 大小写不敏感：`WITHOUT`、`without`、`Without`均可
- ✅ 空格容忍：关键字前后的空格会被自动处理

#### 3.2.2 屏蔽规则

1. **字段匹配**
   - 不区分大小写
   - 精确匹配字段名(columnName)
   - 支持多字段,使用逗号分隔

2. **屏蔽方式**
   - 完全移除屏蔽字段列(而非替换为空值或掩码)
   - 同时处理元数据(metadata)和数据内容(fileContent)
   - 保持与`openFile`接口的一致性

3. **异常处理**
   - 如果指定的屏蔽字段不存在,不报错,正常导出
   - 如果所有字段都被屏蔽,返回空结果集(仅包含结果集结构)

#### 3.2.3 兼容性要求

1. **向后兼容**: 不传`maskedFieldNames`参数时,保持原有行为(导出完整数据)
2. **格式兼容**: 支持CSV和XLSX两种导出格式
3. **性能要求**: 字段屏蔽逻辑不应显著影响导出性能

---

### 3.3 技术实现要求

#### 3.3.1 代码复用

- **复用现有方法**: 直接复用`openFile`中已实现的以下方法:
  - `filterMaskedFieldsFromMetadata()` (FsRestfulApi.java 行841-858)
  - `removeFieldsFromContent()` (FsRestfulApi.java 行787-838)

- **考虑重构**: 如果方法访问级别不合适,建议将这两个方法:
  - 从`private`修改为`protected`或提取到工具类
  - 或直接复制到下载方法和Pipeline执行器中

#### 3.3.2 实现位置

##### (1) 下载功能实现位置

**文件**: `FsRestfulApi.java`

**修改方法**:
1. `resultsetToExcel()` (行972-1084)
2. `resultsetsToExcel()` (行1105-1189)

**关键修改点**:

```java
// 在fileSource.write(fsWriter)之前添加字段过滤逻辑

if (StringUtils.isNotBlank(maskedFieldNames)) {
    Set<String> maskedFields =
        new HashSet<>(Arrays.asList(maskedFieldNames.toLowerCase().split(",")));

    // 获取元数据并过滤
    // 修改FileSource或Writer以支持字段过滤
    // 具体实现需要根据Linkis Storage层的架构决定
}
```

##### (2) Pipeline导出功能实现位置

**核心修改**: `PipelineEngineConnExecutor.scala`

**关键变更**: 扩展正则表达式以支持`without`子句

```scala
// 原有正则（仅支持 from ... to ...）
val regex = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?".r

// 🆕 新正则（支持 from ... to ... without "..."）
val regexWithMask = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+without\\s+\"([^\"]+)\"\\s*".r
val regexNormal = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s*".r
```

**执行逻辑修改**:

```scala
// PipelineEngineConnExecutor.scala (行69-89 修改)
override def executeLine(
    engineExecutorContext: EngineExecutionContext,
    code: String
): ExecuteResponse = {

  thread = Thread.currentThread()

  // 🔍 优先匹配带without子句的语法
  code match {
    // 情况1: 包含屏蔽字段
    case regexWithMask(sourcePath, destPath, maskedFields) =>
      logger.info(s"Pipeline execution with masked fields: $maskedFields")

      // 将屏蔽字段传递给执行器
      val enhancedOptions = new util.HashMap[String, String](newOptions)
      enhancedOptions.put("pipeline.masked.field.names", maskedFields)

      PipelineExecutorSelector
        .select(sourcePath, destPath, enhancedOptions)
        .execute(sourcePath, destPath, engineExecutorContext)

    // 情况2: 不包含屏蔽字段（保持原有行为）
    case regexNormal(sourcePath, destPath) =>
      logger.info(s"Pipeline execution without masking")

      PipelineExecutorSelector
        .select(sourcePath, destPath, newOptions)
        .execute(sourcePath, destPath, engineExecutorContext)

    // 情况3: 语法错误
    case _ =>
      throw new PipeLineErrorException(
        ILLEGAL_OUT_SCRIPT.getErrorCode,
        ILLEGAL_OUT_SCRIPT.getErrorDesc + ". Syntax: from <source> to <dest> [without \"fields\"]"
      )
  }
}
```

**正则表达式详解**:

```scala
// 正则结构分析
val regexWithMask = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+without\\s+\"([^\"]+)\"\\s*".r

// 组成部分:
// (?i)           - 大小写不敏感
// \\s*           - 可选的前导空格
// from           - 关键字 "from"
// \\s+           - 必需的空格
// (\\S+)         - 第1组: 源路径（非空白字符）
// \\s+           - 必需的空格
// to             - 关键字 "to"
// \\s+           - 必需的空格
// (\\S+)         - 第2组: 目标路径（非空白字符）
// \\s+           - 必需的空格
// without        - 关键字 "without"
// \\s+           - 必需的空格
// \"             - 左双引号
// ([^\"]+)       - 第3组: 屏蔽字段列表（除双引号外的任意字符）
// \"             - 右双引号
// \\s*           - 可选的尾随空格
```

**测试用例**:

```scala
// 测试1: 标准语法
"from /a/b.dolphin to /c/d.csv without \"password,apikey\""
// 匹配结果:
//   sourcePath = "/a/b.dolphin"
//   destPath = "/c/d.csv"
//   maskedFields = "password,apikey"

// 测试2: 大小写不敏感
"FROM /a/b.dolphin TO /c/d.csv WITHOUT \"password\""
// 匹配成功

// 测试3: 字段名包含空格
"from /a/b.dolphin to /c/d.csv without \"user password, api key\""
// 匹配结果:
//   maskedFields = "user password, api key"

// 测试4: 兼容原语法
"from /a/b.dolphin to /c/d.csv"
// 匹配 regexNormal，maskedFields为空

// 测试5: 语法错误（缺少引号）
"from /a/b.dolphin to /c/d.csv without password"
// 不匹配任何正则，抛出异常
```

---

**修改文件**: `linkis-engineconn-plugins/pipeline/src/main/scala/org/apache/linkis/manager/engineplugin/pipeline/executor/`

**修改文件清单**:
1. ✅ `PipelineEngineConnExecutor.scala` - 正则解析和参数传递
2. ✅ `CSVExecutor.scala` - 读取options中的屏蔽字段
3. ✅ `ExcelExecutor.scala` - 读取options中的屏蔽字段

**无需修改**:
- ❌ 前端 `resultsExport.vue` - 由其他团队负责
- ❌ `PipelineEngineConfiguration.scala` - 参数通过options传递，无需新增配置

---

#### 3.3.3 实现难点分析

**挑战1**: `openFile`与下载/导出接口的数据流处理方式不同

- `openFile`: 调用`fileSource.collect()`获取完整数据后过滤
- `下载/导出接口`: 调用`fileSource.write(fsWriter)`直接流式写入输出流

**挑战2**: Pipeline执行器基于Scala实现,需要在Scala代码中实现字段过滤

**挑战3**: 需要在多个层级传递屏蔽字段参数

```
前端 resultsExport.vue
    ↓ startupMap参数
Entrance (任务提交)
    ↓ JobRequest.params
PipelineEngineConnExecutor
    ↓ options传递
CSVExecutor / ExcelExecutor
    ↓ 应用屏蔽逻辑
FileSource / Writer
```

**解决方案选型**:

##### 已确定方案：方案A - 在执行器中收集数据后过滤

**技术选型理由**:
- ✅ 实现简单，可快速交付
- ✅ 复用FsRestfulApi中已有的字段过滤逻辑
- ✅ 不需要修改Storage层，风险可控
- ✅ 适合当前MVP需求

**技术限制**:
- ⚠️ 对大结果集（>10万行）有内存压力
- ⚠️ 性能相对流式方案较低
- 💡 可通过结果集大小限制规避风险

**实现步骤**:

1. **PipelineEngineConnExecutor修改** (正则解析)

已在上述"(2) Pipeline导出功能实现位置"中详细说明。

2. **CSVExecutor实现** (`CSVExecutor.scala`)

```scala
override def execute(sourcePath: String, destPath: String, context: EngineExecutionContext): ExecuteResponse = {
  // 1. 🆕 从options获取屏蔽字段参数（由PipelineEngineConnExecutor传入）
  val maskedFieldNames = options.getOrDefault("pipeline.masked.field.names", "")

  // 2. 验证源文件
  if (!sourcePath.contains(STORAGE_RS_FILE_SUFFIX.getValue)) {
    throw new PipeLineErrorException(EXPROTING_MULTIPLE.getErrorCode, EXPROTING_MULTIPLE.getErrorDesc)
  }
  if (!FileSource.isResultSet(sourcePath)) {
    throw new PipeLineErrorException(NOT_A_RESULT_SET_FILE.getErrorCode, NOT_A_RESULT_SET_FILE.getErrorDesc)
  }

  // 3. 创建文件系统
  val sourceFsPath = new FsPath(sourcePath)
  val destFsPath = new FsPath(destPath)
  val sourceFs = FSFactory.getFs(sourceFsPath)
  sourceFs.init(null)
  val destFs = FSFactory.getFs(destFsPath)
  destFs.init(null)

  // 4. 创建FileSource
  val fileSource = FileSource.create(sourceFsPath, sourceFs)
  if (!FileSource.isTableResultSet(fileSource)) {
    throw new PipeLineErrorException(NOT_A_TABLE_RESULT_SET.getErrorCode, NOT_A_TABLE_RESULT_SET.getErrorDesc)
  }

  // 5. 获取配置参数
  var nullValue = options.getOrDefault(PIPELINE_OUTPUT_SHUFFLE_NULL_TYPE, "NULL")
  if (BLANK.equalsIgnoreCase(nullValue)) nullValue = ""

  // 6. 创建输出流和Writer
  val outputStream = destFs.write(destFsPath, PIPELINE_OUTPUT_ISOVERWRITE_SWITCH.getValue(options))
  OutputStreamCache.osCache.put(engineExecutionContext.getJobId.get, outputStream)

  val csvWriter = CSVFsWriter.getCSVFSWriter(
    PIPELINE_OUTPUT_CHARSET_STR.getValue(options),
    PIPELINE_FIELD_SPLIT_STR.getValue(options),
    PIPELINE_FIELD_QUOTE_RETOUCH_ENABLE.getValue(options),
    outputStream
  )

  // 7. 🔑 方案A核心逻辑：判断是否需要屏蔽字段
  try {
    if (StringUtils.isNotBlank(maskedFieldNames)) {
      logger.info(s"Applying field masking: $maskedFieldNames")

      // 7.1 解析屏蔽字段列表
      val maskedFields = maskedFieldNames.toLowerCase().split(",").map(_.trim).toSet

      // 7.2 收集完整数据
      val collectedData = fileSource.collect()

      // 7.3 过滤字段并写入
      filterAndWriteData(collectedData, maskedFields, csvWriter, nullValue)

    } else {
      // 原有流式写入逻辑（无屏蔽）
      logger.info("No field masking, using stream write")
      fileSource.addParams("nullValue", nullValue).write(csvWriter)
    }
  } finally {
    // 8. 资源清理
    IOUtils.closeQuietly(csvWriter)
    IOUtils.closeQuietly(fileSource)
    IOUtils.closeQuietly(sourceFs)
    IOUtils.closeQuietly(destFs)
  }

  super.execute(sourcePath, destPath, engineExecutionContext)
}

// 🆕 字段过滤和写入方法
private def filterAndWriteData(
    collectedData: Array[Pair[Object, ArrayList[String[]]]],
    maskedFields: Set[String],
    csvWriter: CSVFsWriter,
    nullValue: String
): Unit = {

  collectedData.foreach { pair =>
    // 获取元数据和内容
    val metadata = pair.getFirst.asInstanceOf[Array[util.Map[String, Any]]]
    val content = pair.getSecond

    // 计算需要保留的列索引
    val retainedIndices = metadata.zipWithIndex
      .filter { case (col, _) =>
        val columnName = col.get("columnName").toString.toLowerCase()
        !maskedFields.contains(columnName)  // 不在屏蔽列表中的字段
      }
      .map(_._2)
      .toList

    logger.info(s"Retained columns: ${retainedIndices.size}/${metadata.length}")

    // 过滤元数据
    val filteredMetadata = retainedIndices.map(i => metadata(i)).toArray
    val tableMetaData = new TableMetaData(
      filteredMetadata.map { col =>
        Column(
          col.get("columnName").toString,
          DataType.toDataType(col.get("dataType").toString),
          col.get("comment").toString
        )
      }
    )

    // 写入元数据
    csvWriter.addMetaData(tableMetaData)

    // 过滤并写入内容
    content.forEach { row =>
      val filteredRow = retainedIndices.map { i =>
        if (i < row.length) {
          val value = row(i)
          // 处理NULL值
          if (value == null || value.equals("NULL")) nullValue else value
        } else {
          nullValue
        }
      }.toArray

      csvWriter.addRecord(new TableRecord(filteredRow))
    }

    csvWriter.flush()
  }
}
```

3. **ExcelExecutor实现** (`ExcelExecutor.scala`)
   - 实现逻辑与CSVExecutor类似
   - 区别在于使用`ExcelFsWriter`替代`CSVFsWriter`
   - 支持单结果集和多结果集模式

4. **无需修改的部分**
   - ❌ `PipelineEngineConfiguration.scala` - 不需要新增配置项
   - ❌ `resultsExport.vue` - 由其他团队负责代码生成

---

**其他备选方案** (供未来优化参考):

<details>
<summary>方案B: 扩展Writer实现 (性能优化方案)</summary>

- 创建`MaskedFieldsCSVFsWriter`和`MaskedFieldsExcelFsWriter`
- 在Writer内部实现流式字段过滤
- 优点: 内存友好，性能优越
- 缺点: 需修改Storage层，开发周期长

</details>

<details>
<summary>方案C: FileSource原生支持 (终极方案)</summary>

- 在`FileSource`中添加`excludeColumns()`方法
- 架构层面的解决方案，对上层透明
- 优点: 最优雅，所有场景受益
- 缺点: 影响范围大，需深度测试

</details>

<details>
<summary>方案D: Decorator模式 (方案B的优化版)</summary>

- 不修改现有Writer，遵循开闭原则
- 使用装饰器包装Writer实现字段过滤
- 优点: 灵活，可组合
- 缺点: 增加代码复杂度

</details>

**实施建议**: 先实现方案A快速上线，后续根据性能监控考虑升级到方案D

---

### 3.4 配置项说明

#### 现有配置项

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| wds.linkis.workspace.resultset.download.is.limit | true | 是否限制下载大小 |
| wds.linkis.workspace.resultset.download.maxsize.csv | 5000 | CSV下载最大行数 |
| wds.linkis.workspace.resultset.download.maxsize.excel | 5000 | Excel下载最大行数 |

#### 新增配置项（方案A专用）

| 配置项 | 默认值 | 说明 | 重要性 |
|-------|-------|------|--------|
| **pipeline.masked.field.names** | "" | Pipeline导出时屏蔽的字段名列表（逗号分隔） | 核心功能 |
| **pipeline.export.max.rows** | 100000 | Pipeline导出时允许的最大行数（启用屏蔽时） | ⚠️ 内存保护 |
| **pipeline.export.memory.check.enabled** | true | 是否启用内存检查 | ⚠️ 风险控制 |
| **pipeline.export.memory.threshold** | 0.8 | 内存使用阈值（占总内存比例） | ⚠️ 风险控制 |

#### 配置建议

**生产环境推荐配置**:

```properties
# 启用结果集大小限制（方案A必需）
pipeline.export.max.rows=50000

# 启用内存检查
pipeline.export.memory.check.enabled=true
pipeline.export.memory.threshold=0.75

# 下载功能限制（保持现有）
wds.linkis.workspace.resultset.download.is.limit=true
wds.linkis.workspace.resultset.download.maxsize.csv=5000
wds.linkis.workspace.resultset.download.maxsize.excel=5000
```

**开发/测试环境配置**:

```properties
# 可适当放宽限制
pipeline.export.max.rows=100000
pipeline.export.memory.check.enabled=false
```

**内存充足环境配置**:

```properties
# 服务器内存>=32GB时可考虑
pipeline.export.max.rows=500000
pipeline.export.memory.threshold=0.85
```

---

## 4. 风险评估

### 4.1 技术风险

| 风险 | 等级 | 应对措施 | 备注 |
|-----|------|---------|------|
| **方案A内存溢出风险** | 中 | 1. 配置结果集导出行数上限（建议10万行）<br>2. 添加内存监控和告警<br>3. 大结果集提示用户分批导出 | 方案A主要风险 |
| Storage层兼容性问题 | 低 | 充分的兼容性测试，确保Scala/Java互操作正常 | - |
| 性能下降 | 低 | 1. 仅在指定屏蔽字段时启用过滤逻辑<br>2. 不影响未启用屏蔽的导出性能 | - |
| Pipeline参数传递失败 | 低 | 1. 参数传递链路日志记录<br>2. 异常情况降级为不屏蔽 | - |
| 字段过滤逻辑错误 | 中 | 1. 完整的单元测试覆盖<br>2. 与openFile功能对比测试 | 需充分测试 |

### 4.2 业务风险

| 风险 | 等级 | 应对措施 | 备注 |
|-----|------|---------|------|
| 向后兼容性问题 | 低 | 新增可选参数，不传参数时保持原有行为 | - |
| 误屏蔽正常字段 | 低 | 1. 明确文档说明字段名匹配规则<br>2. 前端提供字段名预览和校验 | - |
| 用户体验影响 | 低 | 1. 前端提供友好的配置界面<br>2. 屏蔽字段输入支持自动补全 | 可选优化 |
| 大结果集导出超时 | 中 | 1. 方案A会增加导出时间<br>2. 建议限制行数上限<br>3. 显示导出进度提示 | 方案A特有 |

### 4.3 方案A的特殊风险控制

#### 风险1：内存溢出

**触发条件**:
- 结果集行数 > 10万行
- 结果集列数 > 1000列
- 并发导出任务过多

**监控指标**:
```scala
// 添加内存使用监控
val runtime = Runtime.getRuntime
val usedMemory = runtime.totalMemory() - runtime.freeMemory()
if (usedMemory > MAX_MEMORY_THRESHOLD) {
  logger.warn(s"Memory usage high: $usedMemory bytes")
  throw new PipeLineErrorException("Memory limit exceeded")
}
```

**应对措施**:
1. **配置层控制**: 新增配置项限制导出行数
   ```scala
   val PIPELINE_EXPORT_MAX_ROWS = CommonVars("pipeline.export.max.rows", 100000)
   ```

2. **运行时检查**: 在collect()前检查结果集大小
   ```scala
   val totalLine = fileSource.getTotalLine
   if (totalLine > PIPELINE_EXPORT_MAX_ROWS.getValue) {
     throw new PipeLineErrorException(
       s"Result set too large: $totalLine rows, max allowed: ${PIPELINE_EXPORT_MAX_ROWS.getValue}"
     )
   }
   ```

3. **用户提示**: 前端显示结果集大小，超过阈值时警告

#### 风险2：性能下降

**影响评估**:
- 原流式写入: 无需加载全部数据到内存
- 方案A: 需先collect()全部数据，再过滤，再写入
- **预估性能损失**: 30-50% (取决于结果集大小)

**缓解措施**:
1. 仅在指定屏蔽字段时启用collect模式
2. 未指定屏蔽字段时保持原流式写入
3. 添加性能日志，监控导出耗时

---

## 5. 变更历史

| 版本 | 日期 | 变更内容 | 作者 |
|-----|------|---------|------|
| v1.0 | 2025-10-27 | 初始版本 - 完成需求分析和技术方案设计 | Claude Code |

---

**文档结束**

