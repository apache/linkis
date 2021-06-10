package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;


public interface RMNode extends Node {

    NodeResource getNodeResource();

    void setNodeResource(NodeResource nodeResource);
}
