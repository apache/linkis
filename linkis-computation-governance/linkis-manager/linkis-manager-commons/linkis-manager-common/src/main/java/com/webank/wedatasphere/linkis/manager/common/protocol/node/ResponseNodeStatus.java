package com.webank.wedatasphere.linkis.manager.common.protocol.node;

import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;

/**
 * @author peacewong
 * @date 2020/7/16 20:13
 */
public class ResponseNodeStatus implements NodeStatusProtocol {

    private NodeStatus nodeStatus;

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
