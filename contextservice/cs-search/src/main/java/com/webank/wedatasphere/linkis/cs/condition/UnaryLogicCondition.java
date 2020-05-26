package com.webank.wedatasphere.linkis.cs.condition;

public abstract class UnaryLogicCondition extends AbstractCommonCondition{
    Condition origin;

    public UnaryLogicCondition(Condition origin) {
        this.origin = origin;
    }

    public Condition getOrigin() {
        return origin;
    }

    public void setOrigin(Condition origin) {
        this.origin = origin;
    }
}
