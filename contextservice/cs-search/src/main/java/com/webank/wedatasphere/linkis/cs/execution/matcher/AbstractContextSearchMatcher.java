package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.condition.Condition;

public abstract class AbstractContextSearchMatcher implements ContextSearchMatcher{

    Condition condition;

    public AbstractContextSearchMatcher(Condition condition) {
        this.condition = condition;
    }
}
