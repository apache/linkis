package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.*;

public class ConditionMatcherResolver {

    public static ContextSearchMatcher getMatcher(Condition condition){
        if(condition == null){
            return new SkipContextSearchMather(condition);
        }
        if(condition instanceof ContextTypeCondition){
            return new ContextTypeContextSearchMatcher((ContextTypeCondition) condition);
        } else if(condition instanceof ContextScopeCondition){
            return new ContextScopeContextSearchMatcher((ContextScopeCondition) condition);
        } else if(condition instanceof RegexCondition){
            return new RegexContextSearchMatcher((RegexCondition) condition);
        } else if(condition instanceof ContainsCondition){
            return new ContainsContextSearchMatcher((ContainsCondition) condition);
        } else if(condition instanceof AndCondition){
            return new AndLogicContextSearchMatcher((AndCondition) condition);
        } else if(condition instanceof OrCondition){
            return new OrLogicContextSearchMatcher((OrCondition) condition);
        }else if(condition instanceof NearestCondition){
            return new NearestLogicContextSearchMatcher((NearestCondition) condition);
        }else if(condition instanceof ContextValueTypeCondition){
            return new ContextValueTypeContextSearchMatcher((ContextValueTypeCondition) condition);
        }
        return new SkipContextSearchMather(condition);
    }
}
