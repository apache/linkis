## linkis-engineconn-manager-server  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-engineconn-manager-server |11100|wait for engineConn initial timeout(请求引擎超时，可能是因为队列资源不足导致，请重试)|WAIT_FOR_ENGINECONN|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11101|wait for initial interrupted(请求引擎被中断，可能是因为你操作了引擎取消操作，请重试)|WAIT_FOR_INITIAL|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11102|Not supported BmlResource visibility type: label(不支持的 BmlResource visibility 类型：label).|NOT_SUPPORTED_TYPE|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11102|  |EC_START_FAILED|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11110|Cannot fetch more than {0} lines of logs.(无法获取超过{0}行的日志.)|CANNOT_FETCH_MORE_THAN|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11110|LogFile {0} does not exist or is not a file.(LogFile {0} 不存在或不是文件.)|LOGFILE_IS_NOT_EXISTS|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11110|the parameters of engineConnInstance and ticketId are both not exists.(engineConnInstance 和ticketId 的参数都不存在.)|BOTH_NOT_EXISTS|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |11110|Log directory {0} does not exist.(日志目录 {0} 不存在.)|LOG_IS_NOT_EXISTS|EngineconnServerErrorCodeSummary|
|linkis-engineconn-manager-server |911115|failed to downLoad(下载失败)|LOG_IS_NOT_EXISTS|EngineconnServerErrorCodeSummary|
