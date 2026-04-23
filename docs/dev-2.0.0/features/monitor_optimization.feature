#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: zh-CN
功能：Monitor模块优化
  Monitor模块包含诊断日志清理、诊断功能配置化、连接池扩容三个优化子项

  背景：
    假设Monitor模块部署在服务器上
    并且诊断功能正常工作
    并且配置文件linkis-et-monitor.properties已正确加载

  场景：诊断日志自动清理 - 基本功能
    假设定时任务到达每月凌晨2点
    并且日志清理功能已启用（linkis.monitor.diagnosis.log.enabled=true）
    并且日志保留天数配置为7天（linkis.monitor.diagnosis.log.retention.days=7）
    当定时任务执行时
    那么系统应该扫描诊断日志目录
    并且应该找出创建时间超过7天的诊断日志文件
    并且应该删除这些过期的日志文件
    并且应该记录清理日志，包含删除文件数量和释放空间

  场景：诊断日志自动清理 - 功能开关关闭
    假设定时任务到达每月凌晨2点
    并且日志清理功能已禁用（linkis.monitor.diagnosis.log.enabled=false）
    当定时任务执行时
    那么系统应该跳过日志清理逻辑
    并且应该输出日志提示"日志清理功能已禁用"

  场景：诊断日志自动清理 - 动态调整保留天数
    假设定时任务到达每月凌晨2点
    并且日志保留天数配置为30天（linkis.monitor.diagnosis.log.retention.days=30）
    当定时任务执行时
    那么系统应该只删除创建时间超过30天的日志文件
    并且应该保留30天内的日志文件

  场景：诊断日志自动清理 - 避免误删非诊断文件
    假设日志目录中存在诊断日志文件和其他类型文件
    并且日志保留天数配置为7天
    当定时任务执行时
    那么系统应该只删除诊断日志文件
    并且不应该删除其他类型的文件

  场景：诊断功能配置化 - 启用状态
    假设job扫描定时任务触发
    并且诊断功能已启用（linkis.monitor.jobHistory.diagnosis.enabled=true）
    并且扫描到失败的任务
    那么系统应该执行诊断扫描逻辑
    并且应该创建JobHistoryAnalyzeRule扫描规则
    并且应该调用诊断接口进行失败任务分析

  场景：诊断功能配置化 - 禁用状态
    假设job扫描定时任务触发
    并且诊断功能已禁用（linkis.monitor.jobHistory.diagnosis.enabled=false）
    当扫描到失败的任务时
    那么系统应该跳过诊断扫描逻辑
    并且应该输出日志提示"诊断功能已禁用"
    并且不应该调用诊断接口

  场景：诊断功能配置化 - 向后兼容
    假设配置文件中缺少linkis.monitor.jobHistory.diagnosis.enabled参数
    当job扫描定时任务触发时
    那么系统应该使用默认值true
    并且诊断功能应该正常工作

  场景：Alert连接池扩容 - 参数调整
    当查看ThreadUtils.java文件时
    那么应该看到executors连接池配置为20个线程
    并且配置语句为：Utils.newCachedExecutionContext(20, "alert-pool-thread-", false)

  场景：Alert连接池扩容 - 编译验证
    当执行项目编译命令（mvn clean compile）时
    那么编译应该成功
    并且不应该有语法错误

  场景：完整性验证 - 配置参数文档
    当查看linkis-et-monitor.properties配置文件时
    那么应该包含以下新增配置项：
      | 参数名 | 说明 | 默认值 |
      | linkis.monitor.diagnosis.log.enabled | 是否启用日志清理 | true |
      | linkis.monitor.diagnosis.log.retention.days | 日志保留天数 | 7 |
      | linkis.monitor.jobHistory.diagnosis.enabled | 是否启用诊断功能 | true |

  场景：完整性验证 - 日志输出
    当Monitor服务运行时
    那么关键操作应该输出日志：
      | 场景 | 日志级别 | 关键信息 |
      | 日志清理开始 | INFO | 开始执行诊断日志清理 |
      | 日志清理完成 | INFO | 清理完成，删除X个文件，释放Y MB空间 |
      | 诊断功能禁用 | INFO | 诊断功能已禁用，跳过诊断扫描 |
      | 定时任务执行 | INFO | Start scan jobHistoryFinishedScan |
