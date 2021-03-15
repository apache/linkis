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

package com.webank.wedatasphere.linkis.manager.monitor.node

import java.util
import java.util.concurrent.{ExecutorService, TimeUnit}

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeHealthy
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeMetrics}
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeEntity
import com.webank.wedatasphere.linkis.manager.common.monitor.ManagerMonitor
import com.webank.wedatasphere.linkis.manager.common.protocol.em.StopEMRequest
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineStopRequest, EngineSuicideRequest}
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{NodeHeartbeatMsg, NodeHeartbeatRequest}
import com.webank.wedatasphere.linkis.manager.common.utils.ManagerUtils
import com.webank.wedatasphere.linkis.manager.monitor.conf.ManagerMonitorConf
import com.webank.wedatasphere.linkis.manager.persistence.{NodeManagerPersistence, NodeMetricManagerPersistence}
import com.webank.wedatasphere.linkis.manager.service.common.label.ManagerLabelService
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.rpc.exception.NoInstanceExistsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class NodeHeartbeatMonitor extends ManagerMonitor with Logging {

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  @Autowired
  private var messagePublisher: MessagePublisher = _

  @Autowired
  private var managerLabelService: ManagerLabelService = _

  private val fixedThreadPoll: ExecutorService = Utils.newFixedThreadPool(ManagerMonitorConf.MANAGER_MONITOR_ASYNC_POLL_SIZE.getValue, "manager_async", false)


  /**
    * 1. 定时三分钟扫所有的node，判断Metrics的更新时间，
    * 2. 如果更新时间超过5分钟未更新，主动发起Metrics更新请求。如果node 状态已经是Unhealthy 则直接发起kill请求
    * 3. 如果send报节点不存在，需要移除该node，判断该Node为Engine还是EM信息
    * 4. 如果send报其他异常，就标记为unhealthy
    * 5. 如果正常则更新Metrics
    */
  override def run(): Unit = Utils.tryAndWarn {
    info("Start to check the health of the node")
    // 1. 获取nodes
    val nodes = nodeManagerPersistence.getAllNodes

    val existingEngineInstances = Sender.getInstances(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)

    val engineNodes = nodes.filter(_.getServiceInstance.getApplicationName.equalsIgnoreCase(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue))
    engineNodes.foreach { engineNode =>
      val engineIsStarted = engineNode match {
        case entity: PersistenceNodeEntity =>
          val createTime = entity.getStartTime.getTime
          val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
          (System.currentTimeMillis() - createTime) > maxInterval
        case _ => true
      }
      if (!existingEngineInstances.contains(engineNode.getServiceInstance) && engineIsStarted) {
        warn(s"Failed to find instance ${engineNode.getServiceInstance} from eureka prepare to kill")
        clearEngineNode(engineNode.getServiceInstance)
      }
    }

    val metricList = nodeMetricManagerPersistence.getNodeMetrics(nodes)
    if (null == metricList || metricList.isEmpty) {
      info("Metrics is empty")
      return
    }


    val healthyList = filterHealthyAndWarnList(metricList)
    dealHealthyList(healthyList)
    val unHealthyList = filterUnHealthyList(metricList)
    dealUnHealthyList(unHealthyList)

    val stockAvailableList = filterStockAvailableList(metricList)
    dealStockAvailableList(stockAvailableList)
    val stockUnAvailableList = filterStockUnAvailableList(metricList)
    dealStockUnAvailableList(stockUnAvailableList)
    info("Finished to check the health of the node")
  }

  /**
    * 当EM状态为Healthy和WARN时：
    * 1. 判断Metrics的更新时间,如果超过一定时间没上报，主动发起Metrics更新请求
    * 2. 如果send异常，标识为UnHealthy
    * 3. 如果send的结果不可用则更新为对应状态
    * 4. 如果正常则更新Metrics
    * 当Engine状态为Healthy和WARN时：
    * 1. 判断Metrics的更新时间,如果超过一定时间没上报，主动发起Metrics更新请求
    * 2. 如果send异常，标识为UnHealthy
    * 3. 如果send的结果不可用则更新为UnHealthy状态
    * 4. 如果正常则更新Metrics
    *
    * @param healthyList
    */
  private def dealHealthyList(healthyList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null != healthyList) {
      healthyList.foreach { nodeMetric =>
        var sender: Sender = null
        try {
          sender = Sender.getSender(nodeMetric.getServiceInstance)
        } catch {
          case n: NoInstanceExistsException =>
            updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "找不到对应的服务：NoInstanceExistsException")
        }
        if (sender == null) {
          updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "找不到对应的服务，获取的sender为空")
        }

        sender.ask(new NodeHeartbeatRequest) match {
          case m: NodeHeartbeatMsg =>
            if (!NodeHealthy.isAvailable(m.getHealthyInfo.getNodeHealthy) && managerLabelService.isEngine(nodeMetric.getServiceInstance)) {
              updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "该engine为不可用状态，标识该engine为Unhealthy")
            } else {
              messagePublisher.publish(m)
            }
          case _ =>
            updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "找不到对应的服务，获取的sender为空")
        }
      }
    }
  }

  /**
    * 当EM状态为UnHealthy时：
    * 1. manager主动要求所有的engine强制退出（engine自杀）
    * 2. 如果没有engine了，则强制退出EM
    * 3.TODO 如果EM强制退出失败，告警Major
    *
    * 当Engine为UnHealthy状态时：
    * 1. 由EM进行kill，如果失败执行2
    * 2. 由engine进行自杀，如果失败执行3
    * 3. 发送minor告警
    *
    * @param unhealthyList
    */
  private def dealUnHealthyList(unhealthyList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null == unhealthyList) return
    unhealthyList.foreach { nodeMetric =>
      if (managerLabelService.isEM(nodeMetric.getServiceInstance)) {
        val nodes = nodeManagerPersistence.getEngineNodeByEM(nodeMetric.getServiceInstance)
        if (null == nodes || nodes.isEmpty) {
          triggerEMSuicide(nodeMetric.getServiceInstance)
        } else {
          nodes.foreach(node => triggerEngineSuicide(node.getServiceInstance))
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
    * 当EM状态为StockAvailable时：
    * 1. 判断Metrics的更新时间，如果超过一定时间没修改状态，将Node修改为StockUnavailable状态
    *
    * @param stockAvailableList
    */
  private def dealStockAvailableList(stockAvailableList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null == stockAvailableList) return
    stockAvailableList.foreach { nodeMetric =>
      updateMetricHealthy(nodeMetric, NodeHealthy.StockUnavailable, "Manager认为该EM已经处于StockUnAvailable 状态")
    }
  }

  /**
    * 当EM为StockUnavailable状态时：
    * 1. 判断该EM下面是否还有engine，如果有engine需要manager主动要求所有的engine非强制退出
    * 2. 如果EM下的engine是UnHealthy状态，则将EM翻转为UnHealthy状态
    * 3. TODO 如果StockUnavailable状态如果超过n分钟，则发送IMS告警
    *
    * @param stockUnAvailableList
    */
  private def dealStockUnAvailableList(stockUnAvailableList: util.List[NodeMetrics]): Unit = Utils.tryAndWarn {
    if (null == stockUnAvailableList) return
    stockUnAvailableList.foreach { nodeMetric =>
      if (managerLabelService.isEM(nodeMetric.getServiceInstance)) {
        val nodes = nodeManagerPersistence.getEngineNodeByEM(nodeMetric.getServiceInstance)
        if (null == nodes || nodes.isEmpty) {
          updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "Manager认为该EM已经处UnHealthy状态")
        } else {
          fixedThreadPoll.submit {
            new Runnable {
              override def run(): Unit = nodes.foreach(node => triggerEMToStopEngine(node.getServiceInstance))
            }
          }
        }
      }
    }
  }


  private def filterHealthyAndWarnList(nodeMetrics: java.util.List[NodeMetrics]): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.Healthy == healthy || NodeHealthy.WARN == healthy
      } else {
        false
      }
    }
  }

  private def filterStockAvailableList(nodeMetrics: java.util.List[NodeMetrics]): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.StockAvailable == healthy
      } else {
        false
      }
    }
  }

  private def filterStockUnAvailableList(nodeMetrics: java.util.List[NodeMetrics]): java.util.List[NodeMetrics] = {
    val curTime = System.currentTimeMillis()
    val maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue.toLong
    nodeMetrics.filter { metric =>
      val interval = curTime - metric.getUpdateTime.getTime
      if (interval > maxInterval) {
        val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
        NodeHealthy.StockUnavailable == healthy
      } else {
        false
      }
    }
  }

  private def filterUnHealthyList(nodeMetrics: java.util.List[NodeMetrics]): java.util.List[NodeMetrics] = {
    nodeMetrics.filter { metric =>
      val healthy = metricsConverter.parseHealthyInfo(metric).getNodeHealthy
      NodeHealthy.UnHealthy == healthy
    }
  }

  private def clearUnhealthyNode(ownerNodeMetrics: OwnerNodeMetrics): Unit = {
    if (managerLabelService.isEM(ownerNodeMetrics.nodeMetrics.getServiceInstance)) {
      val stopEMRequest = new StopEMRequest
      stopEMRequest.setEm(ownerNodeMetrics.nodeMetrics.getServiceInstance)
      stopEMRequest.setUser(ownerNodeMetrics.owner)
      messagePublisher.publish(stopEMRequest)
    } else {
      val stopEngineRequest = new EngineStopRequest(ownerNodeMetrics.nodeMetrics.getServiceInstance, ownerNodeMetrics.owner)
      messagePublisher.publish(stopEngineRequest)
    }
  }

  private def clearEngineNode(instance: ServiceInstance): Unit = Utils.tryAndError {
    warn(s"Manager Monitor prepare to kill engine $instance")
    val stopEngineRequest = new EngineStopRequest(instance, ManagerUtils.getAdminUser)

    Utils.tryCatch {
      val job = messagePublisher.publish(stopEngineRequest)
      job.get(ManagerMonitorConf.ENGINE_KILL_TIMEOUT.getValue.toLong, TimeUnit.MILLISECONDS)
    } { e =>
      error(s"Em failed to kill engine $instance", e)
      Utils.tryAndWarn(triggerEngineSuicide(instance))
      null
    }
  }

  private def triggerEMToStopEngine(instance: ServiceInstance): Unit = Utils.tryAndError {
    warn(s"Manager Monitor prepare to kill engine $instance by em")
    val stopEngineRequest = new EngineStopRequest(instance, ManagerUtils.getAdminUser)
    messagePublisher.publish(stopEngineRequest)
  }

  private def triggerEngineSuicide(instance: ServiceInstance): Unit = Utils.tryAndError {
    warn(s"Manager Monitor prepare to triggerEngineSuicide engine $instance")
    val engineSuicide = new EngineSuicideRequest(instance, ManagerUtils.getAdminUser)
    messagePublisher.publish(engineSuicide)
  }

  private def triggerEMSuicide(instance: ServiceInstance): Unit = Utils.tryAndError {
    warn(s"Manager Monitor prepare to kill EM $instance")
    val stopEMRequest = new StopEMRequest
    stopEMRequest.setEm(instance)
    stopEMRequest.setUser(ManagerUtils.getAdminUser)
    messagePublisher.publish(stopEMRequest)
  }


  private def updateMetricHealthy(nodeMetrics: NodeMetrics, nodeHealthy: NodeHealthy, reason: String): Unit = {
    warn(s"update instance ${nodeMetrics.getServiceInstance} from ${nodeMetrics.getHealthy} to ${nodeHealthy}")
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg(s"Manager-Monitor 认为该节点为UnHealthy状态，原因：$reason")
    nodeHealthyInfo.setNodeHealthy(nodeHealthy)
    nodeMetrics.setHealthy(metricsConverter.convertHealthyInfo(nodeHealthyInfo))
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics)
  }

}

case class OwnerNodeMetrics(nodeMetrics: NodeMetrics, owner: String)
