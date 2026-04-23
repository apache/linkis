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
Feature: 修复Entrance Offline后Group缓存未更新的Bug
  修复多实例环境下Entrance offline后Group缓存未主动失效导致并发数计算错误的问题

  Background:
    Given 系统已启动
    And 已部署4个Entrance实例
    And 数据库连接正常
    And RPC服务可用
    And 配置用户并发数为100

  Rule: Bug复现 - 记录修复前的错误行为

    @bug @reproduction @skip
    Scenario: 复现Bug - Entrance offline后并发数未更新
      Given 集群有4个Entrance实例: A, B, C, D
      And Entrance C上有正在运行的任务
      And 已提交任务建立了Group缓存（并发数为25）
      When 管理员将Entrance C标记为offline
      And 用户提交新任务到Entrance A
      Then 系统仍按4个实例计算并发数（25个槽位）
      And 任务应该提交失败
      And 错误信息应该包含"并发数已满"

    @bug @reproduction @skip
    Scenario: 复现Bug - 缓存50分钟后自动更新
      Given 集群有4个Entrance实例: A, B, C, D
      And Entrance C已offline超过50分钟
      And Group缓存已过期
      When 用户提交新任务
      Then 系统按3个实例计算并发数（33个槽位）
      And 任务应该成功提交

  Rule: Bug修复 - 验证修复后的正确行为

    @bugfix @critical @smoke
    Scenario: 修复后 - Entrance offline时缓存立即清除
      Given 集群有4个Entrance实例: A, B, C, D
      And Entrance C上有正在运行的任务
      And 已提交任务建立了Group缓存（并发数为25）
      When 管理员将Entrance C标记为offline
      Then 所有Entrance实例应该收到广播消息
      And 各实例的Group缓存应该在5秒内清除
      And 缓存清除应该被记录到日志

    @bugfix @critical
    Scenario: 修复后 - offline后新任务并发数正确
      Given 集群有4个Entrance实例: A, B, C, D
      And Entrance C已offline
      And Group缓存已清除
      When 用户提交新任务到Entrance A
      Then 系统按3个实例计算并发数（33个槽位）
      And 任务应该成功提交
      And 不应该出现并发数已满错误

    @bugfix
    Scenario Outline: 不同数量的Entrance offline后并发数计算正确
      Given 集群有4个Entrance实例
      And 有<offline_count>个Entrance实例已offline
      And Group缓存已清除
      When 用户提交新任务
      Then 系统按<online_count>个实例计算并发数
      And 每个实例并发数应该是<parallelism_per_instance>

      Examples:
        | offline_count | online_count | parallelism_per_instance |
        | 1             | 3            | 33                        |
        | 2             | 2            | 50                        |
        | 3             | 1            | 100                       |

  Rule: 回归验证 - 确保修复不影响其他场景

    @regression @critical
    Scenario: 正常情况下任务提交仍正常
      Given 集群有4个Entrance实例: A, B, C, D
      And 所有实例都在线
      When 用户提交新任务
      Then 系统按4个实例计算并发数（25个槽位）
      And 任务应该成功提交
      And 行为应该与修复前完全一致

    @regression
    Scenario: 实例频繁上下线场景
      Given 集群有4个Entrance实例
      When Entrance C offline后立即online
      And 再次offline
      And 用户提交新任务
      Then 缓存应该正确更新
      And 并发数应该反映当前在线实例数量
      And 不应该出现异常或错误日志

    @regression
    Scenario: 多个实例同时offline
      Given 集群有4个Entrance实例: A, B, C, D
      When 同时将Entrance C和Entrance D标记为offline
      Then 所有实例应该收到2条广播消息
      And Group缓存应该被清除2次
      And 并发数应该按2个实例计算（50个槽位）

    @regression @critical
    Scenario: 广播失败不影响offline流程
      Given 集群有4个Entrance实例
      And Entrance D不可达（RPC通信失败）
      When 管理员将Entrance C标记为offline
      Then Entrance A和B应该收到广播消息
      Entrance D通信失败应该被记录到ERROR日志
      And Entrance C的offline流程应该成功完成
      And 不应该抛出异常或中断

  Rule: 性能验证 - 验证修复的性能影响

    @performance
    Scenario: 广播延迟测试
      Given 集群有4个Entrance实例
      When 触发Entrance offline广播
      Then 所有实例应该在5秒内收到广播
      And 广播总耗时应该小于5秒

    @performance
    Scenario: 缓存清除性能测试
      Given 缓存中有5000个Group
      When 执行缓存清除操作
      Then 清除操作应该在100ms内完成
      And CPU使用率不应该显著增加

    @performance
    Scenario: 广播期间任务提交不受影响
      Given 集群有4个Entrance实例
      When 正在发送广播消息
      And 同时有用户提交新任务
      Then 任务提交应该正常处理
      And 响应时间不应该明显增加

  Rule: 监控与日志验证

    @monitoring
    Scenario: 广播发送日志记录
      Given 集群有4个Entrance实例
      When 触发Entrance offline广播
      Then 日志应该记录"Sending broadcast to clear Group cache"
      And 日志应该包含offline实例信息

    @monitoring
    Scenario: 广播接收日志记录
      Given 集群有4个Entrance实例
      When 触发Entrance offline广播
      Then 每个实例应该记录"Received broadcast to clear Group cache"
      And 日志应该记录缓存清除操作

    @monitoring
    Scenario: 广播失败日志记录
      Given 集群有4个Entrance实例
      And 某个实例不可达
      When 触发Entrance offline广播
      Then 应该记录"Broadcast to <instance> failed"的ERROR日志
      And 日志应该包含失败原因
