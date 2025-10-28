# AI Development Rules

> ⚠️ **CRITICAL**: 这些是强制性规则，AI必须无条件遵守。违反规则的代码将被拒绝合并。

## 📋 目录
- [需求实现步骤](#需求实现步骤)
- [最小改动原则](#最小改动原则)
- [功能可配置原则](#功能可配置原则)
- [数据库修改原则](#数据库修改原则)
- [配置管理规则](#配置管理规则)
- [代码边界约束](#代码边界约束)

### 需求实现步骤

#### 步骤1：确定当前版本号
- 查看pom.xml文件中的`<revision>`配置
- 如配置为`1.17.0-wds`，则提取版本号为`1.17.0`
- 后文用`${current_version}`代替

#### 步骤2：分支检查（必须通过才能继续）
**⚠️ 关键检查点：以下任一条件不满足，必须立即停止并提示用户手动处理**

执行以下检查：
1. 检查当前分支名称是否为`dev-${current_version}-webank`
   - 命令：`git branch --show-current`
   - 如果不是，停止并提示用户切换分支

2. 检查工作目录是否有未提交的修改
   - 命令：`git status`
   - 如果显示`Changes not staged for commit`或`Changes to be committed`，停止并提示用户处理

3. 检查分支是否与远程同步
   - 命令：`git status`
   - 如果显示`Your branch is behind`，执行`git pull`
   - 如果pull失败或有冲突，停止并提示用户处理

**只有以上3项检查全部通过后，才能继续后续步骤。**

#### 步骤3：创建新的需求修改分支
- 在确认的基础分支上创建新分支
- 分支命名规则：`feature/${current_version}-<需求简述>`

#### 步骤4：创建文档目录
- 创建目录：`docs/${current_version}/requirements`和`docs/${current_version}/design`
- 如果目录已存在则跳过

#### 步骤5：创建需求文档
- 按项目标准需求文档格式创建markdown文档
- 存放路径：`docs/${current_version}/requirements/<需求名称>.md`

#### 步骤6：创建设计文档
- 按项目标准设计文档格式创建markdown文档
- 存放路径：`docs/${current_version}/design/<需求名称>-design.md`

#### 步骤7：代码开发
- 按需求和设计文档进行开发
- 必须遵循本文件中的所有原则（最小改动、功能可配置等）

### 最小改动原则
- 所有功能实现必须遵循最小改动原则，修改内容尽量不影响现有功能。

### 功能可配置原则
- 所有功能必须增加功能开关，在开关关闭时功能相当于回退到上一个版本。开关配置遵循配置管理规则

### 数据库修改原则
- 在能不改动现有表结构和表数据的情况下尽量不改动
- 对于必须改动表结构和数据的情况下，将改动存档。具体路径如下
  - DDL：`linkis-dist/package/db/linkis_ddl.sql`
  - DML：`linkis-dist/package/db/linkis_dml.sql`

### 配置管理规则
- 所有配置统一使用`org.apache.linkis.common.conf.CommonVars`
- 参考示例：`org.apache.linkis.jobhistory.conf.JobhistoryConfiguration`
- 配置存放位置：当前模块的conf目录，一般为xxxConfiguration类

### 代码边界约束

#### 🚫 禁止操作
- **数据库结构**：除非明确指定，严禁修改现有表结构
- **第三方依赖**：不允许引入新的第三方依赖库
- **核心接口**：不得修改现有公共接口的签名

#### ✅ 允许操作
- **新增功能**：在不破坏现有逻辑的前提下扩展功能
- **新增配置**：在现有配置文件中新增配置项
- **新增表字段**：在现有表基础上新增字段

### 其它规则
- 所有功能只用实现后端接口功能，无需考虑前端设计和开发
