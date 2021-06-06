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

package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMEMNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMEngineNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNode;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeEntity;
import com.webank.wedatasphere.linkis.manager.dao.NodeManagerMapper;
import com.webank.wedatasphere.linkis.manager.dao.NodeMetricManagerMapper;
import com.webank.wedatasphere.linkis.manager.exception.NodeInstanceDuplicateException;
import com.webank.wedatasphere.linkis.manager.exception.NodeInstanceNotFoundException;
import com.webank.wedatasphere.linkis.manager.exception.PersistenceErrorException;
import com.webank.wedatasphere.linkis.manager.persistence.NodeManagerPersistence;
import org.springframework.dao.DuplicateKeyException;
import scala.runtime.AbstractFunction0;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultNodeManagerPersistence implements NodeManagerPersistence {

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
        persistenceNode.setCreateTime(new Date());
        persistenceNode.setUpdateTime(new Date());
        persistenceNode.setCreator(node.getOwner());
        persistenceNode.setUpdator(node.getOwner());

        Utils.tryCatch(new AbstractFunction0<Object>() {
            @Override
            public Object apply() {
                nodeManagerMapper.addNodeInstance(persistenceNode);
                return null;
            }
        },Utils.JFunction1(v1->{
            NodeInstanceDuplicateException nodeInstanceDuplicateException = new NodeInstanceDuplicateException(41001, "Node实例已存在");
            nodeInstanceDuplicateException.initCause(v1);
            throw nodeInstanceDuplicateException;
        }));

    }

    @Override
    public void updateEngineNode(ServiceInstance serviceInstance, Node node) throws PersistenceErrorException {
        PersistenceNode persistenceNode = new PersistenceNode();
        persistenceNode.setInstance(node.getServiceInstance().getInstance());
        persistenceNode.setName(node.getServiceInstance().getApplicationName());
        persistenceNode.setOwner(node.getOwner());
        persistenceNode.setMark(node.getMark());
        persistenceNode.setUpdateTime(new Date());
        persistenceNode.setCreator(node.getOwner());//rm中插入记录的时候并未给出creator，所以需要set这个值
        persistenceNode.setUpdator(node.getOwner());
        Utils.tryCatch(new AbstractFunction0<Object>() {
            @Override
            public Object apply() {
                nodeManagerMapper.updateNodeInstance(serviceInstance.getInstance(), persistenceNode);
                nodeManagerMapper.updateNodeRelation(serviceInstance.getInstance(),node.getServiceInstance().getInstance());
                nodeManagerMapper.updateNodeLabelRelation(serviceInstance.getInstance(),node.getServiceInstance().getInstance());
                return null;
            }
        },Utils.JFunction1(v1->{
            NodeInstanceNotFoundException nodeInstanceNotFoundException = new NodeInstanceNotFoundException(41002, "Node实例不存在");
            nodeInstanceNotFoundException.initCause(v1);
            throw nodeInstanceNotFoundException;
        }));
    }

    @Override
    public void removeNodeInstance(Node node) throws PersistenceErrorException {
        String instance = node.getServiceInstance().getInstance();
        Utils.tryCatch(new AbstractFunction0<Object>() {
            @Override
            public Object apply() {
                nodeManagerMapper.removeNodeInstance(instance);
                return null;
            }
        },Utils.JFunction1(v1->{
            NodeInstanceNotFoundException nodeInstanceNotFoundException = new NodeInstanceNotFoundException(41002, "Node实例不存在");
            nodeInstanceNotFoundException.initCause(v1);
            throw nodeInstanceNotFoundException;
        }));

    }

    @Override
    public List<Node> getNodes(String owner) throws PersistenceErrorException {
        List<PersistenceNode> nodeInstances = nodeManagerMapper.getNodeInstancesByOwner(owner);
        List<Node> persistenceNodeEntitys = new ArrayList<>();
        if (!nodeInstances.isEmpty()){
            for (PersistenceNode persistenceNode:nodeInstances){
                PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
                ServiceInstance serviceInstance = new ServiceInstance();
                serviceInstance.setApplicationName(persistenceNode.getName());
                serviceInstance.setInstance(persistenceNode.getInstance());
                persistenceNodeEntity.setServiceInstance(serviceInstance);
                persistenceNodeEntity.setMark(persistenceNode.getMark());
                persistenceNodeEntity.setOwner(persistenceNode.getOwner());
                persistenceNodeEntity.setStartTime(persistenceNode.getCreateTime());
                persistenceNodeEntitys.add(persistenceNodeEntity);
            }
        }
        return persistenceNodeEntitys;
    }

    @Override
    public List<Node> getAllNodes() throws PersistenceErrorException {
        List<PersistenceNode> nodeInstances = nodeManagerMapper.getAllNodes();
        List<Node> persistenceNodeEntitys = new ArrayList<>();
        if (!nodeInstances.isEmpty()){
            for (PersistenceNode persistenceNode:nodeInstances){
                PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
                ServiceInstance serviceInstance = new ServiceInstance();
                serviceInstance.setApplicationName(persistenceNode.getName());
                serviceInstance.setInstance(persistenceNode.getInstance());
                persistenceNodeEntity.setServiceInstance(serviceInstance);
                persistenceNodeEntity.setMark(persistenceNode.getMark());
                persistenceNodeEntity.setOwner(persistenceNode.getOwner());
                persistenceNodeEntity.setStartTime(persistenceNode.getCreateTime());
                persistenceNodeEntitys.add(persistenceNodeEntity);
            }
        }
        return persistenceNodeEntitys;
    }

    @Override
    public void updateNodeInstance(Node node) throws PersistenceErrorException {

        if (null != node) {
            PersistenceNode persistenceNode = new PersistenceNode();
            persistenceNode.setInstance(node.getServiceInstance().getInstance());
            persistenceNode.setName(node.getServiceInstance().getApplicationName());
            persistenceNode.setOwner(node.getOwner());
            persistenceNode.setMark(node.getMark());
            persistenceNode.setCreateTime(new Date());
            persistenceNode.setUpdateTime(new Date());
            persistenceNode.setCreator(node.getOwner());
            persistenceNode.setUpdator(node.getOwner());
            nodeManagerMapper.updateNodeInstanceOverload(persistenceNode);
        }
    }

    @Override
    public Node getNode(ServiceInstance serviceInstance) throws PersistenceErrorException {
        String instance = serviceInstance.getInstance();
        PersistenceNode nodeInstances = nodeManagerMapper.getNodeInstance(instance);
        PersistenceNodeEntity persistenceNodeEntity = new PersistenceNodeEntity();
        persistenceNodeEntity.setServiceInstance(serviceInstance);
        persistenceNodeEntity.setOwner(nodeInstances.getOwner());
        persistenceNodeEntity.setMark(nodeInstances.getMark());
        persistenceNodeEntity.setStartTime(nodeInstances.getCreateTime());
        return persistenceNodeEntity;
    }

    @Override
    public void addEngineNode(EngineNode engineNode) throws PersistenceErrorException {
        //插入engine
        addNodeInstance(engineNode);
        //插入关联关系，todo 异常后续统一处理
        String engineNodeInstance = engineNode.getServiceInstance().getInstance();
        String emNodeInstance = engineNode.getEMNode().getServiceInstance().getInstance();
        nodeManagerMapper.addEngineNode(engineNodeInstance,emNodeInstance);
    }

    @Override
    public void deleteEngineNode(EngineNode engineNode) throws PersistenceErrorException {
        String engineNodeInstance = engineNode.getServiceInstance().getInstance();
        String emNodeInstance = engineNode.getEMNode().getServiceInstance().getInstance();
        //清理 engine和em 的关系表
        nodeManagerMapper.deleteEngineNode(engineNodeInstance,emNodeInstance);
        //清理 metric信息
        metricManagerMapper.deleteNodeMetricsByInstance(engineNodeInstance);
        //metricManagerMapper.deleteNodeMetrics(emNodeId);
        //清除 引擎
        nodeManagerMapper.removeNodeInstance(engineNode.getServiceInstance().getInstance());
    }

    @Override
    public EngineNode getEngineNode(ServiceInstance serviceInstance) throws PersistenceErrorException {
        //给定引擎的 serviceinstance 查到 emNode
        PersistenceNode emNode = nodeManagerMapper.getEMNodeInstanceByEngineNode(serviceInstance.getInstance());
        if (emNode == null) return null;
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

        AMEngineNode amEngineNode = new AMEngineNode();
        amEngineNode.setServiceInstance(serviceInstance);
        PersistenceNode engineNode = nodeManagerMapper.getNodeInstance(serviceInstance.getInstance());
        amEngineNode.setOwner(engineNode.getOwner());
        amEngineNode.setMark(engineNode.getMark());
        amEngineNode.setStartTime(engineNode.getCreateTime());
        amEngineNode.setEMNode(amemNode);
        return amEngineNode;
    }

    @Override
    public List<EngineNode> getEngineNodeByEM(ServiceInstance serviceInstance) throws PersistenceErrorException {
        //给定EM的 serviceinstance
         PersistenceNode emNode = nodeManagerMapper.getNodeInstance(serviceInstance.getInstance());

        List<PersistenceNode> engineNodeList = nodeManagerMapper.getNodeInstances(serviceInstance.getInstance());
        List<EngineNode> amEngineNodeList =new ArrayList<>();
        for (PersistenceNode engineNode : engineNodeList){
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
            amEngineNode.setStartTime(engineNode.getCreateTime());
            amEngineNode.setEMNode(amEmNode);

            amEngineNodeList.add(amEngineNode);
        }
        return amEngineNodeList;
    }
}
