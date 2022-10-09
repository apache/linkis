## linkis-entrance  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-entrance |10000|Unsupported operation(不支持的操作)|UNSUPPORTED_OPERATION|EntranceErrorCodeSummary|
|linkis-entrance |20001|JobReq can't be null(JobReq不能为空)|JOBREQ_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20001|userCreator label or engineType label cannot null(userCreator 标签或 engineType 标签不能为空)|LABEL_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20001|Task is not requestPersistTask, cannot to create Executor(Task不是requestPersistTask，不能创建Executor)|NOT_CREATE_EXECUTOR|EntranceErrorCodeSummary|
|linkis-entrance |20001|Task is not EntranceJob, cannot to create Executor(Task 不是 EntranceJob，不能创建 Executor)|ENTRA_NOT_CREATE_EXECUTOR|EntranceErrorCodeSummary|
|linkis-entrance |20004|JobRequest cannot be null, unable to do persist operation(JobRequest 不能为空，无法进行持久化操作)|JOBREQUEST_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20004|instance can't be null(实例不能为空)|INSTANCE_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20005|execute user can't be null(执行用户不能为空)|EXECUTEUSER_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20007|param executionCode can not be empty (参数 executionCode 不能为空)|PARAM_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20007|param executionCode and scriptPath can not be empty at the same time(参数 executionCode 和 scriptPath 不能同时为空)|EXEC_SCRIP_NOT_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20010|Only code with runtype supported (仅支持运行类型的代码)|ONLY_CODE_SUPPORTED|EntranceErrorCodeSummary|
|linkis-entrance |20011|Request jobHistory failed, reason (请求jobHistory失败,原因):|REQUEST_JOBHISTORY_FAILED|EntranceErrorCodeSummary|
|linkis-entrance |20011|Request jobHistory failed, reason: jobRespProtocol is null (请求jobHistory失败,原因:jobRespProtocol为null)|JOBRESP_PROTOCOL_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20011|read all tasks failed, reason (读取所有任务失败，原因):|READ_TASKS_FAILED|EntranceErrorCodeSummary|
|linkis-entrance |20020|sender rpc failed(发件人 RPC 失败)|SENDER_RPC_FAILED|EntranceErrorCodeSummary|
|linkis-entrance |20039|failed to analysis task ! the reason is(分析任务失败！原因是):|FAILED_ANALYSIS_TASK|EntranceErrorCodeSummary|
|linkis-entrance |20052|Invalid engineType null, cannot use cache(无效的engineType null，不能使用缓存)|INVALID_ENGINETYPE_NULL|EntranceErrorCodeSummary|
|linkis-entrance |20052|Persist jobRequest error, please submit again later(存储Job异常，请稍后重新提交任务)|PERSIST_JOBREQUEST_ERROR|EntranceErrorCodeSummary|
|linkis-entrance |20053|Invalid resultsets, cannot use cache(结果集无效，无法使用缓存)|INVALID_RESULTSETS|EntranceErrorCodeSummary|
|linkis-entrance |30009|Submitting the query failed!(提交查询失败！)|SUBMITTING_QUERY_FAILED|EntranceErrorCodeSummary|
|linkis-entrance |50081|query from jobHistory status failed(从 jobHistory 状态查询失败)|QUERY_STATUS_FAILED|EntranceErrorCodeSummary|
|linkis-entrance |50081|get query response incorrectly(错误地获取查询响应)|GET_QUERY_RESPONSE|EntranceErrorCodeSummary|
|linkis-entrance |50081|query taskId  error,taskId(查询 taskId 错误,taskId):|QUERY_TASKID_ERROR|EntranceErrorCodeSummary|
|linkis-entrance |50081|query from jobhistory not a correct List type taskId is(来自 jobhistory 的查询不是正确的列表类型 taskId 是):|CORRECT_LIST_TYPR|EntranceErrorCodeSummary|
|linkis-entrance |50081|Shell code contains blacklisted code(shell中包含黑名单代码)|SHELL_BLACKLISTED_CODE|EntranceErrorCodeSummary|
|linkis-entrance |20301|logPath cannot be empty(日志路径不能为空)|LOGPATH_NOT_NULL|EntranceErrorCodeSummary|




