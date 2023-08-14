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

package org.apache.linkis.manager.am.service.monitor;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.am.conf.ManagerMonitorConf;
import org.apache.linkis.manager.am.service.em.EMUnregisterService;
import org.apache.linkis.manager.am.service.engine.EngineStopService;
import org.apache.linkis.manager.am.service.heartbeat.AMHeartbeatService;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.monitor.ManagerMonitor;
import org.apache.linkis.manager.common.protocol.em.StopEMRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg;
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatRequest;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.service.common.label.ManagerLabelService;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.rpc.Sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NodeHeartbeatMonitor implements ManagerMonitor {
  private static final Logger logger = LoggerFactory.getLogger(NodeHeartbeatMonitor.class);

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired private AMHeartbeatService amHeartbeatService;

  @Autowired private EngineStopService engineStopService;

  @Autowired private EMUnregisterService emUnregisterService;

  @Autowired private ManagerLabelService managerLabelService;

  private final ExecutorService fixedThreadPoll =
      LinkisUtils.newFixedThreadPool(
          (int) ManagerMonitorConf.MANAGER_MONITOR_ASYNC_POLL_SIZE.getValue(),
          "manager_async",
          false);

  private final String ecName = GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue();
  private final String ecmName = GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue();
  private final long maxCreateInterval =
      ManagerMonitorConf.NODE_MAX_CREATE_TIME.getValue().toLong();

  private final long maxUpdateInterval =
      ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue().toLong();

  private final long ecmHeartBeatTime =
      ManagerMonitorConf.ECM_HEARTBEAT_MAX_UPDATE_TIME.getValue().toLong();

  /**
   * 1. Scan all nodes regularly for three minutes to determine the update time of Metrics, 2. If
   * the update time exceeds a period of time and has not been updated, initiate a Metrics update
   * request proactively. If the node status is already Unhealthy, then directly initiate a kill
   * request 3. If send reports that the node does not exist, you need to remove the node to
   * determine whether the node is Engine or EM information 4. If send reports other abnormalities,
   * it will be marked as unhealthy 5. Update Metrics if normal
   */
  @Override
  public void run() {
    LinkisUtils.tryAndWarn(
        () -> {
          logger.info("Start to check the health of the node");
          // 1.get nodes
          List<Node> nodes = nodeManagerPersistence.getAllNodes();
          List<NodeMetrics> metricList = nodeMetricManagerPersistence.getNodeMetrics(nodes);
          if (null != metricList) {
            for (Node node : nodes) {
              for (NodeMetrics metrics : metricList) {
                if (metrics
                    .getServiceInstance()
                    .toString()
                    .equals(node.getServiceInstance().toString())) {
                  node.setNodeStatus(NodeStatus.values()[metrics.getStatus()]);
                  node.setUpdateTime(metrics.getUpdateTime());
                  break;
                }
              }
            }
          }
          // EngineConn remove
          List<Node> engineNodes = new ArrayList<>();
          for (Node node : nodes) {
            if (node.getServiceInstance().getApplicationName().equalsIgnoreCase(ecName)) {
              engineNodes.add(node);
            }
          }
          LinkisUtils.tryAndWarn(
              () -> {
                dealECNodes(engineNodes);
              },
              logger);

          List<Node> ecmNodes = new ArrayList<>();
          for (Node node : nodes) {
            if (node.getServiceInstance().getApplicationName().equalsIgnoreCase(ecmName)) {
              ecmNodes.add(node);
            }
          }
          dealECMNotExistsInRegistry(ecmNodes);

          /* val engineMetricList = nodeMetricManagerPersistence.getNodeMetrics(engineNodes)
          val healthyList = filterHealthyAndWarnList(engineMetricList)
          dealHealthyList(healthyList)
           val unHealthyList = filterUnHealthyList(engineMetricList)
           dealUnHealthyList(unHealthyList)

           val stockAvailableList = filterStockAvailableList(engineMetricList)
           dealStockAvailableList(stockAvailableList)
           val stockUnAvailableList = filterStockUnAvailableList(engineMetricList)
           dealStockUnAvailableList(stockUnAvailableList) */
          logger.info("Finished to check the health of the node");
        },
        logger);
  }

  /**
   * 1. When the engine starts, the status is empty, and it needs to judge whether the startup
   * timeout, if the startup timeout, kill directly 2. After the engine is in the state, it is
   * normal that the heartbeat information is reported after the startup is completed: if the
   * heartbeat is not updated for a long time, kill it, if it does not exist on Service Registry, it
   * needs to be killed.
   *
   * @param engineNodes
   */
  private void dealECNodes(List<Node> engineNodes) {
    List<ServiceInstance> existingEngineInstances =
        Arrays.stream(Sender.getInstances(ecName)).collect(Collectors.toList());
    Set<ServiceInstance> clearECSet = new HashSet<>();
    engineNodes.forEach(
        engineNode -> {
          if (NodeStatus.isCompleted(engineNode.getNodeStatus())) {
            logger.info(
                "{} is completed {}, will be remove",
                engineNode.getServiceInstance(),
                engineNode.getNodeStatus());
            clearECSet.add(engineNode.getServiceInstance());
          } else {
            boolean engineIsStarted =
                (System.currentTimeMillis() - engineNode.getStartTime().getTime())
                    > maxCreateInterval;
            long updateTime =
                engineNode.getUpdateTime() == null
                    ? engineNode.getStartTime().getTime()
                    : engineNode.getUpdateTime().getTime();

            boolean updateOverdue = (System.currentTimeMillis() - updateTime) > maxUpdateInterval;

            if (engineNode.getNodeStatus() == null) {
              if (!existingEngineInstances.contains(engineNode.getServiceInstance())
                  && engineIsStarted) {
                logger.warn(
                    "Failed to find instance {} from Service Registry prepare to kill, engineIsStarted",
                    engineNode.getServiceInstance());
                clearECSet.add(engineNode.getServiceInstance());
              }
            } else if (updateOverdue) {
              logger.warn("{} heartbeat updateOverdue", engineNode.getServiceInstance());
              clearECSet.add(engineNode.getServiceInstance());
            }
          }
        });
    clearECSet.forEach(this::clearEngineNode);
  }

  private void updateMetrics(Node node) {
    NodeMetrics metric = nodeMetricManagerPersistence.getNodeMetrics(node);
    if (metric != null) {
      node.setNodeStatus(NodeStatus.values()[metric.getStatus()]);
      node.setUpdateTime(metric.getUpdateTime());
    }
  }

  private void dealECMNotExistsInRegistry(List<Node> ecmNodes) {
    List<ServiceInstance> existingECMInstances =
        Arrays.stream(Sender.getInstances(ecName)).collect(Collectors.toList());
    ecmNodes.forEach(
        ecm -> {
          long updateTime =
              ecm.getUpdateTime() == null
                  ? ecm.getStartTime().getTime()
                  : ecm.getUpdateTime().getTime();

          boolean updateOverdue = (System.currentTimeMillis() - updateTime) > ecmHeartBeatTime;

          if (!existingECMInstances.contains(ecm.getServiceInstance()) && updateOverdue) {
            LinkisUtils.tryAndWarn(() -> updateMetrics(ecm), logger);
            boolean isUpdateOverdue =
                ecm.getUpdateTime() == null
                    ? (System.currentTimeMillis() - ecm.getStartTime().getTime()) > ecmHeartBeatTime
                    : (System.currentTimeMillis() - ecm.getUpdateTime().getTime())
                        > ecmHeartBeatTime;
            boolean isExistingECMInstances =
                existingECMInstances.contains(ecm.getServiceInstance());
            if (!isExistingECMInstances && isUpdateOverdue) {
              logger.warn(
                  "Failed to find ecm instance {} from Service Registry to kill",
                  ecm.getServiceInstance());

              triggerEMSuicide(ecm.getServiceInstance());
            }
          }
        });
  }

  /**
   * When the EM status is Healthy and WARN: 1. Determine the update time of Metrics. If it is not
   * reported for more than a certain period of time, initiate a Metrics update request. 2. If send
   * is abnormal, it will be marked as UnHealthy 3. If the result of send is not available, update
   * to the corresponding state 4. Update Metrics if normal When the Engine status is Healthy and
   * WARN: 1. Determine the update time of Metrics. If it is not reported for more than a certain
   * period of time, initiate a Metrics update request. 2. If send is abnormal, it will be marked as
   * UnHealthy 3. If the result of send is not available, update to UnHealthy status 4. Update
   * Metrics if normal
   *
   * @param healthyList
   */
  private void dealHealthyList(List<NodeMetrics> healthyList) {
    if (healthyList != null) {
      healthyList.forEach(
          nodeMetric -> {
            Sender sender = Sender.getSender(nodeMetric.getServiceInstance());
            if (sender == null) {
              updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "sender is null");
            }
            LinkisUtils.tryCatch(
                () -> {
                  Object obj = sender.ask(new NodeHeartbeatRequest());
                  if (obj instanceof NodeHeartbeatMsg) {
                    NodeHeartbeatMsg m = (NodeHeartbeatMsg) obj;
                    if (!NodeHealthy.isAvailable(m.getHealthyInfo().getNodeHealthy())
                        && managerLabelService.isEngine(nodeMetric.getServiceInstance())) {
                      updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "ec is Unhealthy");
                    } else {
                      amHeartbeatService.heartbeatEventDeal(m);
                    }
                  } else {
                    updateMetricHealthy(nodeMetric, NodeHealthy.UnHealthy, "sender is null");
                  }
                  return "";
                },
                e -> {
                  if (e instanceof UndeclaredThrowableException) {
                    dealMetricUpdateTimeOut(nodeMetric, (UndeclaredThrowableException) e);
                  } else {
                    logger.warn(
                        "heartbeat RPC request failed, but it is not caused by timeout, "
                            + "the engine will not be forcibly stopped, engine instance: "
                            + nodeMetric.getServiceInstance(),
                        e);
                  }
                  return "";
                });
          });
    }
  }

  private void clearEngineNode(ServiceInstance instance) {
    logger.warn("Manager Monitor prepare to kill engine " + instance);
    EngineStopRequest stopEngineRequest =
        new EngineStopRequest(instance, ManagerUtils.getAdminUser());
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());

    try {
      engineStopService.stopEngine(stopEngineRequest, sender);
    } catch (Exception e) {
      logger.error("Em failed to kill engine " + instance, e);
      LinkisUtils.tryAndWarn(() -> triggerEngineSuicide(instance), logger);
    }
  }

  private void triggerEMToStopEngine(ServiceInstance instance) {
    logger.warn("Manager Monitor prepare to kill engine " + instance + " by em");
    EngineStopRequest stopEngineRequest =
        new EngineStopRequest(instance, ManagerUtils.getAdminUser());
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    engineStopService.stopEngine(stopEngineRequest, sender);
  }

  private void triggerEngineSuicide(ServiceInstance instance) {
    logger.warn("Manager Monitor prepare to triggerEngineSuicide engine " + instance);
    // val engineSuicide = new EngineSuicideRequest(instance, ManagerUtils.getAdminUser)
    // messagePublisher.publish(engineSuicide)
  }

  private void triggerEMSuicide(ServiceInstance instance) {
    logger.warn("Manager Monitor prepare to kill EM " + instance);
    StopEMRequest stopEMRequest = new StopEMRequest();
    stopEMRequest.setEm(instance);
    stopEMRequest.setUser(ManagerUtils.getAdminUser());
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    emUnregisterService.stopEM(stopEMRequest, sender);
  }

  private void updateMetricHealthy(
      NodeMetrics nodeMetrics, NodeHealthy nodeHealthy, String reason) {
    logger.warn(
        "update instance "
            + nodeMetrics.getServiceInstance()
            + " from "
            + nodeMetrics.getHealthy()
            + " to "
            + nodeHealthy);
    NodeHealthyInfo nodeHealthyInfo = new NodeHealthyInfo();
    nodeHealthyInfo.setMsg(
        "Manager-Monitor considers the node to be in UnHealthy state, reason: " + reason);
    nodeHealthyInfo.setNodeHealthy(nodeHealthy);
    nodeMetrics.setHealthy(metricsConverter.convertHealthyInfo(nodeHealthyInfo));
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics);
  }

  /**
   * When the engine is not found, sending a message will throw an UndeclaredThrowableException
   * exception This time it needs to be deleted forcibly
   *
   * @param nodeMetric
   * @param e
   */
  private void dealMetricUpdateTimeOut(NodeMetrics nodeMetric, UndeclaredThrowableException e) {
    long maxInterval = ManagerMonitorConf.NODE_HEARTBEAT_MAX_UPDATE_TIME.getValue().toLong();
    boolean timeout =
        System.currentTimeMillis() - nodeMetric.getUpdateTime().getTime() > maxInterval;
    if (timeout) {
      logger.warn(
          "The engine failed to send the RPC request, and the engine instance could not be found: "
              + nodeMetric.getServiceInstance()
              + ", "
              + "start sending the request to stop the engine!",
          e);
      triggerEMToStopEngine(nodeMetric.getServiceInstance());
    }
  }
}
