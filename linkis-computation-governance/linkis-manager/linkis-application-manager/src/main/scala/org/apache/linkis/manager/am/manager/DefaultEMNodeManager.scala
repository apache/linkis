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
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.node._
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeEntity
import org.apache.linkis.manager.common.protocol.em.{ECMOperateRequest, ECMOperateResponse}
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.exception.NodeInstanceDuplicateException
import org.apache.linkis.manager.persistence.{NodeManagerPersistence, NodeMetricManagerPersistence}
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util

import scala.collection.JavaConverters._

@Component
class DefaultEMNodeManager extends EMNodeManager with Logging {

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

  override def emRegister(emNode: EMNode): Unit = {
    nodeManagerPersistence.addNodeInstance(emNode)
    // init metric
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
      metricsConverter.getInitMetric(emNode.getServiceInstance)
    )
  }

  override def addEMNodeInstance(emNode: EMNode): Unit = {
    Utils.tryCatch(nodeManagerPersistence.addNodeInstance(emNode)) {
      case e: NodeInstanceDuplicateException =>
        logger.warn(s"em instance had exists, $emNode.")
        nodeManagerPersistence.updateEngineNode(emNode.getServiceInstance, emNode)
      case t: Throwable => throw t
    }
  }

  override def initEMNodeMetrics(emNode: EMNode): Unit = {
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
      metricsConverter.getInitMetric(emNode.getServiceInstance)
    )
  }

  override def listEngines(emNode: EMNode): util.List[EngineNode] = {
    val nodes = nodeManagerPersistence.getEngineNodeByEM(emNode.getServiceInstance)
    val metricses = nodeMetricManagerPersistence
      .getNodeMetrics(nodes)
      .asScala
      .map(m => (m.getServiceInstance.toString, m))
      .toMap
    nodes.asScala.map { node =>
      metricses
        .get(node.getServiceInstance.toString)
        .foreach(metricsConverter.fillMetricsToNode(node, _))
      node
    }
    nodes
  }

  override def listUserEngines(emNode: EMNode, user: String): util.List[EngineNode] = {
    listEngines(emNode).asScala.filter(_.getOwner.equals(user))
  }.asJava

  def listUserNodes(user: String): java.util.List[Node] = {
    nodeManagerPersistence.getNodes(user)
  }

  /**
   * Get detailed em information from the persistence TODO add label to node ?
   *
   * @param scoreServiceInstances
   * @return
   */
  override def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode] = {

    if (null == scoreServiceInstances || scoreServiceInstances.isEmpty) {
      return null
    }
    val emNodes = scoreServiceInstances.map { scoreServiceInstances =>
      val emNode = new AMEMNode()
      emNode.setScore(scoreServiceInstances.getScore)
      emNode.setServiceInstance(scoreServiceInstances.getServiceInstance)
      emNode
    }
    // 1. add nodeMetrics  2 add RM info
    val resourceInfo =
      resourceManager.getResourceInfo(scoreServiceInstances.map(_.getServiceInstance))
    val nodeMetrics = nodeMetricManagerPersistence.getNodeMetrics(emNodes.toList.asJava)
    emNodes.map { emNode =>
      val optionMetrics =
        nodeMetrics.asScala.find(_.getServiceInstance.equals(emNode.getServiceInstance))
      val optionRMNode =
        resourceInfo.resourceInfo.asScala.find(
          _.getServiceInstance.equals(emNode.getServiceInstance)
        )
      optionMetrics.foreach(metricsConverter.fillMetricsToNode(emNode, _))
      optionRMNode.foreach(rmNode => emNode.setNodeResource(rmNode.getNodeResource))
      emNode
    }
    emNodes.toArray
  }

  override def getEM(serviceInstance: ServiceInstance): EMNode = {
    val node = nodeManagerPersistence.getNode(serviceInstance)
    if (null == node) {
      logger.info(s"This em of $serviceInstance not exists in db")
      return null
    }
    val emNode = new AMEMNode()
    emNode.setOwner(node.getOwner)
    emNode.setServiceInstance(node.getServiceInstance)
    node match {
      case a: PersistenceNodeEntity => emNode.setStartTime(a.getStartTime)
      case _ =>
    }
    emNode.setMark(emNode.getMark)
    metricsConverter.fillMetricsToNode(emNode, nodeMetricManagerPersistence.getNodeMetrics(emNode))
    emNode
  }

  override def stopEM(emNode: EMNode): Unit = {
    nodePointerBuilder.buildEMNodePointer(emNode).stopNode()
  }

  override def deleteEM(emNode: EMNode): Unit = {
    nodeManagerPersistence.removeNodeInstance(emNode)
    logger.info(s"Finished to clear emNode instance(${emNode.getServiceInstance}) info ")
    nodeMetricManagerPersistence.deleteNodeMetrics(emNode)
    logger.info(s"Finished to clear emNode(${emNode.getServiceInstance}) metrics info")
  }

  override def pauseEM(serviceInstance: ServiceInstance): Unit = {}

  /**
   *   1. request engineManager to launch engine
   *
   * @param engineBuildRequest
   * @param emNode
   * @return
   */
  override def createEngine(
      engineConnLaunchRequest: EngineConnLaunchRequest,
      emNode: EMNode
  ): EngineNode = {
    nodePointerBuilder.buildEMNodePointer(emNode).createEngine(engineConnLaunchRequest)
  }

  override def stopEngine(engineStopRequest: EngineStopRequest, emNode: EMNode): Unit = {
    nodePointerBuilder.buildEMNodePointer(emNode).stopEngine(engineStopRequest)
  }

  override def executeOperation(ecmNode: EMNode, request: ECMOperateRequest): ECMOperateResponse = {
    nodePointerBuilder.buildEMNodePointer(ecmNode).executeOperation(request)
  }

}
