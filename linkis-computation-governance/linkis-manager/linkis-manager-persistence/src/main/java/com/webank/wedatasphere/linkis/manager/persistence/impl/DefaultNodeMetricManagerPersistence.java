package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeMetrics;
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;
import com.webank.wedatasphere.linkis.manager.dao.NodeManagerMapper;
import com.webank.wedatasphere.linkis.manager.dao.NodeMetricManagerMapper;
import com.webank.wedatasphere.linkis.manager.exception.PersistenceErrorException;
import com.webank.wedatasphere.linkis.manager.persistence.NodeMetricManagerPersistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DefaultNodeMetricManagerPersistence implements NodeMetricManagerPersistence {

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
    public void addNodeMetrics(NodeMetrics nodeMetrics) throws PersistenceErrorException {
        //直接插入 NodeMetric即可
        PersistenceNodeMetrics persistenceNodeMetrics = new PersistenceNodeMetrics();
        persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
        persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
        persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
        persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());
        persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
        persistenceNodeMetrics.setCreateTime(new Date());
        persistenceNodeMetrics.setUpdateTime(new Date());
        //todo 异常信息后面统一处理
        nodeMetricManagerMapper.addNodeMetrics(persistenceNodeMetrics);
    }

    @Override
    public void addOrupdateNodeMetrics(NodeMetrics nodeMetrics) throws PersistenceErrorException {
        PersistenceNodeMetrics persistenceNodeMetrics = new PersistenceNodeMetrics();
        String instance = nodeMetrics.getServiceInstance().getInstance();
        //todo 异常信息后面统一处理
        int isInstanceIdExist = nodeMetricManagerMapper.checkInstanceExist(instance);
        //是否存在
        if (isInstanceIdExist == 0) {
            persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
            persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
            persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
            persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());
            persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
            persistenceNodeMetrics.setCreateTime(new Date());
            persistenceNodeMetrics.setUpdateTime(new Date());
            //todo 异常信息后面统一处理
            nodeMetricManagerMapper.addNodeMetrics(persistenceNodeMetrics);
        } else if (isInstanceIdExist == 1) {
            persistenceNodeMetrics.setInstance(nodeMetrics.getServiceInstance().getInstance());
            persistenceNodeMetrics.setHealthy(nodeMetrics.getHealthy());
            persistenceNodeMetrics.setHeartBeatMsg(nodeMetrics.getHeartBeatMsg());
            persistenceNodeMetrics.setOverLoad(nodeMetrics.getOverLoad());
            persistenceNodeMetrics.setStatus(nodeMetrics.getStatus());
            persistenceNodeMetrics.setCreateTime(new Date());
            persistenceNodeMetrics.setUpdateTime(new Date());
            nodeMetricManagerMapper.updateNodeMetrics(persistenceNodeMetrics, instance);
        } else {
            //其他情况都不处理，打印个告警日志
        }
    }

    @Override
    public List<NodeMetrics> getNodeMetrics(List<? extends Node> nodes) throws PersistenceErrorException {
        if (nodes == null || nodes.isEmpty()) return Collections.emptyList();
        List<NodeMetrics> nodeMetricsList = new ArrayList<>();
        List<String> instances = new ArrayList<>();
        for (Node node : nodes) {
            String instance = node.getServiceInstance().getInstance();
            instances.add(instance);
        }

        //根据  id 查 metric 信息
        List<PersistenceNodeMetrics> persistenceNodeMetricsList = nodeMetricManagerMapper.getNodeMetricsByInstances(instances);

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
    public NodeMetrics getNodeMetrics(Node node) throws PersistenceErrorException {
        PersistenceNodeMetrics persistenceNodeMetrics = nodeMetricManagerMapper.getNodeMetricsByInstance(node.getServiceInstance().getInstance());
        if (persistenceNodeMetrics == null) return null;
        persistenceNodeMetrics.setServiceInstance(node.getServiceInstance());
        return persistenceNodeMetrics;
    }


    @Override
    public void deleteNodeMetrics(Node node) throws PersistenceErrorException {
        String instance = node.getServiceInstance().getInstance();
        nodeMetricManagerMapper.deleteNodeMetrics(instance);
    }

    @Override
    public List<NodeMetrics> getAllNodeMetrics() throws PersistenceErrorException {
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
