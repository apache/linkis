package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node;

import java.util.Date;


public class PersistenceNodeEntity implements Node {

    private ServiceInstance serviceInstance;
    private String owner;
    private String mark;
    private NodeStatus nodeStatus;

    private Date startTime;

    public Date getStartTime() {
        return startTime;
    }


    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    @Override
    public ServiceInstance getServiceInstance() {
        return this.serviceInstance;
    }

    @Override
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @Override
    public NodeStatus getNodeStatus() {
        return this.nodeStatus;
    }

    @Override
    public void setNodeStatus(NodeStatus status) {
        this.nodeStatus = status;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String getMark() {
        return this.mark;
    }

    public void setOwner(String owner){
        this.owner = owner;
    }
}
