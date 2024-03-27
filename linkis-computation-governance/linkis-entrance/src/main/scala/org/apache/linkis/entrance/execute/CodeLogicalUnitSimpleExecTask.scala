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

package org.apache.linkis.entrance.execute

import org.apache.linkis.common.exception.{ErrorException, LinkisRetryException}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.governance.common.protocol.task.{RequestTask, RequestTaskExecute}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException,
  OrchestratorRetryException
}
import org.apache.linkis.orchestrator.execution.{AsyncTaskResponse, TaskResponse}
import org.apache.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import org.apache.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import org.apache.linkis.orchestrator.plans.ast.QueryParams
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.orchestrator.utils.OrchestratorIDCreator
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, SubmitResponse}

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Date

import com.google.common.collect.Lists
import org.slf4j.{Logger, LoggerFactory}

class CodeLogicalUnitSimpleExecTask extends ExecTask {

  implicit val log: Logger = LoggerFactory.getLogger(getClass)
  private val logger: Logger = LoggerFactory.getLogger(classOf[CodeLogicalUnitSimpleExecTask])

  private val codeExecTaskExecutorManager: CodeExecTaskExecutorManager =
    CodeExecTaskExecutorManager.getCodeExecTaskExecutorManager

  private var id: String = _
  private var codeLogicalUnit: CodeLogicalUnit = _
  private var executorUser: String = _
  private var labels: util.List[Label[_]] = _
  private var queryParams: QueryParams = _
  private var isCanceled = false

  def this(executorUser: String, labels: util.List[Label[_]], queryParams: QueryParams) {
    this()
    this.executorUser = executorUser
    this.labels = labels
    this.queryParams = queryParams
  }

  def execute(): TaskResponse = {
    logger.info(s"Start to execute CodeLogicalUnitSimpleExecTask(${getIDInfo()}).")
    var executor: Option[CodeExecTaskExecutor] = None
    var retryException: LinkisRetryException = null
    executor = Utils.tryCatch(codeExecTaskExecutorManager.askExecutor(this)) {
      case retry: LinkisRetryException =>
        retryException = retry
        None
      case e: ErrorException =>
        throw e
      case error: Throwable =>
        throw error
    }

    if (executor.isDefined && !isCanceled) {
      val requestTask = toRequestTask
      val codeExecutor = executor.get
      val msg = if (codeExecutor.getEngineConnExecutor.isReuse()) {
        s"Succeed to reuse ec : ${codeExecutor.getEngineConnExecutor.getServiceInstance}"
      } else {
        s"Succeed to create new ec : ${codeExecutor.getEngineConnExecutor.getServiceInstance}"
      }
      val response = Utils.tryCatch(codeExecutor.getEngineConnExecutor.execute(requestTask)) {
        t: Throwable =>
          logger.error(
            s"Failed to submit ${getIDInfo()} to ${codeExecutor.getEngineConnExecutor.getServiceInstance}",
            t
          )
          throw new LinkisRetryException(ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE, t.getMessage)
      }
      response match {
        case SubmitResponse(engineConnExecId) =>
          codeExecutor.setEngineConnTaskId(engineConnExecId)
          codeExecTaskExecutorManager.addEngineConnTaskInfo(codeExecutor)
          val infoMap = new util.HashMap[String, Object]
          infoMap.put(
            TaskConstant.ENGINE_INSTANCE,
            codeExecutor.getEngineConnExecutor.getServiceInstance.getInstance
          )
          infoMap.put(
            TaskConstant.TICKET_ID,
            // Ensure that the job metric has at least one EC record.
            // When the EC is reuse, the same EC may have two records, One key is Instance, and the other key is ticketId
            if (codeExecutor.getEngineConnExecutor.isReuse()) {
              codeExecutor.getEngineConnExecutor.getServiceInstance.getInstance
            } else {
              codeExecutor.getEngineConnExecutor.getTicketId
            }
          )
          infoMap.put(TaskConstant.ENGINE_CONN_TASK_ID, engineConnExecId)
          infoMap.put(TaskConstant.JOB_SUBMIT_TO_EC_TIME, new Date(System.currentTimeMillis))

          new AsyncTaskResponse {
            override def notifyMe(listener: NotifyListener): Unit = {}

            override def waitForCompleted(): TaskResponse = throw new OrchestratorErrorException(
              OrchestratorErrorCodeSummary.METHOD_NUT_SUPPORT_CODE,
              "waitForCompleted method not support"
            )
          }
        case ErrorExecuteResponse(message, t) =>
          logger.info(s"failed to submit task to engineConn,reason: $message")
          throw new OrchestratorRetryException(
            OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE,
            "failed to submit task to engineConn",
            t
          )
      }
    } else if (null != retryException) {
      new DefaultFailedTaskResponse(
        s"ask Engine failed ${retryException.getMessage}",
        OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE,
        retryException
      )
    } else {
      throw new OrchestratorRetryException(
        OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE,
        "Failed to ask executor"
      )
    }

  }

  private def toRequestTask(): RequestTask = {
    val requestTask: RequestTaskExecute = new RequestTaskExecute()
    requestTask.setCode(getCodeLogicalUnit.toStringCode)
    requestTask.setLabels(getLabels)
    if (getQueryParams.getRuntimeParams.getJobs != null) {
      requestTask.getProperties.putAll(getQueryParams.getRuntimeParams.getJobs)
    }
    requestTask.getProperties.putAll(getQueryParams.getRuntimeParams.toMap)
    requestTask.setSourceID(getIDInfo)
    requestTask
  }

  override def getQueryParams: QueryParams = queryParams

  def setQueryParams(queryParams: QueryParams): Unit = {
    this.queryParams = queryParams
  }

  def getCodeLogicalUnit: CodeLogicalUnit = codeLogicalUnit

  def setCodeLogicalUnit(codeLogicalUnit: CodeLogicalUnit): Unit = {
    this.codeLogicalUnit = codeLogicalUnit
  }

  def getLabels: util.List[Label[_]] = labels

  def getExecutorUser: String = executorUser

  def kill(): Unit = {
    codeExecTaskExecutorManager.getByExecTaskId(this.getId).foreach { codeEngineConnExecutor =>
      if (StringUtils.isNotBlank(codeEngineConnExecutor.getEngineConnTaskId)) {
        logger.info(
          s"execTask($getId) be killed, engineConn execId is${codeEngineConnExecutor.getEngineConnTaskId}"
        )
        Utils.tryAndWarn(
          codeEngineConnExecutor.getEngineConnExecutor
            .killTask(codeEngineConnExecutor.getEngineConnTaskId)
        )
        // Utils.tryAndWarn(codeExecTaskExecutorManager.unLockEngineConn(this, codeEngineConnExecutor))
      }
    }
    isCanceled = true
  }

  def clear(isSucceed: Boolean): Unit = {
    codeExecTaskExecutorManager.getByExecTaskId(this.getId).foreach { codeEngineConnExecutor =>
      codeExecTaskExecutorManager.markTaskCompleted(this, codeEngineConnExecutor, isSucceed)
    }
  }

  override def getId: String = {
    if (null == id) synchronized {
      if (null == id) {
        id = OrchestratorIDCreator.getPhysicalTaskIDCreator.nextID("codeExec")
      }
    }
    id
  }

  protected def isCompleted(status: String): Boolean = {
    Lists.newArrayList("Success", "Failed", "ShuttingDown").contains(status)
  }

}
