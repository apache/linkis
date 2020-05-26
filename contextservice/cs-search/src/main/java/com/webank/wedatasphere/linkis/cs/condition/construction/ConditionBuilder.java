package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.Condition;

import java.util.Collection;

public interface ConditionBuilder {

    public static ConditionBuilder newBuilder(){
        return new ConditionBuilderImpl();
    }

    ConditionBuilder contextTypes(Collection<ContextType> contextTypes);
    ConditionBuilder contextScopes(Collection<ContextScope> contextScopes);
    ConditionBuilder regex(String regex);
    ConditionBuilder contains(String value);
    Condition build();
}
