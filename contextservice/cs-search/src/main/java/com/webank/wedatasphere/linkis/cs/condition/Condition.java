package com.webank.wedatasphere.linkis.cs.condition;

public interface Condition {

    Condition and(Condition right);
    Condition or(Condition right);
    Condition not();
    ConditionType getConditionType();

}
