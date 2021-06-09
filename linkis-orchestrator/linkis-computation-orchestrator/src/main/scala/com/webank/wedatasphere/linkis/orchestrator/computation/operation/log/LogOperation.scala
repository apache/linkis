/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.operation.log

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.orchestrator.core.AbstractOrchestration
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation.OperationBuilder
import com.webank.wedatasphere.linkis.orchestrator.listener.task.{TaskLogEvent, TaskLogListener}
import com.webank.wedatasphere.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorListenerBusContext}
import com.webank.wedatasphere.linkis.orchestrator.{Orchestration, OrchestratorSession}

import scala.collection.mutable

/**
  *
  *
  */
class LogOperation(orchestratorSession: OrchestratorSession) extends Operation[LogProcessor] with TaskLogListener{

  private val execTaskToLogProcessor = new mutable.HashMap[String, LogProcessor]()

  private var isInitialized = false

  def init(): Unit = {
    if (! isInitialized) synchronized{
      if (! isInitialized) {
        orchestratorSession.getOrchestratorSessionState.getOrchestratorAsyncListenerBus.addListener(this)
        isInitialized = true
      }
    }
  }

  override def apply(orchestration: Orchestration): LogProcessor = {
    orchestration match {
      case abstractOrchestration: AbstractOrchestration =>
        if (null != abstractOrchestration.physicalPlan) {
          val execTask = abstractOrchestration.physicalPlan
          val logProcessor = new LogProcessor(execTask.getId, orchestration, this)
          execTaskToLogProcessor.put(execTask.getId, logProcessor)
          return logProcessor
        }
      case _ =>
    }
    null
  }

  override def getName: String = {
    if (! isInitialized) {
      init()
    }
    LogOperation.LOG
  }

  def removeLogProcessor(execTaskId: String): Unit = {
    execTaskToLogProcessor.remove(execTaskId)
  }

  override def onEvent(event: OrchestratorAsyncEvent): Unit = event match {
    case taskLogEvent: TaskLogEvent =>
      onLogUpdate(taskLogEvent)
    case _ =>
  }

  override def onLogUpdate(taskLogEvent: TaskLogEvent): Unit = {
    execTaskToLogProcessor.get(taskLogEvent.execTask.getPhysicalContext.getRootTask.getId).foreach(_.writeLog(taskLogEvent.log))
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}
}

object LogOperation {
  val LOG = "LOG"
}

class LogOperationBuilder extends OperationBuilder {
  override def apply(v1: OrchestratorSession): Operation[_] = {
    val logOperation = new LogOperation(v1)

    logOperation
  }
}