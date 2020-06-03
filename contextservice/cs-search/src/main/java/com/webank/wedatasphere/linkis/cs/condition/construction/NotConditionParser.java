package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;

import java.util.Map;

public class NotConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        Map<Object, Object> origin = (Map<Object, Object>) conditionMap.get("origin");
        return new NotCondition(
            parserMap.get(origin.get("type")).parse(origin)
        );
    }

    @Override
    public String getName() {
        return "Not";
    }
}
