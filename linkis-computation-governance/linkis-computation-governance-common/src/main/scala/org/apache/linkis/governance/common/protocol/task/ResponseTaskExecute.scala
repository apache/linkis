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

package org.apache.linkis.governance.common.protocol.task

import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.protocol.{BroadcastProtocol, RetryableProtocol}
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.protocol.message.RequestProtocol

import java.util

case class ResponseTaskProgress(
    execId: String,
    progress: Float,
    progressInfo: Array[JobProgressInfo]
) extends RequestProtocol

case class ResponseEngineLock(lock: String)

case class EngineConcurrentInfo(
    runningTasks: Int,
    pendingTasks: Int,
    succeedTasks: Int,
    failedTasks: Int
)

case class ResponseTaskLog(execId: String, log: String) extends RequestProtocol

case class ResponseTaskError(execId: String, errorMsg: String)
    extends RetryableProtocol
    with RequestProtocol

case class ResponseTaskStatus(execId: String, status: ExecutionNodeStatus)
    extends RetryableProtocol
    with RequestProtocol

class ResponseTaskStatusWithExecuteCodeIndex(
    execId: String,
    status: ExecutionNodeStatus,
    private var _errorIndex: Int = -1
) extends ResponseTaskStatus(execId, status) {
  def errorIndex: Int = _errorIndex
  def errorIndex_=(value: Int): Unit = _errorIndex = value
}

case class ResponseTaskResultSet(execId: String, output: String, alias: String)
    extends RetryableProtocol
    with RequestProtocol {
  override def toString: String = s"execId: $execId, alias: $alias"
}

case class ResponseTaskResultSize(execId: String, resultSize: Int)
    extends RetryableProtocol
    with RequestProtocol
