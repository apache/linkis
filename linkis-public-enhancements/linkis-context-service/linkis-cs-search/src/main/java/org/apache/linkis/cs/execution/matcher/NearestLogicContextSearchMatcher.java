package org.apache.linkis.cs.execution.matcher;

import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.condition.impl.NearestCondition;

public class NearestLogicContextSearchMatcher extends UnaryLogicContextSearchMatcher{

    public NearestLogicContextSearchMatcher(NearestCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return originalMatcher.match(contextKeyValue);
    }
}
