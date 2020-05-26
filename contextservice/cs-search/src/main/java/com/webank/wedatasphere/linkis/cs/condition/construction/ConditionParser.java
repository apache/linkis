package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.cs.condition.Condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConditionParser {

    public static Map<String, ConditionParser> parserMap = new HashMap<String, ConditionParser>(){{
        List<ConditionParser> conditionParsers = Lists.newArrayList(
                new RegexConditionParser(),
                new ContainsConditionParser(),
                new ContextTypeConditionParser(),
                new ContextScopeConditionParser(),
                new AndConditionParser(),
                new OrConditionParser(),
                new NotConditionParser(),
                new NearestConditionParser(),
                new ContextValueTypeConditionParser()
        );
        for(ConditionParser conditionParser : conditionParsers){
            put(conditionParser.getName(), conditionParser);
        }
    }};


    Condition parse(Map<Object, Object> conditionMap);
    String getName();

}
