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

package org.apache.linkis.engineconn.common.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

import org.apache.commons.lang3.StringUtils

import java.io.File

object EngineConnConf {

  val ENGINE_EXECUTIONS = CommonVars(
    "wds.linkis.engine.connector.executions",
    "org.apache.linkis.engineconn.computation.executor.execute.ComputationEngineConnExecution"
  )

  val ENGINE_CONN_HOOKS = CommonVars(
    "wds.linkis.engine.connector.hooks",
    "org.apache.linkis.engineconn.computation.executor.hook.ComputationEngineConnHook"
  )

  val ENGINE_CONN_ONCE_HOOKS = CommonVars(
    "linkis.engine.connector.once.hooks",
    "org.apache.linkis.engineconn.once.executor.hook.OnceEngineConnHook"
  )

  val ENGINE_LAUNCH_CMD_PARAMS_USER_KEY =
    CommonVars("wds.linkis.engine.launch.cmd.params.user.key", "user")

  val ENGINE_SUPPORT_PARALLELISM =
    CommonVars("wds.linkis.engine.parallelism.support.enabled", false)

  val ENGINE_PUSH_LOG_TO_ENTRANCE = CommonVars("wds.linkis.engine.push.log.enable", true)

  val ENGINE_CONN_PLUGIN_CLAZZ = CommonVars(
    "wds.linkis.engineconn.plugin.default.class",
    "org.apache.linkis.engineplugin.hive.HiveEngineConnPlugin"
  )

  val ENGINE_TASK_EXPIRE_TIME = CommonVars("wds.linkis.engine.task.expire.time", 1000 * 3600 * 24)

  val ENGINE_LOCK_REFRESH_TIME = CommonVars("wds.linkis.engine.lock.refresh.time", 1000 * 60 * 3)

  val ENGINE_CONN_LOCAL_PATH_PWD_KEY = CommonVars("wds.linkis.engine.work.home.key", "PWD")

  val ENGINE_CONN_LOCAL_LOG_DIRS_KEY = CommonVars("wds.linkis.engine.logs.dir.key", "LOG_DIRS")

  val ENGINE_CONN_LOCAL_TMP_DIR = CommonVars("wds.linkis.engine.tmp.dir", "TEMP_DIRS")

  val ENGINE_CONN_CREATION_WAIT_TIME =
    CommonVars("wds.linkis.engine.connector.init.time", new TimeType("8m"))

  // spark: Starting|Submitted|Activating.{1,100}(application_\d{13}_\d+)
  // sqoop, importtsv: Submitted application application_1609166102854_970911
  val SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX = CommonVars(
    "wds.linkis.spark.engine.yarn.app.id.parse.regex",
    "(Starting|Started|Submitting|Submitted|Activating|Activated).{1,200}(application_\\d{13}_\\d+)"
  )

  val SQOOP_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX = CommonVars(
    "wds.linkis.sqoop.engine.yarn.app.id.parse.regex",
    "(12|23): {1,200}(application_\\d{13}_\\d+)"
  )

  val HIVE_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX =
    CommonVars("wds.linkis.hive.engine.yarn.app.id.parse.regex", "(application_\\d{13}_\\d+)")

  val SEATUNNEL_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX =
    CommonVars("wds.linkis.seatunnel.engine.yarn.app.id.parse.regex", "(application_\\d{13}_\\d+)")

  def getWorkHome: String = System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue)

  def getEngineTmpDir: String = System.getenv(ENGINE_CONN_LOCAL_TMP_DIR.getValue)

  def getLogDir: String = {
    val logDir = System.getenv(ENGINE_CONN_LOCAL_LOG_DIRS_KEY.getValue)
    if (StringUtils.isNotEmpty(logDir)) logDir else new File(getWorkHome, "logs").getPath
  }

}
