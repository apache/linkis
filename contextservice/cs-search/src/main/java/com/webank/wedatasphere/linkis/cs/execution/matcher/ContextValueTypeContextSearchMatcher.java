package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextValueTypeCondition;
import org.reflections.ReflectionUtils;

public class ContextValueTypeContextSearchMatcher extends AbstractContextSearchMatcher{

    Class contextValueType;

    public ContextValueTypeContextSearchMatcher(ContextValueTypeCondition condition) {
        super(condition);
        contextValueType = condition.getContextValueType();
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        return contextValueType.isInstance(contextKeyValue.getContextValue().getValue());
    }
}
