package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContainsCondition;

public class ContainsContextSearchMatcher extends AbstractContextSearchMatcher {

    String value;

    public ContainsContextSearchMatcher(ContainsCondition condition) {
        super(condition);
        this.value = condition.getValue();
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        if(contextKeyValue.getContextKey().getKeywords() == null){
            return contextKeyValue.getContextKey().getKey().contains(value);
        } else {
            return contextKeyValue.getContextKey().getKey().contains(value)
                    || contextKeyValue.getContextKey().getKeywords().contains(value);
        }

    }
}
