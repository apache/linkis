package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.impl.NearestCondition;

public class NearestLogicContextSearchMatcher extends UnaryLogicContextSearchMatcher{

    public NearestLogicContextSearchMatcher(NearestCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return originalMatcher.match(contextKeyValue);
    }
}
