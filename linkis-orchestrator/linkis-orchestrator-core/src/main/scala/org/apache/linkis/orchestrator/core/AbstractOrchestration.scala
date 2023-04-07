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

package org.apache.linkis.orchestrator.core

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.{Orchestration, OrchestratorSession}
import org.apache.linkis.orchestrator.core.OrchestrationFuture.NotifyListener
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException,
  OrchestratorRetryException
}
import org.apache.linkis.orchestrator.exception.OrchestratorErrorCodeSummary._
import org.apache.linkis.orchestrator.execution.{
  AsyncTaskResponse,
  CompletedTaskResponse,
  FailedTaskResponse,
  TaskResponse
}
import org.apache.linkis.orchestrator.extensions.operation.Operation
import org.apache.linkis.orchestrator.planner.command.ExplainCommandDesc
import org.apache.linkis.orchestrator.plans.ast.ASTOrchestration
import org.apache.linkis.orchestrator.plans.logical.{CommandTask, Task}
import org.apache.linkis.orchestrator.plans.physical.ExecTask

import org.apache.commons.io.IOUtils

import org.slf4j.LoggerFactory

/**
 */
abstract class AbstractOrchestration(
    override val orchestratorSession: OrchestratorSession,
    planBuilder: PlanBuilder
) extends Orchestration {

  self =>

  def this(orchestratorSession: OrchestratorSession, astPlan: ASTOrchestration[_]) = this(
    orchestratorSession,
    orchestratorSession.getOrchestratorSessionState
      .createPlanBuilder()
      .setOrchestratorSession(orchestratorSession)
      .setASTPlan(astPlan)
  )

  def this(orchestratorSession: OrchestratorSession, logicalPlan: Task) = this(
    orchestratorSession,
    orchestratorSession.getOrchestratorSessionState
      .createPlanBuilder()
      .setOrchestratorSession(orchestratorSession)
      .setLogicalPlan(logicalPlan)
  )

  private[orchestrator] lazy val logicalPlan: Task = planBuilder.getLogicalPlan
  private[orchestrator] lazy val physicalPlan: ExecTask = planBuilder.getBuiltPhysicalPlan
  private var orchestrationResponse: OrchestrationResponse = _

  protected def getOrchestrationResponse(taskResponse: TaskResponse): OrchestrationResponse =
    taskResponse match {
      case failed: FailedTaskResponse => failed
      case resp: CompletedTaskResponse => resp
      case async: AsyncTaskResponse =>
        val asyncTaskResponse = async.waitForCompleted()
        getOrchestrationResponse(asyncTaskResponse)
      case r =>
        throw new OrchestratorErrorException(
          ORCHESTRATION_FOR_RESPONSE_NOT_SUPPORT_ERROR_CODE,
          "Not supported taskResponse " + r
        )
    }

  override def execute(): OrchestrationResponse = {
    val taskResponse =
      orchestratorSession.getOrchestratorSessionState.getExecution.execute(physicalPlan)
    this.orchestrationResponse = getOrchestrationResponse(taskResponse)
    this.orchestrationResponse
  }

  override def collectAsString(): String = {
    if (orchestrationResponse == null) {
      execute()
    }
    orchestrationResponse match {
      case resp: ResultSetOrchestrationResponse => resp.getResultSet
      case resp: ResultSetPathOrchestrationResponse =>
        val fs = getFileSystem(resp.getResultSetPath)
        Utils.tryFinally(
          IOUtils.toString(fs.read(resp.getResultSetPath), Configuration.BDP_ENCODING.getValue)
        )(fs.close())
      case _ =>
        collectResultSet(orchestrationResponse)
    }
  }

  protected def collectResultSet(orchestrationResponse: OrchestrationResponse): String

  protected def getFileSystem(fsPath: FsPath): Fs

  // scalastyle:off println
  override def collectAndPrint(): Unit = println(collectAsString())

  override def asyncExecute(): OrchestrationFuture = {
    val resp =
      orchestratorSession.getOrchestratorSessionState.getExecution.executeAsync(physicalPlan)
    new OrchestrationFutureImpl(resp)
  }

  override def cache(cacheStrategy: CacheStrategy): Unit =
    orchestratorSession.orchestrator.getOrchestratorContext.getGlobalState.orchestrationCacheManager
      .cacheOrchestration(this, cacheStrategy)

  override def cache(): Unit = cache(CacheStrategy.ONLY_SESSION_AND_CS_TERM_CACHE)

  override def uncache(): Unit =
    orchestratorSession.orchestrator.getOrchestratorContext.getGlobalState.orchestrationCacheManager
      .uncacheOrchestration(this)

  override def explain(allPlans: Boolean): String = {
    val commandProcessor = new CommandTask
    val explainCommandDesc = ExplainCommandDesc(this, allPlans)
    commandProcessor.setTaskDesc(explainCommandDesc)
    createOrchestration(commandProcessor).collectAsString()
  }

  protected def createOrchestration(logicalPlan: Task): Orchestration

  class OrchestrationFutureImpl(asyncTaskResponse: AsyncTaskResponse)
      extends OrchestrationFuture
      with Logging {

    private val waitLock = new Array[Byte](0)

    override def cancel(errorMsg: String, cause: Throwable): Unit = {
      logger.info("Orchestrator to kill job {} ", self.physicalPlan.getIDInfo())
      operate(Operation.CANCEL)
    }

    override def getResponse: OrchestrationResponse = orchestrationResponse

    override def operate[T](operationName: String): T =
      orchestratorSession.getOrchestratorSessionState.getOperations
        .find(_.getName == operationName)
        .map(_(self).asInstanceOf[T])
        .getOrElse(
          throw new OrchestratorErrorException(
            ORCHESTRATION_FOR_OPERATION_NOT_SUPPORT_ERROR_CODE,
            "Not supported operationName: " + operationName
          )
        )

    override def notifyMe(listener: NotifyListener): Unit = asyncTaskResponse.notifyMe { resp =>
      orchestrationResponse = getOrchestrationResponse(resp)
      listener(orchestrationResponse)
    }

    override def isCompleted: Boolean = orchestrationResponse != null

    override def waitForCompleted(): Unit = {
      val taskResponse = asyncTaskResponse.waitForCompleted()
      orchestrationResponse = getOrchestrationResponse(taskResponse)
    }

    override def waitForCompleted(waitMills: Long): Unit = {
      notifyMe(resp => {
        waitLock synchronized waitLock.notify()
      })
      if (isCompleted) return
      waitLock synchronized {
        waitLock.wait(waitMills)
      }
      if (!isCompleted) {
        cancel("execute time out kill task")
        throw new OrchestratorRetryException(
          OrchestratorErrorCodeSummary.EXECUTION_TIME_OUT,
          s"wait more than $waitMills"
        )
      }
    }

  }

}
