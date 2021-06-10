package com.webank.wedatasphere.linkis.manager.common.entity.node;


public interface EngineNode extends AMNode, RMNode, LabelNode {

    EMNode getEMNode();

    void setEMNode(EMNode emNode);


    String getLock();

    void setLock(String lock);




}
