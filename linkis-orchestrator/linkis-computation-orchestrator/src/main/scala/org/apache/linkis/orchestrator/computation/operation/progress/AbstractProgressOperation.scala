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

package org.apache.linkis.orchestrator.computation.operation.progress

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.{Orchestration, OrchestratorSession}
import org.apache.linkis.orchestrator.core.AbstractOrchestration
import org.apache.linkis.orchestrator.extensions.operation.Operation
import org.apache.linkis.orchestrator.extensions.operation.Operation.OperationBuilder
import org.apache.linkis.orchestrator.listener.OrchestratorAsyncEvent
import org.apache.linkis.orchestrator.listener.task.{TaskProgressListener, TaskRunningInfoEvent}

import java.util.concurrent.ConcurrentHashMap

/**
 * Abstract class of progress operation
 */
abstract class AbstractProgressOperation(orchestratorSession: OrchestratorSession)
    extends Operation[ProgressProcessor]
    with TaskProgressListener
    with Logging {

  /**
   * Store execTask Id => ProgressProcessor
   */
  protected val execTaskToProgressProcessor = new ConcurrentHashMap[String, ProgressProcessor]()

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
      case progressWithResourceEvent: TaskRunningInfoEvent =>
        onProgressOn(progressWithResourceEvent)
      case _ =>
    }
  }

  def removeLogProcessor(execTaskId: String): Unit = {
    this.execTaskToProgressProcessor.remove(execTaskId)
  }

  override def onEventError(event: Event, t: Throwable): Unit = {
    var eventName: String = "Null Event"
    var message: String = "NULL"
    var cause: String = "NULL"
    if (null != event) {
      eventName = event.getClass.getName
    }
    if (null != t) {
      message = t.getClass.getSimpleName + t.getMessage
      if (null != t.getCause) {
        cause = t.getCause.getMessage
      }
    }
    logger.warn(
      s"Accept error event ${eventName} in progress operation, message: ${message}, cause : ${cause}"
    )
  }

}

class ProgressOperationBuilder extends OperationBuilder {

  override def apply(v1: OrchestratorSession): Operation[_] = {
    val progressOperation = new DefaultProgressOperation(v1)
    progressOperation
  }

}

object ProgressConstraints {
  val PROGRESS_MAP_NAME = "progress-map"
}
