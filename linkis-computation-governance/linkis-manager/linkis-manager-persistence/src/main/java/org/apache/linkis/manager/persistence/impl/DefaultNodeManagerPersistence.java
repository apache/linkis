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
import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.AMEngineNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeEntity;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.dao.NodeMetricManagerMapper;
import org.apache.linkis.manager.exception.NodeInstanceDuplicateException;
import org.apache.linkis.manager.exception.NodeInstanceNotFoundException;
import org.apache.linkis.manager.exception.PersistenceErrorException;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;

import org.apache.commons.collections.CollectionUtils;

import org.springframework.dao.DuplicateKeyException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.errorcode.LinkisManagerPersistenceErrorCodeSummary.*;

public class DefaultNodeManagerPersistence implements NodeManagerPersistence {
  private Logger logger = LoggerFactory.getLogger(DefaultNodeManagerPersistence.class);
  private NodeManagerMapper nodeManagerMapper;

  private NodeMetricManagerMapper metricManagerMapper;

  public NodeManagerMapper getNodeManagerMapper() {
    return nodeManagerMapper;
  }

  public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
    this.nodeManagerMapper = nodeManagerMapper;
  }

  public NodeMetricManagerMapper getMetricManagerMapper() {
    return metricManagerMapper;
  }

  public void setMetricManagerMapper(NodeMetricManagerMapper metricManagerMapper) {
    this.metricManagerMapper = metricManagerMapper;
  }

  @Override
  public void addNodeInstance(Node node) throws PersistenceErrorException {
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance(node.getServiceInstance().getInstance());
    persistenceNode.setName(node.getServiceInstance().getApplicationName());
    persistenceNode.setOwner(node.getOwner());
    persistenceNode.setMark(node.getMark());
    persistenceNode.setTicketId(node.getTicketId());
    persistenceNode.setCreateTime(new Date());
    persistenceNode.setUpdateTime(new Date());
    persistenceNode.setCreator(node.getOwner());
    persistenceNode.setUpdator(node.getOwner());

    try {
      nodeManagerMapper.addNodeInstance(persistenceNode);
    } catch (DuplicateKeyException e) {
      NodeInstanceDuplicateException nodeInstanceDuplicateException =
          new NodeInstanceDuplicateException(
              NODE_INSTANCE_ALREADY_EXISTS.getErrorCode(),
              NODE_INSTANCE_ALREADY_EXISTS.getErrorDesc());
      nodeInstanceDuplicateException.initCause(e);
      throw nodeInstanceDuplicateException;
    }
  }

  @Override
  public void updateEngineNode(ServiceInstance serviceInstance, Node node)
      throws PersistenceErrorException, LinkisRetryException {
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance(node.getServiceInstance().getInstance());
    persistenceNode.setName(node.getServiceInstance().getApplicationName());
    persistenceNode.setOwner(node.getOwner());
    persistenceNode.setMark(node.getMark());
    persistenceNode.setUpdateTime(new Date());
    persistenceNode.setCreator(
        node.getOwner()); // The creator is not given when inserting records in rm, so you need to
    // set this value(rm中插入记录的时候并未给出creator，所以需要set这个值)
    persistenceNode.setUpdator(node.getOwner());
    try {
      nodeManagerMapper.updateNodeInstance(serviceInstance.getInstance(), persistenceNode);
      nodeManagerMapper.updateNodeRelation(
          serviceInstance.getInstance(), node.getServiceInstance().getInstance());
      nodeManagerMapper.updateNodeLabelRelation(
          serviceInstance.getInstance(), node.getServiceInstance().getInstance());
    } catch (DuplicateKeyException e) {
      throw new LinkisRetryException(
          41003, "engine instance name is exist, request of created engine will be retry");
    } catch (Exception e) {
      NodeInstanceNotFoundException nodeInstanceNotFoundException =
          new NodeInstanceNotFoundException(
              NODE_INSTANCE_DOES_NOT_EXIST.getErrorCode(),
              NODE_INSTANCE_DOES_NOT_EXIST.getErrorDesc());
      nodeInstanceNotFoundException.initCause(e);
      throw nodeInstanceNotFoundException;
    }
  }

  @Override
  public void removeNodeInstance(Node node) {
    String instance = node.getServiceInstance().getInstance();
    nodeManagerMapper.removeNodeInstance(instance);
  }

  @Override
  public List<Node> getNodes(String owner) {
    List<PersistenceNode> nodeInstances = nodeManagerMapper.getNodeInstancesByOwner(owner);
    List<Node> persistenceNodeEntitys = new ArrayList<>();
    if (!nodeInstances.isEmpty()) {
      for (PersistenceNode persistenceNode : nodeInstances) {
        PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setApplicationName(persistenceNode.getName());
        serviceInstance.setInstance(persistenceNode.getInstance());
        persistenceNodeEntity.setServiceInstance(serviceInstance);
        persistenceNodeEntity.setMark(persistenceNode.getMark());
        persistenceNodeEntity.setOwner(persistenceNode.getOwner());
        persistenceNodeEntity.setStartTime(persistenceNode.getCreateTime());
        persistenceNodeEntity.setIdentifier(persistenceNode.getIdentifier());
        persistenceNodeEntity.setTicketId(persistenceNode.getTicketId());
        persistenceNodeEntitys.add(persistenceNodeEntity);
      }
    }
    return persistenceNodeEntitys;
  }

  @Override
  public List<Node> getAllNodes() {
    List<PersistenceNode> nodeInstances = nodeManagerMapper.getAllNodes();
    List<Node> persistenceNodeEntitys = new ArrayList<>();
    if (!nodeInstances.isEmpty()) {
      for (PersistenceNode persistenceNode : nodeInstances) {
        PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setApplicationName(persistenceNode.getName());
        serviceInstance.setInstance(persistenceNode.getInstance());
        persistenceNodeEntity.setServiceInstance(serviceInstance);
        persistenceNodeEntity.setMark(persistenceNode.getMark());
        persistenceNodeEntity.setIdentifier(persistenceNode.getIdentifier());
        persistenceNodeEntity.setTicketId(persistenceNode.getTicketId());
        persistenceNodeEntity.setOwner(persistenceNode.getOwner());
        persistenceNodeEntity.setStartTime(persistenceNode.getCreateTime());
        persistenceNodeEntity.setUpdateTime(persistenceNode.getUpdateTime());
        persistenceNodeEntitys.add(persistenceNodeEntity);
      }
    }
    return persistenceNodeEntitys;
  }

  @Override
  public void updateNodeInstance(Node node) {

    if (Objects.nonNull(node)) {
      PersistenceNode persistenceNode = new PersistenceNode();
      persistenceNode.setInstance(node.getServiceInstance().getInstance());
      persistenceNode.setName(node.getServiceInstance().getApplicationName());
      persistenceNode.setMark(node.getMark());
      persistenceNode.setUpdateTime(new Date());
      persistenceNode.setCreator(node.getOwner());
      persistenceNode.setUpdator(node.getOwner());
      persistenceNode.setIdentifier(node.getIdentifier());
      nodeManagerMapper.updateNodeInstanceByInstance(persistenceNode);
    }
  }

  @Override
  public Node getNode(ServiceInstance serviceInstance) {
    String instance = serviceInstance.getInstance();
    PersistenceNode nodeInstances = nodeManagerMapper.getNodeInstance(instance);
    if (null == nodeInstances) {
      return null;
    }
    PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
    persistenceNodeEntity.setServiceInstance(serviceInstance);
    persistenceNodeEntity.setOwner(nodeInstances.getOwner());
    persistenceNodeEntity.setMark(nodeInstances.getMark());
    persistenceNodeEntity.setIdentifier(nodeInstances.getIdentifier());
    persistenceNodeEntity.setTicketId(nodeInstances.getTicketId());
    persistenceNodeEntity.setStartTime(nodeInstances.getCreateTime());
    return persistenceNodeEntity;
  }

  @Override
  public void addEngineNode(EngineNode engineNode) throws PersistenceErrorException {
    // insert engine(插入engine)
    addNodeInstance(engineNode);
    // insert relationship,(插入关联关系，)
    String engineNodeInstance = engineNode.getServiceInstance().getInstance();
    if (null == engineNode.getEMNode()) {
      throw new PersistenceErrorException(
          THE_EMNODE_IS_NULL.getErrorCode(),
          MessageFormat.format(THE_EMNODE_IS_NULL.getErrorDesc(), engineNode.getServiceInstance()));
    }
    String emNodeInstance = engineNode.getEMNode().getServiceInstance().getInstance();
    nodeManagerMapper.addEngineNode(engineNodeInstance, emNodeInstance);
  }

  @Override
  public void deleteEngineNode(EngineNode engineNode) {
    String engineNodeInstance = engineNode.getServiceInstance().getInstance();
    if (null != engineNode.getEMNode()) {
      String emNodeInstance = engineNode.getEMNode().getServiceInstance().getInstance();
      // Clean up the relation table between engine and em(清理 engine和em 的关系表)
      nodeManagerMapper.deleteEngineNode(engineNodeInstance, emNodeInstance);
    }
    // 清理 metric信息
    metricManagerMapper.deleteNodeMetricsByInstance(engineNodeInstance);
    // metricManagerMapper.deleteNodeMetrics(emNodeId);
    // 清除 引擎(clear engine)
    nodeManagerMapper.removeNodeInstance(engineNode.getServiceInstance().getInstance());
  }

  @Override
  public EngineNode getEngineNode(ServiceInstance serviceInstance) {
    // The serviceinstance of a given engine finds emNode (给定引擎的 serviceinstance 查到 emNode)
    AMEngineNode amEngineNode = new AMEngineNode();
    amEngineNode.setServiceInstance(serviceInstance);
    PersistenceNode engineNode = nodeManagerMapper.getNodeInstance(serviceInstance.getInstance());
    if (null == engineNode) {
      return null;
    }
    amEngineNode.setOwner(engineNode.getOwner());
    amEngineNode.setMark(engineNode.getMark());
    amEngineNode.setIdentifier(engineNode.getIdentifier());
    amEngineNode.setTicketId(engineNode.getTicketId());
    amEngineNode.setStartTime(engineNode.getCreateTime());
    PersistenceNode emNode =
        nodeManagerMapper.getEMNodeInstanceByEngineNode(serviceInstance.getInstance());
    if (emNode != null) {
      String emInstance = emNode.getInstance();
      String emName = emNode.getName();
      ServiceInstance emServiceInstance = new ServiceInstance();
      emServiceInstance.setApplicationName(emName);
      emServiceInstance.setInstance(emInstance);
      AMEMNode amemNode = new AMEMNode();
      amemNode.setMark(emNode.getMark());
      amemNode.setOwner(emNode.getOwner());
      amemNode.setServiceInstance(emServiceInstance);
      amemNode.setStartTime(emNode.getCreateTime());
      amEngineNode.setEMNode(amemNode);
    }
    return amEngineNode;
  }

  @Override
  public List<EngineNode> getEngineNodeByEM(ServiceInstance serviceInstance) {
    // serviceinstance for a given EM(给定EM的 serviceinstance)
    PersistenceNode emNode = nodeManagerMapper.getNodeInstance(serviceInstance.getInstance());
    if (null == emNode) {
      return new ArrayList<>();
    }
    List<PersistenceNode> engineNodeList =
        nodeManagerMapper.getNodeInstances(serviceInstance.getInstance());
    List<EngineNode> amEngineNodeList = new ArrayList<>();
    for (PersistenceNode engineNode : engineNodeList) {
      AMEMNode amEmNode = new AMEMNode();
      amEmNode.setServiceInstance(serviceInstance);
      amEmNode.setOwner(emNode.getOwner());
      amEmNode.setMark(emNode.getMark());
      amEmNode.setStartTime(emNode.getCreateTime());

      AMEngineNode amEngineNode = new AMEngineNode();
      ServiceInstance engineServiceInstance = new ServiceInstance();
      engineServiceInstance.setInstance(engineNode.getInstance());
      engineServiceInstance.setApplicationName(engineNode.getName());
      amEngineNode.setServiceInstance(engineServiceInstance);
      amEngineNode.setOwner(engineNode.getOwner());
      amEngineNode.setMark(engineNode.getMark());
      amEngineNode.setIdentifier(engineNode.getIdentifier());
      amEngineNode.setTicketId(engineNode.getTicketId());
      amEngineNode.setStartTime(engineNode.getCreateTime());
      amEngineNode.setEMNode(amEmNode);

      amEngineNodeList.add(amEngineNode);
    }
    return amEngineNodeList;
  }

  @Override
  public List<EngineNode> getEngineNodeByServiceInstance(
      List<ServiceInstance> serviceInstanceList) {
    List<EngineNode> amEngineNodeList = new ArrayList<>();
    // Limit database size per query
    List<List<ServiceInstance>> partition = Lists.partition(serviceInstanceList, 100);
    // Batch query
    partition.forEach(
        instanceList -> {
          // Get each batch of ServiceInstances
          List<String> collect =
              instanceList.stream().map(ServiceInstance::getInstance).collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(collect)) {
            // Get engineNodes in batches through ServiceInstance
            List<PersistenceNode> engineNodeList = nodeManagerMapper.getNodesByInstances(collect);
            if (CollectionUtils.isNotEmpty(engineNodeList)) {
              // Assembly data amEngineNodeList
              instanceList.forEach(
                  serviceInstance -> {
                    PersistenceNode engineNode =
                        engineNodeList.stream()
                            .filter(
                                engineNodeInfo ->
                                    engineNodeInfo
                                        .getInstance()
                                        .equals(serviceInstance.getInstance()))
                            .findFirst()
                            .orElse(new PersistenceNode());
                    AMEngineNode amEngineNode = new AMEngineNode();
                    amEngineNode.setServiceInstance(serviceInstance);
                    amEngineNode.setOwner(engineNode.getOwner());
                    amEngineNode.setMark(engineNode.getMark());
                    amEngineNode.setIdentifier(engineNode.getIdentifier());
                    amEngineNode.setTicketId(engineNode.getTicketId());
                    amEngineNode.setStartTime(engineNode.getCreateTime());
                    amEngineNodeList.add(amEngineNode);
                  });
            }
          }
        });
    return amEngineNodeList;
  }

  @Override
  public List<Node> getNodesByOwnerList(List<String> ownerlist) {
    if (CollectionUtils.isEmpty(ownerlist)) {
      return Lists.newArrayList();
    }
    List<PersistenceNode> nodeInstances = nodeManagerMapper.getNodeInstancesByOwnerList(ownerlist);
    List<Node> persistenceNodeEntitys = new ArrayList<>();
    if (!nodeInstances.isEmpty()) {
      for (PersistenceNode persistenceNode : nodeInstances) {
        PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setApplicationName(persistenceNode.getName());
        serviceInstance.setInstance(persistenceNode.getInstance());
        persistenceNodeEntity.setServiceInstance(serviceInstance);
        persistenceNodeEntity.setMark(persistenceNode.getMark());
        persistenceNodeEntity.setIdentifier(persistenceNode.getIdentifier());
        persistenceNodeEntity.setTicketId(persistenceNode.getTicketId());
        persistenceNodeEntity.setOwner(persistenceNode.getOwner());
        persistenceNodeEntity.setStartTime(persistenceNode.getCreateTime());
        persistenceNodeEntitys.add(persistenceNodeEntity);
      }
    }
    return persistenceNodeEntitys;
  }
}
