package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;

public abstract class BinaryLogicContextSearchMatcher extends AbstractContextSearchMatcher{

    ContextSearchMatcher leftMatcher;
    ContextSearchMatcher rightMatcher;

    public BinaryLogicContextSearchMatcher(BinaryLogicCondition condition) {
        super(condition);
        this.leftMatcher = ConditionMatcherResolver.getMatcher(condition.getLeft());
        this.rightMatcher = ConditionMatcherResolver.getMatcher(condition.getRight());
    }

}
