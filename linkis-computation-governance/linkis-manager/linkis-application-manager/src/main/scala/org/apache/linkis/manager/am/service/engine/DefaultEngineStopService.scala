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

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{AMEMNode, EngineNode}
import org.apache.linkis.manager.common.entity.resource.{
  DriverAndYarnResource,
  LoadInstanceResource
}
import org.apache.linkis.manager.common.exception.RMErrorException
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnReleaseRequest,
  EngineStopRequest,
  EngineSuicideRequest
}
import org.apache.linkis.manager.dao.NodeMetricManagerMapper
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.label.service.impl.DefaultNodeLabelRemoveService
import org.apache.linkis.manager.rm.exception.RMErrorCode
import org.apache.linkis.manager.rm.service.impl.DefaultResourceManager
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutorService, Future}
import scala.util.control.Breaks._

import com.fasterxml.jackson.core.JsonProcessingException

@Service
class DefaultEngineStopService extends AbstractEngineService with EngineStopService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var resourceManager: DefaultResourceManager = _

  @Autowired
  private var nodeLabelRemoveService: DefaultNodeLabelRemoveService = _

  @Autowired
  private var engineInfoService: EngineInfoService = _

  @Autowired
  private var engineStopService: EngineStopService = _

  @Autowired
  private var nodeMetricManagerMapper: NodeMetricManagerMapper = _

  private implicit val executor: ExecutionContextExecutorService =
    Utils.newCachedExecutionContext(
      AMConfiguration.ASYNC_STOP_ENGINE_MAX_THREAD_SIZE,
      "AsyncStopEngineService-Thread-"
    )

  @Receiver
  override def stopEngine(engineStopRequest: EngineStopRequest, sender: Sender): Unit = {
    engineStopRequest.getServiceInstance.setApplicationName(
      GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue
    )
    logger.info(
      s" user ${engineStopRequest.getUser} prepare to stop engine ${engineStopRequest.getServiceInstance}"
    )
    val node = getEngineNodeManager.getEngineNode(engineStopRequest.getServiceInstance)
    if (null == node) {
      logger.info(s" engineConn does not exist in db: $engineStopRequest ")
      return
    }
    // 1. request em to kill ec
    logger.info(s"Start to kill engine invoke enginePointer ${node.getServiceInstance}")
    Utils.tryAndErrorMsg {
      getEMService().stopEngine(node, node.getEMNode)
      logger.info(s"Finished to kill engine invoke enginePointer ${node.getServiceInstance}")
    }(s"Failed to stop engine ${node.getServiceInstance}")
    node.setLabels(nodeLabelService.getNodeLabels(engineStopRequest.getServiceInstance))
    if (null == node.getNodeStatus) {
      node.setNodeStatus(NodeStatus.ShuttingDown)
    }
    engineConnInfoClear(node)
    logger.info(
      s" user ${engineStopRequest.getUser} finished to stop engine ${engineStopRequest.getServiceInstance}"
    )
  }

  override def stopUnlockEngineByECM(
      ecmInstance: String,
      operatorName: String
  ): java.util.Map[String, Any] = {

    val resultMap = new util.HashMap[String, Any]
    var killEngineNum = 0;

    // get all unlock ec node of the specified ecm
    val ecmInstanceService = new ServiceInstance
    ecmInstanceService.setInstance(ecmInstance)
    ecmInstanceService.setApplicationName(
      GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue
    )

    val emNode = new AMEMNode
    emNode.setServiceInstance(ecmInstanceService)

    val engineNodes = engineInfoService.listEMEngines(emNode).asScala

    val unlockEngineNodes = engineNodes
      .filter(node => NodeStatus.Unlock.equals(node.getNodeStatus))
      .filter(node => !node.getLabels.isEmpty)

    logger.info(
      s"get ec node total num:${engineNodes.size} and unlock node num:${unlockEngineNodes.size} of ecm:${ecmInstance} "
    )

    var loadInstanceResourceTotal = new LoadInstanceResource(0, 0, 0)

    unlockEngineNodes.foreach { node =>
      if (logger.isDebugEnabled) {
        try logger.debug("ec node:" + JsonUtils.jackson.writeValueAsString(node))
        catch {
          case e: JsonProcessingException =>
            logger.debug("convert jobReq to string with error:" + e.getMessage)
        }
      }

      val engineTypeLabel =
        node.getLabels.asScala.find(_.isInstanceOf[EngineTypeLabel]).getOrElse(null)
      val engineTypeStr = engineTypeLabel.asInstanceOf[EngineTypeLabel] getEngineType
      val isAllowKill = AMConfiguration.isAllowKilledEngineType(engineTypeStr)

      if (isAllowKill == false) {
        logger.info(
          s"skipped to kill engine node:${node.getServiceInstance.getInstance},engine type:${engineTypeStr}"
        )
      } else {
        // calculate the resources that can be released
        if (node.getNodeResource.getUsedResource != null) {
          val realResource = node.getNodeResource.getUsedResource match {
            case dy: DriverAndYarnResource => dy.loadInstanceResource
            case _ => node.getNodeResource.getUsedResource
          }
          loadInstanceResourceTotal =
            loadInstanceResourceTotal.add(node.getNodeResource.getUsedResource)
        }

        logger.info(s"try to asyn kill engine node:${node.getServiceInstance.getInstance}")
        killEngineNum = killEngineNum + 1
        // asyn to stop
        val stopEngineRequest = new EngineStopRequest(node.getServiceInstance, operatorName)
        asyncStopEngineWithUpdateMetrics(stopEngineRequest)
      }
    }
    resultMap.put("killEngineNum", killEngineNum)
    resultMap.put("memory", loadInstanceResourceTotal.memory)
    resultMap.put("cores", loadInstanceResourceTotal.cores)
    resultMap.put("batchKillEngineType", AMConfiguration.ALLOW_BATCH_KILL_ENGINE_TYPES.getValue)
    resultMap
  }

  /**
   *   1. to clear rm info 2. to clear label info 3. to clear am info
   *
   * @param ecNode
   */
  override def engineConnInfoClear(ecNode: EngineNode): Unit = {
    logger.info(s"Start to clear ec info $ecNode")
    // 1. to clear engine resource
    Utils.tryCatch {
      resourceManager.resourceReleased(ecNode)
    } {
      case exception: RMErrorException =>
        if (exception.getErrCode != RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode) {
          throw exception
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
  override def engineSuicide(engineSuicideRequest: EngineSuicideRequest, sender: Sender): Unit = {
    logger.info(
      s"Will ask engine : ${engineSuicideRequest.getServiceInstance.toString} of user : ${engineSuicideRequest.getUser} to suicide."
    )
    EngineStopService.askEngineToSuicide(engineSuicideRequest)
  }

  @Receiver
  override def dealEngineRelease(
      engineConnReleaseRequest: EngineConnReleaseRequest,
      sender: Sender
  ): Unit = {
    logger.info(
      s"Start to kill engine , with msg : ${engineConnReleaseRequest.getMsg}, ${engineConnReleaseRequest.getServiceInstance.toString}"
    )
    if (null == engineConnReleaseRequest.getServiceInstance) {
      logger.warn(s"Invalid empty serviceInstance, will not kill engine.")
      return
    }
    val engineNode =
      getEngineNodeManager.getEngineNode(engineConnReleaseRequest.getServiceInstance)
    if (null != engineNode) {
      logger.info(
        s"Send stop  engine request ${engineConnReleaseRequest.getServiceInstance.toString}"
      )
      engineNode.setLabels(nodeLabelService.getNodeLabels(engineNode.getServiceInstance))
      engineNode.setNodeStatus(engineConnReleaseRequest.getNodeStatus)
      engineConnInfoClear(engineNode)
    } else {
      logger.warn(
        s"Cannot find valid engineNode from serviceInstance : ${engineConnReleaseRequest.getServiceInstance.toString}"
      )
    }
  }

  override def asyncStopEngine(engineStopRequest: EngineStopRequest): Unit = {
    Future {
      logger.info(s"Start to async stop engineFailed $engineStopRequest")
      Utils.tryAndErrorMsg(
        stopEngine(engineStopRequest, Sender.getSender(Sender.getThisServiceInstance))
      )(s"async stop engineFailed $engineStopRequest")
    }
  }

  override def asyncStopEngineWithUpdateMetrics(engineStopRequest: EngineStopRequest): Unit = {

    Future {
      Utils.tryCatch {
        logger.info(s"Start to async stop engine node:$engineStopRequest")
        //  1. set ec node Metrics status Unlock to ShuttingDown
        //  2. ec node metircs report ignore update Shutingdown node
        val instance = engineStopRequest.getServiceInstance.getInstance
        logger.info(s"Try to update ec node:$engineStopRequest status Unlock --> ShuttingDown")
        val ok = nodeMetricManagerMapper.updateNodeStatus(
          instance,
          NodeStatus.ShuttingDown.ordinal(),
          NodeStatus.Unlock.ordinal()
        )
        if (ok > 0) {
          logger.info(s"Try to do stop ec node $engineStopRequest action")
          stopEngine(engineStopRequest, Sender.getSender(Sender.getThisServiceInstance))
        } else {
          logger.info(
            s"ec node:${instance} status update failed! maybe the status is not unlock. will skip to kill this ec node"
          )
        }
      } { case e: Exception =>
        logger.error(s"asyncStopEngineWithUpdateMetrics with error:, ${e.getMessage}", e)

      }

    }

  }

}
