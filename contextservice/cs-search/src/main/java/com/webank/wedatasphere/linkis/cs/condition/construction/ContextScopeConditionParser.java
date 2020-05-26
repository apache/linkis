package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextScopeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;

import java.util.Map;

public class ContextScopeConditionParser implements ConditionParser{
    @Override
    public Condition parse(Map<Object, Object> conditionMap) {
        return new ContextScopeCondition(ContextScope.valueOf((String) conditionMap.get("contextScope")));
    }

    @Override
    public String getName() {
        return "ContextScope";
    }
}
