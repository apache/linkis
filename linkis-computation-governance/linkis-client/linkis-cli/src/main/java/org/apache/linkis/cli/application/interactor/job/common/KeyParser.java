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

package org.apache.linkis.cli.application.interactor.job.common;

import org.apache.linkis.cli.application.constants.CliKeys;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class KeyParser {

  public static Map<String, Object> removePrefixForKeysInMap(Map<String, Object> map) {
    final String[] PREFIX =
        new String[] {
          CliKeys.JOB_PARAM_CONF,
          CliKeys.JOB_PARAM_RUNTIME,
          CliKeys.JOB_PARAM_VAR,
          CliKeys.JOB_EXEC,
          CliKeys.JOB_SOURCE,
          CliKeys.JOB_LABEL,
          CliKeys.JOB_CONTENT
        };
    for (String prefix : PREFIX) {
      map = removePrefixForKeysInMap(map, prefix);
    }
    return map;
  }

  public static void removePrefixAndPutValToMap(
      Map<String, Object> map, String key, Object value, String prefix) {
    String realKey = getRealKey(key, prefix);
    if (StringUtils.isNotBlank(realKey) && !(value instanceof Map)) {
      map.put(realKey, value);
    }
  }

  private static Map<String, Object> removePrefixForKeysInMap(
      Map<String, Object> map, String prefix) {
    if (map == null) {
      return null;
    }
    Map<String, Object> newMap = new HashMap<>();
    for (String key : map.keySet()) {
      String realKey = getRealKey(key, prefix);
      if (StringUtils.isNotBlank(realKey)) {
        if (StringUtils.startsWith(key, prefix)) {
          newMap.put(realKey, map.get(key));
        } else {
          newMap.put(key, map.get(key));
        }
      }
    }
    return newMap;
  }

  private static String getRealKey(String key, String prefix) {
    String realKey = key;
    if (StringUtils.startsWith(key, prefix)) {
      realKey = StringUtils.substring(key, prefix.length() + 1);
    }
    return realKey;
  }
}
