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

package com.webank.wedatasphere.linkis.orchestrator.ecm

import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineAskAsyncResponse, EngineAskRequest, EngineCreateError, EngineCreateSuccess}
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.orchestrator.ecm.cache.EngineAsyncResponseCache
import com.webank.wedatasphere.linkis.orchestrator.ecm.conf.ECMPluginConf
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.{DefaultMark, Mark, MarkReq, Policy}
import com.webank.wedatasphere.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineConnExecutor
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.impl.{ComputationConcurrentEngineConnExecutor, ComputationEngineConnExecutor}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.rpc.exception.DWCRPCRetryException

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration


class ComputationEngineConnManager extends AbstractEngineConnManager with Logging {

  private val idCreator = new AtomicInteger()

  private val cacheMap = EngineAsyncResponseCache.getCache

  override def getPolicy(): Policy = Policy.Process

  /**
    * 申请获取一个Mark
    * 1. 如果没有对应的Mark就生成新的
    * 2. 生成新的Mark会存在请求引擎的过程，如果请求到了则存入Map中：Mark为Key，EngineConnExecutor为Value
    * 3. 将Mark进行返回
    *
    * @param markReq
    * @return
    */
  override def applyMark(markReq: MarkReq): Mark = {
    if (null == markReq) return null
    val markCache = getMarkCache().keys
    val maybeMark = markCache.find(_.getMarkReq.equals(markReq))
    maybeMark.getOrElse {
      val mark = new DefaultMark(nextMarkId(), markReq)
      addMark(mark, new util.ArrayList[ServiceInstance]())
      mark
    }
  }


  private def nextMarkId(): String = {
    "mark_" + idCreator.getAndIncrement()
  }

  override protected def askEngineConnExecutor(engineAskRequest: EngineAskRequest): EngineConnExecutor = {
    engineAskRequest.setTimeOut(getEngineConnApplyTime)
    var count = getEngineConnApplyAttempts()
    while (count >= 1) {
      count = count - 1
      val start = System.currentTimeMillis()
      try {
        val engineNode = getEngineNodeAskManager(engineAskRequest)
        if (null != engineNode) {
          val engineConnExecutor = if (null != engineAskRequest.getLabels &&
            engineAskRequest.getLabels.containsKey(LabelKeyConstant.CONCURRENT_ENGINE_KEY)) {
            new ComputationConcurrentEngineConnExecutor(engineNode, getParallelism())
          } else {
            new ComputationEngineConnExecutor(engineNode)
          }
          if (null != engineNode.getLabels) {
            engineConnExecutor.setLabels(engineNode.getLabels.toList.toArray)
          }
          return engineConnExecutor
        }
      } catch {
        case t: DWCRPCRetryException =>
          count = count - 1
          val taken = System.currentTimeMillis() - start
          error(s"Failed to askEngineAskRequest time taken ($taken), with DWCRPCRetryException", t)
        case t: Throwable =>
          count = count - 1
          val taken = System.currentTimeMillis() - start
          error(s"Failed to askEngineAskRequest time taken ($taken), ${t.getMessage}")
          throw t
      }
    }
    throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE,
      s"Failed to ask engineAskRequest $engineAskRequest by retry ${getEngineConnApplyAttempts - count}  ")
  }

  private def getEngineNodeAskManager(engineAskRequest: EngineAskRequest): EngineNode = {
    getManagerSender().ask(engineAskRequest) match {
      case engineNode: EngineNode =>
        info(s"Succeed to get engineNode $engineNode")
        engineNode
      case EngineAskAsyncResponse(id, serviceInstance) =>
        cacheMap.getAndRemove(id, Duration(getEngineConnApplyTime, TimeUnit.MILLISECONDS)) match {
          case EngineCreateSuccess(id, engineNode) =>
            info(s"id:$id success to async get EngineNode $engineNode")
            engineNode
          case EngineCreateError(id, exception) =>
            error(s"id:$id Failed  to async get EngineNode")
            throw new ECMPluginErrorException(ECMPluginConf.ECM_ENGNE_CREATION_ERROR_CODE, exception)
        }
      case _ =>
        info(s"Failed to ask engineAskRequest $engineAskRequest, response is not engineNode")
        null
    }
  }

  private def getManagerSender(): Sender = {
    Sender.getSender(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue)
  }
}
