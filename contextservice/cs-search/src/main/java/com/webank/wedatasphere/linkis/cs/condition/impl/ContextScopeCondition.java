package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class ContextScopeCondition extends AtomicCondition {

    ContextScope contextScop;

    public ContextScopeCondition(ContextScope contextScop) {
        this.contextScop = contextScop;
    }

    public ContextScope getContextScop() {
        return contextScop;
    }

    public void setContextScop(ContextScope contextScop) {
        this.contextScop = contextScop;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Equals;
    }
}
