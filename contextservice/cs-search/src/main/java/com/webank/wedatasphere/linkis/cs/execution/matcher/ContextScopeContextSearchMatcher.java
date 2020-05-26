package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextScopeCondition;

public class ContextScopeContextSearchMatcher extends AbstractContextSearchMatcher {

    ContextScope contextScope;

    public ContextScopeContextSearchMatcher(ContextScopeCondition condition) {
        super(condition);
        this.contextScope = condition.getContextScop();
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return contextScope.equals(contextKeyValue.getContextKey().getContextScope());
    }
}
