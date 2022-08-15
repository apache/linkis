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

package org.apache.linkis.engineconn.computation.executor.upstream

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineconn.computation.executor.upstream.access.{
  ConnectionInfoAccessRequest,
  ECTaskEntranceInfoAccess,
  ECTaskEntranceInfoAccessRequest
}
import org.apache.linkis.engineconn.computation.executor.upstream.handler.{
  ECTaskKillHandler,
  ECTaskKillHandlerRequest,
  MonitorHandlerRequest
}
import org.apache.linkis.engineconn.computation.executor.upstream.wrapper.{
  ConnectionInfoWrapper,
  ECTaskEntranceConnectionWrapper
}

import java.util

class ECTaskEntranceMonitor
    extends SingleThreadUpstreamConnectionMonitor(
      name = "ECTask-upstream-connection-monitor",
      infoAccess = new ECTaskEntranceInfoAccess,
      handler = new ECTaskKillHandler
    )
    with Logging {

  def register(task: EngineConnTask, executor: ComputationExecutor): Unit = {
    panicIfNull(task, "engineConnTask should not be null")
    panicIfNull(executor, "executor should not be null")
    val taskID = task.getTaskId
    if (wrapperMap.containsKey(taskID)) {
      logger.error("registered duplicate EngineConnTask!! task-id: " + taskID)
    }
    wrapperMap.putIfAbsent(taskID, new ECTaskEntranceConnectionWrapper(taskID, task, executor))
  }

  def unregister(taskID: String): Unit = {
    if (!wrapperMap.containsKey(taskID)) {
      logger.error("attempted to unregister non-existing EngineConnTask!! task-id: " + taskID)
    }
    wrapperMap.remove(taskID)
  }

  override def generateInfoAccessRequest(
      wrapperList: util.List[ConnectionInfoWrapper]
  ): ConnectionInfoAccessRequest = {
    panicIfNull(wrapperList, "wrapperList cannot be null")
    new ECTaskEntranceInfoAccessRequest(wrapperList)
  }

  override def generateHandlerRequest(
      wrapperList: util.List[ConnectionInfoWrapper]
  ): MonitorHandlerRequest = {
    panicIfNull(wrapperList, "wrapperList cannot be null")
    new ECTaskKillHandlerRequest(wrapperList)
  }

}
