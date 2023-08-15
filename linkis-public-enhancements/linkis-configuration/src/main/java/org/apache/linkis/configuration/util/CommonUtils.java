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

import org.apache.linkis.configuration.conf.AcrossClusterConfiguration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    queueRuleMap.put("suffix", AcrossClusterConfiguration.ACROSS_CLUSTER_QUEUE_SUFFIX());
    timeRuleMap.put("startTime", startTime);
    timeRuleMap.put("endTime", endTime);
    thresholdRuleMap.put("CPUThreshold", CPUThreshold);
    thresholdRuleMap.put("MemoryThreshold", MemoryThreshold);
    thresholdRuleMap.put("CPUPercentageThreshold", CPUPercentageThreshold);
    thresholdRuleMap.put("MemoryPercentageThreshold", MemoryPercentageThreshold);
    ruleMap.put("queueRule", queueRuleMap);
    ruleMap.put("timeRule", timeRuleMap);
    ruleMap.put("thresholdRule", thresholdRuleMap);
    ObjectMapper map2Json = new ObjectMapper();
    String rules = map2Json.writeValueAsString(ruleMap);

    return rules;
  }
}
