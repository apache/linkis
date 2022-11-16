## linkis-rpc errorcode


| 模块名(服务名) | 错误码  | 描述 | enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|linkis-rpc|10000|The service does not exist for the available Receiver.(服务不存在可用的Receiver.)|URL_ERROR|EngineConnManager|
|linkis-rpc|10000|method call failed:(方法调用失败：)|METHON_CALL_FAILED|LinkisRpcErrorCodeSummary|
|linkis-rpc|10001|The transmitted bean is Null.(传输的bean为Null.|TRANSMITTED_BEAN_IS_NULL|LinkisRpcErrorCodeSummary|
|linkis-rpc|10002|The timeout period is not set!(超时时间未设置！)|TIMEOUT_PERIOD|LinkisRpcErrorCodeSummary|
|linkis-rpc|10003|The corresponding anti-sequence class $objectClass was not found:(找不到对应的反序列类:)|CORRESPONDING_NOT_FOUND|LinkisRpcErrorCodeSummary|
|linkis-rpc|10004|The corresponding anti-sequence class:{0} failed to initialize(对应的反序列类:{0} 初始化失败|CORRESPONDING_TO_INITIALIZE|LinkisRpcErrorCodeSummary|
|linkis-rpc|10021|Failed to get user parameters! Reason: RPC request{0} Service failed!(获取用户参数失败！原因：RPC请求{0}服务失败！)|FETCH_MAPCACHE_ERROR|RPCErrorConstants|
|linkis-rpc|10051|The instance {0} of application {1} is not exists.(应用程序{0} 的实例{1} 不存在.)|APPLICATION_IS_NOT_EXISTS|LinkisRpcErrorCodeSummary|
|linkis-rpc|10054|Asyn RPC Consumer Thread has stopped!(Asyn RPC Consumer 线程已停止！)|RPC_INIT_ERROR|RPCErrorConstants|
|linkis-rpc|15555| Asyn RPC Consumer Queue is full, please retry after some times.（Asyn RPC Consumer Queue 已满，请稍后重试。）|RPC_RETRY_ERROR_CODE|DWCRPCRetryException|








