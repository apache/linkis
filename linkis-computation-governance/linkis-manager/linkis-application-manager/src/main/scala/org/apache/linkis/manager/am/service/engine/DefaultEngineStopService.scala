/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.am.service.engine

import java.util.concurrent.TimeUnit
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineInfoClearRequest, EngineStopRequest, EngineSuicideRequest}
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.label.service.impl.DefaultNodeLabelRemoveService
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest
import org.apache.linkis.resourcemanager.exception.{RMErrorCode, RMErrorException}
import org.apache.linkis.resourcemanager.service.impl.DefaultResourceManager
import org.apache.linkis.rpc.Sender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.concurrent.{ExecutionContextExecutorService, Future}

@Service
class DefaultEngineStopService extends AbstractEngineService with EngineStopService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
   var resourceManager: DefaultResourceManager = _

  @Autowired
  private var nodeLabelRemoveService: DefaultNodeLabelRemoveService = _

  private implicit val executor: ExecutionContextExecutorService = Utils.newCachedExecutionContext(AMConfiguration.ASYNC_STOP_ENGINE_MAX_THREAD_SIZE, "AsyncStopEngineService-Thread-")


  @Receiver
  override def stopEngine(engineStopRequest: EngineStopRequest): Unit = {
    //TODO delete
    engineStopRequest.getServiceInstance.setApplicationName(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    logger.info(s" user ${engineStopRequest.getUser} prepare to stop engine ${engineStopRequest.getServiceInstance}")
    val node = getEngineNodeManager.getEngineNode(engineStopRequest.getServiceInstance)
    if (null == node) {
      logger.info(s" engineConn is not exists in db: $engineStopRequest ")
      return
    }
    // 1. request em to kill ec
    logger.info(s"Start to kill engine invoke enginePointer ${node.getServiceInstance}")
    Utils.tryAndErrorMsg{
      getEMService().stopEngine(node,
        node.getEMNode)
      logger.info(s"Finished to kill engine invoke enginePointer ${node.getServiceInstance}")
    }(s"Failed to stop engine ${node.getServiceInstance}")
    node.setLabels(nodeLabelService.getNodeLabels(engineStopRequest.getServiceInstance))
    engineConnInfoClear(node)
    logger.info(s" user ${engineStopRequest.getUser} finished to stop engine ${engineStopRequest.getServiceInstance}")
  }

  /**
   * 1. to clear rm info
   * 2. to clear label info
   * 3. to clear am info
   * @param ecNode
   */
  private def engineConnInfoClear(ecNode: EngineNode): Unit = {
    logger.info(s"Start to clear ec info $ecNode")
    // 1. to clear engine resource
    Utils.tryCatch{
      resourceManager.resourceReleased(ecNode.getLabels)
    }{
      case exception: RMErrorException => {
        if (exception.getErrCode != RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getCode) {
          throw exception
        }
      }
      case exception: Exception => throw exception
    }

    // 2. to  clear Label
    val instanceLabelRemoveRequest = new NodeLabelRemoveRequest(ecNode.getServiceInstance, true)
    nodeLabelRemoveService.removeNodeLabel(instanceLabelRemoveRequest)
    // 3. to clear engine node info
    getEngineNodeManager.deleteEngineNode(ecNode)
    logger.info(s"Finished to clear ec info $ecNode")
  }

  @Receiver
  override def engineSuicide(engineSuicideRequest: EngineSuicideRequest): Unit = {
    logger.info(s"Will ask engine : ${engineSuicideRequest.getServiceInstance.toString} of user : ${engineSuicideRequest.getUser} to suicide.")
    EngineStopService.askEngineToSuicide(engineSuicideRequest)
  }

  @Receiver
  override def dealEngineRelease(engineConnReleaseRequest: EngineConnReleaseRequest): Unit = {
    info(s"Start to kill engine , with msg : ${engineConnReleaseRequest.getMsg}, ${engineConnReleaseRequest.getServiceInstance.toString}")
    if (null == engineConnReleaseRequest.getServiceInstance) {
      warn(s"Invalid empty serviceInstance, will not kill engine.")
      return
    }
    val engineNode = getEngineNodeManager.getEngineNode(engineConnReleaseRequest.getServiceInstance)
    if (null != engineNode) {
      logger.info(s"Send stop  engine request ${engineConnReleaseRequest.getServiceInstance.toString}")
      engineNode.setLabels(nodeLabelService.getNodeLabels(engineNode.getServiceInstance))
      engineConnInfoClear(engineNode)
    } else {
      warn(s"Cannot find valid engineNode from serviceInstance : ${engineConnReleaseRequest.getServiceInstance.toString}")
    }
  }

  override def asyncStopEngine(engineStopRequest: EngineStopRequest): Unit = {
    Future {
      info(s"Start to async stop engineFailed $engineStopRequest")
      Utils.tryAndErrorMsg(stopEngine(engineStopRequest))(s"async stop engineFailed $engineStopRequest")
    }
  }
}
