## linkis-storage  errorcode

| 模块名(服务名) | 错误码  | 描述 |enumeration name(枚举)|  Exception Class|
| -------- | -------- | ----- |-----|-----|
|linkis-storage |50000|Unsupported result type:{0}(不支持的结果类型)|UNSUPPORTED_RESULT|LinkisStorageErrorCodeSummary|
|linkis-storage |50000|Unsupported file system type:{0}(不支持的文件系统类型)|UNSUPPORTED_FILE|LinkisStorageErrorCodeSummary|
|linkis-storage |50001|HDFS configuration was not read, please configure hadoop.config.dir or add env:HADOOP_CONF_DIR(HDFS 配置未读取，请配置 hadoop.config.dir 或添加 env:HADOOP_CONF_DIR)|CONFIGURATION_NOT_READ|LinkisStorageErrorCodeSummary|
|linkis-storage |51000|The file{}is empty(文件{}为空)|THE_FILE_IS_EMPTY|LinkisStorageErrorCodeSummary|
|linkis-storage |51000|failed to read integer(读取整数失败)|FAILED_TO_READ_INTEGER|LinkisStorageErrorCodeSummary|
|linkis-storage |52000|FSNotInitException|FSN_NOT_INIT_EXCEPTION|LinkisStorageErrorCodeSummary|
|linkis-storage |52001|Parsing metadata failed(解析元数据失败)|PARSING_METADATA_FAILED|LinkisStorageErrorCodeSummary|
|linkis-storage |52002|Result sets that are not tables are not supported(不支持不是表格的结果集)|TABLE_ARE_NOT_SUPPORTED|LinkisStorageErrorCodeSummary|
|linkis-storage |52002|Failed to init FS for user:(为用户初始化 FS 失败：)|FAILED_TO_INIT_USER|LinkisIoFileClientErrorCodeSummary|
|linkis-storage |52002|has been closed, IO operation was illegal.(已经关闭，IO操作是非法的.)|ENGINE_CLOSED_IO_ILLEGAL|LinkisIoFileClientErrorCodeSummary|
|linkis-storage |52002|storage has been closed.(存储已关闭.)|STORAGE_HAS_BEEN_CLOSED|LinkisIoFileClientErrorCodeSummary|
|linkis-storage |52002|proxy user not set, can not get the permission information.(没有设置代理 proxy 用户，无法获取权限信息)|NO_PROXY_USER|LinkisIoFileClientErrorCodeSummary|
|linkis-storage |52002|FS Can not proxy to:{}(FS 不能代理到：{}) |FS_CAN_NOT_PROXY_TO|LinkisIoFileErrorCodeSummary|
|linkis-storage |52004|You must register IOClient before you can use proxy mode.(必须先注册IOClient,才能使用代理模式)|MUST_REGISTER_TOC|LinkisStorageErrorCodeSummary|
|linkis-storage |52004|You must register IOMethodInterceptorCreator before you can use proxy mode.(必须先注册IOMethodInterceptorCreator，才能使用代理模式)|MUST_REGISTER_TOM|LinkisStorageErrorCodeSummary|
|linkis-storage |53001|please init first(请先初始化)|FS_NOT_INIT|StorageErrorCode|
|linkis-storage |53002|The read method parameter cannot be empty(read方法参数不能为空)|CANNOT_BE_EMPTY|LinkisIoFileErrorCodeSummary|
|linkis-storage |53003|Unsupported parameter calls(不支持的参数调用)|PARAMETER_CALLS|LinkisIoFileErrorCodeSummary|
|linkis-storage |53003|not exists method {} in fs {}(方法不存在) |NOT_EXISTS_METHOD|LinkisIoFileErrorCodeSummary|
|linkis-storage |54001|Unsupported open file type(不支持打开的文件类型)|UNSUPPORTED_OPEN_FILE_TYPE|LinkisStorageErrorCodeSummary|
|linkis-storage |65000|Invalid custom parameter(不合法的自定义参数)|INCALID_CUSTOM_PARAMETER|LinkisStorageErrorCodeSummary|
