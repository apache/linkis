package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;
import com.webank.wedatasphere.linkis.cs.condition.UnaryLogicCondition;

public class NearestCondition extends UnaryLogicCondition {

    String currentNode;
    Integer number;
    Boolean upstreamOnly;

    public NearestCondition(Condition origin, String currentNode, Integer number, Boolean upstreamOnly) {
        super(origin);
        this.currentNode = currentNode;
        this.number = number;
        this.upstreamOnly = upstreamOnly;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean getUpstreamOnly() {
        return upstreamOnly;
    }

    public void setUpstreamOnly(Boolean upstreamOnly) {
        this.upstreamOnly = upstreamOnly;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Logic;
    }
}
