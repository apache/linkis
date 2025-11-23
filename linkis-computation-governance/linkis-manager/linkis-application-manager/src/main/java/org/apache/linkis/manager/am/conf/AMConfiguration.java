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

package org.apache.linkis.manager.am.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.manager.common.entity.enumeration.MaintainType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AMConfiguration {

  // The configuration key for the YARN queue name.
  public static final String YARN_QUEUE_NAME_CONFIG_KEY = "wds.linkis.rm.yarnqueue";

  // Identifier for cross-queue tasks.
  public static final String CROSS_QUEUE = "crossQueue";

  // Identifier for across-cluster tasks.
  public static final String ACROSS_CLUSTER_TASK = "acrossClusterTask";

  // Identifier for priority clusters.
  public static final String PRIORITY_CLUSTER = "priorityCluster";

  // Target identifier for distinguishing target clusters.
  public static final String PRIORITY_CLUSTER_TARGET = "bdp";

  // Origin identifier for distinguishing source clusters.
  public static final String PRIORITY_CLUSTER_ORIGIN = "bdap";

  // Configuration key for the target cluster CPU threshold.
  public static final String TARGET_CPU_THRESHOLD = "targetCPUThreshold";

  // Configuration key for the target cluster memory threshold.
  public static final String TARGET_MEMORY_THRESHOLD = "targetMemoryThreshold";

  // Configuration key for the target cluster CPU percentage threshold.
  public static final String TARGET_CPU_PERCENTAGE_THRESHOLD = "targetCPUPercentageThreshold";

  // Configuration key for the target cluster memory percentage threshold.
  public static final String TARGET_MEMORY_PERCENTAGE_THRESHOLD = "targetMemoryPercentageThreshold";

  // Configuration key for the origin cluster CPU percentage threshold.
  public static final String ORIGIN_CPU_PERCENTAGE_THRESHOLD = "originCPUPercentageThreshold";

  // Configuration key for the origin cluster memory percentage threshold.
  public static final String ORIGIN_MEMORY_PERCENTAGE_THRESHOLD = "originMemoryPercentageThreshold";

  public static final double ACROSS_CLUSTER_TOTAL_MEMORY_PERCENTAGE_THRESHOLD =
      CommonVars.apply("linkis.yarn.across.cluster.memory.threshold", 0.8).getValue();

  public static final double ACROSS_CLUSTER_TOTAL_CPU_PERCENTAGE_THRESHOLD =
      CommonVars.apply("linkis.yarn.across.cluster.cpu.threshold", 0.8).getValue();

  public static final CommonVars<String> ECM_ADMIN_OPERATIONS =
      CommonVars.apply("wds.linkis.governance.admin.operations", "");

  public static final CommonVars<TimeType> ENGINE_START_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.start.max.time", new TimeType("8m"));

  public static final CommonVars<TimeType> ENGINE_CONN_START_REST_MAX_WAIT_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.rest.start.max.time", new TimeType("40s"));

  public static final CommonVars<TimeType> ENGINE_REUSE_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.max.time", new TimeType("5m"));

  public static final Integer ENGINE_REUSE_COUNT_LIMIT =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.count.limit", 2).getValue();

  public static final CommonVars<String> DEFAULT_NODE_OWNER =
      CommonVars.apply("wds.linkis.manager.am.default.node.owner", "hadoop");

  public static final CommonVars<Long> EM_NEW_WAIT_MILLS =
      CommonVars.apply("wds.linkis.manager.am.em.new.wait.mills", 1000 * 60L);

  public static final CommonVars<String> MULTI_USER_ENGINE_TYPES =
      CommonVars.apply(
          "wds.linkis.multi.user.engine.types",
          "es,presto,io_file,appconn,openlookeng,trino,jobserver,nebula,hbase,doris");

  public static final CommonVars<String> ALLOW_BATCH_KILL_ENGINE_TYPES =
      CommonVars.apply("wds.linkis.allow.batch.kill.engine.types", "spark,hive,python");

  public static final CommonVars<String> UNALLOW_BATCH_KILL_ENGINE_TYPES =
      CommonVars.apply(
          "wds.linkis.unallow.batch.kill.engine.types", "trino,appconn,io_file,nebula,jdbc");
  public static final CommonVars<String> MULTI_USER_ENGINE_USER =
      CommonVars.apply("wds.linkis.multi.user.engine.user", getDefaultMultiEngineUser());
  public static final String UDF_KILL_ENGINE_TYPE =
      CommonVars.apply("linkis.udf.kill.engine.type", "spark,hive").getValue();

  public static final CommonVars<Integer> ENGINE_LOCKER_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.locker.max.time", 1000 * 60 * 5);

  public static final String AM_CAN_RETRY_LOGS =
      CommonVars.apply(
              "wds.linkis.manager.am.can.retry.logs", "already in use;Cannot allocate memory")
          .getValue();

  public static final int REUSE_ENGINE_ASYNC_MAX_THREAD_SIZE =
      CommonVars.apply("wds.linkis.manager.reuse.max.thread.size", 200).getValue();

  public static final int CREATE_ENGINE_ASYNC_MAX_THREAD_SIZE =
      CommonVars.apply("wds.linkis.manager.create.max.thread.size", 200).getValue();

  public static final int ASK_ENGINE_ERROR_ASYNC_MAX_THREAD_SIZE =
      CommonVars.apply("wds.linkis.manager.ask.error.max.thread.size", 100).getValue();

  public static final int ASK_ENGINE_ASYNC_MAX_THREAD_SIZE =
      CommonVars.apply("wds.linkis.ecm.launch.max.thread.size", 200).getValue();

  public static final int ASYNC_STOP_ENGINE_MAX_THREAD_SIZE =
      CommonVars.apply("wds.linkis.async.stop.engine.size", 20).getValue();

  public static final CommonVars<String> EC_MAINTAIN_TIME_STR =
      CommonVars.apply("wds.linkis.ec.maintain.time.key", MaintainType.Default.toString());

  public static final int EC_MAINTAIN_WORK_START_TIME =
      CommonVars.apply("wds.linkis.ec.maintain.time.work.start.time", 8).getValue();

  public static final int EC_MAINTAIN_WORK_END_TIME =
      CommonVars.apply("wds.linkis.ec.maintain.time.work.end.time", 19).getValue();

  public static final Boolean NODE_SELECT_HOTSPOT_EXCLUSION_RULE =
      CommonVars.apply("linkis.node.select.hotspot.exclusion.rule.enable", true).getValue();

  public static final CommonVars<String> NODE_SELECT_HOTSPOT_EXCLUSION_SHUFFLE_RULER =
      CommonVars.apply("linkis.node.select.hotspot.exclusion.shuffle.ruler", "size-limit");

  public static final boolean EC_REUSE_WITH_RESOURCE_RULE_ENABLE =
      CommonVars.apply("linkis.ec.reuse.with.resource.rule.enable", false).getValue();

  public static final boolean EC_REUSE_WITH_TEMPLATE_RULE_ENABLE =
      CommonVars.apply("linkis.ec.reuse.with.template.rule.enable", false).getValue();

  public static final String EC_REUSE_WITH_RESOURCE_WITH_ECS =
      CommonVars.apply("linkis.ec.reuse.with.resource.with.ecs", "spark,hive,shell,python")
          .getValue();

  public static final String SUPPORT_CLUSTER_RULE_EC_TYPES =
      CommonVars.apply("linkis.support.cluster.rule.ec.types", "").getValue();

  public static final boolean HIVE_CLUSTER_EC_EXECUTE_ONCE_RULE_ENABLE =
      CommonVars.apply("linkis.hive.cluster.ec.execute.once.rule.enable", true).getValue();

  public static final String LONG_LIVED_LABEL =
      CommonVars.apply("linkis.label.node.long.lived.label.keys", "tenant|yarnCluster").getValue();

  public static final String TMP_LIVED_LABEL =
      CommonVars.apply("linkis.label.node.tmp.lived.label.keys", "taskId").getValue();

  public static final boolean COMBINED_WITHOUT_YARN_DEFAULT =
      CommonVars.apply("linkis.combined.without.yarn.default", true).getValue();

  public static final Map<String, Integer> AM_ENGINE_ASK_MAX_NUMBER = new HashMap<>();

  static {
    String keyValue =
        CommonVars.apply("linkis.am.engine.ask.max.number", "appconn=5,trino=10").getValue();
    String[] keyValuePairs = keyValue.split(",");
    for (String pair : keyValuePairs) {
      String[] array = pair.split("=");
      if (array.length != 2) {
        throw new IllegalArgumentException(
            "linkis.am.engine.ask.max.number value is illegal, value is " + pair);
      } else {
        AM_ENGINE_ASK_MAX_NUMBER.put(array[0], Integer.parseInt(array[1]));
      }
    }
  }

  public static final boolean AM_ECM_RESET_RESOURCE =
      CommonVars.apply("linkis.am.ecm.reset.resource.enable", true).getValue();

  public static final boolean AM_USER_RESET_RESOURCE =
      CommonVars.apply("linkis.am.user.reset.resource.enable", true).getValue();

  public static final CommonVars<Boolean> ENGINE_REUSE_ENABLE_CACHE =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.enable.cache", false);

  public static final CommonVars<TimeType> ENGINE_REUSE_CACHE_EXPIRE_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.cache.expire.time", new TimeType("5s"));

  public static final CommonVars<Long> ENGINE_REUSE_CACHE_MAX_SIZE =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.cache.max.size", 1000L);

  public static final CommonVars<String> ENGINE_REUSE_CACHE_SUPPORT_ENGINES =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.cache.support.engines", "shell");
  public static final CommonVars<String> ENGINE_REUSE_SHUFF_SUPPORT_ENGINES =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.shuff.support.engines", "shell");

  public static String getDefaultMultiEngineUser() {
    String jvmUser = Utils.getJvmUser();
    return String.format(
        "{jdbc:\"%s\", es: \"%s\", presto:\"%s\", appconn:\"%s\", openlookeng:\"%s\", trino:\"%s\", nebula:\"%s\",doris:\"%s\", hbase:\"%s\", jobserver:\"%s\",io_file:\"root\"}",
        jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser);
  }

  public static boolean isMultiUserEngine(String engineType) {
    String[] multiUserEngine = AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue().split(",");
    Optional<String> findResult =
        Arrays.stream(multiUserEngine).filter(e -> e.equalsIgnoreCase(engineType)).findFirst();
    return findResult.isPresent();
  }

  public static boolean isAllowKilledEngineType(String engineType) {
    String[] allowBatchKillEngine =
        AMConfiguration.ALLOW_BATCH_KILL_ENGINE_TYPES.getValue().split(",");
    Optional<String> findResult =
        Arrays.stream(allowBatchKillEngine).filter(e -> e.equalsIgnoreCase(engineType)).findFirst();
    return findResult.isPresent();
  }

  public static boolean isUnAllowKilledEngineType(String engineType) {
    String[] unAllowBatchKillEngine =
        AMConfiguration.UNALLOW_BATCH_KILL_ENGINE_TYPES.getValue().split(",");
    Optional<String> findResult =
        Arrays.stream(unAllowBatchKillEngine)
            .filter(e -> engineType.toLowerCase().contains(e))
            .findFirst();
    return findResult.isPresent();
  }
}
