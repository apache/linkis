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

package org.apache.linkis.storage.io.client

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.entity.entrance.BindEngineLabel
import org.apache.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtil}
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.execution.{
  ArrayResultSetTaskResponse,
  FailedTaskResponse,
  SucceedTaskResponse
}
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.storage.domain.MethodEntity
import org.apache.linkis.storage.exception.{
  FSNotInitException,
  StorageErrorCode,
  StorageErrorException
}
import org.apache.linkis.storage.io.conf.IOFileClientConf
import org.apache.linkis.storage.io.orchestrator.IOFileOrchestratorFactory
import org.apache.linkis.storage.io.utils.IOClientUtils

import org.springframework.stereotype.Component

import java.util

@Component
class DefaultIOClient extends IOClient with Logging {

  private val loadBalanceLabel = IOClientUtils.getDefaultLoadBalanceLabel

  private val extraLabels: Array[Label[_]] = Utils.tryCatch(IOClientUtils.getExtraLabels()) {
    case throwable: Throwable =>
      logger.error("Failed to create extraLabels, No extra labels will be used", throwable)
      Array.empty[Label[_]]
  }

  override def execute(
      user: String,
      methodEntity: MethodEntity,
      bindEngineLabel: BindEngineLabel
  ): String = {
    val params = new util.HashMap[String, AnyRef]()
    if (null != bindEngineLabel) {
      IOClientUtils.addLabelToParams(bindEngineLabel, params)
    }
    executeResult(user, methodEntity, params)
  }

  def executeResult(
      user: String,
      methodEntity: MethodEntity,
      params: java.util.Map[String, AnyRef],
      retryLimit: Int = 0
  ): String = {
    val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(
      EngineType.mapFsTypeToEngineType(methodEntity.getFsType)
    )
    IOClientUtils.addLabelToParams(loadBalanceLabel, params)
    IOClientUtils.addLabelToParams(engineTypeLabel, params)
    extraLabels.foreach(label => IOClientUtils.addLabelToParams(label, params))
    val startTime = System.currentTimeMillis()
    val jobReq = IOClientUtils.buildJobReq(user, methodEntity, params)
    if (null == jobReq) {
      throw new StorageErrorException(
        IOFileClientConf.IO_EXECUTE_FAILED_CODE,
        s"Job with id ${jobReq.getId} failed to execute method fs for user : ${user}, code : $methodEntity， Because jobReq is null"
      )
    }
    val bindEngineLabel = LabelUtil.getBindEngineLabel(jobReq.getLabels)
    if (null == bindEngineLabel) {
      throw new StorageErrorException(
        IOFileClientConf.IO_EXECUTE_FAILED_CODE,
        s"Job with id ${jobReq.getId} failed to execute method fs for user : ${user}, code : $methodEntity， Because bindEngineLabel is null"
      )
    }

    val orchestration = IOFileOrchestratorFactory.getOrchestratorSession().orchestrate(jobReq)
    val orchestrationTime = System.currentTimeMillis()
    val response = if (retryLimit > 0) {
      var response = orchestration.execute()
      var initCount = 0
      while (!response.isInstanceOf[SucceedTaskResponse] && initCount < retryLimit) {
        initCount += 1
        logger.info(
          s"JobId ${jobReq.getId} execute method ${methodEntity} failed, to retry $initCount"
        )
        val reTryOrchestration =
          IOFileOrchestratorFactory.getOrchestratorSession().orchestrate(jobReq)
        response = reTryOrchestration.execute()
      }
      response
    } else {
      val future = orchestration.asyncExecute()
      future.waitForCompleted(IOFileClientConf.IO__JOB_WAIT_S * 1000)
      future.getResponse
    }
    val result: String = response match {
      case succeedResponse: SucceedTaskResponse =>
        succeedResponse match {
          case arrayResultSetPathResp: ArrayResultSetTaskResponse =>
            val firstResultSet = arrayResultSetPathResp.getResultSets.headOption.orNull
            if (null != firstResultSet) {
              // tod check
              firstResultSet.result
            } else {
              logger.info(s"JobId ${jobReq.getId} execute method ${methodEntity} with null result.")
              IOClientUtils.SUCCESS
            }
          case _ =>
            logger.info(s"JobId ${jobReq.getId} execute method ${methodEntity} with null result.")
            IOClientUtils.SUCCESS
        }
      case failedResponse: FailedTaskResponse =>
        val msg =
          s"IO_FILE job: ${jobReq.getId} failed to execute code : ${methodEntity}, reason : ${failedResponse.getErrorMsg}."
        logger.info(msg)
        if (
            failedResponse.getErrorMsg.contains(
              StorageErrorCode.FS_NOT_INIT.getMessage
            ) || failedResponse.getErrorMsg.contains(
              ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE.toString
            )
        ) {
          throw new FSNotInitException()
        }
        throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_FAILED_CODE, msg)
      case o =>
        val msg =
          s"IO_FILE job : ${jobReq.getId} failed to execute code : ${methodEntity}, return a unknown response : ${BDPJettyServerHelper.gson
            .toJson(o)}"
        logger.warn(msg)
        throw new StorageErrorException(IOFileClientConf.IO_EXECUTE_UNKNOWN_REASON_CODE, msg)
    }
    val executeTime = System.currentTimeMillis()
    logger.info(
      s"${jobReq.getId} execute method ${methodEntity.getMethodName}, orchestratorTime(${orchestrationTime - startTime}ms) execute time(${executeTime - orchestrationTime}ms)"
    )
    result
  }

  override def executeWithRetry(
      user: String,
      methodEntity: MethodEntity,
      bindEngineLabel: BindEngineLabel,
      reTryLimit: Int = defaultRetry
  ): String = {
    val params = new util.HashMap[String, AnyRef]()
    if (null != bindEngineLabel) {
      IOClientUtils.addLabelToParams(bindEngineLabel, params)
    }
    val result = executeResult(user, methodEntity, params, reTryLimit)
    result
  }

}
