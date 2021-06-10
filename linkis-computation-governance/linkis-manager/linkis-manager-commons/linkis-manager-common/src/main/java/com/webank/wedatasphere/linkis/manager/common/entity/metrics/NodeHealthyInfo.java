package com.webank.wedatasphere.linkis.manager.common.entity.metrics;

import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeHealthy;


public class NodeHealthyInfo {

    private NodeHealthy nodeHealthy;

    private String msg;

    public NodeHealthy getNodeHealthy() {
        return nodeHealthy;
    }

    public void setNodeHealthy(NodeHealthy nodeHealthy) {
        this.nodeHealthy = nodeHealthy;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
