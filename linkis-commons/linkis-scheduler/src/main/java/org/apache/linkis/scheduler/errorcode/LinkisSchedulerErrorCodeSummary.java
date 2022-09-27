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
  THE_REQUEST_ENGINE_TIMES_OUT(
      11055,
      "The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去观看EngineConnManager的linkis.out和linkis.log日志).",
      "The request engine times out (请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以去观看EngineConnManager的linkis.out和linkis.log日志)."),
  TASK_STATUS_FLIP_STATE(
      12000,
      "Task status flip error! Cause: Failed to flip from {} to $state.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)",
      "Task status flip error! Cause: Failed to flip from {} to $state.(任务状态翻转出错！原因：不允许从{} 翻转为{}.)"),
  THE_SUNMISSION_IS_FULL(
      12001,
      "The submission job failed and the queue is full!(提交作业失败，队列已满！)",
      "The submission job failed and the queue is full!(提交作业失败，队列已满！)"),
  UNRECOGNIZED_EXECID(
      12011, "Unrecognized execId (不能识别的execId):", "Unrecognized execId (不能识别的execId):"),
  NEED_A_SUPPORTED(
      13000,
      "FIFOConsumerManager need a FIFOGroup, but {} is supported.(FIFOConsumerManager 需要一个 FIFOGroup，但支持 {}.)",
      "FIFOConsumerManager need a FIFOGroup, but {} is supported.(FIFOConsumerManager 需要一个 FIFOGroup，但支持 {}.)"),
  IS_IN_STATE(20001, "{} is in state {}. ({} 处于状态 {}.)", "{} is in state {}. ({} 处于状态 {}.)");

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
