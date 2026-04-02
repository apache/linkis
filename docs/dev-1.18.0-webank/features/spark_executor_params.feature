# language: zh-CN
功能: Spark引擎支持设置executor参数
  为Linkis Spark引擎增加executor端参数设置能力，支持开关控制和参数排除

  背景:
    配置文件位置为: linkis-engineconn.properties
    集成位置为: SparkEngineConnExecutor.executeLine方法的sc.setJobGroup之后

  场景1: 功能开关关闭时，不执行任何参数设置
    假设 功能开关配置为: wds.linkis.spark.executor.params.enabled=false
    当 用户执行Spark代码
    那么 不执行任何参数设置操作
    并且 日志记录禁用状态信息

  场景2: 功能开关开启时，正确设置所有Spark参数到executor端
    假设 功能开关配置为: wds.linkis.spark.executor.params.enabled=true
    并且 排除参数配置为: wds.linkis.spark.executor.params.exclude=
    当 用户执行Spark代码
    那么 遍历所有Spark参数
    并且 通过sc.setLocalProperty设置每个参数到executor端
    并且 日志记录设置的参数总数

  场景3: 配置排除参数时，排除的参数不会被设置
    假设 功能开关配置为: wds.linkis.spark.executor.params.enabled=true
    并且 排除参数配置为: wds.linkis.spark.executor.params.exclude=spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors
    当 用户执行Spark代码
    那么 spark.sql.shuffle.partitions参数不会被设置
    并且 spark.dynamicAllocation.maxExecutors参数不会被设置
    并且 其他正常参数会被设置
    并且 日志记录排除的参数数量

  场景4: 参数设置失败时，记录WARNING日志并继续执行
    假设 功能开关配置为: wds.linkis.spark.executor.params.enabled=true
    并且 存在无效的Spark参数名称
    当 参数设置过程中遇到异常
    那么 记录WARNING级别日志，包含参数key和异常信息
    并且 继续设置下一个参数
    并且 不影响Spark作业正常执行

  场景5: 使用默认配置时，功能关闭不影响现有功能
    当 用户不配置任何executor参数相关配置
    那么 使用默认配置: wds.linkis.spark.executor.params.enabled=false
    并且 不执行任何参数设置
    并且 与现有Spark行为完全一致

  场景6: 验证参数设置在sc.setJobGroup后执行
    当 用户执行Spark代码
    那么 executeLine方法执行流程为:
      | 步骤 | 操作 |
      | 1.0 | 调用Pre-Execution Hook |
      | 2.0 | 设置JobGroup: sc.setJobGroup(jobGroup, _code, true) |
      | 3.0 | 设置Driver参数: setSparkDriverParams(sc) | <-- 新增 |
      | 4.0 | 执行实际代码: runCode(...) |
      | 5.0 | 调用Post-Execution Hook |
