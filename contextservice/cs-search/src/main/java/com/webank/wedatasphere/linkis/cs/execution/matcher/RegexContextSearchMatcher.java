/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
