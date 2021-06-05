package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;

public class InfoRMNode implements RMNode {

    private ServiceInstance serviceInstance;

    private NodeResource nodeResource;

    private String owner;

    private String mark;

    private NodeStatus nodeStatus;


    @Override
    public NodeResource getNodeResource() {
        return nodeResource;
    }

    @Override
    public void setNodeResource(NodeResource nodeResource) {
        this.nodeResource = nodeResource;
    }

    @Override
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    @Override
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @Override
    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    @Override
    public void setNodeStatus(NodeStatus status) {
        this.nodeStatus = status;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getMark() {
        return mark;
    }
}
