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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.dao.NodeMetricManagerMapper;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNodeMetricManagerPersistence implements NodeMetricManagerPersistence {

  private static Logger logger = LoggerFactory.getLogger(DefaultNodeMetricManagerPersistence.class);

  private NodeManagerMapper nodeManagerMapper;

  private NodeMetricManagerMapper nodeMetricManagerMapper;

  public NodeManagerMapper getNodeManagerMapper() {
    return nodeManagerMapper;
  }

  public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
    this.nodeManagerMapper = nodeManagerMapper;
  }

  public NodeMetricManagerMapper getNodeMetricManagerMapper() {
    return nodeMetricManagerMapper;
  }

  public void setNodeMetricManagerMapper(NodeMetricManagerMapper nodeMetricManagerMapper) {
    this.nodeMetricManagerMapper = nodeMetricManagerMapper;
  }

  @Override
  public void addNodeMetrics(NodeMetrics nodeMetrics) {
    // 直接插入 NodeMetric即可
    PersistenceNodeMetrics persistenceNodeMetrics = new PersistenceNodeMetrics();
    persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
    persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
    persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
    persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());
    persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
    persistenceNodeMetrics.setCreateTime(new Date());
    persistenceNodeMetrics.setUpdateTime(new Date());
    // todo 异常信息后面统一处理
    nodeMetricManagerMapper.addNodeMetrics(persistenceNodeMetrics);
  }

  @Override
  public void addOrupdateNodeMetrics(NodeMetrics nodeMetrics) {
    if (null == nodeMetrics.getServiceInstance()) {
      logger.warn(
          "The request of update node metrics was ignored, because the node metrics service instance is null");
      return;
    }
    String instance = nodeMetrics.getServiceInstance().getInstance();
    PersistenceNode node = nodeManagerMapper.getNodeInstance(instance);
    if (node == null) {
      logger.warn(
          "The request of update node metrics was ignored, because the node "
              + instance
              + " is not exist.");
      return;
    }
    int isInstanceIdExist = nodeMetricManagerMapper.checkInstanceExist(instance);
    PersistenceNodeMetrics persistenceNodeMetrics = new PersistenceNodeMetrics();
    if (isInstanceIdExist == 0) {
      persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
      persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
      persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
      persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());
      persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
      persistenceNodeMetrics.setCreateTime(new Date());
      persistenceNodeMetrics.setUpdateTime(new Date());
      nodeMetricManagerMapper.addNodeMetrics(persistenceNodeMetrics);
    } else if (isInstanceIdExist == 1) {
      // ec node metircs report ignore update Shutingdown node (for case: asyn stop engine)
      PersistenceNodeMetrics oldMetrics =
          nodeMetricManagerMapper.getNodeMetricsByInstance(instance);
      boolean isECM =
          nodeMetrics
              .getServiceInstance()
              .getApplicationName()
              .equalsIgnoreCase(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue());
      if (!isECM
          && oldMetrics != null
          && NodeStatus.ShuttingDown.ordinal() <= oldMetrics.getStatus()) {
        logger.info(
            "ignore update ShuttingDown status node:{} to status:{}",
            instance,
            NodeStatus.values()[nodeMetrics.getStatus()].name());
        persistenceNodeMetrics.setStatus(null);
      } else {
        persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
      }
      persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
      persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
      persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
      persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());

      persistenceNodeMetrics.setUpdateTime(new Date());
      nodeMetricManagerMapper.updateNodeMetrics(persistenceNodeMetrics, instance);
    } else {
      // 其他情况都不处理，打印个告警日志
    }
  }

  @Override
  public List<NodeMetrics> getNodeMetrics(List<? extends Node> nodes) {
    if (nodes == null || nodes.isEmpty()) return Collections.emptyList();
    List<NodeMetrics> nodeMetricsList = new ArrayList<>();
    List<String> instances = new ArrayList<>();
    for (Node node : nodes) {
      String instance = node.getServiceInstance().getInstance();
      instances.add(instance);
    }

    // 根据  id 查 metric 信息
    List<PersistenceNodeMetrics> persistenceNodeMetricsList =
        nodeMetricManagerMapper.getNodeMetricsByInstances(instances);

    for (PersistenceNodeMetrics persistenceNodeMetric : persistenceNodeMetricsList) {
      for (Node node : nodes) {
        if (persistenceNodeMetric.getInstance().equals(node.getServiceInstance().getInstance())) {
          persistenceNodeMetric.setServiceInstance(node.getServiceInstance());
          nodeMetricsList.add(persistenceNodeMetric);
        }
      }
    }

    return nodeMetricsList;
  }

  @Override
  public NodeMetrics getNodeMetrics(Node node) {
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance(node.getServiceInstance().getInstance());
    if (persistenceNodeMetrics == null) return null;
    persistenceNodeMetrics.setServiceInstance(node.getServiceInstance());
    return persistenceNodeMetrics;
  }

  @Override
  public void deleteNodeMetrics(Node node) {
    String instance = node.getServiceInstance().getInstance();
    nodeMetricManagerMapper.deleteNodeMetricsByInstance(instance);
  }

  @Override
  public List<NodeMetrics> getAllNodeMetrics() {
    List<PersistenceNodeMetricsEntity> allNodeMetrics = nodeMetricManagerMapper.getAllNodeMetrics();
    List<NodeMetrics> persistenceNodeMetricsList = new ArrayList<>();
    for (PersistenceNodeMetricsEntity persistenceNodeMetricsEntity : allNodeMetrics) {
      PersistenceNodeMetrics persistenceNodeMetrics = new PersistenceNodeMetrics();
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setApplicationName(persistenceNodeMetricsEntity.getName());
      serviceInstance.setInstance(persistenceNodeMetricsEntity.getInstance());
      persistenceNodeMetrics.setServiceInstance(serviceInstance);
      persistenceNodeMetrics.setInstance(persistenceNodeMetricsEntity.getHealthy());
      persistenceNodeMetrics.setHeartBeatMsg(persistenceNodeMetricsEntity.getHeartBeatMsg());
      persistenceNodeMetrics.setOverLoad(persistenceNodeMetricsEntity.getOverLoad());
      persistenceNodeMetrics.setStatus(persistenceNodeMetricsEntity.getStatus());
      persistenceNodeMetrics.setCreateTime(persistenceNodeMetricsEntity.getCreateTime());
      persistenceNodeMetrics.setUpdateTime(persistenceNodeMetricsEntity.getUpdateTime());
      persistenceNodeMetricsList.add(persistenceNodeMetrics);
    }
    return persistenceNodeMetricsList;
  }
}
