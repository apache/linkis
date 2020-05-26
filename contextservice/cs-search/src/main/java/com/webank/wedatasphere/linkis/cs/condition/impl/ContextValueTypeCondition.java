package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class ContextValueTypeCondition extends AtomicCondition {

    Class contextValueType;

    public ContextValueTypeCondition(Class contextValueType) {
        this.contextValueType = contextValueType;
    }

    public Class getContextValueType() {
        return contextValueType;
    }

    public void setContextValueType(Class contextValueType) {
        this.contextValueType = contextValueType;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Equals;
    }
}
