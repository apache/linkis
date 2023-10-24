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
import java.util.Optional;

public class AMConfiguration {

  public static final CommonVars<String> ECM_ADMIN_OPERATIONS =
      CommonVars.apply("wds.linkis.governance.admin.operations", "");

  public static final CommonVars<TimeType> ENGINE_START_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.start.max.time", new TimeType("11m"));

  public static final CommonVars<TimeType> ENGINE_CONN_START_REST_MAX_WAIT_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.rest.start.max.time", new TimeType("40s"));

  public static final CommonVars<TimeType> ENGINE_REUSE_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.max.time", new TimeType("5m"));

  public static final CommonVars<Integer> ENGINE_REUSE_COUNT_LIMIT =
      CommonVars.apply("wds.linkis.manager.am.engine.reuse.count.limit", 2);

  public static final CommonVars<TimeType> NODE_STATUS_HEARTBEAT_TIME =
      CommonVars.apply("wds.linkis.manager.am.node.heartbeat", new TimeType("3m"));

  public static final CommonVars<TimeType> NODE_HEARTBEAT_MAX_UPDATE_TIME =
      CommonVars.apply("wds.linkis.manager.am.node.heartbeat", new TimeType("5m"));

  public static final CommonVars<String> DEFAULT_NODE_OWNER =
      CommonVars.apply("wds.linkis.manager.am.default.node.owner", "hadoop");

  public static final CommonVars<TimeType> STOP_ENGINE_WAIT =
      CommonVars.apply("wds.linkis.manager.am.stop.engine.wait", new TimeType("5m"));

  public static final CommonVars<TimeType> STOP_EM_WAIT =
      CommonVars.apply("wds.linkis.manager.am.stop.em.wait", new TimeType("5m"));

  public static final CommonVars<TimeType> EM_LABEL_INIT_WAIT =
      CommonVars.apply("wds.linkis.manager.am.em.label.init.wait", new TimeType("5m"));

  public static final CommonVars<Long> EM_NEW_WAIT_MILLS =
      CommonVars.apply("wds.linkis.manager.am.em.new.wait.mills", 1000 * 60L);

  public static final CommonVars<Boolean> ENGINECONN_DEBUG_ENABLED =
      CommonVars.apply("wds.linkis.engineconn.debug.mode.enable", false);

  public static final CommonVars<String> MULTI_USER_ENGINE_TYPES =
      CommonVars.apply(
          "wds.linkis.multi.user.engine.types",
          "jdbc,es,presto,io_file,appconn,openlookeng,trino,nebula,hbase");

  public static final CommonVars<String> ALLOW_BATCH_KILL_ENGINE_TYPES =
      CommonVars.apply("wds.linkis.allow.batch.kill.engine.types", "spark,hive,python");

  public static final CommonVars<String> MULTI_USER_ENGINE_USER =
      CommonVars.apply("wds.linkis.multi.user.engine.user", getDefaultMultiEngineUser());

  public static final CommonVars<Integer> ENGINE_LOCKER_MAX_TIME =
      CommonVars.apply("wds.linkis.manager.am.engine.locker.max.time", 1000 * 60 * 5);

  public static final CommonVars<String> AM_CAN_RETRY_LOGS =
      CommonVars.apply(
          "wds.linkis.manager.am.can.retry.logs", "already in use;Cannot allocate memory");

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

  public static String getDefaultMultiEngineUser() {
    String jvmUser = Utils.getJvmUser();
    return String.format(
        "{jdbc:\"%s\", es: \"%s\", presto:\"%s\", appconn:\"%s\", openlookeng:\"%s\", trino:\"%s\", nebula:\"%s\", hbase:\"%s\",io_file:\"root\"}",
        jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser, jvmUser);
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
}
