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

package org.apache.linkis.entrance.execute.simple

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.EntranceServer
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.orchestrator.listener.OrchestratorSyncEvent
import org.apache.linkis.orchestrator.listener.task.{
  TaskErrorResponseEvent,
  TaskResultSetEvent,
  TaskResultSetListener,
  TaskResultSetSizeEvent,
  TaskStatusEvent,
  TaskStatusListener
}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * 1.TaskLogListener: job.getLogListener.foreach(_.onLogUpdate(job, logEvent.log))
 *
 * 2.TaskProgressListener: entranceJob.getProgressListener.foreach( _.onProgressUpdate(entranceJob,
 * progressInfoEvent.progress, entranceJob.getProgressInfo)
 *
 * 3.TaskResultSetListener entranceContext.getOrCreatePersistenceManager().onResultSizeCreated(j,
 * taskResultSize.resultSize) .getOrCreatePersistenceManager() .onResultSetCreated(
 * entranceExecuteRequest.getJob, AliasOutputExecuteResponse(firstResultSet.alias,
 * firstResultSet.result) )
 *
 * 4. TaskStatusListener getEngineExecuteAsyncReturn.foreach { jobReturn => jobReturn.notifyStatus(
 * ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Succeed) ) } val msg
 * = failedResponse.getErrorCode + ", " + failedResponse.getErrorMsg
 * getEngineExecuteAsyncReturn.foreach { jobReturn => jobReturn.notifyError(msg,
 * failedResponse.getCause) jobReturn.notifyStatus(
 * ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Failed) ) }
 */
@Component
class SimpleSyncListener extends TaskStatusListener with TaskResultSetListener with Logging {

  @Autowired private var entranceServer: EntranceServer = _

  @PostConstruct
  def init(): Unit = {
    if (EntranceConfiguration.LINKIS_ENTRANCE_SKIP_ORCHESTRATOR) {
      SimpleExecuteBusContext
        .getOrchestratorListenerBusContext()
        .getOrchestratorSyncListenerBus
        .addListener(this)
    }
  }

  override def onStatusUpdate(taskStatusEvent: TaskStatusEvent): Unit = {}

  override def onTaskErrorResponseEvent(taskErrorResponseEvent: TaskErrorResponseEvent): Unit = {}

  override def onResultSetCreate(taskResultSetEvent: TaskResultSetEvent): Unit = {}

  override def onResultSizeCreated(taskResultSetSizeEvent: TaskResultSetSizeEvent): Unit = {}

  override def onSyncEvent(event: OrchestratorSyncEvent): Unit = {}

  override def onEventError(event: Event, t: Throwable): Unit = {}
}
