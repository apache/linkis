package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContainsCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.RegexCondition;

import java.util.Map;

public class ContainsConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        return new ContainsCondition((String) conditionMap.get("value"));
    }

    @Override
    public String getName() {
        return "Contains";
    }
}
