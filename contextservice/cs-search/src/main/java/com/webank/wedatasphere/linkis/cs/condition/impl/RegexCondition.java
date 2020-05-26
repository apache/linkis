package com.webank.wedatasphere.linkis.cs.condition.impl;

import com.webank.wedatasphere.linkis.cs.condition.AtomicCondition;
import com.webank.wedatasphere.linkis.cs.condition.ConditionType;

public class RegexCondition extends AtomicCondition {

    String regex;

    public RegexCondition(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Regex;
    }
}
