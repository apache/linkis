package com.webank.wedatasphere.linkis.manager.common.entity.node;

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource;

/**
 * @author peacewong
 * @date 2020/6/10 16:17
 */
public interface RMNode extends Node {

    NodeResource getNodeResource();

    void setNodeResource(NodeResource nodeResource);
}
