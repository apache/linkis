package com.webank.wedatasphere.linkis.cs.condition;

import com.webank.wedatasphere.linkis.cs.condition.impl.AndCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;

public abstract class AbstractCommonCondition implements Condition{

    @Override
    public Condition and(Condition right) {
        return new AndCondition(this, right);
    }

    @Override
    public Condition or(Condition right) {
        return new OrCondition(this, right);
    }

    @Override
    public Condition not() {
        return new NotCondition(this);
    }

}
