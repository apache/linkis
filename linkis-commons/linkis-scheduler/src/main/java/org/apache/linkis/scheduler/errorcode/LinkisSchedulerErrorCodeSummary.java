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

package org.apache.linkis.scheduler.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisSchedulerErrorCodeSummary implements LinkisErrorCode {
  REQUEST_ENGINE_TIME_OUT(
      11055,
      "The job request engine time out (任务请求引擎超时，可能是EngineConnManager 启动EngineConn失败导致，可以查看EngineConnManager对应的out和log日志)."),
  TASK_STATUS_FLIP_ERROR(
      12000,
      "Task status flip error，because: failed to flip from:{0} to:{1}(任务状态翻转出错，原因：不允许从{0} 翻转为{1})."),
  JOB_QUEUE_IS_FULL(12001, "The submission job failed and the queue is full!(提交作业失败，队列已满！)"),
  UNRECOGNIZED_EXECID(12011, "Unrecognized execId (不能识别的execId):"),
  NEED_SUPPORTED_GROUP(
      13000,
      "FIFOConsumerManager just support FIFO group, {0} is not FIFO group.(FIFOConsumerManager只支持FIFO类型的消费组，{0} 不是这类消费组.)"),
  NODE_STATE_ERROR(20001, "{0} is in state {0}({0} 处于状态 {0}).");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisSchedulerErrorCodeSummary(int errorCode, String errorDesc) {
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
