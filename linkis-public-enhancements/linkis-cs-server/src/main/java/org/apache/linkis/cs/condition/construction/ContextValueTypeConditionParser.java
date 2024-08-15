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

import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.impl.ContextValueTypeCondition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.linkis.cs.conf.CSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextValueTypeConditionParser implements ConditionParser {

  private static final Logger logger =
      LoggerFactory.getLogger(ContextValueTypeConditionParser.class);

  @Override
  public Condition parse(Map<Object, Object> conditionMap) {

    Class contextValueType = Object.class;
    try {
      String valueType = (String) conditionMap.get("contextValueType");
      List<String> contextValueTypeWhiteList =
              Arrays.asList(CSConfiguration.CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST.getValue()
                      .split(","));
      if (CSConfiguration.ENABLE_CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST_CHECK.getValue()) {
        if (contextValueTypeWhiteList.stream().anyMatch(ele -> StringUtils.startsWith(valueType, ele))) {
          contextValueType = Class.forName(valueType);
        } else {
          logger.error("ContextValueType: {} is illegal", valueType);
        }
      } else {
        contextValueType = Class.forName(valueType);
      }
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
