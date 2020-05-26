package com.webank.wedatasphere.linkis.cs.execution.matcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.RegexCondition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexContextSearchMatcher extends AbstractContextSearchMatcher{

    Pattern pattern;

    public RegexContextSearchMatcher(RegexCondition condition) {
        super(condition);
        pattern = Pattern.compile(condition.getRegex());
    }

    @Override
    public Boolean match(ContextKeyValue contextKeyValue) {
        Matcher keyMatcher = pattern.matcher(contextKeyValue.getContextKey().getKey());
        if(contextKeyValue.getContextKey().getKeywords() == null){
            return keyMatcher.find();
        } else {
            Matcher keywordsMatcher = pattern.matcher(contextKeyValue.getContextKey().getKeywords());
            return keyMatcher.find() || keywordsMatcher.find();
        }
    }
}
