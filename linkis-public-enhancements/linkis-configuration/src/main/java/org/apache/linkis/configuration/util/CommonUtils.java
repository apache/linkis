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
      String crossQueue,
      String priorityCluster,
      String targetCPUThreshold,
      String targetMemoryThreshold,
      String targetCPUPercentageThreshold,
      String targetMemoryPercentageThreshold,
      String originCPUPercentageThreshold,
      String originMemoryPercentageThreshold)
      throws JsonProcessingException {
    Map<String, String> timeRuleMap = new HashMap<>();
    Map<String, String> queueRuleMap = new HashMap<>();
    Map<String, String> targetClusterRuleMap = new HashMap<>();
    Map<String, String> originClusterRuleMap = new HashMap<>();
    Map<String, String> priorityClusterRuleMap = new HashMap<>();
    Map<String, Object> ruleMap = new HashMap<>();
    timeRuleMap.put(KEY_START_TIME, startTime);
    timeRuleMap.put(KEY_END_TIME, endTime);
    queueRuleMap.put(KEY_CROSS_QUEUE, crossQueue);
    targetClusterRuleMap.put(KEY_TARGET_CPU_THRESHOLD, targetCPUThreshold);
    targetClusterRuleMap.put(KEY_TARGET_MEMORY_THRESHOLD, targetMemoryThreshold);
    targetClusterRuleMap.put(KEY_TARGET_CPU_PERCENTAGE_THRESHOLD, targetCPUPercentageThreshold);
    targetClusterRuleMap.put(
        KEY_TARGET_MEMORY_PERCENTAGE_THRESHOLD, targetMemoryPercentageThreshold);
    originClusterRuleMap.put(KEY_ORIGIN_CPU_PERCENTAGE_THRESHOLD, originCPUPercentageThreshold);
    originClusterRuleMap.put(
        KEY_ORIGIN_MEMORY_PERCENTAGE_THRESHOLD, originMemoryPercentageThreshold);
    priorityClusterRuleMap.put(KEY_PRIORITY_CLUSTER, priorityCluster);
    ruleMap.put(KEY_TIME_RULE, timeRuleMap);
    ruleMap.put(KEY_QUEUE_RULE, queueRuleMap);
    ruleMap.put(KEY_TARGET_CLUSTER_RULE, targetClusterRuleMap);
    ruleMap.put(KEY_ORIGIN_CLUSTER_RULE, originClusterRuleMap);
    ruleMap.put(KEY_PRIORITY_CLUSTER_RULE, priorityClusterRuleMap);
    ObjectMapper map2Json = BDPJettyServerHelper.jacksonJson();
    String rules = map2Json.writeValueAsString(ruleMap);

    return rules;
  }

  public static String concatQueue(String crossQueue) {
    return String.format("\"crossQueue\":\"%s\"", crossQueue);
  }
}
