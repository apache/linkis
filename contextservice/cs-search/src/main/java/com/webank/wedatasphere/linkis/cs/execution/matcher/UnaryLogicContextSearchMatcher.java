package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.UnaryLogicCondition;

public abstract class UnaryLogicContextSearchMatcher extends AbstractContextSearchMatcher{

    ContextSearchMatcher originalMatcher;

    public UnaryLogicContextSearchMatcher(UnaryLogicCondition condition) {
        super(condition);
        originalMatcher = ConditionMatcherResolver.getMatcher(condition.getOrigin());
    }
}
