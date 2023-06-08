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

package org.apache.linkis.manager.am.hook

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import org.apache.linkis.manager.am.service.engine.EngineOperateService
import org.apache.linkis.manager.am.utils.AMUtils
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.OperateRequest
import org.apache.linkis.manager.common.protocol.engine.{EngineOperateRequest, EngineSuicideRequest}
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter

class DefaultDetachEngineHeartbeatHook(
    engineOperateService: EngineOperateService,
    engineNode: EngineNode,
    user: String = getClass.getSimpleName
) extends DetachEngineHeartbeatHook
    with Logging {

  override def getName(): String = getClass.getSimpleName

  override def onHeartbeatAndRemoveHook(heartbeatMsg: NodeHeartbeatMsg): Unit = {
    val serviceInstance = engineNode.getServiceInstance
    if (!serviceInstance.equals(heartbeatMsg.getServiceInstance)) {
      return
    }
    val metricsJsonStr = heartbeatMsg.getHeartBeatMsg
    var hasAppid = false
    if (StringUtils.isNotBlank(metricsJsonStr)) {
      Utils.tryAndWarn {
        val metricsMap =
          AMUtils.GSON.fromJson(metricsJsonStr, classOf[util.HashMap[String, Object]])
        if (metricsMap.containsKey(ECConstants.YARN_APPID_NAME_KEY)) {
          val yarnAppid = metricsMap.getOrDefault(ECConstants.YARN_APPID_NAME_KEY, "").toString
          val yarnAppUrl = metricsMap.getOrDefault(ECConstants.YARN_APP_URL_KEY, "").toString
          if (StringUtils.isNotBlank(yarnAppid) && StringUtils.isNotBlank(yarnAppUrl)) {
            logger.info(
              s"Detach ec ${serviceInstance.toString} got appId ${yarnAppid}, appUrl : ${yarnAppUrl}. Will ask ec to exit."
            )
            handShake(yarnAppid)
            hasAppid = true
          } else {
            logger.info(
              s"Detach ec ${serviceInstance.toString} got no valid appId, will wait for next heartbeat."
            )
          }
        }
      }
      if (hasAppid || NodeStatus.isCompleted(heartbeatMsg.getStatus)) {
        // clean this hook
        if (ECHeartbeatHookHolder.hasHook(serviceInstance)) {
          DefaultDetachEngineHeartbeatHook.LOCK.synchronized {
            if (ECHeartbeatHookHolder.hasHook(serviceInstance)) {
              val hooks = ECHeartbeatHookHolder.getHooks(serviceInstance)
              if (null == hooks) {
                logger.error(
                  s"Hook : ${getName()} of serviceInstance: ${serviceInstance.toString} has been removed."
                )
              } else {
                val leftHooks = hooks.asScala.filter(_.getName() != getName()).toList
                if (leftHooks.isEmpty) {
                  ECHeartbeatHookHolder.removeEcHooks(serviceInstance)
                } else {
                  val list = new util.ArrayList[DetachEngineHeartbeatHook]()
                  leftHooks.foreach(list.add)
                  ECHeartbeatHookHolder.putEcHook(serviceInstance, list)
                }
                logger.info(
                  s"Succeed to remove hook : ${getName()} for detach ec ${serviceInstance.toString}"
                )
              }
            }
          }
        }

      }
    }

  }

  private def handShake(appId: String): Unit = {
    val params = new util.HashMap[String, AnyRef]()
    // check
    params.put(OperateRequest.OPERATOR_NAME_KEY, ECConstants.EC_OPERATE_LIST)
    params.put(ECConstants.YARN_APPID_NAME_KEY, appId)
    val engineOperateReq = new EngineOperateRequest(user, params)
    val resp = engineOperateService.executeOperation(engineNode, engineOperateReq)
    if (resp.isError) {
      logger.error(
        s"Handshake failed with ec : ${engineNode.getServiceInstance.toString}, ${resp.errorMsg}"
      )
      throw new AMErrorException(
        AMErrorCode.EC_OPERATE_ERROR.getErrorCode,
        AMErrorCode.EC_OPERATE_ERROR.getErrorDesc + s" ${resp.errorMsg}"
      )
    }
  }

}

object DefaultDetachEngineHeartbeatHook {

  def newInstance(
      engineOperateService: EngineOperateService,
      engineNode: EngineNode
  ): DefaultDetachEngineHeartbeatHook =
    new DefaultDetachEngineHeartbeatHook(engineOperateService, engineNode)

  val LOCK = new Object()
}
