/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.errorcode;

public enum EntranceErrorCodeSummary {
  UNSUPPORTED_OPERATION(10000, "Unsupported operation(不支持的操作)", "Unsupported operation(不支持的操作)"),
  JOBREQ_NOT_NULL(20001, "JobReq can't be null(JobReq不能为空)", "JobReq can't be null(JobReq不能为空)"),
  LABEL_NOT_NULL(
      20001,
      "userCreator label or engineType label cannot null(userCreator 标签或 engineType 标签不能为空)",
      "userCreator label or engineType label cannot null(userCreator 标签或 engineType 标签不能为空)"),
  NOT_CREATE_EXECUTOR(
      20001,
      "Task is not requestPersistTask, cannot to create Executor(Task不是requestPersistTask，不能创建Executor)",
      "Task is not requestPersistTask, cannot to create Executor(Task不是requestPersistTask，不能创建Executor)"),
  ENTRA_NOT_CREATE_EXECUTOR(
      20001,
      "Task is not EntranceJob, cannot to create Executor(Task 不是 EntranceJob，不能创建 Executor)",
      "Task is not EntranceJob, cannot to create Executor(Task 不是 EntranceJob，不能创建 Executor)"),

  JOB_NOT_NULL(20002, "job can't be null(job不能为空)", "job can't be null(job不能为空)"),
  JOBREQUEST_NOT_NULL(
      20004,
      "JobRequest cannot be null, unable to do persist operation(JobRequest 不能为空，无法进行持久化操作)",
      "JobRequest cannot be null, unable to do persist operation(JobRequest 不能为空，无法进行持久化操作)"),
  INSTANCE_NOT_NULL(20004, "instance can't be null(实例不能为空)", "instance can't be null(实例不能为空)"),
  EXECUTEUSER_NOT_NULL(
      20005, "execute user can't be null(执行用户不能为空)", "execute user can't be null(执行用户不能为空)"),

  PARAM_NOT_NULL(
      20007,
      "param executionCode can not be empty (参数 executionCode 不能为空)",
      "param executionCode can not be empty (参数 executionCode 不能为空)"),
  EXEC_SCRIP_NOT_NULL(
      20007,
      "param executionCode and scriptPath can not be empty at the same time(参数 executionCode 和 scriptPath 不能同时为空)",
      "param executionCode and scriptPath can not be empty at the same time(参数 executionCode 和 scriptPath 不能同时为空)"),

  ONLY_CODE_SUPPORTED(
      20010,
      "Only code with runtype supported (仅支持运行类型的代码)",
      "Only code with runtype supported (仅支持运行类型的代码)"),
  REQUEST_JOBHISTORY_FAILED(
      20011,
      "Request jobHistory failed, reason (请求jobHistory失败,原因):",
      "Request jobHistory failed, reason (请求jobHistory失败,原因):"),
  JOBRESP_PROTOCOL_NULL(
      20011,
      "Request jobHistory failed, reason: jobRespProtocol is null (请求jobHistory失败,原因:jobRespProtocol为null)",
      "Request jobHistory failed, reason: jobRespProtocol is null (请求jobHistory失败,原因:jobRespProtocol为null)"),
  READ_TASKS_FAILED(
      20011,
      "read all tasks failed, reason (读取所有任务失败，原因):",
      "read all tasks failed, reason (读取所有任务失败，原因):"),

  SENDER_RPC_FAILED(20020, "sender rpc failed(发件人 RPC 失败)", "sender rpc failed(发件人 RPC 失败)"),

  FAILED_ANALYSIS_TASK(
      20039,
      "failed to analysis task ! the reason is(分析任务失败！原因是):",
      "failed to analysis task ! the reason is(分析任务失败！原因是):"),

  INVALID_ENGINETYPE_NULL(
      20052,
      "Invalid engineType null, cannot use cache(无效的engineType null，不能使用缓存)",
      "Invalid engineType null, cannot use cache(无效的engineType null，不能使用缓存)"),
  PERSIST_JOBREQUEST_ERROR(
      20052,
      "Persist jobRequest error, please submit again later(存储Job异常，请稍后重新提交任务)",
      "Persist jobRequest error, please submit again later(存储Job异常，请稍后重新提交任务)"),

  INVALID_RESULTSETS(
      20053,
      "Invalid resultsets, cannot use cache(结果集无效，无法使用缓存)",
      "Invalid resultsets, cannot use cache(结果集无效，无法使用缓存)"),
  SUBMITTING_QUERY_FAILED(
      30009, "Submitting the query failed!(提交查询失败！)", "Submitting the query failed!(提交查询失败！)"),
  QUERY_STATUS_FAILED(
      50081,
      "query from jobHistory status failed(从 jobHistory 状态查询失败)",
      "query from jobHistory status failed(从 jobHistory 状态查询失败)"),
  GET_QUERY_RESPONSE(
      50081,
      "get query response incorrectly(错误地获取查询响应)",
      "get query response incorrectly(错误地获取查询响应)"),
  QUERY_TASKID_ERROR(
      50081,
      "query taskId  error,taskId(查询 taskId 错误,taskId):",
      "query taskId  error,taskId(查询 taskId 错误,taskId):"),
  CORRECT_LIST_TYPR(
      50081,
      "query from jobhistory not a correct List type taskId is(来自 jobhistory 的查询不是正确的列表类型 taskId 是):",
      "query from jobhistory not a correct List type taskId is(来自 jobhistory 的查询不是正确的列表类型 taskId 是):"),
  SHELL_BLACKLISTED_CODE(
      50081,
      "Shell code contains blacklisted code(shell中包含黑名单代码)",
      "Shell code contains blacklisted code(shell中包含黑名单代码)"),
  JOB_HISTORY_FAILED_ID(50081, "", ""),

  LOGPATH_NOT_NULL(20301, "logPath cannot be empty(日志路径不能为空)", "logPath cannot be empty(日志路径不能为空)");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  EntranceErrorCodeSummary(int errorCode, String errorDesc, String comment) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorDesc() {
    return errorDesc;
  }

  public void setErrorDesc(String errorDesc) {
    this.errorDesc = errorDesc;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
