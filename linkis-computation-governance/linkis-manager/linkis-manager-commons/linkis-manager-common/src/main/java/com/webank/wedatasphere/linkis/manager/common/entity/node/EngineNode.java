package com.webank.wedatasphere.linkis.manager.common.entity.node;

/**
 * @author peacewong
 * @date 2020/6/7 18:49
 */
public interface EngineNode extends AMNode, RMNode, LabelNode {

    EMNode getEMNode();

    void setEMNode(EMNode emNode);


    String getLock();

    void setLock(String lock);




}
