package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NearestCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;

import java.util.Map;

public class NearestConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        Map<Object, Object> origin = (Map<Object, Object>) conditionMap.get("origin");
        return new NearestCondition(
            parserMap.get(origin.get("type")).parse(origin),
            conditionMap.get("currentNode").toString(),
            Integer.parseInt(conditionMap.get("number").toString()),
            Boolean.parseBoolean(conditionMap.get("upstreamOnly").toString())
        );
    }

    @Override
    public String getName() {
        return "Nearest";
    }
}
