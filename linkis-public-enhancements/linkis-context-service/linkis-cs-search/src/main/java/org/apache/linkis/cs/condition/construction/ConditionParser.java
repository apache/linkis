/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.condition.construction;

import com.google.common.collect.Lists;
import org.apache.linkis.cs.condition.Condition;

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
