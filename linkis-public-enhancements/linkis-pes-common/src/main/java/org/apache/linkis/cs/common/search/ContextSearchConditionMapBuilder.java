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

package org.apache.linkis.cs.common.search;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;

import java.util.Map;

import com.google.common.collect.Maps;

public class ContextSearchConditionMapBuilder {

  Map<Object, Object> contextTypeMap;
  Map<Object, Object> contextValueTypeMap;
  Map<Object, Object> contextScopeMap;
  Map<Object, Object> regexMap;
  Map<Object, Object> containsValueMap;
  boolean nearest = false;
  String nearestNode = "";
  Integer nearestNumber = 1;
  Boolean nearestUpstreamOnly = false;

  public static ContextSearchConditionMapBuilder newBuilder() {
    return new ContextSearchConditionMapBuilder();
  }

  public Map<Object, Object> build() {
    Map<Object, Object> conditionMap = null;
    if (contextTypeMap != null) {
      conditionMap = contextTypeMap;
    }
    if (contextValueTypeMap != null) {
      conditionMap =
          conditionMap == null ? contextValueTypeMap : and(conditionMap, contextValueTypeMap);
    }
    if (contextScopeMap != null) {
      conditionMap = conditionMap == null ? contextScopeMap : and(conditionMap, contextScopeMap);
    }
    if (regexMap != null) {
      conditionMap = conditionMap == null ? regexMap : and(conditionMap, regexMap);
    }
    if (containsValueMap != null) {
      conditionMap = conditionMap == null ? containsValueMap : and(conditionMap, containsValueMap);
    }
    if (nearest) {
      conditionMap = nearest(conditionMap, nearestNode, nearestNumber, nearestUpstreamOnly);
    }
    return conditionMap;
  }

  public ContextSearchConditionMapBuilder contextTypes(ContextType... contextTypes) {
    for (ContextType contextType : contextTypes) {
      if (contextTypeMap == null) {
        contextTypeMap = getByContextType(contextType);
      } else {
        contextTypeMap = or(contextTypeMap, getByContextType(contextType));
      }
    }
    return this;
  }

  public ContextSearchConditionMapBuilder contextValueTypes(Class... contextValueTypes) {
    for (Class contextValueType : contextValueTypes) {
      if (contextValueTypeMap == null) {
        contextValueTypeMap = getByContextValueType(contextValueType);
      } else {
        contextValueTypeMap = or(contextValueTypeMap, getByContextValueType(contextValueType));
      }
    }
    return this;
  }

  public ContextSearchConditionMapBuilder contextScopes(ContextScope... contextScopes) {
    for (ContextScope contextScope : contextScopes) {
      if (contextScopeMap == null) {
        contextScopeMap = getByContextScope(contextScope);
      } else {
        contextScopeMap = or(contextScopeMap, getByContextScope(contextScope));
      }
    }
    return this;
  }

  public ContextSearchConditionMapBuilder regex(String regex) {
    regexMap = getByRegex(regex);
    return this;
  }

  public ContextSearchConditionMapBuilder contains(String containsValue) {
    containsValueMap = getByContainsValue(containsValue);
    return this;
  }

  public ContextSearchConditionMapBuilder nearest(
      String currentNode, Integer number, Boolean upstreamOnly) {
    this.nearest = true;
    this.nearestNode = currentNode;
    this.nearestNumber = number;
    this.nearestUpstreamOnly = upstreamOnly;
    return this;
  }

  private Map<Object, Object> getByContextType(ContextType contextType) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "ContextType");
    conditionMap.put("contextType", contextType.toString());
    return conditionMap;
  }

  private Map<Object, Object> getByContextValueType(Class contextValueType) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "ContextValueType");
    conditionMap.put("contextValueType", contextValueType.getName());
    return conditionMap;
  }

  private Map<Object, Object> getByContextScope(ContextScope contextScope) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "ContextScope");
    conditionMap.put("contextScope", contextScope.toString());
    return conditionMap;
  }

  private Map<Object, Object> getByRegex(String regex) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "Regex");
    conditionMap.put("regex", regex);
    return conditionMap;
  }

  private Map<Object, Object> getByContainsValue(String value) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "Contains");
    conditionMap.put("value", value);
    return conditionMap;
  }

  public static Map<Object, Object> nearest(
      Map<Object, Object> origin, String currentNode, Integer number, Boolean upstreamOnly) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "Nearest");
    conditionMap.put("origin", origin);
    conditionMap.put("currentNode", currentNode);
    conditionMap.put("number", number);
    conditionMap.put("upstreamOnly", upstreamOnly);
    return conditionMap;
  }

  public static Map<Object, Object> not(Map<Object, Object> origin) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "Not");
    conditionMap.put("origin", origin);
    return conditionMap;
  }

  public static Map<Object, Object> and(Map<Object, Object> left, Map<Object, Object> right) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "And");
    conditionMap.put("left", left);
    conditionMap.put("right", right);
    return conditionMap;
  }

  public static Map<Object, Object> or(Map<Object, Object> left, Map<Object, Object> right) {
    Map<Object, Object> conditionMap = Maps.newHashMap();
    conditionMap.put("type", "Or");
    conditionMap.put("left", left);
    conditionMap.put("right", right);
    return conditionMap;
  }
}
