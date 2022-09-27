## linkis-scheduler   errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-scheduler  |11055|The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去观看EngineConnManager的linkis.out和linkis.log日志).|THE_REQUEST_ENGINE_TIMES_OUT|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12000|Task status flip error! Cause: Failed to flip from {} to $state.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)|TASK_STATUS_FLIP_STATE|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12001|The submission job failed and the queue is full!(提交作业失败，队列已满！)|THE_SUNMISSION_IS_FULL|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12011|Unrecognized execId (不能识别的execId):", "Unrecognized execId (不能识别的execId):|UNRECOGNIZED_EXECID|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |13000|FIFOConsumerManager need a FIFOGroup, but {} is supported.(FIFOConsumerManager 需要一个 FIFOGroup，但支持 {}.)|NEED_A_SUPPORTED|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |13000|{} is in state {}. ({} 处于状态 {}.)|IS_IN_STATE|LinkisSchedulerErrorCodeSummary|

