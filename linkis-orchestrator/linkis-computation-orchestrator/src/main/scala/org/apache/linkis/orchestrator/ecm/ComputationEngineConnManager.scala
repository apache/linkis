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

package org.apache.linkis.orchestrator.ecm

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.engine.{
  EngineAskAsyncResponse,
  EngineAskRequest,
  EngineCreateError,
  EngineCreateSuccess
}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.ecm.cache.EngineAsyncResponseCache
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.entity.{DefaultMark, Mark, MarkReq, Policy}
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.EngineConnExecutor
import org.apache.linkis.orchestrator.ecm.service.impl.{
  ComputationConcurrentEngineConnExecutor,
  ComputationEngineConnExecutor
}
import org.apache.linkis.orchestrator.listener.task.TaskLogEvent
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.exception.ExceptionUtils

import java.net.{SocketException, SocketTimeoutException}
import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

/**
 */
class ComputationEngineConnManager extends AbstractEngineConnManager with Logging {

  private val idCreator = new AtomicInteger()

  private val cacheMap = EngineAsyncResponseCache.getCache

  override def getPolicy(): Policy = Policy.Process

  override def applyMark(markReq: MarkReq): Mark = {
    if (null == markReq) return null
    createMark(markReq)
  }

  override def createMark(markReq: MarkReq): Mark = {
    val mark = new DefaultMark(nextMarkId(), markReq)
    addMark(mark, new util.ArrayList[ServiceInstance]())
    mark
  }

  protected def nextMarkId(): String = {
    "mark_" + idCreator.getAndIncrement()
  }

  override protected def askEngineConnExecutor(
      engineAskRequest: EngineAskRequest,
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): EngineConnExecutor = {
    engineAskRequest.setTimeOut(getEngineConnApplyTime)
    var count = getEngineConnApplyAttempts()
    var retryException: LinkisRetryException = null
    while (count >= 1) {
      count = count - 1
      val start = System.currentTimeMillis()
      try {
        val (engineNode, reuse) =
          getEngineNodeAskManager(engineAskRequest, mark, execTask)
        if (null != engineNode) {
          val engineConnExecutor =
            if (
                null != engineAskRequest.getLabels &&
                engineAskRequest.getLabels.containsKey(LabelKeyConstant.CONCURRENT_ENGINE_KEY)
            ) {
              new ComputationConcurrentEngineConnExecutor(engineNode, getParallelism())
            } else {
              new ComputationEngineConnExecutor(engineNode)
            }
          if (null != engineNode.getLabels) {
            engineConnExecutor.setLabels(engineNode.getLabels.asScala.toList.toArray)
          }
          engineConnExecutor.setReuse(reuse)
          return engineConnExecutor
        }
      } catch {
        case t: LinkisRetryException =>
          val taken = ByteTimeUtils.msDurationToString(System.currentTimeMillis - start)
          logger.warn(
            s"${mark.getMarkId()} Failed to askEngineAskRequest time taken ($taken), ${t.getMessage}"
          )
          retryException = t
          // add isCrossClusterRetryException flag
          engineAskRequest.getProperties.put("isCrossClusterRetryException", "true")

        case t: Throwable =>
          val taken = ByteTimeUtils.msDurationToString(System.currentTimeMillis - start)
          logger.warn(s"${mark.getMarkId()} Failed to askEngineAskRequest time taken ($taken)")
          throw t
      }
    }
    if (retryException != null) {
      throw retryException
    } else {
      throw new ECMPluginErrorException(
        ECMPluginConf.ECM_ERROR_CODE,
        s"${mark.getMarkId()} Failed to ask engineAskRequest $engineAskRequest by retry ${getEngineConnApplyAttempts - count}  "
      )
    }
  }

  private def getEngineNodeAskManager(
      engineAskRequest: EngineAskRequest,
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): (EngineNode, Boolean) = {
    val response = Utils.tryCatch(getManagerSender().ask(engineAskRequest)) { t: Throwable =>
      val baseMsg = s"mark ${mark.getMarkId()}  failed to ask linkis Manager Can be retried "
      ExceptionUtils.getRootCause(t) match {
        case socketTimeoutException: SocketTimeoutException =>
          val msg = baseMsg + ExceptionUtils.getMessage(socketTimeoutException)
          throw new LinkisRetryException(ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE, msg)
        case socketException: SocketException =>
          val msg = baseMsg + ExceptionUtils.getMessage(socketException)
          throw new LinkisRetryException(ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE, msg)
        case _ =>
          throw t
      }
    }

    response match {
      case engineNode: EngineNode =>
        logger.debug(s"Succeed to reuse engineNode $engineNode mark ${mark.getMarkId()}")
        (engineNode, true)
      case engineAskAsyncResponse: EngineAskAsyncResponse =>
        logger.info(
          "{} received EngineAskAsyncResponse id: {} serviceInstance: {}",
          Array(
            mark.getMarkId(),
            engineAskAsyncResponse.getId,
            engineAskAsyncResponse.getManagerInstance
          ): _*
        )
        execTask.getPhysicalContext.pushLog(
          TaskLogEvent(execTask, LogUtils.generateInfo(s"Request LinkisManager:${response}"))
        )
        cacheMap.getAndRemove(
          engineAskAsyncResponse.getId,
          Duration(engineAskRequest.getTimeOut + 100000, TimeUnit.MILLISECONDS)
        ) match {
          case engineCreateSucces: EngineCreateSuccess =>
            logger.info(
              "{} async id: {} success to async get EngineNode {}",
              Array(
                mark.getMarkId(),
                engineCreateSucces.getId,
                engineCreateSucces.getEngineNode
              ): _*
            )
            (engineCreateSucces.getEngineNode, false)
          case engineCreateError: EngineCreateError =>
            logger.debug(
              "{} async id: {} Failed  to async get EngineNode, {}",
              Array(mark.getMarkId(), engineCreateError.getId, engineCreateError.getException): _*
            )
            if (engineCreateError.getRetry) {
              throw new LinkisRetryException(
                ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE,
                engineCreateError.getId + " Failed  to async get EngineNode " + engineCreateError.getException
              )
            } else {
              throw new ECMPluginErrorException(
                ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE,
                engineCreateError.getId + " Failed  to async get EngineNode " + engineCreateError.getException
              )
            }
        }
      case _ =>
        logger.info(
          "{} Failed to ask engineAskRequest {}, response is not engineNode",
          mark.getMarkId(): Any,
          engineAskRequest: Any
        )
        (null, false)
    }
  }

  private def getManagerSender(): Sender = {
    Sender.getSender(GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue)
  }

}
