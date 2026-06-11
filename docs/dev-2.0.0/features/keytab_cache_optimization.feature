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
Feature: Keytab文件缓存优化
  作为一个系统管理员或开发人员
  我希望通过缓存keytab临时文件
  以减少Full GC频率，提升系统性能

  Background:
    Given LINKIS_KEYTAB_SWITCH已启用
    And 存在加密的keytab源文件 "/mnt/bdap/keytab/user1.keytab"

  Scenario: 首次调用时应创建并缓存临时文件
    When 用户user1首次调用getLinkisUserKeytabFile
    Then 系统应创建临时文件
    And 系统应设置文件权限为 "rw-------"
    And 系统应将文件路径缓存到keytabFileCache

  Scenario: 相同用户后续调用应复用缓存
    Given 用户user1已调用getLinkisUserKeytabFile并缓存
    When 用户user1再次调用getLinkisUserKeytabFile
    Then 系统应返回已缓存的文件路径
    And 系统不应创建新的临时文件

  Scenario: 不同用户调用应创建不同的缓存
    Given 用户user1已调用getLinkisUserKeytabFile并缓存
    When 用户user2调用getLinkisUserKeytabFile
    Then 系统应为user2创建新的临时文件
    And 系统应返回与user1不同的文件路径

  Scenario: 不同label的同一用户应创建不同的缓存
    Given 指定cluster1标签
    And 用户user1已调用getLinkisUserKeytabFile并缓存
    When 指定cluster2标签
    And 用户user1再次调用getLinkisUserKeytabFile
    Then 系统应为cluster2创建新的缓存
    And 系统应返回不同的文件路径

  Scenario: LINKIS_KEYTAB_SWITCH关闭时应直接返回源文件路径
    Given LINKIS_KEYTAB_SWITCH已关闭
    When 用户user1调用getLinkisUserKeytabFile
    Then 系统应返回源文件路径而非临时文件路径
    And 系统不应创建临时文件

  Scenario: 缓存文件应能被定期清理
    Given 用户user1已调用getLinkisUserKeytabFile并缓存
    And 缓存文件的空闲时间超过 HDFS_ENABLE_CACHE_IDLE_TIME(180秒)
    When 缓存清理定时任务执行
    Then 系统应删除cached临时文件
    And 系统应从keytabFileCache中移除缓存条目

  Scenario: 并发调用应保证线程安全
    Given 10个并发线程
    And 所有线程使用相同的用户名user1
    When 所有线程同时调用getLinkisUserKeytabFile
    Then 所有线程应获得相同的文件路径
    And 系统应保证缓存一致性

  Scenario: 缓存失效时应能正常降级
    Given 用户user1已调用getLinkisUserKeytabFile并缓存
    And 缓存文件已被外部删除
    When 用户user1再次调用getLinkisUserKeytabFile
    Then 系统应检测到缓存失效
    And 系统应重新创建临时文件
    And 系统应成功返回文件路径
