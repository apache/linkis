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

package org.apache.linkis.configuration.util;

import org.apache.linkis.server.BDPJettyServerHelper;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.linkis.configuration.conf.AcrossClusterRuleKeys.*;

public class CommonUtils {
  public static boolean ipCheck(String str) {
    if (str != null && !str.isEmpty()) {
      String pattern = ConfigurationConfiguration.IPCHECK;
      if (str.matches(pattern)) {
        return true;
      }
      return false;
    }
    return false;
  }

  public static String ruleMap2String(
      String startTime,
      String endTime,
      String CPUThreshold,
      String MemoryThreshold,
      String CPUPercentageThreshold,
      String MemoryPercentageThreshold)
      throws JsonProcessingException {
    Map<String, String> queueRuleMap = new HashMap<>();
    Map<String, String> timeRuleMap = new HashMap<>();
    Map<String, String> thresholdRuleMap = new HashMap<>();
    Map<String, Object> ruleMap = new HashMap<>();
    queueRuleMap.put(KEY_QUEUE_SUFFIX, KEY_ACROSS_CLUSTER_QUEUE_SUFFIX);
    timeRuleMap.put(KEY_START_TIME, startTime);
    timeRuleMap.put(KEY_END_TIME, endTime);
    thresholdRuleMap.put(KEY_CPU_THRESHOLD, CPUThreshold);
    thresholdRuleMap.put(KEY_MEMORY_THRESHOLD, MemoryThreshold);
    thresholdRuleMap.put(KEY_CPU_PERCENTAGE_THRESHOLD, CPUPercentageThreshold);
    thresholdRuleMap.put(KEY_MEMORY_PERCENTAGE_THRESHOLD, MemoryPercentageThreshold);
    ruleMap.put(KEY_QUEUE_RULE, queueRuleMap);
    ruleMap.put(KEY_TIME_RULE, timeRuleMap);
    ruleMap.put(KEY_THRESHOLD_RULE, thresholdRuleMap);
    ObjectMapper map2Json = BDPJettyServerHelper.jacksonJson();
    String rules = map2Json.writeValueAsString(ruleMap);

    return rules;
  }
}
