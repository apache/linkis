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

package org.apache.linkis.manager.am.manager;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.*;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeEntity;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest;
import org.apache.linkis.manager.common.protocol.em.ECMOperateResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest;
import org.apache.linkis.manager.exception.NodeInstanceDuplicateException;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.rm.ResourceInfo;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultEMNodeManager implements EMNodeManager {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEMNodeManager.class);

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired private NodePointerBuilder nodePointerBuilder;

  @Autowired private ResourceManager resourceManager;

  @Override
  public void emRegister(EMNode emNode) {
    nodeManagerPersistence.addNodeInstance(emNode);
    // init metric
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
        metricsConverter.getInitMetric(emNode.getServiceInstance()));
  }

  @Override
  public void addEMNodeInstance(EMNode emNode) {
    try {
      nodeManagerPersistence.addNodeInstance(emNode);
    } catch (NodeInstanceDuplicateException e) {
      logger.warn("em instance had exists, {}.", emNode);
      nodeManagerPersistence.updateEngineNode(emNode.getServiceInstance(), emNode);
    }
  }

  @Override
  public void initEMNodeMetrics(EMNode emNode) {
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
        metricsConverter.getInitMetric(emNode.getServiceInstance()));
  }

  @Override
  public List<EngineNode> listEngines(EMNode emNode) {
    List<EngineNode> result = new ArrayList<>();
    List<EngineNode> nodes = nodeManagerPersistence.getEngineNodeByEM(emNode.getServiceInstance());
    Map<String, NodeMetrics> metricses = new HashMap<>();
    for (NodeMetrics m : nodeMetricManagerPersistence.getNodeMetrics(nodes)) {
      metricses.put(m.getServiceInstance().toString(), m);
    }
    for (EngineNode node : nodes) {
      NodeMetrics metrics = metricses.get(node.getServiceInstance().toString());
      if (metrics != null) {
        metricsConverter.fillMetricsToNode(node, metrics);
      }
      result.add(node);
    }
    return result;
  }

  @Override
  public List<EngineNode> listUserEngines(EMNode emNode, String user) {
    List<EngineNode> result = new ArrayList<>();
    for (EngineNode node : listEngines(emNode)) {
      if (node.getOwner().equals(user)) {
        result.add(node);
      }
    }
    return result;
  }

  @Override
  public List<Node> listUserNodes(String user) {
    return nodeManagerPersistence.getNodes(user);
  }

  /**
   * Get detailed em information from the persistence TODO add label to node ?
   *
   * @param scoreServiceInstances
   * @return
   */
  @Override
  public EMNode[] getEMNodes(ScoreServiceInstance[] scoreServiceInstances) {
    if (null == scoreServiceInstances || scoreServiceInstances.length == 0) {
      return null;
    }
    EMNode[] emNodes =
        Arrays.stream(scoreServiceInstances)
            .map(
                scoreServiceInstance -> {
                  AMEMNode emNode = new AMEMNode();
                  emNode.setScore(scoreServiceInstance.getScore());
                  emNode.setServiceInstance(scoreServiceInstance.getServiceInstance());
                  return emNode;
                })
            .toArray(EMNode[]::new);

    // 1. add nodeMetrics 2 add RM info
    ResourceInfo resourceInfo =
        resourceManager.getResourceInfo(
            Arrays.stream(scoreServiceInstances)
                .map(ScoreServiceInstance::getServiceInstance)
                .toArray(ServiceInstance[]::new));
    List<NodeMetrics> nodeMetrics =
        nodeMetricManagerPersistence.getNodeMetrics(Arrays.asList(emNodes));

    for (EMNode emNode : emNodes) {
      Optional<NodeMetrics> optionMetrics =
          nodeMetrics.stream()
              .filter(metrics -> metrics.getServiceInstance().equals(emNode.getServiceInstance()))
              .findFirst();
      Optional<RMNode> optionRMNode =
          resourceInfo.getResourceInfo().stream()
              .filter(rmNode -> rmNode.getServiceInstance().equals(emNode.getServiceInstance()))
              .findFirst();

      optionMetrics.ifPresent(metrics -> metricsConverter.fillMetricsToNode(emNode, metrics));
      optionRMNode.ifPresent(rmNode -> emNode.setNodeResource(rmNode.getNodeResource()));
    }
    return emNodes;
  }

  @Override
  public EMNode getEM(ServiceInstance serviceInstance) {
    Node node = nodeManagerPersistence.getNode(serviceInstance);
    if (null == node) {
      logger.info("This em of " + serviceInstance + " not exists in db");
      return null;
    }
    AMEMNode emNode = new AMEMNode();
    emNode.setOwner(node.getOwner());
    emNode.setServiceInstance(node.getServiceInstance());
    if (node instanceof PersistenceNodeEntity) {
      emNode.setStartTime(((PersistenceNodeEntity) node).getStartTime());
    }
    emNode.setMark(emNode.getMark());
    metricsConverter.fillMetricsToNode(emNode, nodeMetricManagerPersistence.getNodeMetrics(emNode));
    return emNode;
  }

  @Override
  public void stopEM(EMNode emNode) {
    nodePointerBuilder.buildEMNodePointer(emNode).stopNode();
  }

  @Override
  public void deleteEM(EMNode emNode) {
    nodeManagerPersistence.removeNodeInstance(emNode);
    logger.info("Finished to clear emNode instance(" + emNode.getServiceInstance() + ") info ");
    nodeMetricManagerPersistence.deleteNodeMetrics(emNode);
    logger.info("Finished to clear emNode(" + emNode.getServiceInstance() + ") metrics info");
  }

  @Override
  public void pauseEM(ServiceInstance serviceInstance) {}

  /**
   * 1. request engineManager to launch engine
   *
   * @param engineBuildRequest
   * @param emNode
   * @return
   */
  @Override
  public EngineNode createEngine(EngineConnLaunchRequest engineConnLaunchRequest, EMNode emNode) {
    return nodePointerBuilder.buildEMNodePointer(emNode).createEngine(engineConnLaunchRequest);
  }

  @Override
  public void stopEngine(EngineStopRequest engineStopRequest, EMNode emNode) {
    nodePointerBuilder.buildEMNodePointer(emNode).stopEngine(engineStopRequest);
  }

  @Override
  public ECMOperateResponse executeOperation(EMNode ecmNode, ECMOperateRequest request) {
    return nodePointerBuilder.buildEMNodePointer(ecmNode).executeOperation(request);
  }
}
