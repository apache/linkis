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

package org.apache.linkis.orchestrator.computation.operation.resource

import org.apache.linkis.common.listener.Event
import org.apache.linkis.orchestrator.{Orchestration, OrchestratorSession}
import org.apache.linkis.orchestrator.core.AbstractOrchestration
import org.apache.linkis.orchestrator.extensions.operation.Operation
import org.apache.linkis.orchestrator.extensions.operation.Operation.OperationBuilder
import org.apache.linkis.orchestrator.listener.OrchestratorAsyncEvent
import org.apache.linkis.orchestrator.listener.task.{ResourceReportListener, TaskYarnResourceEvent}

import java.util.concurrent.ConcurrentHashMap

class ResourceReportOperation(orchestratorSession: OrchestratorSession)
    extends Operation[ResourceReportProcessor]
    with ResourceReportListener {

  private val execTaskToLogProcessor = new ConcurrentHashMap[String, ResourceReportProcessor]()

  private var isInitialized = false

  def init(): Unit = {
    if (!isInitialized) synchronized {
      if (!isInitialized) {
        orchestratorSession.getOrchestratorSessionState.getOrchestratorAsyncListenerBus
          .addListener(this)
        isInitialized = true
      }
    }
  }

  override def apply(orchestration: Orchestration): ResourceReportProcessor = {
    orchestration match {
      case abstractOrchestration: AbstractOrchestration =>
        if (null != abstractOrchestration.physicalPlan) {
          val execTask = abstractOrchestration.physicalPlan
          val resourceReportProcessor =
            new ResourceReportProcessor(execTask.getId, orchestration, this)
          execTaskToLogProcessor.put(execTask.getId, resourceReportProcessor)
          return resourceReportProcessor
        }
      case _ =>
    }
    null
  }

  override def getName: String = {
    if (!isInitialized) {
      init()
    }
    ResourceReportOperation.RESOURCE
  }

  def removeResourceReportProcessor(execTaskId: String): Unit = {
    execTaskToLogProcessor.remove(execTaskId)
  }

  override def onEvent(event: OrchestratorAsyncEvent): Unit = event match {
    case taskYarnResourceEvent: TaskYarnResourceEvent =>
      resourceReport(taskYarnResourceEvent)
    case _ =>
  }

  override def resourceReport(taskYarnResourceEvent: TaskYarnResourceEvent): Unit = {
    Option(
      execTaskToLogProcessor
        .get(taskYarnResourceEvent.execTask.getPhysicalContext.getRootTask.getId)
    ).foreach(_.resourceReport(taskYarnResourceEvent.resourceMap))
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}
}

object ResourceReportOperation {
  val RESOURCE = "resource"
}

class ResourceReportOperationBuilder extends OperationBuilder {

  override def apply(v1: OrchestratorSession): Operation[_] = {
    val resourceReportOperation = new ResourceReportOperation(v1)
    resourceReportOperation
  }

}
