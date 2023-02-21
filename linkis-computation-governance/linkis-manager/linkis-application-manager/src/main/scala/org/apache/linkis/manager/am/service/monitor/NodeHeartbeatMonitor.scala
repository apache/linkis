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

package org.apache.linkis.manager.am.service.monitor

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.conf.ManagerMonitorConf
import org.apache.linkis.manager.am.service.em.EMUnregisterService
import org.apache.linkis.manager.am.service.engine.EngineStopService
import org.apache.linkis.manager.am.service.heartbeat.AMHeartbeatService
import org.apache.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import org.apache.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeMetrics}
import org.apache.linkis.manager.common.entity.node.Node
import org.apache.linkis.manager.common.monitor.ManagerMonitor
import org.apache.linkis.manager.common.protocol.em.StopEMRequest
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.apache.linkis.manager.common.protocol.node.{NodeHeartbeatMsg, NodeHeartbeatRequest}
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.persistence.{NodeManagerPersistence, NodeMetricManagerPersistence}
import org.apache.linkis.manager.service.common.label.ManagerLabelService
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.exception.NoInstanceExistsException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.lang.reflect.UndeclaredThrowableException
import java.util
import java.util.concurrent.ExecutorService

import scala.collection.JavaConverters._
import scala.collection.mutable

@Component
class NodeHeartbeatMonitor extends ManagerMonitor with Logging {

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  @Autowired
  private var amHeartbeatService: AMHeartbeatService = _

  @Autowired
  private var engineStopService: EngineStopService = _

  @Autowired
  private var emUnregisterService: EMUnregisterService = _

  @Autowired
  private var managerLabelService: ManagerLabelService = _

  private val fixedThreadPoll: ExecutorService = Utils.newFixedThreadPool(
    ManagerMonitorConf.MANAGER_MONITOR_ASYNC_POLL_SIZE.getValue,
    "manager_async",
    false
  )

  private val ecName = GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue
  private val ecmName = GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue
  private val maxCreateInterval = ManagerMonitorConf.NODE_MAX_CREATE_TIME.getValue.toLong

  private val maxUpdateInterval =
    ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong

  private val ecmHeartBeatTime = ManagerMonitorConf.ECM_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong

  /**
   *   1. Scan all nodes regularly for three minutes to determine the update time of Metrics, 2. If
   *      the update time exceeds a period of time and has not been updated, initiate a Metrics
   *      update request proactively. If the node status is already Unhealthy, then directly
   *      initiate a kill request 3. If send reports that the node does not exist, you need to
   *      remove the node to determine whether the node is Engine or EM information 4. If send
   *      reports other abnormalities, it will be marked as unhealthy 5. Update Metrics if normal
   */
  override def run(): Unit = Utils.tryAndWarn {
    logger.info("Start to check the health of the node")
    // 1.get nodes
    val nodes = nodeManagerPersistence.getAllNodes
    val metricList = nodeMetricManagerPersistence.getNodeMetrics(nodes)
    if (null != metricList) {
      val metricses = metricList.asScala.map(m => (m.getServiceInstance.toString, m)).toMap
      nodes.asScala.foreach { node =>
        metricses.get(node.getServiceInstance.toString).foreach { metrics =>
          node.setNodeStatus(NodeStatus.values()(metrics.getStatus))
          node.setUpdateTime(metrics.getUpdateTime)
        }
      }
    }
    // EngineConn remove
    val engineNodes =
      nodes.asScala.filter(_.getServiceInstance.getApplicationName.equalsIgnoreCase(ecName))
    Utils.tryAndWarn(dealECNodes(engineNodes.asJava))
    val ecmNodes =
      nodes.asScala.filter(_.getServiceInstance.getApplicationName.equalsIgnoreCase(ecmName))
    dealECMNotExistsInRegistry(ecmNodes.asJava)

    /* val engineMetricList = nodeMetricManagerPersistence.getNodeMetrics(engineNodes)
    val healthyList = filterHealthyAndWarnList(engineMetricList)
    dealHealthyList(healthyList)
     val unHealthyList = filterUnHealthyList(engineMetricList)
     dealUnHealthyList(unHealthyList)

     val stockAvailableList = filterStockAvailableList(engineMetricList)
     dealStockAvailableList(stockAvailableList)
     val stockUnAvailableList = filterStockUnAvailableList(engineMetricList)
     dealStockUnAvailableList(stockUnAvailableList) */
    logger.info("Finished to check the health of the node")
  }

  /**
   *   1. When the engine starts, the status is empty, and it needs to judge whether the startup
   *      timeout, if the startup timeout, kill directly 2. After the engine is in the state, it is
   *      normal that the heartbeat information is reported after the startup is completed: if the
   *      heartbeat is not updated for a long time, kill it, if it does not exist on Service
   *      Registry, it needs to be killed.
   *
   * @param engineNodes
   */
  private def dealECNodes(engineNodes: util.List[Node]): Unit = {
    val existingEngineInstances = Sender.getInstances(ecName)
    val clearECSet = new mutable.HashSet[ServiceInstance]()
    engineNodes.asScala.foreach { engineNode =>
      if (NodeStatus.isCompleted(engineNode.getNodeStatus)) {
        logger.info(
          "{} is completed {}, will be remove",
          engineNode.getServiceInstance: Any,
          engineNode.getNodeStatus: Any
        )
        clearECSet.add(engineNode.getServiceInstance)
      } else {
        val engineIsStarted =
          (System.currentTimeMillis() - engineNode.getStartTime.getTime) > maxCreateInterval
        val updateTime = if (null == engineNode.getUpdateTime) {
          engineNode.getStartTime.getTime
        } else {
          engineNode.getUpdateTime.getTime
        }
        val updateOverdue = (System.currentTimeMillis() - updateTime) > maxUpdateInterval
        if (null == engineNode.getNodeStatus) {
          if (!existingEngineInstances.contains(engineNode.getServiceInstance) && engineIsStarted) {
            logger.warn(
              "Failed to find instance {} from Service Registry prepare to kill, engineIsStarted",
              engineNode.getServiceInstance
            )
            clearECSet.add(engineNode.getServiceInstance)
          }
        } else if (updateOverdue) {
          logger.warn("{} heartbeat updateOverdue", engineNode.getServiceInstance)
          clearECSet.add(engineNode.getServiceInstance)
        }
      }
    }
    clearECSet.foreach(clearEngineNode)
  }

  private def updateMetrics(node: Node): Unit = {
    val metric = nodeMetricManagerPersistence.getNodeMetrics(node)
    if (null != metric) {
      node.setNodeStatus(NodeStatus.values()(metric.getStatus))
      node.setUpdateTime(metric.getUpdateTime)
    }
  }

  private def dealECMNotExistsInRegistry(ecmNodes: util.List[Node]): Unit = {
    val existingECMInstances = Sender.getInstances(ecmName)
    ecmNodes.asScala.foreach { ecm =>
      val updateTime = if (null == ecm.getUpdateTime) {
        ecm.getStartTime.getTime
      } else {
        ecm.getUpdateTime.getTime
      }
      val updateOverdue = (System.currentTimeMillis() - updateTime) > ecmHeartBeatTime
      if (!existingECMInstances.contains(ecm.getServiceInstance) && updateOverdue) {
        Utils.tryAndWarn(updateMetrics(ecm))
        val isUpdateOverdue = if (null == ecm.getUpdateTime) {
          (System.currentTimeMillis() - ecm.getStartTime.getTime) > ecmHeartBeatTime
        } else {
          (System.currentTimeMillis() - ecm.getUpdateTime.getTime) > ecmHeartBeatTime
        }
        val isExistingECMInstances = Sender.getInstances(ecmName).contains(ecm.getServiceInstance)
        if (!isExistingECMInstances && isUpdateOverdue) {
          logger.warn(
            "Failed to find ecm instance {} from Service Registry to kill",
            ecm.getServiceInstance
          )
          Utils.tryAndWarnMsg(triggerEMSuicide(ecm.getServiceInstance))(
            s"ecm ${ecm.getServiceInstance} clear failed"
          )
        }
      }
    }
  }

  /**
   * When the EM status is Healthy and WARN:
   *   1. Determine the update time of Metrics. If it is not reported for more than a certain period
   *      of time, initiate a Metrics update request. 2. If send is abnormal, it will be marked as
   *      UnHealthy 3. If the result of send is not available, update to the corresponding state 4.
   *      Update Metrics if normal When the Engine status is Healthy and WARN:
   *   1. Determine the update time of Metrics. If it is not reported for more than a certain period
   *      of time, initiate a Metrics update request. 2. If send is abnormal, it will be marked as
   *      UnHealthy 3. If the result of send is not available, update to UnHealthy status 4. Update
   *      Metrics if normal
   *
   * @param healthyList
   */
  private def dealHealthyList(healthyList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null != healthyList) {
      healthyList.asScala.foreach { nodeMetric =>
        var sender: Sender = null
        try {
          sender = Sender.getSender(nodeMetric.getServiceInstance)
        } catch {
          case n: NoInstanceExistsException =>
            updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "NoInstanceExistsException")
        }
        if (sender == null) {
          updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "sender is null")
        }
        Utils.tryCatch(sender.ask(new NodeHeartbeatRequest) match {
          case m: NodeHeartbeatMsg =>
            if (
                !NodeHealthy.isAvailable(m.getHealthyInfo.getNodeHealthy) && managerLabelService
                  .isEngine(nodeMetric.getServiceInstance)
            ) {
              updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "ec is Unhealthy")
            } else {
              amHeartbeatService.heartbeatEventDeal(m)
            }
          case _ =>
            updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "sender is null")
        }) {
          case e: UndeclaredThrowableException =>
            dealMetricUpdateTimeOut(nodeMetric, e)

          case exception: Exception =>
            logger.warn(
              s"heartbeat RPC request failed, but it is not caused by timeout, " +
                s"the engine will not be forcibly stopped, engine instance: ${nodeMetric.getServiceInstance}",
              exception
            )

        }
      }
    }
  }

  /**
   * When the EM status is UnHealthy:
   *   1. The manager actively requires all engines to be forced to quit (engine suicide) 2. If
   *      there is no engine, exit EM forcibly 3. TODO, if EM forced exit fails, Major will be
   *      alarmed
   *
   * When Engine is in UnHealthy state:
   *   1. Kill by EM, if it fails, execute 2 2. Suicide by the engine, if it fails, execute 3 3.
   *      Send minor alarm
   *
   * @param unhealthyList
   */
  private def dealUnHealthyList(unhealthyList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null == unhealthyList) return
    unhealthyList.asScala.foreach { nodeMetric =>
      if (managerLabelService.isEM(nodeMetric.getServiceInstance)) {
        val nodes = nodeManagerPersistence.getEngineNodeByEM(nodeMetric.getServiceInstance)
        if (null == nodes || nodes.isEmpty) {
          triggerEMSuicide(nodeMetric.getServiceInstance)
        } else {
          nodes.asScala.foreach(node => triggerEngineSuicide(node.getServiceInstance))
        }
      } else {
        fixedThreadPoll.submit {
          new Runnable {
            override def run(): Unit = clearEngineNode(nodeMetric.getServiceInstance)
          }
        }
      }
    }
  }

  /**
   * When the EM status is StockAvailable:
   *   1. Determine the update time of Metrics. If the state is not changed for a certain period of
   *      time, change the Node to the StockUnavailable state
   *
   * @param stockAvailableList
   */
  private def dealStockAvailableList(stockAvailableList: util.List[NodeMetrics]): Unit =
    Utils.tryAndWarn {
      if (null == stockAvailableList) return
      stockAvailableList.asScala.foreach { nodeMetric =>
        updateMetricHealthy(
          nodeMetric,
          NodeHealthy.StockUnavailable,
          "Manager believes that the EM is already in the StockUnAvailable state"
        )
      }
    }

  private def dealStockUnAvailableList(stockUnAvailableList: util.List[NodeMetrics]): Unit =
    Utils.tryAndWarn {
      if (null == stockUnAvailableList) return
      stockUnAvailableList.asScala.foreach { nodeMetric =>
        if (managerLabelService.isEM(nodeMetric.getServiceInstance)) {
          val nodes = nodeManagerPersistence.getEngineNodeByEM(nodeMetric.getServiceInstance)
          if (null == nodes || nodes.isEmpty) {
            updateMetricHealthy(
              nodeMetric,
              NodeHealthy.UnHealthy,
              "Manager believes that the EM is already in the Unhealthy state"
            )
          } else {
            fixedThreadPoll.submit {
              new Runnable {
                override def run(): Unit =
                  nodes.asScala.foreach(node => triggerEMToStopEngine(node.getServiceInstance))
              }
            }
          }
        }
      }
    }

  private def filterHealthyAndWarnList(
      nodeMetrics: java.util.List[NodeMetrics]
  ): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.asScala.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.Healthy == healthy || NodeHealthy.WARN == healthy
      } else {
        false
      }
    }
  }.asJava

  private def filterStockAvailableList(
      nodeMetrics: java.util.List[NodeMetrics]
  ): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.asScala.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.StockAvailable == healthy
      } else {
        false
      }
    }
  }.asJava

  private def filterStockUnAvailableList(
      nodeMetrics: java.util.List[NodeMetrics]
  ): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.asScala.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.StockUnavailable == healthy
      } else {
        false
      }
    }
  }.asJava

  private def filterUnHealthyList(
      nodeMetrics: java.util.List[NodeMetrics]
  ): java.util.List[NodeMetrics] = {
    nodeMetrics.asScala.filter { metric =>
      val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
      NodeHealthy.UnHealthy == healthy
    }
  }.asJava

  private def clearUnhealthyNode(ownerNodeMetrics: OwnerNodeMetrics): Unit = {
    val sender = Sender.getSender(Sender.getThisServiceInstance)
    if (managerLabelService.isEM(ownerNodeMetrics.nodeMetrics.getServiceInstance)) {
      val stopEMRequest = new StopEMRequest
      stopEMRequest.setEm(ownerNodeMetrics.nodeMetrics.getServiceInstance)
      stopEMRequest.setUser(ownerNodeMetrics.owner)
      emUnregisterService.stopEM(stopEMRequest, sender)
    } else {
      val stopEngineRequest = new EngineStopRequest(
        ownerNodeMetrics.nodeMetrics.getServiceInstance,
        ownerNodeMetrics.owner
      )
      engineStopService.stopEngine(stopEngineRequest, sender)
    }
  }

  private def clearEngineNode(instance: ServiceInstance): Unit = Utils.tryAndError {
    logger.warn(s"Manager Monitor prepare to kill engine $instance")
    val stopEngineRequest = new EngineStopRequest(instance, ManagerUtils.getAdminUser)
    val sender = Sender.getSender(Sender.getThisServiceInstance)
    Utils.tryCatch {
      engineStopService.stopEngine(stopEngineRequest, sender)
    } { e =>
      logger.error(s"Em failed to kill engine $instance", e)
      Utils.tryAndWarn(triggerEngineSuicide(instance))
      null
    }
  }

  private def triggerEMToStopEngine(instance: ServiceInstance): Unit = Utils.tryAndError {
    logger.warn(s"Manager Monitor prepare to kill engine $instance by em")
    val stopEngineRequest = new EngineStopRequest(instance, ManagerUtils.getAdminUser)
    val sender = Sender.getSender(Sender.getThisServiceInstance)
    engineStopService.stopEngine(stopEngineRequest, sender)
  }

  private def triggerEngineSuicide(instance: ServiceInstance): Unit = Utils.tryAndError {
    logger.warn(s"Manager Monitor prepare to triggerEngineSuicide engine $instance")
    // val engineSuicide = new EngineSuicideRequest(instance, ManagerUtils.getAdminUser)
    // messagePublisher.publish(engineSuicide)
  }

  private def triggerEMSuicide(instance: ServiceInstance): Unit = Utils.tryAndError {
    logger.warn(s"Manager Monitor prepare to kill EM $instance")
    val stopEMRequest = new StopEMRequest
    stopEMRequest.setEm(instance)
    stopEMRequest.setUser(ManagerUtils.getAdminUser)
    val sender = Sender.getSender(Sender.getThisServiceInstance)
    emUnregisterService.stopEM(stopEMRequest, sender)
  }

  private def updateMetricHealthy(
      nodeMetrics: NodeMetrics,
      nodeHealthy: NodeHealthy,
      reason: String
  ): Unit = {
    logger.warn(
      s"update instance ${nodeMetrics.getServiceInstance} from ${nodeMetrics.getHealthy} to ${nodeHealthy}"
    )
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg(
      s"Manager-Monitor considers the node to be in UnHealthy state, reason: $reason"
    )
    nodeHealthyInfo.setNodeHealthy(nodeHealthy)
    nodeMetrics.setHealthy(metricsConverter.convertHealthyInfo(nodeHealthyInfo))
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics)
  }

  /**
   * When the engine is not found, sending a message will throw an UndeclaredThrowableException
   * exception This time it needs to be deleted forcibly
   *
   * @param nodeMetric
   * @param e
   */
  private def dealMetricUpdateTimeOut(nodeMetric: NodeMetrics, e: UndeclaredThrowableException) = {
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    val timeout = System.currentTimeMillis() - nodeMetric.getUpdateTime.getTime > maxInterval
    if (timeout) {
      logger.warn(
        s"The engine failed to send the RPC request, and the engine instance could not be found: ${nodeMetric.getServiceInstance}, " +
          s"start sending the request to stop the engine!",
        e
      )
      triggerEMToStopEngine(nodeMetric.getServiceInstance)
    }
  }

}

case class OwnerNodeMetrics(nodeMetrics: NodeMetrics, owner: String)
