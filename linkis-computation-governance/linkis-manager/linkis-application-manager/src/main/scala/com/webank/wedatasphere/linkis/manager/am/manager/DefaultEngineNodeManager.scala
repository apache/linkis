/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.manager

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.locker.EngineNodeLocker
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEngineNode, EngineNode, ScoreServiceInstance}
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.manager.persistence.{LabelManagerPersistence, NodeManagerPersistence, NodeMetricManagerPersistence}
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.manager.service.common.pointer.NodePointerBuilder
import com.webank.wedatasphere.linkis.resourcemanager.service.ResourceManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._


@Service
class DefaultEngineNodeManager extends EngineNodeManager with Logging {

  @Autowired
  private var engineLocker: EngineNodeLocker = _

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  @Autowired
  private var nodePointerBuilder: NodePointerBuilder = _


  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var labelManagerPersistence: LabelManagerPersistence = _

  private val  labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def listEngines(user: String): util.List[EngineNode] = {
    // TODO: user 应该是除了root，hadoop
    val nodes = nodeManagerPersistence.getNodes(user).map(_.getServiceInstance).map(nodeManagerPersistence.getEngineNode)
    val metricses = nodeMetricManagerPersistence.getNodeMetrics(nodes).map(m => (m.getServiceInstance.toString, m)).toMap
    nodes.map { node =>
      metricses.get(node.getServiceInstance.toString).foreach(metricsConverter.fillMetricsToNode(node, _))
      node
    }
  }

  override def getEngineNodeInfo(engineNode: EngineNode): EngineNode = {
    /**
      * 修改为实时请求对应的EngineNode
      */
    val engine = nodePointerBuilder.buildEngineNodePointer(engineNode)
    val heartMsg = engine.getNodeHeartbeatMsg()
    engineNode.setNodeHealthyInfo(heartMsg.getHealthyInfo)
    engineNode.setNodeOverLoadInfo(heartMsg.getOverLoadInfo)
    engineNode.setNodeResource(heartMsg.getNodeResource)
    engineNode.setNodeStatus(heartMsg.getStatus)
    engineNode
  }

  override def getEngineNodeInfoByDB(engineNode: EngineNode): EngineNode = {
    //1. 从持久化器中获取EngineNode信息，需要获取Task信息和Status信息，方便后面使用
    metricsConverter.fillMetricsToNode(engineNode, nodeMetricManagerPersistence.getNodeMetrics(engineNode))
    engineNode
  }

  override def updateEngineStatus(serviceInstance: ServiceInstance, fromState: NodeStatus, toState: NodeStatus): Unit = {

  }

  override def updateEngine(engineNode: EngineNode): Unit = {

  }

  override def switchEngine(engineNode: EngineNode): EngineNode = {
    null
  }

  override def reuseEngine(engineNode: EngineNode): EngineNode = {
    useEngine(engineNode)
  }

  /**
    * TODO use Engine需要考虑流式引擎的场景，后续需要通过Label加额外的处理
    *
    * @param engineNode
    * @param timeout
    * @return
    */
  override def useEngine(engineNode: EngineNode, timeout: Long): EngineNode = {
    val node = getEngineNodeInfo(engineNode)
    if (!NodeStatus.isAvailable(node.getNodeStatus)) {
      return null
    }
    if (!NodeStatus.isLocked(node.getNodeStatus)) {
      val lockStr = engineLocker.lockEngine(node, timeout)
      if (lockStr.isEmpty) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Failed to request lock from engine ${node.getServiceInstance}")
      }
      node.setLock(lockStr.get)
      node
    } else {
      null
    }
  }


  override def useEngine(engineNode: EngineNode): EngineNode = {
    useEngine(engineNode, AMConfiguration.ENGINE_LOCKER_MAX_TIME.getValue)
  }


  /**
    * Get detailed engine information from the persistence
    * //TODO 是否增加owner到node
    *
    * @param scoreServiceInstances
    * @return
    */
  override def getEngineNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EngineNode] = {
    if (null == scoreServiceInstances || scoreServiceInstances.isEmpty) {
      return null
    }
    val engineNodes = scoreServiceInstances.map {
      scoreServiceInstances =>
        val engineNode = new AMEngineNode()
        engineNode.setScore(scoreServiceInstances.getScore)
        engineNode.setServiceInstance(scoreServiceInstances.getServiceInstance)
        engineNode
    }
    //1. 增加nodeMetrics 2 增加RM信息
    val resourceInfo = resourceManager.getResourceInfo(scoreServiceInstances.map(_.getServiceInstance))
    val nodeMetrics = nodeMetricManagerPersistence.getNodeMetrics(engineNodes.toList)
    engineNodes.map { engineNode =>
      val optionMetrics = nodeMetrics.find(_.getServiceInstance.equals(engineNode.getServiceInstance))

      val optionRMNode = resourceInfo.resourceInfo.find(_.getServiceInstance.equals(engineNode.getServiceInstance))

      optionMetrics.foreach(metricsConverter.fillMetricsToNode(engineNode, _))
      optionRMNode.foreach(rmNode => engineNode.setNodeResource(rmNode.getNodeResource))

      engineNode
    }
  }


  /*private def fillMetricsToNode(engineNode: EngineNode, metrics: NodeMetrics): EngineNode = {

    engineNode.setNodeStatus(metricsConverter.parseStatus(metrics))
    engineNode.setNodeTaskInfo(metricsConverter.parseTaskInfo(metrics))
    engineNode.setNodeHealthyInfo(metricsConverter.parseHealthyInfo(metrics))
    engineNode.setNodeOverLoadInfo(metricsConverter.parseOverLoadInfo(metrics))
    engineNode
  }*/

  /* /**
     * clear engine info from persistence
     * invoke deleteNode
     *
     * @param stopEngineRequest
     */
   override def stopEngine(stopEngineRequest: EngineStopRequest): Unit = {
     val instance = stopEngineRequest.getServiceInstance
     val node = new AMEngineNode()
     node.setServiceInstance(instance)
     nodePointerBuilder.buildEngineNodePointer(node).stopNode()
     deleteEngineNode(node)
   }*/

  /**
    * add info to persistence
    *
    * @param engineNode
    */
  override def addEngineNode(engineNode: EngineNode): Unit = {
    nodeManagerPersistence.addEngineNode(engineNode)
    // init metric
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(metricsConverter.getInitMetric(engineNode.getServiceInstance))
  }


  /**
    * delete info to persistence
    *
    * @param engineNode
    */
  override def deleteEngineNode(engineNode: EngineNode): Unit = {
    nodeManagerPersistence.deleteEngineNode(engineNode)

  }

  override def getEngineNode(serviceInstance: ServiceInstance): EngineNode = {
    nodeManagerPersistence.getEngineNode(serviceInstance)
  }

  override def updateEngineNode(serviceInstance: ServiceInstance, engineNode: EngineNode): Unit = {
    nodeManagerPersistence.updateEngineNode(serviceInstance, engineNode)
    //1.serviceInstance中取出instance（实际是ticketId）
    //2.update serviceInstance 表，包括 instance替换，替换mark，owner，updator，creator的空值，更新updateTime
    //3.update engine_em关联表
    //4.update label ticket_id ==> instance
    val engineLabel = labelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
    engineLabel.setInstance(engineNode.getServiceInstance.getInstance)
    engineLabel.setServiceName(engineNode.getServiceInstance.getApplicationName)
    val oldEngineLabel = labelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
    oldEngineLabel.setInstance(serviceInstance.getInstance)
    oldEngineLabel.setServiceName(engineNode.getServiceInstance.getApplicationName)
    val oldPersistenceLabel = labelBuilderFactory.convertLabel(oldEngineLabel, classOf[PersistenceLabel])
    val label = labelManagerPersistence.getLabelByKeyValue(oldPersistenceLabel.getLabelKey, oldPersistenceLabel.getStringValue)
    val persistenceLabel = labelBuilderFactory.convertLabel(engineLabel, classOf[PersistenceLabel])
    persistenceLabel.setLabelValueSize(persistenceLabel.getValue.size())
    labelManagerPersistence.updateLabel(label.getId, persistenceLabel)
  }


}
