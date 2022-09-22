{} linkis-cli-core errorcode


| 模块名(服务名) | 错误码  | 描述 | class |
| -------- | -------- | ----- |-----|
|linkis-cli-core|BLD0007|Cannot build UjesClientDriverContext: gatewayUrl is empty(无法构建 UjesClientDriverContext：gatewayUrl 为空)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0010|Cannot specify admin-user as submit-user(无法将管理员用户指定为提交用户)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0011|Submit-User should be the same as Auth-Key under Static-Authentication-Strategy (Submit-User 应与 Static-Authentication-Strategy 下的 Auth-Key 相同)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0005|User specified script file does not exist: (用户指定的脚本文件不存在)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0006|Cannot read user specified script file(无法读取用户指定的脚本文件)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0003|JobOperator of LinkisManageJob should be instance of LinkisJobOperator(LinkisManageJob 的 JobOperator 应该是 LinkisJobOperator 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0030|JobSubType is not instance of JobManSubType(JobSubType 不是 JobManSubType 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0002|JobSubType is not supported(不支持 JobSubType)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0036|user or jobID or execID is null(user 或 jobID 或 execID 为 null)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0034|JobData is not LinkisResultData(JobData 不是 LinkisResultData)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0034|JobData is not LinkisLogData(JobData 不是 LinkisLogData)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0038|jobStatus is null(工作状态为空)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0037|Got blank ResultLocation from server. Job may not have result-set. Will not try to retrieve any Result(从服务器获得空白 ResultLocation.作业可能没有结果集.不会尝试检索任何结果)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0039|Got null or empty ResultSetPaths(ResultSetPaths 为null 或者空)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0040|Something foes wrong. Got null as hasNextPage.(可能异常，hasNextPage 可能为null)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0013|Cannot get jobStatus from server continuously for N seconds. Client aborted! Error message'.(无法连续 N 秒从服务器获取 jobStatus.客户端中止！ 错误信息)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0035|logData is not Cloneable(logData 不可克隆)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0006|Job is in abnormal status(作业处于异常状态)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0041|onceJob is not properly initiated(onceJob 未正确启动)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0011|UjesClientDriver is null(UjesClientDriver 为空)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0012|Failed to submit job， Reason: {}(提交作业失败，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0013|Get job status failed. retry time (获取作业状态失败.重试时间{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0015|Get log failed. retry time : {}/{}. taskID={} Reason: {} ( 获取日志失败.重试时间{},任务id={},原因:{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0016|Get log failed. Retry time ( 获取日志失败.重试时间x)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0016|Get log failed. Retry exhausted. taskID={}, Reason: {} ( 获取日志失败.不能重试.任务 ID={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0017|Get log from openLog failed. Retry exhausted. taskID={}(从 openLog 获取日志失败.不能重试.任务ID={})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0017|Get log from openLog failed. Retry exhausted. taskID={}, Reason: {} (从 openLog 获取日志失败.不能重试.任务 ID={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0019|Get progress failed. Retry exhausted. taskID={}(获取进度失败.重试已用尽.任务ID={})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0020|Get progress failed. Retry exhausted. taskID={}, Reason: {} (获取进度失败.重试已用尽.任务 ID={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0025|Kill job failed. taskId={} Retry exhausted.(杀死作业失败.taskId={} 重试已用完。)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0025|Kill job failed. Retry exhausted. taskId={}, Reason: {} (杀死作业失败.不能重试.taskId={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0021|Get ResultSet Failed: Cannot get a valid jobInfo(获取 ResultSet 失败：无法获取有效的 jobInfo)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0021|Get ResultSet Failed: job Status is not Succeed.(获取 ResultSet 失败：作业状态不成功。)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0021|ResultLocation is blank.(结果位置为空白。)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0022|Get resultSetArray failed. Retry exhausted. taskID={}(获取 resultSetArray 失败.不能重试.任务ID={})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0023|Get resultSetArray failed. retry exhausted.  taskId={}, Reason: {} (获取 resultSetArray 失败.不能重试.taskId={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0024|Get resultSet failed. Retry exhausted. path= {}(获取结果集失败.不能重试.路径={})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0024|Get resultSet failed. Retry exhausted. Path={}, Reason: {}(获取结果集失败.不能重试.路径={}，原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0010|Cannot init UJESClient(无法初始化 UJESClient)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0010|Cannot init DWSClientConfig(无法初始化 DWSClientConfig)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0029|Command Type is not supported(不支持命令类型)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PST0001|Input model for LinkisLogPresenter is not instance of LinkisJobIncLogModel(LinkisLogPresenter 的输入模型不是 LinkisJobIncLogModel 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PST0001|  Input model for  LinkisResultInfoPresenter is not instance of LinkisResultInfoModel(LinkisResultInfoPresenter 的输入模型不是 LinkisResultInfoModel 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|EXE0029|Job is not completed but triggered ResultPresenter(作业未完成但触发 ResultPresenter)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PST0001|Input model for  LinkisResultPresenter is not instance of LinkisResultModel(LinkisResultPresenter 的输入模型不是 LinkisResultModel 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PST0002| Input PresentWay for  LinkisResultPresenter is not instance of PresentWayImpl(LinkisResultPresenter 的输入 PresentWay 不是 PresentWayImpl 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PST0002| Linkis resultsets are visited in descending order or are not visited one-by-one(  Linkis 结果集按降序访问或不逐一访问)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PRP0007|configuration root path specified by env variable:{}  is empty. (env 变量指定的配置根路径：{} 为空。)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|PRP0007|User cannot specify non-customizable configuration: {}  (用户不能指定不可定制的配置：{}  )|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0001|Input of UJESResultAdapter is not of correct type. Current type: {} (UJESResultAdapter 的输入类型不正确.当前类型： {})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0005|Failed to convert ResultSetMeta(ResultSetMeta 转换失败)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0007|Failed to convert ResultSet(转换结果集失败)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init LinkisJobInfoModel: {} is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：{} 不是 LinkisJobDataImpl 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010| Failed to init LinkisJobKillModel: {} is not instance of LinkisJobDataImpl(初始化 LinkisJobKillModel 失败：{} 不是 LinkisJobDataImpl 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init LinkisLogModel: {} is not instance of LinkisLogData(初始化 LinkisLogModel 失败：{} 不是 LinkisLogData 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init LinkisResultInfoModel: {} is not instance of LinkisResultData(初始化 LinkisResultInfoModel 失败：{} 不是 LinkisResultData 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init LinkisResultModel: {} is not instance of LinkisResultData(初始化 LinkisResultModel 失败：{} 不是 LinkisResultData 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init LinkisJobInfoModel: {} is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：{} 不是 LinkisJobDataImpl 的实例)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0001|Can only specify 1 of: {}(只能指定 1 个：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0001|has raw-value but failed to convert it into String-array. Raw-value:{}(具有原始值，但未能将其转换为字符串数组.原始值：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|CMD0011|Illegal argument::{}(非法论据：：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0001|Argument:{} is not a linkis-cli option. Assume it's script file, but no file named  is found (参数：{} 不是 linkis-cli 选项.假设它是脚本文件，但没有找到名为的文件)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0001|Can only specify at most one of linkis-cli option: {}(最多只能指定一个 linkis-cli 选项：)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0001|Need to specify at least one of linkis-cli option:{} or  script-path and script-arguments (需要指定至少一个 linkis-cli 选项：{} 或 script-path 和 script-arguments)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0007|Input of LinkisSubmitValidator is not instance of LinkisManageJob. Type: {}(LinkisSubmitValidator 的输入不是 LinkisManageJob 的实例.类型：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0008|LinkisJobMan validation failed. Reason: {}(LinkisJobMan 验证失败.原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0007|Input of LinkisSubmitValidator is not instance of LinkisSubmitJob. Type: {}(LinkisSubmitValidator 的输入不是 LinkisSubmitJob 的实例.类型：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0009|Input of UJESContextValidator is not instance of UjesClientDriverContext. Type: {}(UJESContextValidator 的输入不是 UjesClientDriverContext 的实例.类型：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|VLD0010|LinkisJob validation failed. Reason: {}(LinkisJob 验证失败.原因：{})|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0003|Cause: stdVarAccess or sysVarAccess is null(原因：stdVarAccess 或 sysVarAccess 为空)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0022|template: {} already exists(模板：{}已存在)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0010|Failed to set option value: optionMap contains objects that is not Option(设置选项值失败：optionMap 包含不是选项的对象)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD001|Failed to set param value: parameters contains objects that is not Parameter(设置参数值失败：参数包含不是参数的对象)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0009|input or template is null(输入或模板为空)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0013|failed to init parser: fitter is null(无法初始化解析器：fitter 为空)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0020|ParamMapper should not map different keys into same key.(ParamMapper 不应将不同的键映射到同一个键。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0018|failed to make deep copy of template(未能制作模板的深拷贝)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CMD0021|Input should be a Map-entry described by kv-pairs. e.g. key1=value1(输入应该是由 kv-pairs 描述的 Map-entry.例如 键1=值1)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|CODE-001|Failed to generate template.(无法生成模板。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0001|Null or empty Jobs is submitted to current execution.(将 Null 或空作业提交到当前执行。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0001|Backend for {} does not support async(后端不支持异步。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0001|SubExecType should not be null(SubExecType 不应为空。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0001|Multiple Jobs is not Supported by current execution(当前执行不支持多个作业。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0001|Backend for {} is not manageable(后端不可管理。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0002|Executor Type: {} is not Supported(执行器类型：{} 不支持。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0002|job is not instance of AsyncBackendJob(作业不是 AsyncBackendJob 的实例。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0005|Retry exhausted checking job submission. Job is probably not submitted(不能重试检查作业提交.作业可能未提交。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0031|Presenter or model is null(演示者或模型为空。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0027|Attempting to register a duplicate jobOperator, name:{} (正在尝试注册一个重复的 jobOperator, name:{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|EXE0028|Failed to get a reusable joboperator, name:{} (获取可重用作业操作符失败，名称:{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0010|Input for HelpInfoPresenter is not instance of model (HelpInfoPresenter 的输入不是模型的实例)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0012|Attempting to register a duplicate DisplayOperator, name: {}(尝试注册重复的 DisplayOperator，名称：{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0004|input data is not instance of FileDisplayData(输入数据不是 FileDisplayData 的实例)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0005|Cannot mkdir for path: {}(无法为路径 mkdir：{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0006|Cannot create file for path: {}(无法为路径创建文件：{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PST0008|input data is not instance of StdoutDisplayData(输入数据不是 StdoutDisplayData 的实例)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0004|Failed to  properties files because rootPath is empty(属性文件失败，因为 rootPath 为空)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0005|Failed to list properties files(无法列出属性文件)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0006|PropsFilesScanner has scanned 0 files given root:{}(PropsFilesScanner 扫描了 0 个给定 root 的文件:{})|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0003|properties loader is not inited because it contains no reader(属性加载器未启动，因为它不包含读取器)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0002|Source:{}propsPath(来源：{}propsPath)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|PRP0001|properties reader for source:{}propsPath  is not inited. because of blank propsId or propsPath(source:{}propsPath 的属性读取器未启动.因为空白的 propsId 或 propsPath)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|TFM0012|Failed to convert option into ParamItem: params contains duplicated identifier: {}(无法将选项转换为 ParamItem：参数包含重复的标识符：)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|TFM0010|Failed to init HelpInfoModel: {} is not instance of  CmdTemplate (初始化 HelpInfoModel 失败：{} 不是 CmdTemplate 的实例)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VLD0006|Input of ParsedTplValidator is not instance of CmdTemplate. (LinkisSubmitValidator 的输入不是 CmdTemplate 的实例。)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VLD0003|CmdOption value cannot be empty: paramName:{}paramName  CmdType: {}CmdType(CmdOption 值不能为空：paramName:{}paramName CmdType:{}CmdType)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VA0003|stdVarAccess is not inited. cmdParams:{}cmdParams defaultConf:{}defaultConf subMapCache:subMapCache (未启动 stdVarAccess.cmdParams:{}cmdParams defaultConf:{}defaultConf subMapCache:subMapCache)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VA0002|Cannot getVar {}KEY  from config. Cause: value is not String (无法从配置中获取 Var {}KEY.原因：值不是字符串)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VA0004|Cannot convertStringVal   to  : designated type is not supported(无法将StringVal 转换为：不支持指定类型)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VA0001|sys_prop and sys_env are both null(sys_prop 和 sys_env 都为空)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|VA0002|same key occurred in sys_prop and sys_env. will use sys_prop(sys_prop 和 sys_env 中出现了相同的键.将使用 sys_prop)|LinkisCliCoreErrorCodeSummary|
|linkis-cli-core|BLD0011| Authentication strategy  is not supported(不支持认证策略)|LinkisCliApplicationErrorCodeSummary|
|linkis-cli-core|BLD0010|Cannot specify proxy-user when proxy-user-specification switch is off(代理用户规范开关关闭时无法指定代理用户)|LinkisCliApplicationErrorCodeSummary|














