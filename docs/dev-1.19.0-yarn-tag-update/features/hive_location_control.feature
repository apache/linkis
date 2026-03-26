# language: zh-CN
功能: Hive表Location路径控制

  作为 数据平台管理员
  我希望能够禁止用户在CREATE TABLE语句中指定LOCATION参数
  以防止用户通过指定LOCATION路径创建表，保护数据安全

  背景:
    Given Entrance服务已启动
    And location控制功能已启用

  # ===== P0功能：拦截带LOCATION的CREATE TABLE =====

  场景: 不带LOCATION的CREATE TABLE（成功）
    When 用户执行SQL:
      """
      CREATE TABLE test_table (
        id INT,
        name STRING
      )
      """
    Then 表创建成功
    And 不记录拦截日志

  场景: 带LOCATION的CREATE TABLE（被拦截）
    When 用户执行SQL:
      """
      CREATE TABLE test_table (
        id INT,
        name STRING
      )
      LOCATION '/user/hive/warehouse/test_table'
      """
    Then 表创建失败
    And 错误信息包含: "Location parameter is not allowed in CREATE TABLE statement"
    And 审计日志记录: "sql_type=CREATE_TABLE, location=/user/hive/warehouse/test_table, is_blocked=true"

  # ===== P0功能：功能开关 =====

  场景: 禁用location控制后允许带LOCATION的CREATE TABLE
    Given location控制功能已禁用
    When 用户执行SQL:
      """
      CREATE TABLE test_table (
        id INT,
        name STRING
      )
      LOCATION '/any/path/test_table'
      """
    Then 表创建成功
    And 不执行location拦截

  # ===== P1功能：CTAS语句 =====

  场景: CTAS未指定location（成功）
    When 用户执行SQL:
      """
      CREATE TABLE test_table AS
      SELECT * FROM source_table
      """
    Then 表创建成功
    And 不记录拦截日志

  场景: CTAS指定location（被拦截）
    When 用户执行SQL:
      """
      CREATE TABLE test_table
      LOCATION '/user/hive/warehouse/test_table'
      AS
      SELECT * FROM source_table
      """
    Then 表创建失败
    And 错误信息包含: "Location parameter is not allowed in CREATE TABLE statement"
    And 审计日志记录: "sql_type=CTAS, location=/user/hive/warehouse/test_table, is_blocked=true"

  # ===== 不在范围：ALTER TABLE =====

  场景: ALTER TABLE SET LOCATION（不拦截）
    When 用户执行SQL:
      """
      ALTER TABLE test_table SET LOCATION '/user/hive/warehouse/new_table'
      """
    Then 操作不被拦截
    And 执行结果由Hive引擎决定

  # ===== 边界场景 =====

  场景: CREATE TEMPORARY TABLE with LOCATION（被拦截）
    When 用户执行SQL:
      """
      CREATE TEMPORARY TABLE temp_table (
        id INT
      )
      LOCATION '/tmp/hive/temp_table'
      """
    Then 表创建失败
    And 错误信息包含: "Location parameter is not allowed in CREATE TABLE statement"

  场景: CREATE EXTERNAL TABLE with LOCATION（被拦截）
    When 用户执行SQL:
      """
      CREATE EXTERNAL TABLE external_table (
        id INT,
        name STRING
      )
      LOCATION '/user/hive/warehouse/external_table'
      """
    Then 表创建失败
    And 错误信息包含: "Location parameter is not allowed in CREATE TABLE statement"

  场景: 多行SQL格式带LOCATION（被拦截）
    When 用户执行SQL:
      """
      CREATE TABLE test_table
      (
        id INT COMMENT 'ID',
        name STRING COMMENT 'Name'
      )
      COMMENT 'Test table'
      LOCATION '/user/hive/warehouse/test_table'
      """
    Then 表创建失败
    And 错误信息包含: "Location parameter is not allowed in CREATE TABLE statement"

  # ===== 性能测试场景 =====

  场景: 大量并发建表操作（不带LOCATION）
    When 100个用户并发执行:
      """
      CREATE TABLE test_table (id INT)
      """
    Then 所有操作成功
    And 性能影响<3%

  场景: 大量并发建表操作（带LOCATION）
    When 100个用户并发执行:
      """
      CREATE TABLE test_table (id INT) LOCATION '/any/path'
      """
    Then 所有操作都被拦截
    And 性能影响<3%

  # ===== 错误处理场景 =====

  场景: SQL语法错误
    When 用户执行SQL:
      """
      CREATE TABLE test_table (
        id INT
      ) LOCATIO '/invalid/path'
      """
    Then SQL解析失败
    And 返回语法错误信息

  场景: 空SQL语句
    When 用户执行空SQL
    Then 不执行location检查
    And 返回SQL为空的错误

  # ===== 审计日志完整性 =====

  场景: 验证所有被拦截的操作都有审计日志
    Given 用户执行以下操作:
      | SQL类型 | Location路径 |
      | CREATE_TABLE | /user/hive/warehouse/table1 |
      | CREATE_TABLE | /invalid/path |
      | CTAS | /user/data/table2 |
    When 检查审计日志
    Then 所有被拦截的操作都有日志记录
    And 日志包含: timestamp, user, sql_type, location_path, is_blocked, reason

  # ===== 错误信息清晰度测试 =====

  场景: 验证错误信息包含原始SQL
    When 用户执行SQL:
      """
      CREATE TABLE test_table (id INT) LOCATION '/user/critical/data'
      """
    Then 表创建失败
    And 错误信息包含: "Please remove the LOCATION clause and retry"
    And 错误信息包含原始SQL片段
