package com.webank.wedatasphere.linkis.cs.condition.construction;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextValueTypeCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ContextValueTypeConditionParser implements ConditionParser{

    private static final Logger logger = LoggerFactory.getLogger(ContextValueTypeConditionParser.class);


    @Override
    public Condition parse(Map<Object, Object> conditionMap) {

        Class contextValueType = Object.class;
        try {
            contextValueType = Class.forName((String) conditionMap.get("contextValueType"));
        } catch (ClassNotFoundException e) {
            logger.error("Cannot find contextValueType:" + conditionMap.get("contextValueType"));
        }
        return new ContextValueTypeCondition(contextValueType);
    }

    @Override
    public String getName() {
        return "ContextValueType";
    }
}
