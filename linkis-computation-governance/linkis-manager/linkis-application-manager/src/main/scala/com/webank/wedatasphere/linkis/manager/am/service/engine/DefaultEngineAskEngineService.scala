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
 */

package com.webank.wedatasphere.linkis.manager.am.service.engine

import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.common.protocol.engine._
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.rpc.Sender
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}

/**
  * @date 2020/6/14 18:21
  */
@Service
class DefaultEngineAskEngineService extends AbstractEngineService with EngineAskEngineService with Logging {

  @Autowired
  private var engineCreateService: EngineCreateService = _

  @Autowired
  private var engineReuseService: EngineReuseService = _

  @Autowired
  private var engineSwitchService: EngineSwitchService = _

  private val idCreator = new AtomicInteger()

  private val idPrefix = Sender.getThisServiceInstance.getInstance

  @Receiver
  override def askEngine(engineAskRequest: EngineAskRequest, smc: ServiceMethodContext): Any = {

    val engineReuseRequest = new EngineReuseRequest()
    engineReuseRequest.setLabels(engineAskRequest.getLabels)
    engineReuseRequest.setTimeOut(engineAskRequest.getTimeOut)
    engineReuseRequest.setUser(engineAskRequest.getUser)

    val reuseNode = Utils.tryAndWarn(engineReuseService.reuseEngine(engineReuseRequest))
    if (null != reuseNode) {
      info(s"Finished to ask engine for user ${engineAskRequest.getUser} by reuse node $reuseNode")
      return reuseNode
    }

    val createNodeThread = Future {
      //如果原来的labels含engineInstance ，先去掉
      engineAskRequest.getLabels.remove("engineInstance")
      val engineCreateRequest = new EngineCreateRequest
      engineCreateRequest.setLabels(engineAskRequest.getLabels)
      engineCreateRequest.setTimeOut(engineAskRequest.getTimeOut)
      engineCreateRequest.setUser(engineAskRequest.getUser)
      engineCreateRequest.setProperties(engineAskRequest.getProperties)

      val createNode = engineCreateService.createEngine(engineCreateRequest, smc)
      val timeout = if (engineCreateRequest.getTimeOut <= 0) AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong else engineCreateRequest.getTimeOut
      //useEngine 需要加上超时
      val createEngineNode = getEngineNodeManager.useEngine(createNode, timeout)
      info(s"Finished to ask engine for user ${engineAskRequest.getUser} by create node $createEngineNode")
      createEngineNode
    }

    val engineAskAsyncId = getAsyncId
    createNodeThread.onComplete {
      case Success(engineNode) =>
        info(s"Success to async($engineAskAsyncId) createEngine $engineNode")
        smc.getSender.send(EngineCreateSuccess(engineAskAsyncId, engineNode))
      case Failure(exception) =>
        info(s"Failed  to async($engineAskAsyncId) createEngine ", exception)
        smc.getSender.send(EngineCreateError(engineAskAsyncId, ExceptionUtils.getRootCauseMessage(exception)))
    }

    EngineAskAsyncResponse(engineAskAsyncId, Sender.getThisServiceInstance)
  }

  private def getAsyncId: String = {
    idPrefix + "_" + idCreator.getAndIncrement()
  }


}
