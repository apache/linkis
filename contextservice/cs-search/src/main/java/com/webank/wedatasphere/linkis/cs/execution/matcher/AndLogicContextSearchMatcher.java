package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.AndCondition;

public class AndLogicContextSearchMatcher extends BinaryLogicContextSearchMatcher{

    public AndLogicContextSearchMatcher(AndCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return leftMatcher.match(contextKeyValue)
                && rightMatcher.match(contextKeyValue);
    }
}
