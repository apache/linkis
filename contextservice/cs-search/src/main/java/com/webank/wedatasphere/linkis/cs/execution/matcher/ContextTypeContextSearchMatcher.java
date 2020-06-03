package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;

public class ContextTypeContextSearchMatcher extends AbstractContextSearchMatcher{

    ContextType contextType;

    public ContextTypeContextSearchMatcher(ContextTypeCondition condition) {
        super(condition);
        contextType = condition.getContextType();
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return contextType.equals(contextKeyValue.getContextKey().getContextType());
    }
}
