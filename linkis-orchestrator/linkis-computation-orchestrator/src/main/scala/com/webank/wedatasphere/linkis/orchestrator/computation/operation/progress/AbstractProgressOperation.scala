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

package com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress.ProgressProcessor
import com.webank.wedatasphere.linkis.orchestrator.core.AbstractOrchestration
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation.OperationBuilder
import com.webank.wedatasphere.linkis.orchestrator.listener.task.{TaskProgressEvent, TaskProgressListener}
import com.webank.wedatasphere.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorListenerBusContext}
import com.webank.wedatasphere.linkis.orchestrator.{Orchestration, OrchestratorSession}

import scala.collection.mutable

/**
  * Abstract class of progress operation
  */
abstract class AbstractProgressOperation(orchestratorSession: OrchestratorSession) extends  Operation[ProgressProcessor] with TaskProgressListener with Logging{

  /**
    * Store execTask Id => ProgressProcessor
    */
  protected val execTaskToProgressProcessor = new mutable.HashMap[String, ProgressProcessor]()

  override def apply(orchestration: Orchestration): ProgressProcessor = {
    orchestration match {
      case abstractOrchestration: AbstractOrchestration =>
        var progressProcessor: ProgressProcessor = null
        Option(abstractOrchestration.physicalPlan).foreach(execTask => {
          progressProcessor = new ProgressProcessor(execTask.getId, orchestration, this)
          execTaskToProgressProcessor.put(execTask.getId, progressProcessor)
        })
        progressProcessor
      case _ => null
    }
  }

  override def onEvent(event: OrchestratorAsyncEvent): Unit = {
    event match {
      case progressEvent: TaskProgressEvent =>
        onProgressOn(progressEvent)
      case _ =>
    }
  }

  def removeLogProcessor(execTaskId: String): Unit = {
    this.execTaskToProgressProcessor.remove(execTaskId)
  }

  override def onProgressOn(taskProgressEvent: TaskProgressEvent): Unit = {
    val execTask = taskProgressEvent.execTask
    execTaskToProgressProcessor.get(execTask.getPhysicalContext.getRootTask.getId).foreach( progress => {
      progress.onProgress(taskProgressEvent.progress,
        taskProgressEvent.progressInfo)
    })
  }

  override def onEventError(event: Event, t: Throwable): Unit = {
    var eventName: String = "Null Event"
    var message: String = "NULL"
    var cause: String = "NULL"
    if (null != event) {
      eventName = event.getClass.getName
    }
    if (null != t) {
      message = t.getMessage
      if (null != t.getCause) {
        cause = t.getCause.getMessage
      }
    }
    debug(s"Accept error event ${eventName} in progress operation, message: ${message}, cause : ${cause}")
  }
}

class ProgressOperationBuilder extends OperationBuilder {
  override def apply(v1: OrchestratorSession): Operation[_] = {
    val progressOperation = new DefaultProgressOperation(v1)
    progressOperation
  }
}

object ProgressConstraints{
  val PROGRESS_MAP_NAME = "progress-map"
}
