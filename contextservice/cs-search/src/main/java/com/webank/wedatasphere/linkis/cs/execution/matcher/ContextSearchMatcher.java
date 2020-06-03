package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;

public interface ContextSearchMatcher {

    Boolean match(ContextKeyValue contextKeyValue);

}
