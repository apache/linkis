package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.AndCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;

import java.util.Map;

public class OrConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        Map<Object, Object> left = (Map<Object, Object>) conditionMap.get("left");
        Map<Object, Object> right = (Map<Object, Object>) conditionMap.get("right");
        return new OrCondition(
            parserMap.get(left.get("type")).parse(left),
            parserMap.get(right.get("type")).parse(right)
        );
    }

    @Override
    public String getName() {
        return "Or";
    }
}
