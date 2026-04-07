# language: zh-CN
@regression @hive-engine @critical
Feature: Hive引擎模块回归测试

  作为Linkis系统，需要保证Hive引擎模块的核心功能稳定
  以便确保用户能够安全、高效地执行Hive SQL任务

  Background:
    Given Hive引擎服务已启动
    And 配置参数 "wds.linkis.hive.location.control.enable" 为 "true"

  # ==================== 拦截功能测试（P0） ====================

  @functional @P0 @location-control
  Scenario Outline: 拦截带LOCATION的CREATE TABLE语句
    When 用户提交SQL "<sql>"
    Then SQL应该被拒绝执行
    And 返回错误信息包含 "LOCATION clause is not allowed"

    Examples:
      | sql |
      | CREATE TABLE test_table (id int) LOCATION '/user/data/test' |
      | CREATE EXTERNAL TABLE ext_table (id int) LOCATION '/user/data/external' |
      | CREATE TABLE new_table AS SELECT * FROM source_table LOCATION '/user/data/new' |

  @functional @P0 @location-control
  Scenario Outline: 不带LOCATION的CREATE TABLE语句正常执行
    When 用户提交SQL "<sql>"
    Then SQL应该成功执行
    And 表创建成功

    Examples:
      | sql |
      | CREATE TABLE normal_table (id int, name string) |
      | CREATE TABLE copy_table AS SELECT * FROM source_table |

  @functional @P1 @location-control
  Scenario: ALTER TABLE SET LOCATION不被拦截
    When 用户提交SQL "ALTER TABLE existing_table SET LOCATION '/new/path'"
    Then SQL应该成功执行
    And 表位置成功修改

  # ==================== 配置开关测试（P0） ====================

  @functional @P0 @config-switch
  Scenario Outline: 配置开关控制拦截行为
    Given 配置参数 "wds.linkis.hive.location.control.enable" 为 "<enabled>"
    When 用户提交SQL "CREATE TABLE test_table (id int) LOCATION '/user/data/test'"
    Then 执行结果为 "<result>"

    Examples:
      | enabled | result |
      | false   | 成功执行 |
      | true    | 被拒绝执行 |

  # ==================== 边界条件测试（P1） ====================

  @functional @P1 @boundary
  Scenario: 带注释的CREATE TABLE with LOCATION被拦截
    When 用户提交SQL """
      -- This is a test table
      CREATE TABLE test_table (
          id int,
          name string
      )
      -- This is the location
      LOCATION '/user/data/test'
      """
    Then SQL应该被拒绝执行
    And 注释不影响拦截逻辑

  @functional @P1 @boundary
  Scenario: 多行SQL中包含带LOCATION的CREATE TABLE
    When 用户提交SQL """
      CREATE TABLE table1 (id int);
      CREATE TABLE table2 (id int) LOCATION '/user/data/table2';
      CREATE TABLE table3 (id int);
      """
    Then 整个脚本被拒绝执行
    And 返回错误信息指出第2个语句包含LOCATION

  @functional @P1 @boundary
  Scenario Outline: 空SQL或空字符串处理
    When 用户提交SQL "<sql>"
    Then 正常处理不抛出异常

    Examples:
      | sql |
      |     |
      |    |
      | -- Just a comment |

  @functional @P1 @boundary
  Scenario Outline: 大写LOCATION关键字被识别
    When 用户提交SQL "CREATE TABLE test_table (id int) <location> '/user/data/test'"
    Then SQL应该被拒绝执行

    Examples:
      | location |
      | LOCATION |
      | location |
      | LoCaTiOn |

  @functional @P1 @boundary
  Scenario Outline: 不同引号的LOCATION路径被识别
    When 用户提交SQL "CREATE TABLE test_table (id int) LOCATION <quote>/user/data/test<quote>"
    Then SQL应该被拒绝执行

    Examples:
      | quote |
      | '     |
      | "     |
      | `     |

  @functional @P1 @boundary
  Scenario: 跨多行的CREATE TABLE with LOCATION
    When 用户提交SQL """
      CREATE TABLE test_table (
          id int COMMENT 'ID column',
          name string COMMENT 'Name column'
      )
      COMMENT 'This is a test table'
      ROW FORMAT DELIMITED
      FIELDS TERMINATED BY ','
      STORED AS TEXTFILE
      LOCATION '/user/hive/warehouse/test_table'
      """
    Then SQL应该被拒绝执行
    And 跨多行的LOCATION被正确识别

  # ==================== 错误处理测试（P1） ====================

  @functional @P1 @error-handling
  Scenario: 拦截错误信息包含SQL片段
    When 用户提交超长SQL "CREATE TABLE test_table (id int, very_long_column_name_that_exceeds_normal_length string) LOCATION '/user/data/test'"
    Then 错误信息包含SQL片段
    And 错误信息清晰可读

  @functional @P1 @error-handling
  Scenario: 异常情况下的Fail-open策略
    When 模拟SQL解析异常
    Then 返回true放行确保可用性
    And 记录警告日志

  # ==================== 审计日志测试（P1） ====================

  @functional @P1 @audit-log
  Scenario: 被拦截操作记录警告日志
    When 用户提交带LOCATION的CREATE TABLE语句
    Then 日志包含警告信息 "Failed to check LOCATION in SQL"
    And 日志包含用户信息和SQL片段

  @functional @P2 @audit-log
  Scenario: 日志格式符合Linkis规范
    When 触发拦截操作
    Then 日志使用LogUtils标准方法
    And 日志包含时间戳、日志级别、类名、线程信息

  # ==================== 性能测试（P1/P2） ====================

  @performance @P1
  Scenario: 单次解析延迟测试
    When 准备1000条不同复杂度的CREATE TABLE语句
    And 启用location控制
    Then 平均延迟增加应该小于 3%

  @performance @P1
  Scenario: 批量解析吞吐量测试
    When 准备10000条CREATE TABLE语句（10%包含LOCATION）
    And 启用location控制
    Then 吞吐量降低应该小于 2%

  @performance @P2
  Scenario: 内存增量测试
    When 启动Entrance服务
    And 启用location控制
    And 执行1000次SQL解析
    Then 内存增量应该小于 20MB

  # ==================== 兼容性测试（P2） ====================

  @compatibility @P2 @hive-version
  Scenario Outline: 多版本Hive兼容性测试
    Given 使用Hive版本 "<hiveVersion>"
    When 执行TC-001至TC-006测试用例
    Then 所有测试用例应该通过

    Examples:
      | hiveVersion |
      | 1.2.1        |
      | 2.3.3        |
      | 3.1.2        |

  @compatibility @P2 @sql-dialect
  Scenario: 带Hive分区语法的CREATE TABLE被拦截
    When 用户提交SQL """
      CREATE TABLE partitioned_table (
          id int,
          name string,
          dt string
      )
      PARTITIONED BY (dt)
      LOCATION '/user/data/partitioned'
      """
    Then SQL应该被正确拦截

  @compatibility @P2 @sql-dialect
  Scenario: 带存储格式语法的CREATE TABLE被拦截
    When 用户提交SQL """
      CREATE TABLE formatted_table (
          id int
      )
      ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
      STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
      OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
      LOCATION '/user/data/formatted'
      """
    Then SQL应该被正确拦截

  # ==================== 安全性测试（P0/P1） ====================

  @security @P0 @bypass-test
  Scenario Outline: 尝试通过大小写绕过拦截
    When 用户提交SQL "CREATE TABLE test_table (id int) <location> '/user/data/test'"
    Then SQL应该被拒绝执行
    And 无绕过可能

    Examples:
      | location |
      | LOCATION |
      | location |
      | LoCaTiOn |
      | lOcAtIoN |

  @security @P0 @bypass-test
  Scenario: 尝试通过注释绕过拦截
    When 用户提交SQL "CREATE TABLE test_table (id int) LOC/**/ATION '/user/data/test'"
    Then SQL应该被拒绝执行

  @security @P0 @bypass-test
  Scenario Outline: 尝试通过空格/换行绕过拦截
    When 用户提交SQL "CREATE TABLE test_table (id int) LOCATION<whitespace>'/user/data/test'"
    Then SQL应该被拒绝执行

    Examples:
      | whitespace |
      |            |
      | \n         |
      | \t         |

  @security @P1 @injection-test
  Scenario: SQL注入尝试测试
    When 用户提交SQL "CREATE TABLE test_table (id int) LOCATION '/path'; DROP TABLE other_table; --"
    Then 拦截逻辑正常工作
    And 不导致SQL注入漏洞

  @security @P1 @path-traversal
  Scenario: 路径遍历尝试测试
    When 用户提交SQL "CREATE TABLE test_table (id int) LOCATION '../../../etc/passwd'"
    Then 拦截逻辑正常工作
    And 不导致路径遍历漏洞

  # ==================== 回归测试（P0/P1） ====================

  @regression @P0 @existing-feature
  Scenario: SQL LIMIT功能不受影响
    When 用户提交无LIMIT的SELECT语句
    Then 自动添加LIMIT 5000
    When 用户提交LIMIT超过5000的SELECT语句
    Then LIMIT被修改为5000

  @regression @P1 @existing-feature
  Scenario: DROP TABLE拦截功能不受影响
    When 用户提交DROP TABLE语句
    Then DROP TABLE被正确拦截

  @regression @P1 @existing-feature
  Scenario: CREATE DATABASE拦截功能不受影响
    When 用户提交CREATE DATABASE语句
    Then CREATE DATABASE被正确拦截

  @regression @P1 @existing-feature
  Scenario: Python/Scala代码检查功能不受影响
    When 用户提交包含sys模块导入尝试的Python代码
    Then Python代码被正确拦截
    When 用户提交包含System.exit尝试的Scala代码
    Then Scala代码被正确拦截
