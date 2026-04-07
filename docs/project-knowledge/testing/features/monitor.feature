# language: zh-CN
@regression @monitor
Feature: Monitor模块回归测试

  作为Linkis监控系统，需要保证Monitor模块的核心功能稳定
  以便确保诊断日志管理、诊断功能、Alert连接池等功能正常运行

  Background:
    Given Monitor模块已启动
    And 应用配置正确加载
    And 数据库连接正常

  Scenario: FC-001-定时任务正常触发
    Given 配置诊断日志清理为启用
    And 配置保留天数为7天
    And 配置定时任务为"0 0 2 * * ?"
    And 创建若干job_id目录和detail JSON文件
    When 等待定时任务触发时间（凌晨2点）
    Then 日志中出现"Start to clear diagnosis logs"
    And 日志中出现"Diagnosis log cleanup completed"
    And 定时任务按时触发并执行

  Scenario: FC-002-过期日志文件清理
    Given 配置保留天数为7天
    And 创建10天前的job_id目录：12345/
    And 创建5天前的job_id目录：67890/
    And 创建今天的job_id目录：11111/
    When 手动触发清理任务
    Then 12345/目录被删除
    And 67890/目录被删除
    And 11111/目录保留
    And 日志显示Deleted files: 2

  Scenario: FC-003-Detail JSON文件清理
    Given 配置保留天数为7天
    And 创建json/目录
    And 创建10天前的文件：12345_detail.json
    And 创建5天前的文件：67890_detail.json
    And 创建今天的文件：11111_detail.json
    When 手动触发清理任务
    Then 12345_detail.json被删除
    And 67890_detail.json被删除
    And 11111_detail.json保留
    And 日志显示Deleted files: 2

  Scenario: FC-004-保留未过期日志
    Given 配置保留天数为7天
    And 创建3天前的job_id目录：12345/
    And 创建5天前的job_id目录：67890/
    When 手动触发清理任务
    Then 12345/目录保留
    And 67890/目录保留
    And 日志显示Deleted files: 0

  Scenario: FC-005-配置参数生效
    Given 配置保留天数为3天
    And 创建5天前的job_id目录：12345/
    And 创建1天前的job_id目录：67890/
    When 手动触发清理任务
    Then 12345/目录被删除
    And 67890/目录保留

  Scenario: FC-006-启用诊断功能
    Given 配置linkis.monitor.jobHistory.diagnosis.enabled=true
    And 创建一个失败的任务记录
    When 等待job扫描任务触发
    Then 日志中出现"JobHistory diagnosis is enabled, scan rule added"
    And 失败任务触发诊断流程
    And 诊断接口被调用

  Scenario: FC-007-禁用诊断功能
    Given 配置linkis.monitor.jobHistory.diagnosis.enabled=false
    And 创建一个失败的任务记录
    When 等待job扫描任务触发
    Then 日志中出现"JobHistory diagnosis is disabled by config, skip diagnosis scan"
    And 失败任务不触发诊断流程
    And 诊断接口未被调用

  Scenario: FC-008-连接池线程数验证
    Given 应用启动完成
    When 检查ThreadUtils代码（第44行）
    Then 代码显示executors = Utils.newCachedExecutionContext(20, ...)
    And 线程池最大线程数为20
    And 线程名前缀为"alert-pool-thread-"

  Scenario: FC-009-并发任务处理
    Given 应用启动完成
    And 模拟创建20个失败任务
    When 提交20个诊断任务到连接池
    Then 20个任务可以同时提交
    And 任务在合理时间内完成（预计<30秒）
    And 无明显排队等待

  Scenario: FC-010-完整流程测试
    Given 配置启用诊断和日志清理
    And 创建5个失败任务
    When 等待job扫描
    Then 诊断功能正常执行
    And 诊断日志正确生成
    When 修改日志文件时间为10天前
    And 等待日志清理触发
    Then 清理任务正常执行
    And 过期日志被删除

  Scenario: FC-011-JobHistory扫描
    Given 创建已完成任务记录
    When 等待job扫描任务触发
    Then 正确扫描到已完成任务

  Scenario: FC-012-任务状态判断
    Given 创建失败任务和成功任务记录
    When 触发诊断扫描
    Then 仅失败任务触发诊断

  Scenario: FC-013-诊断接口调用
    Given 创建失败任务
    And 启用诊断功能
    When 等待诊断扫描触发
    Then 诊断接口被正确调用

  Scenario: FC-014-诊断结果记录
    Given 诊断功能执行完成
    When 检查诊断结果记录
    Then 诊断结果正确保存到数据库或文件

  Scenario: FC-015-禁用日志清理
    Given 配置linkis.monitor.diagnosis.log.enabled=false
    And 创建若干过期job_id目录
    When 手动触发清理任务
    Then 日志中出现"Diagnosis log cleanup is disabled by config"
    And 所有目录保持不变
    And 日志中没有删除记录

  Scenario: FC-016-日志目录不存在
    Given 配置路径为不存在的目录
    And 启用日志清理功能
    When 手动触发清理任务
    Then 日志中出现"Diagnosis log path does not exist"警告
    And 任务正常完成，不抛出异常
    And 后续正常流程不受影响

  Scenario: FC-017-文件删除失败
    Given 创建过期job_id目录，设置为只读权限
    And 创建其他可删除的过期目录
    When 手动触发清理任务
    Then 日志中出现"Failed to delete directory"错误
    And 其他可删除的目录被正确删除
    And 任务继续执行，不中断

  Scenario: FC-018-向后兼容性
    Given 不配置linkis.monitor.jobHistory.diagnosis.enabled
    And 创建一个失败的任务记录
    When 等待job扫描任务触发
    Then 默认值被识别为true
    And 日志中出现"JobHistory diagnosis is enabled, scan rule added"
    And 诊断功能正常执行

  Scenario: PC-001-连接池并发能力测试
    Given 应用启动完成
    When 模拟10-15个失败任务/分钟（高峰期）
    And 持续测试30分钟
    And 监控任务排队率和完成时间
    Then 任务平均完成时间 < 2.5秒
    And 任务排队率 < 5%
    And 无任务超时
    And 连接池无异常

  Scenario: PC-002-日志清理性能测试
    Given 创建10000个过期job_id目录（约1GB）
    When 手动触发清理任务
    And 监控执行时间和内存占用
    Then 清理任务执行时间 < 5分钟
    And 内存增量 < 100MB
    And 无内存泄漏

  Scenario: PC-003-性能提升验证
    Given 应用启动完成
    When 提交10个诊断任务
    And 记录任务完成时间
    And 计算平均响应时间
    And 对比扩容前后数据
    Then 平均响应时间<2.5秒
    And 相比扩容前有性能提升
    And 无任务排队

  Scenario: IC-001-配置组合测试
    Given 配置诊断=true, 清理=true
    And 创建测试任务和日志
    When 验证配置组合1的行为
    Then 两个功能都执行
    When 配置诊断=true, 清理=false
    And 验证配置组合2的行为
    Then 仅执行诊断
    When 配置诊断=false, 清理=true
    And 验证配置组合3的行为
    Then 仅执行清理
    When 配置诊断=false, 清理=false
    And 验证配置组合4的行为
    Then 两个功能都跳过

  Scenario: RC-001-其他连接池
    Given 应用启动完成
    When 检查analyze和archive连接池的配置和功能
    Then analyze和archive连接池配置保持不变
    And analyze和archive连接池功能正常
