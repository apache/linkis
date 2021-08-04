/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.governance.common.protocol.task

import java.util

import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol
import com.webank.wedatasphere.linkis.protocol.{BroadcastProtocol, RetryableProtocol}

case class ResponseTaskExecute(execId: String)

case class ResponseTaskProgress(execId: String, progress: Float, progressInfo: Array[JobProgressInfo]) extends RetryableProtocol with RequestProtocol

case class ResponseEngineLock(lock: String)

case class EngineConcurrentInfo(runningTasks: Int, pendingTasks: Int, succeedTasks: Int, failedTasks: Int)
case class EngineOverloadInfo(maxMemory: Long, usedMemory: Long, systemCPUUsed: Float)

case class ResponseEngineStatusChanged(instance: String, fromStatus: ExecutionNodeStatus, toStatus: ExecutionNodeStatus,
                                       overload: EngineOverloadInfo, concurrent: EngineConcurrentInfo)
  extends BroadcastProtocol

case class ResponseEngineInfo(createEntranceInstance: String, creator: String, user: String, properties: util.Map[String, String])

case class ResponseEngineStatus(instance: String, status: ExecutionNodeStatus, overload: EngineOverloadInfo, concurrent: EngineConcurrentInfo,
                                engineInfo: ResponseEngineInfo)

case class ResponseTaskLog(execId: String, log: String) extends RetryableProtocol with RequestProtocol

case class ResponseTaskError(execId: String, errorMsg: String) extends RetryableProtocol with RequestProtocol

case class ResponseTaskStatus(execId: String, status: ExecutionNodeStatus) extends RetryableProtocol with RequestProtocol

case class ResponseTaskResultSet(execId: String, output: String, alias: String) extends RetryableProtocol with RequestProtocol {
  override def toString: String = s"execId: $execId, output: $alias"
}

case class ResponseTaskResultSize(execId: String, resultSize: Int) extends RetryableProtocol with RequestProtocol