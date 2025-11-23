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
import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.converter.MetricsConverter;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.locker.EngineNodeLocker;
import org.apache.linkis.manager.am.pointer.EngineNodePointer;
import org.apache.linkis.manager.am.pointer.NodePointerBuilder;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.*;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateResponse;
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.rm.ResourceInfo;
import org.apache.linkis.manager.rm.service.ResourceManager;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineNodeManager implements EngineNodeManager {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineNodeManager.class);

  @Autowired private EngineNodeLocker engineLocker;

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private MetricsConverter metricsConverter;

  @Autowired private NodePointerBuilder nodePointerBuilder;

  @Autowired private ResourceManager resourceManager;

  @Autowired private NodeManagerMapper nodeManagerMapper;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  private final LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Override
  public List<EngineNode> listEngines(String user) {
    List<Node> userNodes = nodeManagerPersistence.getNodes(user);

    List<EngineNode> nodes =
        userNodes.stream()
            .map(Node::getServiceInstance)
            .map(nodeManagerPersistence::getEngineNode)
            .collect(Collectors.toList());

    List<NodeMetrics> nodeMetrics = nodeMetricManagerPersistence.getNodeMetrics(nodes);
    Map<String, NodeMetrics> metricses =
        nodeMetrics.stream()
            .collect(
                Collectors.toMap(
                    m -> m.getServiceInstance().toString(),
                    m -> m,
                    (existingValue, newValue) -> newValue));

    nodes.forEach(
        node -> {
          Optional<NodeMetrics> nodeMetricsOptional =
              Optional.ofNullable(metricses.get(node.getServiceInstance().toString()));
          nodeMetricsOptional.ifPresent(m -> metricsConverter.fillMetricsToNode(node, m));
        });
    return nodes;
  }

  @Retryable(
      value = {feign.RetryableException.class, UndeclaredThrowableException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 10000))
  @Override
  public EngineNode getEngineNodeInfo(EngineNode engineNode) {
    EngineNodePointer engine = nodePointerBuilder.buildEngineNodePointer(engineNode);
    NodeHeartbeatMsg heartMsg = engine.getNodeHeartbeatMsg();
    engineNode.setNodeHealthyInfo(heartMsg.getHealthyInfo());
    engineNode.setNodeOverLoadInfo(heartMsg.getOverLoadInfo());
    engineNode.setNodeStatus(heartMsg.getStatus());
    return engineNode;
  }

  @Override
  public EngineNode getEngineNodeInfoByDB(EngineNode engineNode) {
    EngineNode dbEngineNode = nodeManagerPersistence.getEngineNode(engineNode.getServiceInstance());
    if (null == dbEngineNode) {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE, engineNode + " not exists in db");
    }
    metricsConverter.fillMetricsToNode(
        dbEngineNode, nodeMetricManagerPersistence.getNodeMetrics(dbEngineNode));
    return dbEngineNode;
  }

  @Override
  public EngineNode getEngineNodeInfoByTicketId(String ticketId) {
    EngineNode dbEngineNode = nodeManagerPersistence.getEngineNodeByTicketId(ticketId);
    if (null == dbEngineNode) {
      throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, ticketId + " not exists in db");
    }
    metricsConverter.fillMetricsToNode(
        dbEngineNode, nodeMetricManagerPersistence.getNodeMetrics(dbEngineNode));
    return dbEngineNode;
  }

  @Override
  public void updateEngineStatus(
      ServiceInstance serviceInstance, NodeStatus fromState, NodeStatus toState) {}

  @Override
  public void updateEngine(EngineNode engineNode) {
    nodeManagerPersistence.updateNodeInstance(engineNode);
  }

  @Override
  public EngineNode switchEngine(EngineNode engineNode) {
    return null;
  }

  @Override
  public EngineNode reuseEngine(EngineNode engineNode) {
    EngineNode node = getEngineNodeInfo(engineNode);
    if (node == null || !NodeStatus.isAvailable(node.getNodeStatus())) {
      return null;
    }
    if (!NodeStatus.isLocked(node.getNodeStatus())) {
      Optional<String> lockStr =
          engineLocker.lockEngine(node, (long) AMConfiguration.ENGINE_LOCKER_MAX_TIME.getValue());
      if (!lockStr.isPresent()) {
        throw new LinkisRetryException(
            AMConstant.ENGINE_ERROR_CODE,
            String.format(
                "Failed to request lock from engine by reuse %s", node.getServiceInstance()));
      }
      node.setLock(lockStr.get());
      return node;
    } else {
      return null;
    }
  }

  /**
   * TODO use Engine需要考虑流式引擎的场景，后续需要通过Label加额外的处理
   *
   * @param engineNode
   * @param timeout
   * @return
   */
  @Override
  public EngineNode useEngine(EngineNode engineNode, long timeout) {
    // wait until engine to be available
    EngineNode node = getEngineNodeInfo(engineNode);
    if (node == null || !NodeStatus.isAvailable(node.getNodeStatus())) {
      return null;
    }
    if (!NodeStatus.isLocked(node.getNodeStatus())) {
      Optional<String> lockStr = engineLocker.lockEngine(node, timeout);
      if (!lockStr.isPresent()) {
        throw new LinkisRetryException(
            AMConstant.ENGINE_ERROR_CODE,
            String.format("Failed to request lock from engine %s", node.getServiceInstance()));
      }
      node.setLock(lockStr.get());
      return node;
    } else {
      return null;
    }
  }

  @Override
  public EngineNode useEngine(EngineNode engineNode) {
    return useEngine(engineNode, AMConfiguration.ENGINE_LOCKER_MAX_TIME.getValue());
  }

  /**
   * Get detailed engine information from the persistence
   *
   * @param scoreServiceInstances
   * @return
   */
  @Override
  public EngineNode[] getEngineNodes(ScoreServiceInstance[] scoreServiceInstances) {
    if (scoreServiceInstances == null || scoreServiceInstances.length == 0) {
      return null;
    }
    List<String> instances = new ArrayList<String>();
    List<ScoreServiceInstance> scoreServiceInstancesList = Arrays.asList(scoreServiceInstances);
    EngineNode[] engineNodes =
        scoreServiceInstancesList.stream()
            .map(
                scoreServiceInstance -> {
                  AMEngineNode engineNode = new AMEngineNode();
                  engineNode.setScore(scoreServiceInstance.getScore());
                  engineNode.setServiceInstance(scoreServiceInstance.getServiceInstance());
                  instances.add(scoreServiceInstance.getServiceInstance().getInstance());
                  return engineNode;
                })
            .toArray(EngineNode[]::new);

    List<ServiceInstance> serviceInstancesList =
        scoreServiceInstancesList.stream()
            .map(ScoreServiceInstance::getServiceInstance)
            .collect(Collectors.toList());

    try {
      logger.info("start getEngineNodes.");
      ResourceInfo resourceInfo =
          resourceManager.getResourceInfo(serviceInstancesList.toArray(new ServiceInstance[0]));
      logger.info("end resourceInfo {}", resourceInfo);
      if (serviceInstancesList.isEmpty()) {
        throw new LinkisRetryException(
            AMConstant.ENGINE_ERROR_CODE, "Service instances cannot be empty.");
      }

      List<NodeMetrics> nodeMetrics =
          nodeMetricManagerPersistence.getNodeMetrics(Arrays.asList(engineNodes));
      logger.info(
          "get nodeMetrics, with engineNode size: {}, res size: {}",
          engineNodes.length,
          nodeMetrics.size());
      List<PersistenceNode> persistenceNodes = nodeManagerMapper.getNodesByInstances(instances);
      logger.info(
          "get persistenceNodes, with instance size: {}, res size: {}",
          instances.size(),
          persistenceNodes.size());

      for (EngineNode engineNode : engineNodes) {
        Optional<NodeMetrics> optionMetrics =
            nodeMetrics.stream()
                .filter(
                    nodeMetric ->
                        nodeMetric.getServiceInstance().equals(engineNode.getServiceInstance()))
                .findFirst();

        Optional<RMNode> optionRMNode =
            resourceInfo.resourceInfo().stream()
                .filter(
                    resourceNode ->
                        resourceNode.getServiceInstance().equals(engineNode.getServiceInstance()))
                .findFirst();

        optionMetrics.ifPresent(metrics -> metricsConverter.fillMetricsToNode(engineNode, metrics));
        optionRMNode.ifPresent(rmNode -> engineNode.setNodeResource(rmNode.getNodeResource()));

        persistenceNodes.stream()
            .filter(
                node -> node.getInstance().equals(engineNode.getServiceInstance().getInstance()))
            .findFirst()
            .ifPresent(persistenceNode -> engineNode.setParams(persistenceNode.getParams()));
      }
    } catch (Exception e) {
      LinkisRetryException linkisRetryException =
          new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, "Failed to process data.");
      linkisRetryException.initCause(e);
      throw linkisRetryException;
    }
    logger.info("end getEngineNodes");
    return engineNodes;
  }

  /**
   * add info to persistence
   *
   * @param engineNode
   */
  @Override
  public void addEngineNode(EngineNode engineNode) {
    nodeManagerPersistence.addEngineNode(engineNode);
    // init metric
    nodeMetricManagerPersistence.addOrupdateNodeMetrics(
        metricsConverter.getInitMetric(engineNode.getServiceInstance()));
  }

  /**
   * delete info to persistence
   *
   * @param engineNode
   */
  @Override
  public void deleteEngineNode(EngineNode engineNode) {
    nodeManagerPersistence.deleteEngineNode(engineNode);
  }

  @Override
  public EngineNode getEngineNode(ServiceInstance serviceInstance) {
    return nodeManagerPersistence.getEngineNode(serviceInstance);
  }

  /**
   * 1.serviceInstance中取出instance（实际是ticketId） 2.update serviceInstance 表，包括
   * instance替换，替换mark，owner，updator，creator的空值，更新updateTime 3.update engine_em关联表 4.update label
   * ticket_id ==> instance
   *
   * @param serviceInstance
   * @param engineNode
   */
  @Override
  public void updateEngineNode(ServiceInstance serviceInstance, EngineNode engineNode) {
    nodeManagerPersistence.updateEngineNode(serviceInstance, engineNode);
    nodeMetricManagerPersistence.deleteNodeMetrics(engineNode);

    EngineInstanceLabel engineLabel = labelBuilderFactory.createLabel(EngineInstanceLabel.class);
    engineLabel.setInstance(engineNode.getServiceInstance().getInstance());
    engineLabel.setServiceName(engineNode.getServiceInstance().getApplicationName());

    EngineInstanceLabel oldEngineLabel = labelBuilderFactory.createLabel(EngineInstanceLabel.class);
    oldEngineLabel.setInstance(serviceInstance.getInstance());
    oldEngineLabel.setServiceName(engineNode.getServiceInstance().getApplicationName());
    PersistenceLabel oldPersistenceLabel =
        labelBuilderFactory.convertLabel(oldEngineLabel, PersistenceLabel.class);
    PersistenceLabel label =
        labelManagerPersistence.getLabelByKeyValue(
            oldPersistenceLabel.getLabelKey(), oldPersistenceLabel.getStringValue());

    PersistenceLabel persistenceLabel =
        labelBuilderFactory.convertLabel(engineLabel, PersistenceLabel.class);
    persistenceLabel.setLabelValueSize(persistenceLabel.getValue().size());
    labelManagerPersistence.updateLabel(label.getId(), persistenceLabel);
  }

  public EngineOperateResponse executeOperation(
      EngineNode engineNode, EngineOperateRequest request) {
    EngineNodePointer engine = nodePointerBuilder.buildEngineNodePointer(engineNode);
    return engine.executeOperation(request);
  }

  public EngineNode getEngineNodeInfo(ServiceInstance serviceInstance) {
    EngineNode engineNode = getEngineNode(serviceInstance);
    if (Objects.isNull(engineNode)) {
      throw new AMErrorException(
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorCode(),
          AMErrorCode.NOT_EXISTS_ENGINE_CONN.getErrorDesc());
    }
    NodeMetrics nodeMetric = nodeMetricManagerPersistence.getNodeMetrics(engineNode);
    if (engineNode.getNodeStatus() == null) {
      if (null != nodeMetric && null != nodeMetric.getStatus()) {
        engineNode.setNodeStatus(NodeStatus.values()[nodeMetric.getStatus()]);
      } else {
        engineNode.setNodeStatus(NodeStatus.Starting);
      }
      if (null != nodeMetric && StringUtils.isNotBlank(nodeMetric.getHeartBeatMsg())) {
        engineNode.setEcMetrics(nodeMetric.getHeartBeatMsg());
      }
    }
    return engineNode;
  }
}
