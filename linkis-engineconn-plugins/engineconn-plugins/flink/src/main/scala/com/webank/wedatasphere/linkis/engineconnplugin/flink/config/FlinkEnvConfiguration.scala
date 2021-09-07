/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.flink.config

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.config.entries.ExecutionEntry



object FlinkEnvConfiguration {

  val FLINK_HOME_ENV = "FLINK_HOME"
  val FLINK_CONF_DIR_ENV = "FLINK_CONF_DIR"
  val FLINK_HOME = CommonVars("flink.home", CommonVars(FLINK_HOME_ENV, "/appcom/Install/flink").getValue)
  val FLINK_CONF_DIR = CommonVars("flink.conf.dir", CommonVars(FLINK_CONF_DIR_ENV, "/appcom/config/flink-config").getValue)
  val FLINK_DIST_JAR_PATH = CommonVars("flink.dist.jar.path", CommonVars(FLINK_HOME_ENV, "/appcom/Install/flink").getValue + "/lib/flink-dist_2.11-1.12.2.jar")
  val FLINK_LIB_REMOTE_PATH = CommonVars("flink.lib.path", "")
  val FLINK_USER_LIB_REMOTE_PATH = CommonVars("flink.user.lib.path", "", "The hdfs lib path of each user in Flink EngineConn.")
  val FLINK_LIB_LOCAL_PATH = CommonVars("flink.local.lib.path", "/appcom/Install/flink/lib", "The local lib path of Flink EngineConn.")
  val FLINK_USER_LIB_LOCAL_PATH = CommonVars("flink.user.local.lib.path", "/appcom/Install/flink/lib", "The local lib path of each user in Flink EngineConn.")
  val FLINK_SHIP_DIRECTORIES = CommonVars("flink.yarn.ship-directories", "")


  val FLINK_SAVE_POINT_PATH = CommonVars("flink.app.savePointPath", "")
  val FLINK_APP_ALLOW_NON_RESTORED_STATUS = CommonVars("flink.app.allowNonRestoredStatus", "false")
  val FLINK_SQL_PLANNER = CommonVars("flink.sql.planner", ExecutionEntry.EXECUTION_PLANNER_VALUE_BLINK)
  val FLINK_SQL_EXECUTION_TYPE = CommonVars("flink.sql.executionType", ExecutionEntry.EXECUTION_TYPE_VALUE_STREAMING)

  val FLINK_SQL_DEV_SELECT_MAX_LINES = CommonVars("flink.dev.sql.select.lines.max", 500)
  val FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME = CommonVars("flink.dev.sql.result.wait.time.max", new TimeType("1m"))

  val FLINK_APPLICATION_ARGS = CommonVars("flink.app.args", "")
  val FLINK_APPLICATION_MAIN_CLASS = CommonVars("flink.app.main.class", "")
  val FLINK_APPLICATION_MAIN_CLASS_JAR = CommonVars("flink.app.main.class.jar", "")

  val FLINK_CLIENT_REQUEST_TIMEOUT = CommonVars("flink.client.request.timeout", new TimeType("30s"))
  val FLINK_ONCE_APP_STATUS_FETCH_INTERVAL = CommonVars("flink.app.fetch.status.interval", new TimeType("5s"))

  val FLINK_REPORTER_ENABLE = CommonVars("linkis.flink.reporter.enable", false)
  val FLINK_REPORTER_CLASS = CommonVars("linkis.flink.reporter.class", "")
  val FLINK_REPORTER_INTERVAL = CommonVars("linkis.flink.reporter.interval", new TimeType("60s"))

}
