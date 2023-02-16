## spark  errorcode

| module name(模块名) | error code(错误码)  | describe(描述) |enumeration name(枚举)| Exception Class(类名)|
| -------- | -------- | ----- |-----|-----|
|spark |40001|read record  exception(读取记录异常)|READ_RECORD_EXCEPTION|SparkErrorCodeSummary|
|spark |40002|dataFrame to local exception(dataFrame 到本地异常)|DATAFRAME_EXCEPTION|SparkErrorCodeSummary|
|spark |40003| |OUT_ID|SparkErrorCodeSummary|
|spark |40004|Spark application has already stopped, please restart it(Spark 应用程序已停止，请重新启动)|SPARK_STOPPED|SparkErrorCodeSummary|
|spark |40005|execute sparkScala failed!(执行 sparkScala 失败！)|EXECUTE_SPARKSCALA_FAILED|SparkErrorCodeSummary|
|spark |40006|sparkILoop is null(sparkILoop 为空)|SPARK_IS_NULL|SparkErrorCodeSummary|
|spark |40007|The csTable that name is：{0} not found in cs(在 cs 中找不到名称为:{0} 的 csTable)|CSTABLE_NOT_FOUND|SparkErrorCodeSummary|
|spark |40007|Pyspark process  has stopped, query failed!(Pyspark 进程已停止，查询失败！)|PYSPARK_STOPPED|SparkErrorCodeSummary|
|spark |40009|sparkSession can not be null(sparkSession 不能为空)|CAN_NOT_NULL|SparkErrorCodeSummary|
|spark |80002|spark repl classdir create exception(spark repl classdir 创建异常)|SPARK_CREATE_EXCEPTION|SparkErrorCodeSummary|
|spark |40010|The request to the MDQ service to parse into executable SQL failed(向MDQ服务请求解析为可以执行的sql时失败)|REQUEST_MDQ_FAILED|SparkErrorCodeSummary|
|spark |420001|Invalid EngineConn engine session obj, failed to create sparkSql executor(EngineConn 引擎会话 obj 无效，无法创建 sparkSql 执行程序)|INVALID_CREATE_SPARKSQL|SparkErrorCodeSummary|
|spark |420002|Invalid EngineConn engine session obj, failed to create sparkPython executor(EngineConn 引擎会话 obj 无效，无法创建 sparkPython 执行程序)|INVALID_CREATE_SPARKPYTHON|SparkErrorCodeSummary|
|spark |43001|Config data validate failed (data_calc JSON验证失败)|DATA_CALC_CONFIG_VALID_FAILED|SparkErrorCodeSummary|
|spark |43002|xxx is not a valid type (data_calc 配置类型xxx不支持)|DATA_CALC_CONFIG_TYPE_NOT_VALID|SparkErrorCodeSummary|
|spark |43011|DataSource xxx is not configured! (data_calc 数据源xxx未配置)|DATA_CALC_DATASOURCE_NOT_CONFIG|SparkErrorCodeSummary|
|spark |43012|DataSource type is not supported (data_calc 数据源类型不支持)|DATA_CALC_DATABASE_NOT_SUPPORT|SparkErrorCodeSummary|
|spark |43021|The columns' name or data type in the select statement does not match target table column (查询语句中的字段和目标表字段不匹配)|DATA_CALC_COLUMN_NOT_MATCH|SparkErrorCodeSummary|
|spark |43022|The data to be inserted need to have the same number of columns as the target table (插入表的字段数量需要和select语句中的字段数量相等)|DATA_CALC_COLUMN_NUM_NOT_MATCH|SparkErrorCodeSummary|
|spark |43023|Target table's columns(xxx) are not exist in source columns (目标表中的字段xxx在select语句中不存在)|DATA_CALC_FIELD_NOT_EXIST|SparkErrorCodeSummary|
|spark |43024|Please set xxx in variables (data_calc需要配置变量xxx)|DATA_CALC_VARIABLE_NOT_EXIST|SparkErrorCodeSummary|
|spark |43031|Not support Adapter for spark application. (不支持 Spark 应用的 ClusterDescriptorAdapter)|NOT_SUPPORT_ADAPTER|SparkErrorCodeSummary|
|spark |43032|The application start failed, since yarn applicationId is null. (提交到Yarn上的程序提交/启动失败)|YARN_APPLICATION_START_FAILED|SparkErrorCodeSummary|
|spark |43040|Not support method for requestExpectedResource. (不支持 requestExpectedResource 的方法)|NOT_SUPPORT_METHOD|SparkErrorCodeSummary|
