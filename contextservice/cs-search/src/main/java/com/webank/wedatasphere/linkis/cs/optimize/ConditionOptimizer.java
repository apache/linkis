package com.webank.wedatasphere.linkis.cs.optimize;

import com.webank.wedatasphere.linkis.cs.condition.Condition;

public interface ConditionOptimizer {

    public OptimizedCondition optimize(Condition condition);

}
