package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class ContextTypeCondition extends AtomicCondition {

    ContextType contextType;

    public ContextTypeCondition(ContextType contextType) {
        this.contextType = contextType;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Equals;
    }
}
