package com.apache.wedatasphere.linkis.cs.execution.matcher;

import com.apache.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.apache.wedatasphere.linkis.cs.condition.impl.NearestCondition;

public class NearestLogicContextSearchMatcher extends UnaryLogicContextSearchMatcher{

    public NearestLogicContextSearchMatcher(NearestCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return originalMatcher.match(contextKeyValue);
    }
}
