package com.webank.wedatasphere.linkis.manager.common.protocol.node;

import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;


public class ResponseNodeStatus implements NodeStatusProtocol {

    private NodeStatus nodeStatus;

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
