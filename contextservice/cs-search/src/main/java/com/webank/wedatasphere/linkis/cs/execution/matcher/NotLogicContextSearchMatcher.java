package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;

public class NotLogicContextSearchMatcher extends UnaryLogicContextSearchMatcher{

    public NotLogicContextSearchMatcher(NotCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return !originalMatcher.match(contextKeyValue);
    }
}
