package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContainsCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;

import java.util.Map;

public class ContextTypeConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        return new ContextTypeCondition(ContextType.valueOf((String) conditionMap.get("contextType")));
    }

    @Override
    public String getName() {
        return "ContextType";
    }
}
