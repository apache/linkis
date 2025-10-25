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
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.hook.{AskEngineConnHook, AskEngineConnHookContext}
import org.apache.linkis.manager.am.label.MultiUserEngineReuseLabelChooser
import org.apache.linkis.manager.am.service.engine.EngineAskEngineService.getAsyncId
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.engine._
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service

import java.net.SocketTimeoutException
import java.util
import java.util.Locale
import java.util.concurrent.{ConcurrentHashMap, Semaphore, ThreadPoolExecutor}
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters.asScalaBufferConverter
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

  @Autowired
  private var multiUserEngineReuseLabelChooser: MultiUserEngineReuseLabelChooser = _

  @Autowired(required = false)
  @Qualifier
  /* The implementation class of hook must be annotated with @Qualifier to take effect(hook的实现类必须加上@Qualifier注解才能生效) */
  var hooksArray: Array[AskEngineConnHook] = _

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  private val (reuseExecutor, reuseThreadPool)
      : (ExecutionContextExecutorService, ThreadPoolExecutor) =
    Utils.newCachedExecutionContextWithExecutor(
      AMConfiguration.REUSE_ENGINE_ASYNC_MAX_THREAD_SIZE,
      "ReuseEngineService-Thread-"
    )

  private val (createExecutor, createThreadPool)
      : (ExecutionContextExecutorService, ThreadPoolExecutor) =
    Utils.newCachedExecutionContextWithExecutor(
      AMConfiguration.CREATE_ENGINE_ASYNC_MAX_THREAD_SIZE,
      "CreateEngineService-Thread-"
    )

  private val (errorSendExecutor, errorSendThreadPool)
      : (ExecutionContextExecutorService, ThreadPoolExecutor) =
    Utils.newCachedExecutionContextWithExecutor(
      AMConfiguration.ASK_ENGINE_ERROR_ASYNC_MAX_THREAD_SIZE,
      "AskEngineErrorService-Thread-"
    )

  private val engineCreateSemaphoreMap: java.util.Map[String, Semaphore] =
    new ConcurrentHashMap[String, Semaphore]()

  @Receiver
  override def askEngine(engineAskRequest: EngineAskRequest, sender: Sender): Any = {
    val taskId = JobUtils.getJobIdFromStringMap(engineAskRequest.getProperties)
    LoggerUtils.setJobIdMDC(taskId)
    logger.info(s"received task: $taskId, engineAskRequest $engineAskRequest")
    if (hooksArray != null && hooksArray.size > 0) {
      val ctx = new AskEngineConnHookContext(engineAskRequest, sender)

      /** Throwing exceptions in hook will block(hook中抛异常会阻断) */
      hooksArray.foreach(h =>
        Utils.tryCatch(h.doHook(ctx)) { t =>
          {
            val engineAskAsyncId = getAsyncId
            val retryFlag = t match {
              case _: LinkisRetryException => true
              case _: RetryableException => true
              case _ =>
                ExceptionUtils.getRootCause(t) match {
                  case _: SocketTimeoutException => true
                  case _: TimeoutException => true
                  case _ =>
                    false
                }
            }
            return EngineCreateError(
              engineAskAsyncId,
              ExceptionUtils.getRootCauseMessage(t),
              retryFlag
            )
          }
        }
      )
    }

    val engineAskAsyncId = getAsyncId
    if (!engineAskRequest.getLabels.containsKey(LabelKeyConstant.EXECUTE_ONCE_KEY)) {
      val reuseNodeThread = Future {
        LoggerUtils.setJobIdMDC(taskId)
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
                  s"Task: $taskId user ${engineAskRequest.getUser} reuse engine failed ${t.getMessage}"
                )
              case _ =>
                logger.info(
                  s"Task: $taskId user ${engineAskRequest.getUser} reuse engine failed",
                  t
                )
            }
            null
        }
        if (reuseNode != null) {
          logger.info(
            s"Task: $taskId finished to ask engine for user ${engineAskRequest.getUser} by reuse node $reuseNode"
          )
          if (null != sender) {
            sender.send(EngineCreateSuccess(engineAskAsyncId, reuseNode, true))
            logger.info(
              s"Task: $taskId has sent EngineCreateSuccess($engineAskAsyncId, reuse=true) to Entrance."
            )
          } else {
            logger.warn(f"Task: $taskId will not send async using null sender.")
          }
        } else {
          logger.info(
            s"Task: $taskId reuse engine failed, will try to start a thread to async($engineAskAsyncId) createEngine, ${engineAskRequest.getCreateService}"
          )
          createEngine(engineAskRequest, taskId, engineAskAsyncId, sender)
        }
        LoggerUtils.removeJobIdMDC()
      }(reuseExecutor)
      logger.info(
        s"reuseExecutor: poolSize: ${reuseThreadPool.getPoolSize}, activeCount: ${reuseThreadPool.getActiveCount}, queueSize: ${reuseThreadPool.getQueue.size()}"
      )
      futureDeal(reuseNodeThread, taskId, engineAskAsyncId, sender, "reuse")
    } else {
      createEngine(engineAskRequest, taskId, engineAskAsyncId, sender)
    }
    LoggerUtils.removeJobIdMDC()
    EngineAskAsyncResponse(engineAskAsyncId, Sender.getThisServiceInstance)
  }

  private def createEngine(
      engineAskRequest: EngineAskRequest,
      taskId: String,
      engineAskAsyncId: String,
      sender: Sender
  ): Unit = {
    val createNodeThread = Future {
      LoggerUtils.setJobIdMDC(taskId)
      val (engineCreateKey, semaphore) =
        Utils.tryAndWarn(getKeyAndSemaphore(engineAskRequest.getLabels))
      Utils.tryFinally {
        logger.info(
          s"Task: $taskId start to async($engineAskAsyncId) createEngine, ${engineAskRequest.getCreateService}"
        )
        if (null != semaphore) {
          try {
            semaphore.acquire()
            logger.info(s"$engineCreateKey succeed to get lock")
          } catch {
            case e: Exception =>
              logger.warn(
                s"Task: $taskId user ${engineAskRequest.getUser} acquire semaphore failed",
                e
              )
          }
        }
        // If the original labels contain engineInstance, remove it first (如果原来的labels含engineInstance ，先去掉)
        engineAskRequest.getLabels.remove(LabelKeyConstant.ENGINE_INSTANCE_KEY)
        // 添加引擎启动驱动任务id标签
        val labels: util.Map[String, AnyRef] = engineAskRequest.getLabels
        labels.put(LabelKeyConstant.DRIVER_TASK_KEY, taskId)

        logger.info(s"Task: ${taskId} start to reuse engine.")
        var reuseNode: EngineNode = null
        if (!engineAskRequest.getLabels.containsKey(LabelKeyConstant.EXECUTE_ONCE_KEY)) {
          val engineReuseRequest = new EngineReuseRequest()
          engineReuseRequest.setLabels(engineAskRequest.getLabels)
          engineReuseRequest.setTimeOut(engineAskRequest.getTimeOut)
          engineReuseRequest.setUser(engineAskRequest.getUser)
          engineReuseRequest.setProperties(engineAskRequest.getProperties)
          reuseNode = Utils.tryCatch(engineReuseService.reuseEngine(engineReuseRequest, sender)) {
            t: Throwable =>
              t match {
                case retryException: LinkisRetryException =>
                  logger.info(
                    s"Task: $taskId user ${engineAskRequest.getUser} reuse engine failed ${t.getMessage}"
                  )
                case _ =>
                  logger.info(
                    s"Task: $taskId user ${engineAskRequest.getUser} reuse engine failed",
                    t
                  )
              }
              null
          }
        }

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
        // UseEngine requires a timeout (useEngine 需要加上超时)
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
        if (null != sender) {
          sender.send(EngineCreateSuccess(engineAskAsyncId, createEngineNode))
          logger.info(
            s"Task: $taskId has sent EngineCreateSuccess($engineAskAsyncId, reuse=false) to Entrance."
          )
        } else {
          logger.warn(s"Task: $taskId will not send async using null sender.")
        }
      } {
        Utils.tryAndWarn {
          if (null != semaphore) {
            semaphore.release()
            logger.info(s"$engineCreateKey succeed to relaese lock")
          }
        }
        LoggerUtils.removeJobIdMDC()
      }

    }(createExecutor)

    logger.info(
      s"createExecutor: poolSize: ${createThreadPool.getPoolSize}, activeCount: ${createThreadPool.getActiveCount}, queueSize: ${createThreadPool.getQueue.size()}"
    )
    futureDeal(createNodeThread, taskId, engineAskAsyncId, sender, "create")
  }

  private def futureDeal(
      future: Future[_],
      taskId: String,
      engineAskAsyncId: String,
      sender: Sender,
      functionStr: String
  ): Unit = {
    future.onComplete {
      case Success(_) => ()
      case Failure(exception) =>
        LoggerUtils.setJobIdMDC(taskId)
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
          s"Task: $taskId Failed to async($engineAskAsyncId) $functionStr EngineConn, can Retry $retryFlag";
        if (!retryFlag) {
          logger.info(msg, exception)
        } else {
          logger.info(s"msg: ${msg} canRetry Exception: ${exception.getClass.getName}")
        }

        Utils.tryFinally {
          sender.send(
            EngineCreateError(
              engineAskAsyncId,
              ExceptionUtils.getRootCauseMessage(exception),
              retryFlag
            )
          )
        } {
          LoggerUtils.removeJobIdMDC()
        }
    }(errorSendExecutor)
    logger.info(
      s"errorSendExecutor: poolSize: ${errorSendThreadPool.getPoolSize}, activeCount: ${errorSendThreadPool.getActiveCount}, queueSize: ${errorSendThreadPool.getQueue.size()}"
    )
  }

  /**
   * Current limiting logic, according to the engine type, the current is limited through the token
   * algorithm, for example, the default configuration of appconn is 10, and the appconn requested
   * at the same time can only be 10
   *   1. Returns as unique keys, the key labels of userCreator, engineTypelabel, and tenant label
   *      2. Semaphore is the corresponding number of tokens
   *
   * 限流逻辑，针对引擎类型，通过令牌算法进行限流，比如appconn 默认配置的10同时进行请求的appconn只能为10
   *   1. 返回为唯一键，为userCreator、engineTypelabel和tenant label的key标签 2. Semaphore 为对应的令牌数
   *
   * @param labels
   * @return
   */
  private def getKeyAndSemaphore(labels: util.Map[String, AnyRef]): (String, Semaphore) = {
    val labelList = LabelUtils.distinctLabel(
      labelBuilderFactory.getLabels(labels),
      new util.ArrayList[Label[_]]()
    )
    val chooseLabels = multiUserEngineReuseLabelChooser.chooseLabels(labelList)
    val userCreatorAndEngineTypeLabel =
      new RMLabelContainer(chooseLabels).getCombinedResourceLabel
    val engineCreateKey = if (null != userCreatorAndEngineTypeLabel) {
      userCreatorAndEngineTypeLabel.getStringValue + LabelUtil.getTenantValue(chooseLabels)
    } else {
      ""
    }
    val engineType = LabelUtil.getEngineType(chooseLabels)
    val semaphore =
      if (
          AMConfiguration.AM_ENGINE_ASK_MAX_NUMBER.containsKey(
            engineType.toLowerCase(Locale.getDefault)
          ) && StringUtils.isNotBlank(engineCreateKey)
      ) {
        val keyLock = engineCreateKey.intern()
        if (!engineCreateSemaphoreMap.containsKey(engineCreateKey)) keyLock synchronized {
          if (!engineCreateSemaphoreMap.containsKey(engineCreateKey)) {
            engineCreateSemaphoreMap.put(
              engineCreateKey,
              new Semaphore(
                AMConfiguration.AM_ENGINE_ASK_MAX_NUMBER
                  .getOrDefault(engineType.toLowerCase(Locale.getDefault), 10)
              )
            )
          }
        }
        engineCreateSemaphoreMap.get(engineCreateKey)
      } else {
        null
      }
    (engineCreateKey, semaphore)
  }

}
