package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.RegexCondition;

import java.util.Map;

public class RegexConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        return new RegexCondition((String) conditionMap.get("regex"));
    }

    @Override
    public String getName() {
        return "Regex";
    }
}
