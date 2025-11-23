## linkis-scheduler   errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-scheduler  |11055|The job request engine time out (任务请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以查看EngineConnManager对应的out和log日志).|REQUEST_ENGINE_TIME_OUT|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12000|Task status flip error! Cause: Failed to flip from {} to {}.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)|TASK_STATUS_FLIP_ERROR|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12001|The submission job failed and the queue is full!(提交作业失败，队列已满！)|JOB_QUEUE_IS_FULL|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |12011|Unrecognized execId (不能识别的execId):", "Unrecognized execId (不能识别的execId):|UNRECOGNIZED_EXECID|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |13000|FIFOConsumerManager just support FIFO group, {} is not FIFO group.(FIFOConsumerManager只支持FIFO类型的消费组，{} 不是这类消费组.)|NEED_SUPPORTTED_GROUP|LinkisSchedulerErrorCodeSummary|
|linkis-scheduler  |13000|{} is in state {}. ({} 处于状态 {}.)|NODE_STATE_ERROR|LinkisSchedulerErrorCodeSummary|

