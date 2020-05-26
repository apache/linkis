package com.webank.wedatasphere.linkis.cs.optimize.cost;

import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContainsCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextValueTypeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.RegexCondition;

import java.util.Map;

public class ConditionCostCalculator {

    static Map<Class, Double> initialCost = Maps.newHashMap();

    static{
        initialCost.put(RegexCondition.class, 100d);
        initialCost.put(ContainsCondition.class, 10d);
        initialCost.put(ContextScope.class, 10d);
        initialCost.put(ContextType.class, 1d);
        initialCost.put(ContextValueTypeCondition.class, 1d);
    }

    public Double calculate(Condition condition){
        if(condition instanceof AtomicCondition){
            return initialCost.get(condition.getClass());
        }
        return 0d;
    }

}
