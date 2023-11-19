/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum EntranceErrorCodeSummary implements LinkisErrorCode {
  UNSUPPORTED_OPERATION(10000, "Unsupported operation(不支持的操作)"),
  JOBREQ_NOT_NULL(20001, "JobReq cannot be null(JobReq不能为空)"),
  LABEL_NOT_NULL(
      20001,
      "The label of userCreator or engineType cannot be null(标签 userCreator 或 engineType 不能为空)"),
  NOT_CREATE_EXECUTOR(
      20001,
      "Task is not requestPersistTask, cannot to create Executor(Task不是requestPersistTask，不能创建Executor)"),
  ENTRA_NOT_CREATE_EXECUTOR(
      20001,
      "Task is not EntranceJob, cannot to create Executor(Task 不是 EntranceJob，不能创建 Executor)"),

  JOB_NOT_NULL(20002, "job cannot  be null(job不能为空)"),
  JOBREQUEST_NOT_NULL(
      20004,
      "JobRequest cannot be null, unable to do persist operation(JobRequest 不能为空，无法进行持久化操作)"),
  INSTANCE_NOT_NULL(20004, "The instance cannot be null(实例不能为空)"),
  EXECUTEUSER_NOT_NULL(20005, "The execute user cannot be null(执行用户不能为空)"),

  PARAM_NOT_NULL(20007, "The param executionCode cannot be empty (参数 executionCode 不能为空)"),
  EXEC_SCRIP_NOT_NULL(
      20007,
      "The param executionCode and scriptPath cannot be empty at the same time(参数 executionCode 和 scriptPath 不能同时为空)"),

  ONLY_CODE_SUPPORTED(20010, "Only code with runtype supported (仅支持运行类型的代码)"),
  REQUEST_JOBHISTORY_FAILED(20011, "Request jobHistory failed,because:{0} (请求jobHistory失败)"),
  JOBRESP_PROTOCOL_NULL(
      20011,
      "Request jobHistory failed, because:jobRespProtocol is null (请求jobHistory失败,因为jobRespProtocol为null)"),
  READ_TASKS_FAILED(20011, "Read all tasks failed, because:{0}(获取所有任务失败)"),

  SENDER_RPC_FAILED(20020, "Sender rpc failed"),

  FAILED_ANALYSIS_TASK(20039, "Failed to analysis task,because:{0}(分析任务失败)!"),

  INVALID_ENGINETYPE_NULL(
      20052, "Invalid engineType null, cannot use cache(无效的 engineType null，无法使用 cache)"),
  PERSIST_JOBREQUEST_ERROR(
      20052, "Persist jobRequest error, please submit again later(存储Job异常，请稍后重新提交任务)"),

  INVALID_RESULTSETS(20053, "Invalid resultsets, cannot use cache(结果集无效，无法使用 cache)"),
  SUBMITTING_QUERY_FAILED(30009, "Submitting the query failed(提交查询失败)!"),
  QUERY_STATUS_FAILED(50081, "Query from jobHistory status failed(从 jobHistory 状态查询失败)"),
  GET_QUERY_RESPONSE(50081, "Get query response incorrectly(获取查询响应结果不正确)"),
  QUERY_TASKID_ERROR(50081, "Query task of taskId:{0} error(查询任务id：{}的任务出错)"),
  CORRECT_LIST_TYPR(
      50081,
      "Query from jobhistory with incorrect list type of taskId, the taskId is：{0} (从jobhistory 中查询的参数类型不正确)"),
  SHELL_BLACKLISTED_CODE(50081, "Shell code contains blacklisted code(shell中包含黑名单代码)"),
  JOB_HISTORY_FAILED_ID(50081, ""),

  LOGPATH_NOT_NULL(20301, "The logPath cannot be empty(日志路径不能为空)"),

  FAILOVER_RUNNING_TO_CANCELLED(
      30001,
      "Job {0} failover, status changed from Running to Cancelled (任务故障转移，状态从Running变更为Cancelled)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  EntranceErrorCodeSummary(int errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  @Override
  public int getErrorCode() {
    return errorCode;
  }

  @Override
  public String getErrorDesc() {
    return errorDesc;
  }
}
