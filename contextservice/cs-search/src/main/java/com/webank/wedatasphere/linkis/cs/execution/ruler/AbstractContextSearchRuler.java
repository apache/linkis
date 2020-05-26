package com.webank.wedatasphere.linkis.cs.execution.ruler;

import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextSearchMatcher;

public abstract class AbstractContextSearchRuler implements ContextSearchRuler{

    ContextSearchMatcher matcher;

    public AbstractContextSearchRuler(ContextSearchMatcher matcher) {
        this.matcher = matcher;
    }
}
