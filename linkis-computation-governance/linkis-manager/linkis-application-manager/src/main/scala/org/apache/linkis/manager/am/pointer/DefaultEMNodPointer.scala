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

package org.apache.linkis.manager.am.pointer

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.manager.am.exception.AMErrorException
import org.apache.linkis.manager.am.utils.AMUtils
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.node.{EngineNode, Node}
import org.apache.linkis.manager.common.protocol.em.{ECMOperateRequest, ECMOperateResponse}
import org.apache.linkis.manager.common.protocol.engine.{EngineStopRequest, EngineStopResponse}
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.service.common.pointer.EMNodPointer

class DefaultEMNodPointer(val node: Node) extends AbstractNodePointer with EMNodPointer {

  /**
   * 与该远程指针关联的node信息
   *
   * @return
   */
  override def getNode(): Node = node

  override def createEngine(engineConnLaunchRequest: EngineConnLaunchRequest): EngineNode = {
    logger.info(s"Start to createEngine ask em ${getNode().getServiceInstance}")
    getSender.ask(engineConnLaunchRequest) match {
      case engineNode: EngineNode =>
        logger.info(
          s"Succeed to createEngine ask em ${getNode().getServiceInstance}, engineNode $engineNode "
        )
        engineNode
      case _ =>
        throw new AMErrorException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Failed to createEngine ask em ${getNode().getServiceInstance}"
        )
    }
  }

  override def stopEngine(engineStopRequest: EngineStopRequest): Unit = {
    Utils.tryAndWarn {
      getSender.ask(engineStopRequest) match {
        case engineStopResponse: EngineStopResponse =>
          if (!engineStopResponse.getStopStatus) {
            logger.info(
              s"Kill engine : ${engineStopRequest.getServiceInstance.toString} failed, because ${engineStopResponse.getMsg} . Will ask engine to suicide."
            )
          } else {
            logger.info(s"Succeed to kill engine ${engineStopRequest.getServiceInstance.toString}.")
          }
        case o: AnyRef =>
          logger.warn(
            s"Ask em : ${getNode().getServiceInstance.toString} to kill engine : ${engineStopRequest.getServiceInstance.toString} failed, response is : ${AMUtils.GSON
              .toJson(o)}. "
          )
      }
    }
  }

  override def executeOperation(request: ECMOperateRequest): ECMOperateResponse = {
    getSender.ask(request) match {
      case response: ECMOperateResponse => response
      case _ =>
        throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, "Failed to execute ECM operation.")
    }
  }

}
