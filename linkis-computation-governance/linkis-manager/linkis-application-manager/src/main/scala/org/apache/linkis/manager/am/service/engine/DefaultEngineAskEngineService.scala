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

package org.apache.linkis.manager.am.service.engine

import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.protocol.engine._
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent._
import scala.util.{Failure, Success}

import feign.RetryableException

@Service
class DefaultEngineAskEngineService
    extends AbstractEngineService
    with EngineAskEngineService
    with Logging {

  @Autowired
  private var engineCreateService: EngineCreateService = _

  @Autowired
  private var engineReuseService: EngineReuseService = _

  @Autowired
  private var engineSwitchService: EngineSwitchService = _

  private val idCreator = new AtomicInteger()

  private val idPrefix = Sender.getThisServiceInstance.getInstance

  private implicit val executor: ExecutionContextExecutorService =
    Utils.newCachedExecutionContext(
      AMConfiguration.ASK_ENGINE_ASYNC_MAX_THREAD_SIZE,
      "AskEngineService-Thread-"
    )

  @Receiver
  override def askEngine(engineAskRequest: EngineAskRequest, sender: Sender): Any = {
    val taskId = JobUtils.getJobIdFromStringMap(engineAskRequest.getProperties)
    logger.info(s"received task: $taskId, engineAskRequest $engineAskRequest")
    if (!engineAskRequest.getLabels.containsKey(LabelKeyConstant.EXECUTE_ONCE_KEY)) {
      val engineReuseRequest = new EngineReuseRequest()
      engineReuseRequest.setLabels(engineAskRequest.getLabels)
      engineReuseRequest.setTimeOut(engineAskRequest.getTimeOut)
      engineReuseRequest.setUser(engineAskRequest.getUser)
      engineReuseRequest.setProperties(engineAskRequest.getProperties)
      val reuseNode = Utils.tryCatch(engineReuseService.reuseEngine(engineReuseRequest, sender)) {
        t: Throwable =>
          t match {
            case retryException: LinkisRetryException =>
              logger.info(
                s"task: $taskId user ${engineAskRequest.getUser} reuse engine failed ${t.getMessage}"
              )
            case _ =>
              logger.info(s"task: $taskId user ${engineAskRequest.getUser} reuse engine failed", t)
          }
          null
      }
      if (null != reuseNode) {
        logger.info(
          s"Finished to ask engine for task: $taskId user ${engineAskRequest.getUser} by reuse node $reuseNode"
        )
        return reuseNode
      }
    }

    val engineAskAsyncId = getAsyncId
    val createNodeThread = Future {
      logger.info(
        s"Task: $taskId start to async($engineAskAsyncId) createEngine, ${engineAskRequest.getCreateService}"
      )
      // 如果原来的labels含engineInstance ，先去掉
      engineAskRequest.getLabels.remove("engineInstance")
      val engineCreateRequest = new EngineCreateRequest
      engineCreateRequest.setLabels(engineAskRequest.getLabels)
      engineCreateRequest.setTimeout(engineAskRequest.getTimeOut)
      engineCreateRequest.setUser(engineAskRequest.getUser)
      engineCreateRequest.setProperties(engineAskRequest.getProperties)
      engineCreateRequest.setCreateService(engineAskRequest.getCreateService)
      val createNode = engineCreateService.createEngine(engineCreateRequest, sender)
      val timeout =
        if (engineCreateRequest.getTimeout <= 0) {
          AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong
        } else engineCreateRequest.getTimeout
      // useEngine 需要加上超时
      val createEngineNode = getEngineNodeManager.useEngine(createNode, timeout)
      if (null == createEngineNode) {
        throw new LinkisRetryException(
          AMConstant.EM_ERROR_CODE,
          s"create engine${createNode.getServiceInstance} success, but to use engine failed"
        )
      }
      logger.info(
        s"Task: $taskId finished to ask engine for user ${engineAskRequest.getUser} by create node $createEngineNode"
      )
      createEngineNode
    }

    createNodeThread.onComplete {
      case Success(engineNode) =>
        logger.info(s"Task: $taskId Success to async($engineAskAsyncId) createEngine $engineNode")
        sender.send(EngineCreateSuccess(engineAskAsyncId, engineNode))
      case Failure(exception) =>
        val retryFlag = exception match {
          case retryException: LinkisRetryException => true
          case retryableException: RetryableException => true
          case _ =>
            ExceptionUtils.getRootCause(exception) match {
              case socketTimeoutException: SocketTimeoutException => true
              case timeoutException: TimeoutException => true
              case _ =>
                false
            }
        }
        val msg =
          s"Task: $taskId Failed  to async($engineAskAsyncId) createEngine, can Retry $retryFlag";
        if (!retryFlag) {
          logger.info(msg, exception)
        } else {
          logger.info(s"msg: ${msg} canRetry Exception: ${exception.getClass.getName}")
        }

        sender.send(
          EngineCreateError(
            engineAskAsyncId,
            ExceptionUtils.getRootCauseMessage(exception),
            retryFlag
          )
        )
    }

    EngineAskAsyncResponse(engineAskAsyncId, Sender.getThisServiceInstance)
  }

  private def getAsyncId: String = {
    idPrefix + "_" + idCreator.getAndIncrement()
  }

}
