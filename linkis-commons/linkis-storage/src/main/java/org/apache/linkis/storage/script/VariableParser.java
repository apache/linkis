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

package org.apache.linkis.storage.script;

import java.util.*;

public class VariableParser {

  public static final String CONFIGURATION = "configuration";
  public static final String VARIABLE = "variable";
  public static final String RUNTIME = "runtime";
  public static final String STARTUP = "startup";
  public static final String SPECIAL = "special";

  public static Variable[] getVariables(Map<String, Object> params) {
    List<Variable> variables = new ArrayList<>();
    Map<String, Object> variableMap =
        (Map<String, Object>) params.getOrDefault(VARIABLE, new HashMap<String, Object>());
    for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
      variables.add(new Variable(VARIABLE, null, entry.getKey(), entry.getValue().toString()));
    }

    Map<String, Object> configurationMap =
        (Map<String, Object>) params.getOrDefault(CONFIGURATION, new HashMap<String, Object>());
    for (Map.Entry<String, Object> entry : configurationMap.entrySet()) {
      Map<String, Object> subMap = (Map<String, Object>) entry.getValue();
      for (Map.Entry<String, Object> subEntry : subMap.entrySet()) {
        if (!isContextIDINFO(subEntry.getKey())) {
          Object value = subEntry.getValue();
          if (value instanceof Map) {
            Map<String, Object> innerMap = (Map<String, Object>) value;
            for (Map.Entry<String, Object> innerEntry : innerMap.entrySet()) {
              if (!isContextIDINFO(innerEntry.getKey())) {
                variables.add(
                    new Variable(
                        entry.getKey(),
                        subEntry.getKey(),
                        innerEntry.getKey(),
                        innerEntry.getValue().toString()));
              }
            }
          } else {
            if (value == null) {
              variables.add(new Variable(CONFIGURATION, entry.getKey(), subEntry.getKey(), ""));
            } else {
              variables.add(
                  new Variable(CONFIGURATION, entry.getKey(), subEntry.getKey(), value.toString()));
            }
          }
        }
      }
    }
    return variables.toArray(new Variable[variables.size()]);
  }

  private static boolean isContextIDINFO(String key) {
    return "contextID".equalsIgnoreCase(key) || "nodeName".equalsIgnoreCase(key);
  }

  public static Map<String, Object> getMap(Variable[] variables) {
    Map<String, String> variableKey2Value = new HashMap<>();
    Map<String, Object> confs = new HashMap<>();

    Arrays.stream(variables)
        .filter(variable -> variable.sort == null)
        .forEach(v -> variableKey2Value.put(v.key, v.value));

    Arrays.stream(variables)
        .filter(variable -> variable.sort != null)
        .forEach(
            v -> {
              switch (v.getSort()) {
                case STARTUP:
                case RUNTIME:
                case SPECIAL:
                  if (!confs.containsKey(v.getSort())) {
                    confs.put(v.getSort(), createMap(v));
                  } else {
                    Map<String, Object> subMap = (Map<String, Object>) confs.get(v.getSort());
                    subMap.put(v.getKey(), v.getValue());
                  }
                  break;
                default:
                  if (!confs.containsKey(v.getSortParent())) {
                    Map<String, Object> subMap = new HashMap<>();
                    subMap.put(v.getSort(), createMap(v));
                    confs.put(v.getSortParent(), subMap);
                  } else {
                    Map<String, Object> subMap = (Map<String, Object>) confs.get(v.getSortParent());
                    if (!subMap.containsKey(v.getSort())) {
                      subMap.put(v.getSort(), createMap(v));
                    } else {
                      Map<String, Object> innerMap = (Map<String, Object>) subMap.get(v.getSort());
                      innerMap.put(v.getKey(), v.getValue());
                    }
                  }
                  break;
              }
            });

    Map<String, Object> params = new HashMap<>();
    if (!variableKey2Value.isEmpty()) {
      params.put(VARIABLE, variableKey2Value);
    }
    if (!confs.isEmpty()) {
      params.put(CONFIGURATION, confs);
    }
    return params;
  }

  private static Map<String, Object> createMap(Variable variable) {
    Map<String, Object> map = new HashMap<>();
    map.put(variable.getKey(), variable.getValue());
    return map;
  }
}
