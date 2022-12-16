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

package org.apache.linkis.manager.am.manager

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{Logging, RetryHandler, Utils}
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import org.apache.linkis.manager.am.locker.EngineNodeLocker
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{AMEngineNode, EngineNode, ScoreServiceInstance}
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel
import org.apache.linkis.manager.common.protocol.engine.{
  EngineOperateRequest,
  EngineOperateResponse
}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.persistence.{
  LabelManagerPersistence,
  NodeManagerPersistence,
  NodeMetricManagerPersistence
}
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.lang.reflect.UndeclaredThrowableException
import java.util

import scala.collection.JavaConverters._

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

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def listEngines(user: String): util.List[EngineNode] = {
    // TODO: user 应该是除了root，hadoop
    val nodes = nodeManagerPersistence
      .getNodes(user)
      .asScala
      .map(_.getServiceInstance)
      .map(nodeManagerPersistence.getEngineNode)
    val metricses = nodeMetricManagerPersistence
      .getNodeMetrics(nodes.asJava)
      .asScala
      .map(m => (m.getServiceInstance.toString, m))
      .toMap
    nodes.map { node =>
      metricses
        .get(node.getServiceInstance.toString)
        .foreach(metricsConverter.fillMetricsToNode(node, _))
      node
    }
  }.asJava

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
    // 1. 从持久化器中获取EngineNode信息，需要获取Task信息和Status信息，方便后面使用
    metricsConverter.fillMetricsToNode(
      engineNode,
      nodeMetricManagerPersistence.getNodeMetrics(engineNode)
    )
    engineNode
  }

  override def updateEngineStatus(
      serviceInstance: ServiceInstance,
      fromState: NodeStatus,
      toState: NodeStatus
  ): Unit = {}

  override def updateEngine(engineNode: EngineNode): Unit = {}

  override def switchEngine(engineNode: EngineNode): EngineNode = {
    null
  }

  override def reuseEngine(engineNode: EngineNode): EngineNode = {
    val node = getEngineNodeInfo(engineNode)
    if (!NodeStatus.isAvailable(node.getNodeStatus)) {
      return null
    }
    if (!NodeStatus.isLocked(node.getNodeStatus)) {
      val lockStr = engineLocker.lockEngine(node, AMConfiguration.ENGINE_LOCKER_MAX_TIME.getValue)
      if (lockStr.isEmpty) {
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Failed to request lock from engine by reuse ${node.getServiceInstance}"
        )
      }
      node.setLock(lockStr.get)
      node
    } else {
      null
    }
  }

  /**
   * TODO use Engine需要考虑流式引擎的场景，后续需要通过Label加额外的处理
   *
   * @param engineNode
   * @param timeout
   * @return
   */
  override def useEngine(engineNode: EngineNode, timeout: Long): EngineNode = {
    val retryHandler = new RetryHandler {}
    retryHandler.addRetryException(classOf[feign.RetryableException])
    retryHandler.addRetryException(classOf[UndeclaredThrowableException])
    val node = retryHandler.retry[EngineNode](getEngineNodeInfo(engineNode), "getEngineNodeInfo")
    // val node = getEngineNodeInfo(engineNode)
    if (!NodeStatus.isAvailable(node.getNodeStatus)) {
      return null
    }
    if (!NodeStatus.isLocked(node.getNodeStatus)) {
      val lockStr = engineLocker.lockEngine(node, timeout)
      if (lockStr.isEmpty) {
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Failed to request lock from engine ${node.getServiceInstance}"
        )
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
   * Get detailed engine information from the persistence //TODO 是否增加owner到node
   *
   * @param scoreServiceInstances
   * @return
   */
  override def getEngineNodes(
      scoreServiceInstances: Array[ScoreServiceInstance]
  ): Array[EngineNode] = {
    if (null == scoreServiceInstances || scoreServiceInstances.isEmpty) {
      return null
    }
    val engineNodes = scoreServiceInstances.map { scoreServiceInstances =>
      val engineNode = new AMEngineNode()
      engineNode.setScore(scoreServiceInstances.getScore)
      engineNode.setServiceInstance(scoreServiceInstances.getServiceInstance)
      engineNode
    }
    // 1. add nodeMetrics 2 add RM info
    val resourceInfo =
      resourceManager.getResourceInfo(scoreServiceInstances.map(_.getServiceInstance))
    val nodeMetrics = nodeMetricManagerPersistence.getNodeMetrics(engineNodes.toList.asJava)
    engineNodes.map { engineNode =>
      val optionMetrics =
        nodeMetrics.asScala.find(_.getServiceInstance.equals(engineNode.getServiceInstance))

      val optionRMNode =
        resourceInfo.resourceInfo.asScala.find(
          _.getServiceInstance.equals(engineNode.getServiceInstance)
        )

      optionMetrics.foreach(metricsConverter.fillMetricsToNode(engineNode, _))
      optionRMNode.foreach(rmNode => engineNode.setNodeResource(rmNode.getNodeResource))

      engineNode
    }
  }

  /**
   * add info to persistence
   *
   * @param engineNode
   */
  override def addEngineNode(engineNode: EngineNode): Unit = {
    nodeManagerPersistence.addEngineNode(engineNode)
    // init metric
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
      metricsConverter.getInitMetric(engineNode.getServiceInstance)
    )
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

  /**
   * 1.serviceInstance中取出instance（实际是ticketId） 2.update serviceInstance 表，包括
   * instance替换，替换mark，owner，updator，creator的空值，更新updateTime 3.update engine_em关联表 4.update label
   * ticket_id ==> instance
   *
   * @param serviceInstance
   * @param engineNode
   */
  override def updateEngineNode(serviceInstance: ServiceInstance, engineNode: EngineNode): Unit = {
    nodeManagerPersistence.updateEngineNode(serviceInstance, engineNode)
    Utils.tryAndWarnMsg(nodeMetricManagerPersistence.deleteNodeMetrics(engineNode))(
      "Failed to clear old metrics"
    )
    val engineLabel = labelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
    engineLabel.setInstance(engineNode.getServiceInstance.getInstance)
    engineLabel.setServiceName(engineNode.getServiceInstance.getApplicationName)
    val oldEngineLabel = labelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
    oldEngineLabel.setInstance(serviceInstance.getInstance)
    oldEngineLabel.setServiceName(engineNode.getServiceInstance.getApplicationName)
    val oldPersistenceLabel =
      labelBuilderFactory.convertLabel(oldEngineLabel, classOf[PersistenceLabel])
    val label = labelManagerPersistence.getLabelByKeyValue(
      oldPersistenceLabel.getLabelKey,
      oldPersistenceLabel.getStringValue
    )
    val persistenceLabel =
      labelBuilderFactory.convertLabel(engineLabel, classOf[PersistenceLabel])
    persistenceLabel.setLabelValueSize(persistenceLabel.getValue.size())
    labelManagerPersistence.updateLabel(label.getId, persistenceLabel)
  }

  override def executeOperation(
      engineNode: EngineNode,
      request: EngineOperateRequest
  ): EngineOperateResponse = {
    val engine = nodePointerBuilder.buildEngineNodePointer(engineNode)
    engine.executeOperation(request)
  }

  override def getEngineNodeInfo(serviceInstance: ServiceInstance): EngineNode = {
    val engineNode = getEngineNode(serviceInstance)
    if (engineNode != null) {
      if (engineNode.getNodeStatus == null) {
        val nodeMetric = nodeMetricManagerPersistence.getNodeMetrics(engineNode)
        engineNode.setNodeStatus(
          if (Option(nodeMetric).isDefined) NodeStatus.values()(nodeMetric.getStatus)
          else NodeStatus.Starting
        )
      }
      return engineNode
    }
    throw new AMErrorException(
      AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorCode,
      AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorDesc
    )
  }

}
