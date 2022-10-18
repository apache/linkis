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

package org.apache.linkis.scheduler.errorcode;

public enum LinkisSchedulerErrorCodeSummary {
  REQUEST_ENGINE_TIMES_OUT(
      11055,
      "The job request engine time out (任务请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以查看EngineConnManager对应的out和log日志).",
      "The job request engine time out (任务请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以查看EngineConnManager对应的out和log日志)."),
  TASK_STATUS_FLIP_ERROR(
      12000,
      "Task status flip error! Cause: Failed to flip from {} to {}.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)",
      "Task status flip error! Cause: Failed to flip from {} to {}.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)"),
  JOB_QUEUE_IS_FULL(
      12001,
      "The submission job failed and the queue is full!(提交作业失败，队列已满！)",
      "The submission job failed and the queue is full!(提交作业失败，队列已满！)"),
  UNRECOGNIZED_EXECID(
      12011, "Unrecognized execId (不能识别的execId):", "Unrecognized execId (不能识别的execId):"),
  NEED_SUPPORTTED_GROUP(
      13000,
      "FIFOConsumerManager just support FIFO group, {} is not FIFO group.(FIFOConsumerManager只支持FIFO类型的消费组，{} 不是这类消费组.)",
      "FIFOConsumerManager just support FIFO group, {} is not FIFO group.(FIFOConsumerManager只支持FIFO类型的消费组，{} 不是这类消费组.)"),
  NODE_STATE_ERROR(20001, "{} is in state {}. ({} 处于状态 {}.)", "节点状态不符合预期");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  LinkisSchedulerErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
