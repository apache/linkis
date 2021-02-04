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

package com.webank.wedatasphere.linkis.protocol.engine

import java.util

import com.webank.wedatasphere.linkis.protocol.{BroadcastProtocol, IRServiceGroupProtocol, RetryableProtocol, UserWithCreator}

/**
  * Created by enjoyyin on 2018/9/14.
  */
case class ResponseTaskExecute(execId: String)
case class JobProgressInfo(id: String, totalTasks: Int, runningTasks: Int, failedTasks: Int, succeedTasks: Int)
case class ResponseTaskProgress(execId: String, progress: Float, progressInfo: Array[JobProgressInfo])(
  implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol with IRServiceGroupProtocol
case class ResponseEngineLock(lock: String)
case class EngineConcurrentInfo(runningTasks: Int, pendingTasks: Int, succeedTasks: Int, failedTasks: Int)
case class EngineOverloadInfo(maxMemory: Long, usedMemory: Long, systemCPUUsed: Float)
case class ResponseEngineStatusChanged(instance: String, fromState: Int, toState: Int,
                                       overload: EngineOverloadInfo, concurrent: EngineConcurrentInfo)
  extends BroadcastProtocol
case class ResponseEngineInfo(createEntranceInstance: String, creator: String, user: String, properties: util.Map[String, String])
case class ResponseEngineStatus(instance: String, state: Int, overload: EngineOverloadInfo, concurrent: EngineConcurrentInfo,
                                engineInfo: ResponseEngineInfo)
case class ResponseTaskLog(execId: String, log: String)(
  implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol with IRServiceGroupProtocol

case class ResponseTaskError(execId: String, errorMsg: String)(
  implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol with IRServiceGroupProtocol

case class ResponseTaskStatus(execId: String, state: Int)(
  implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol with IRServiceGroupProtocol

case class ResponseTaskResultSet(execId: String, output: String, alias: String)(
  implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol with IRServiceGroupProtocol

case class ResponseTaskResultSize(execId: String, resultSize: Int)(implicit override val userWithCreator: UserWithCreator) extends RetryableProtocol
  with IRServiceGroupProtocol