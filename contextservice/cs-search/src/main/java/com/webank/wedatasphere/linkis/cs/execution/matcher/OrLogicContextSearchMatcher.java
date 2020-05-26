package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;

public class OrLogicContextSearchMatcher extends BinaryLogicContextSearchMatcher{

    public OrLogicContextSearchMatcher(OrCondition condition) {
        super(condition);
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return leftMatcher.match(contextKeyValue)
                || rightMatcher.match(contextKeyValue);
    }
}
