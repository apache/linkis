/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.condition.construction;

import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.impl.NotCondition;

import java.util.Map;

public class NotConditionParser implements ConditionParser {
  @Override
  public Condition parse(Map<Object, Object> conditionMap) {
    Map<Object, Object> origin = (Map<Object, Object>) conditionMap.get("origin");
    return new NotCondition(parserMap.get(origin.get("type")).parse(origin));
  }

  @Override
  public String getName() {
    return "Not";
  }
}
