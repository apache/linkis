package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class AndCondition extends BinaryLogicCondition {

    public AndCondition(Condition left, Condition right) {
        super(left, right);
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Logic;
    }
}
