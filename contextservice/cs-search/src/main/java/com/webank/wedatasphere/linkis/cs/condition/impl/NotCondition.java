package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;
import com.webank.wedatasphere.linkis.cs.condition.UnaryLogicCondition;

public class NotCondition extends UnaryLogicCondition {
    public NotCondition(Condition origin) {
        super(origin);
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Logic;
    }
}
