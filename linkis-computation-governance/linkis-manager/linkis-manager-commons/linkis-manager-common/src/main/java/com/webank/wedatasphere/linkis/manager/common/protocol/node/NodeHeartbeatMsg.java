package com.webank.wedatasphere.linkis.manager.common.protocol.node;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeOverLoadInfo;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;
import com.webank.wedatasphere.linkis.protocol.AbstractRetryableProtocol;


public class NodeHeartbeatMsg extends AbstractRetryableProtocol implements HeartbeatProtocol {

    private NodeStatus status;

    private NodeHealthyInfo healthyInfo;

    private String heartBeatMsg;

    private NodeOverLoadInfo overLoadInfo;

    private ServiceInstance serviceInstance;

    private NodeResource nodeResource;

    public NodeStatus getStatus() {
        return status;
    }

    public NodeHeartbeatMsg setStatus(NodeStatus status) {
        this.status = status;
        return this;
    }

    public NodeHealthyInfo getHealthyInfo() {
        return healthyInfo;
    }

    public NodeHeartbeatMsg setHealthyInfo(NodeHealthyInfo healthyInfo) {
        this.healthyInfo = healthyInfo;
        return this;
    }

    public String getHeartBeatMsg() {
        return heartBeatMsg;
    }

    public NodeHeartbeatMsg setHeartBeatMsg(String heartBeatMsg) {
        this.heartBeatMsg = heartBeatMsg;
        return this;
    }

    public NodeOverLoadInfo getOverLoadInfo() {
        return overLoadInfo;
    }

    public NodeHeartbeatMsg setOverLoadInfo(NodeOverLoadInfo overLoadInfo) {
        this.overLoadInfo = overLoadInfo;
        return this;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public NodeHeartbeatMsg setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
        return this;
    }

    public NodeResource getNodeResource() {
        return nodeResource;
    }

    public NodeHeartbeatMsg setNodeResource(NodeResource nodeResource) {
        this.nodeResource = nodeResource;
        return this;
    }

    @Override
    public String toString() {
        return "NodeHeartbeatMsg{" +
                "status=" + status +
                ", serviceInstance=" + serviceInstance +
                '}';
    }
}
