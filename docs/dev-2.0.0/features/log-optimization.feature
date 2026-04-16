# language: zh-CN
功能: Linkis日志优化

  作为 Linkis 系统管理员
  我希望 对系统日志进行优化
  以便 提升 安全性、可追溯性 和 运维效率

  背景:
    Given Linkis系统已启动
    And 日志系统已配置

  @p0 @token-desensitization
  场景: Token脱敏处理
    Given 用户通过UJES客户端访问Linkis
    And 客户端Token为 "abc123def456789"
    When 日志系统记录客户端连接信息
    Then 日志中应该输出脱敏后的Token "abc***789"
    And 原始Token "abc123def456789" 不应出现在日志中

  @p0 @token-desensitization
  场景: 短Token脱敏处理
    Given 引擎连接Token为 "abc123"
    When 日志系统记录引擎连接信息
    Then 日志中应该输出脱敏后的Token "abc***"
    And 原始Token "abc123" 不应出现在日志中

  @p1 @bml-hdfs-log
  场景: BML资源上传记录HDFS路径
    Given 用户 "admin" 准备上传资源
    And 资源ID为 "10001"
    When 资源上传成功
    And HDFS存储路径为 "hdfs://linkis/bml/resource/10001/v001"
    Then 应该记录INFO级别日志
    And 日志包含 "resourceId: 10001"
    And 日志包含 "version: v001"
    And 日志包含 "hdfsPath: hdfs://linkis/bml/resource/10001/v001"
    And 日志包含 "user: admin"

  @p1 @bml-hdfs-log
  场景: BML资源下载记录HDFS路径
    Given 用户 "admin" 准备下载资源
    And 资源ID为 "10001"
    And 版本号为 "v001"
    当下载资源
    And HDFS源路径为 "hdfs://linkis/bml/resource/10001/v001"
    Then 应该记录INFO级别日志
    And 日志包含 "resourceId: 10001"
    And 日志包含 "version: v001"
    And 日志包含 "hdfsPath: hdfs://linkis/bml/resource/10001/v001"
    And 日志包含 "user: admin"

  @p1 @bml-hdfs-log
  场景: BML资源版本更新记录HDFS路径
    Given 用户 "admin" 准备更新资源版本
    And 资源ID为 "10001"
    When 版本更新成功
    And 新版本为 "v002"
    And 新HDFS路径为 "hdfs://linkis/bml/resource/10001/v002"
    Then 应该记录INFO级别日志
    And 日志包含 "resourceId: 10001"
    And 日志包含 "version: v002"
    And 日志包含 "hdfsPath: hdfs://linkis/bml/resource/10001/v002"
    And 日志包含 "user: admin"

  @p1 @bml-hdfs-log
  场景: BML资源删除记录HDFS路径
    Given 管理员准备删除资源所有版本
    And 资源ID为 "10001"
    When 删除操作执行
    Then 应该记录INFO级别日志
    And 日志包含 "resourceId: 10001"
    And 日志包含 "hdfsPath"

  @p1 @manager-kill-log
  场景: Linkis Manager killEngine记录详细信息
    Given 用户 "admin" 有一个运行中的Spark引擎
    And 引擎实例ID为 "engineConnExecId: 1"
    When Linkis Manager执行killEngine操作
    Then 应该记录INFO级别日志
    And 日志包含 "engineType: spark"
    And 日志包含 "user: admin"
    And 日志包含 "engineConnExecId: 1"
    And TicketId已脱敏

  @p1 @hadoop-client-log
  场景: Spark引擎HDFS文件操作记录
    Given Spark引擎正在执行任务
    When 执行HDFS mkdir操作
    And 目录路径为 "/user/admin/tmp"
    Then 应该记录INFO级别日志
    And 日志包含 "type: mkdir"
    And 日志包含 "path: /user/admin/tmp"
    And 日志包含 "user: admin"
    And 日志包含 "result: success"

  @p1 @hadoop-client-log
  场景: Spark引擎Kerberos认证记录
    Given Spark引擎使用Keytab认证
    When 执行Kerberos登录
    And 用户为 "admin"
    And Keytab路径为 "/path/to/keytab"
    Then 应该记录INFO级别日志
    And 日志包含 "user: admin"
    And 日志包含 "Kerberos"

  @p1 @hadoop-client-log
  场景: Spark引擎Kerberos认证失败记录
    Given Spark引擎使用Keytab认证
    When Kerberos登录失败
    And 错误原因为 "Invalid principal"
    Then 应该记录WARN级别日志
    And 日志包含 "Kerberos auth failed"
    And 日志包含 "error: Invalid principal"

  @p1 @hadoop-client-log
  场景: Hive引擎HDFS文件操作记录
    Given Hive引擎正在执行任务
    When 执行HDFS文件读取操作
    And 文件路径为 "/user/admin/data.csv"
    Then 应该记录INFO级别日志
    And 日志包含 "type: read"
    And 日志包含 "path: /user/admin/data.csv"

  @p2 @spark-broadcast-log
  场景: Spark广播表FutureWarning日志级别
    Given Spark引擎使用广播表功能
    When 产生HiveContext deprecated告警
    Then 日志级别应为WARN
    And 日志内容包含 "FutureWarning: HiveContext is deprecated in Spark 2.0.0"

  @regression
  场景: 验证Token脱敏不影响业务逻辑
    Given 用户通过UJES客户端访问Linkis
    And 客户端Token为 "abc123def456789"
    When 执行任务提交操作
    Then 任务应该成功执行
    And Token验证应通过
    And 日志中Token已脱敏

  @regression
  场景: 验证日志量增加不影响性能
    Given 用户执行BML资源操作
    When 记录HDFS路径日志
    Then 操作响应时间应小于100ms
    And 日志记录时间应小于10ms
