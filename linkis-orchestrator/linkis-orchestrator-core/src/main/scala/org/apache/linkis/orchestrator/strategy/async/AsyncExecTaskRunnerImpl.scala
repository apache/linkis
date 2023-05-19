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

package org.apache.linkis.orchestrator.strategy.async

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.core.ResultSet
import org.apache.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import org.apache.linkis.orchestrator.execution._
import org.apache.linkis.orchestrator.execution.impl.{
  DefaultFailedTaskResponse,
  DefaultResultSetTaskResponse
}
import org.apache.linkis.orchestrator.listener.execution.ExecTaskRunnerCompletedEvent
import org.apache.linkis.orchestrator.plans.physical.ExecTask
import org.apache.linkis.orchestrator.strategy.{
  ExecTaskStatusInfo,
  ResultSetExecTask,
  StatusInfoExecTask
}
import org.apache.linkis.orchestrator.utils.OrchestratorLoggerUtils

import scala.collection.mutable.ArrayBuffer

/**
 */
class AsyncExecTaskRunnerImpl(override val task: ExecTask)
    extends AsyncExecTaskRunner
    with Logging {

  private var status: ExecutionNodeStatus = ExecutionNodeStatus.Inited

  private var taskResponse: TaskResponse = _

  private val resultSets = new ArrayBuffer[ResultSet]()

  // private val syncListenerBus: OrchestratorSyncListenerBus = OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorSyncListenerBus
  private var resultSize: Int = -1

  override def getTaskResponse: TaskResponse = taskResponse

  override def isCompleted: Boolean = {
    ExecutionNodeStatus.isCompleted(status)
  }

  override def isRunning: Boolean = ExecutionNodeStatus.isRunning(status)

  override def isSucceed: Boolean = ExecutionNodeStatus.isScheduled(status)

  override def run(): Unit = {
    Utils.tryCatch {
      OrchestratorLoggerUtils.setJobIdMDC(task)
      logger.info(s"ExecTaskRunner Submit execTask(${task.getIDInfo}) to running")
      val response = task.execute()
      this.taskResponse = response
      response match {
        case async: AsyncTaskResponse =>
          transientStatus(ExecutionNodeStatus.Running)
        case succeed: SucceedTaskResponse =>
          logger.info(s"Succeed to execute ExecTask(${task.getIDInfo})")
          transientStatus(ExecutionNodeStatus.Succeed)
        case failedTaskResponse: FailedTaskResponse =>
          logger.info(s"Failed to execute ExecTask(${task.getIDInfo})")
          transientStatus(ExecutionNodeStatus.Failed)
        case retry: RetryTaskResponse =>
          logger.warn(s"ExecTask(${task.getIDInfo}) need to retry")
          transientStatus(ExecutionNodeStatus.WaitForRetry)
      }
    } { case e: Throwable =>
      logger.error(s"Failed to execute task ${task.getIDInfo}", e)
      this.taskResponse = new DefaultFailedTaskResponse(
        e.getMessage,
        OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
        e
      )
      transientStatus(ExecutionNodeStatus.Failed)
    }
    OrchestratorLoggerUtils.removeJobIdMDC()
  }

  override def transientStatus(status: ExecutionNodeStatus): Unit = {
    if (status.ordinal() < this.status.ordinal() && status != ExecutionNodeStatus.WaitForRetry) {
      logger.info(
        s"Task${task.getIDInfo()} status flip error! Cause: Failed to flip from ${this.status} to $status."
      )
      return
    }
    // throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE, s"Task status flip error! Cause: Failed to flip from ${this.status} to $status.") //抛异常
    logger.info(s"${task.getIDInfo} change status ${this.status} => $status.")
    beforeStatusChanged(this.status, status)
    val oldStatus = this.status
    this.status = status
    afterStatusChanged(oldStatus, status)
  }

  def afterStatusChanged(fromStatus: ExecutionNodeStatus, toStatus: ExecutionNodeStatus): Unit = {

    // result put to physicalContext
    if (ExecutionNodeStatus.isSucceed(toStatus)) {
      task match {
        case resultSetExecTask: ResultSetExecTask =>
          if (resultSets.nonEmpty) {
            this.taskResponse = new DefaultResultSetTaskResponse(resultSets.toArray)
          }
          this.taskResponse match {
            case resultSetTaskResponse: ArrayResultSetTaskResponse =>
              resultSetExecTask.addResultSet(resultSetTaskResponse)
            case _ =>
          }
        case _ =>
      }
    }
    // status put to physicalContext
    if (ExecutionNodeStatus.isCompleted(toStatus)) {
      task match {
        case statusExecTask: StatusInfoExecTask =>
          statusExecTask.addExecTaskStatusInfo(ExecTaskStatusInfo(toStatus, this.taskResponse))
        case _ =>
      }
      task match {
        case asyncExecTask: AsyncExecTask =>
          asyncExecTask.clear(ExecutionNodeStatus.isSucceed(toStatus))
        case _ =>
      }
      // to notify taskManager clear running tasks
      task.getPhysicalContext.broadcastSyncEvent(ExecTaskRunnerCompletedEvent(this))
    }
  }

  /**
   * @param fromStatus
   * @param toStatus
   */
  def beforeStatusChanged(fromStatus: ExecutionNodeStatus, toStatus: ExecutionNodeStatus): Unit = {
    task match {
      case asyncExecTask: AsyncExecTask =>
        if (
            ExecutionNodeStatus.isSucceed(
              toStatus
            ) && (resultSize < 0 || resultSets.size < resultSize)
        ) {
          val startWaitForPersistedTime = System.currentTimeMillis
          resultSets synchronized {
            while (
                (resultSize < 0 || resultSets.size < resultSize) && !isWaitForPersistedTimeout(
                  startWaitForPersistedTime
                )
            )
              resultSets.wait(1000)
          }
        }
      case _ =>
    }
  }

  protected def isWaitForPersistedTimeout(startWaitForPersistedTime: Long): Boolean =
    System.currentTimeMillis - startWaitForPersistedTime >= OrchestratorConfiguration.TASK_MAX_PERSIST_WAIT_TIME.getValue.toLong

  override def interrupt(): Unit = {
    markFailed("Job be cancelled", null)
    task match {
      case asyncExecTask: AsyncExecTask =>
        asyncExecTask.kill()
      case _ =>
    }
    transientStatus(ExecutionNodeStatus.Cancelled)
  }

  override def setResultSize(resultSize: Int): Unit = {
    logger.info(s"BaseExecTaskRunner ${task.getIDInfo()} get result size is $resultSize")
    if (this.resultSize == -1) this.resultSize = resultSize
    resultSets.notify()
  }

  override def addResultSet(resultSet: ResultSet): Unit = {
    logger.info(
      s"BaseExecTaskRunner ${task.getIDInfo()} get result, now size is ${resultSets.size}"
    )
    resultSets += resultSet
    resultSets.notify()
  }

  override def markFailed(errorMsg: String, cause: Throwable): Unit = {
    this.taskResponse = new DefaultFailedTaskResponse(
      errorMsg,
      OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE,
      cause
    )
  }

}
