package com.webank.wedatasphere.linkis.cs;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.construction.ConditionParser;
import com.webank.wedatasphere.linkis.cs.condition.impl.*;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.exception.ContextSearchFailedException;
import com.webank.wedatasphere.linkis.cs.execution.ConditionExecution;
import com.webank.wedatasphere.linkis.cs.execution.impl.*;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DefaultContextSearch implements ContextSearch {

    private static Logger logger = LoggerFactory.getLogger(DefaultContextSearch.class);

    @Override
    public List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException {
        logger.info("Got search condition: \n" + BDPJettyServerHelper.gson().toJson(conditionMap));
        ConditionParser conditionParser = ConditionParser.parserMap.get(conditionMap.get("type"));
        return search(contextCacheService, contextID, conditionParser.parse(conditionMap));
    }

    @Override
    public List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Condition condition) throws ContextSearchFailedException {
        return getExecution(contextCacheService, contextID, condition).execute();
    }

    private ConditionExecution getExecution(ContextCacheService contextCacheService, ContextID contextID, Condition condition) throws ContextSearchFailedException {
        if(condition instanceof ContextTypeCondition){
            return new ContextTypeConditionExecution((ContextTypeCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof ContextScopeCondition){
            return new ContextScopeConditionExecution((ContextScopeCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof RegexCondition){
            return new RegexConditionExecution((RegexCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof ContainsCondition){
            return new ContainsConditionExecution((ContainsCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof AndCondition){
            return new AndConditionExecution((AndCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof OrCondition){
            return new OrConditionExecution((OrCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof NotCondition){
            return new NotConditionExecution((NotCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof NearestCondition){
            return new NearestConditionExecution((NearestCondition) condition, contextCacheService, contextID);
        } else if(condition instanceof ContextValueTypeCondition){
            return new ContextValueTypeConditionExecution((ContextValueTypeCondition) condition, contextCacheService, contextID);
        }
        throw new ContextSearchFailedException(1200001, "Unknown Condition Type");
    }
}
