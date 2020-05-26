package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class ContainsCondition extends AtomicCondition {
   String value;

    public ContainsCondition(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Contains;
    }
}
