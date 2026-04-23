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
功能: Hive引擎YARN任务标签用户名增强
  作为 运维人员或开发人员
  我想要 在YARN界面上快速识别Hive任务的提交用户
  以便 能够快速定位任务来源并进行问题排查

  背景:
    Given Linkis系统已正常启动
    And Hive引擎已成功连接
    And engineExecutorContext中包含execUser属性

  @p0 @normal
  场景: 正常用户名标签添加
    Given 用户提交Hive任务
    And jobId为"123456789"
    And execUser为"zhangsan"
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789,USER_zhangsan"
    And 日志应输出"set mapreduce.job.tags=LINKIS_123456789,USER_zhangsan"

  @p0 @edge
  场景: 用户名为空字符串时不添加USER标签
    Given 用户提交Hive任务
    And jobId为"123456789"
    And execUser为空字符串""
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789"
    And 日志不应包含USER_前缀

  @p0 @edge
  场景: 用户名为null时不添加USER标签
    Given 用户提交Hive任务
    And jobId为"123456789"
    And execUser为null
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789"
    And 日志不应包含USER_前缀

  @p1 @edge
  场景: jobId为空时不设置标签
    Given 用户提交Hive任务
    And jobId为空字符串""
    And execUser为"zhangsan"
    When Hive引擎向YARN提交任务
    Then 不应设置mapreduce.job.tags参数

  @p1 @normal
  场景: 特殊字符用户名保持原样
    Given 用户提交Hive任务
    And jobId为"123456789"
    And execUser为"user@example.com"
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789,USER_user@example.com"
    And 特殊字符"@"应保持原样

  @p1 @normal
  场景: 结合jobTags使用
    Given 用户提交Hive任务
    And jobId为"123456789"
    And jobTags为"EMR"
    And execUser为"zhangsan"
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789,EMR,USER_zhangsan"
    And 标签顺序应为LINKIS_jobId,jobTags,USER_username

  @p1 @regression
  场景: 向后兼容性验证-无execUser属性
    Given 用户提交Hive任务
    And jobId为"123456789"
    And engineExecutorContext.getProperties不包含execUser
    When Hive引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789"
    And 任务应正常执行
    And 不应抛出异常

  @p1 @regression
  场景: 向后兼容性验证-HiveEngineConcurrentConnExecutor
    Given 用户使用并发Hive引擎提交任务
    And jobId为"123456789"
    And execUser为"lisi"
    When Hive并发引擎向YARN提交任务
    Then mapreduce.job.tags应设置为"LINKIS_123456789,USER_lisi"
    And 任务应正常执行

  @p2 @performance
  场景: 性能影响验证
    Given 用户提交Hive任务
    And jobId为"123456789"
    And execUser为"zhangsan"
    When Hive引擎向YARN提交任务
    Then 标签设置耗时应小于10ms
    And 不应影响任务执行时间
