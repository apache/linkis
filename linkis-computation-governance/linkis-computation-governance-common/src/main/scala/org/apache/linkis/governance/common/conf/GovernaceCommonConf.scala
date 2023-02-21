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

package org.apache.linkis.governance.common.conf

import org.apache.linkis.common.conf.{CommonVars, Configuration}

object GovernanceCommonConf {

  val CONF_FILTER_RM = "wds.linkis.rm"

  val SPARK_ENGINE_VERSION = CommonVars("wds.linkis.spark.engine.version", "2.4.3")

  val HIVE_ENGINE_VERSION = CommonVars("wds.linkis.hive.engine.version", "1.2.1")

  val PYTHON_ENGINE_VERSION = CommonVars("wds.linkis.python.engine.version", "python2")

  val PYTHON_CODE_PARSER_SWITCH = CommonVars("wds.linkis.python.code_parser.enabled", false)

  val SCALA_CODE_PARSER_SWITCH = CommonVars("wds.linkis.scala.code_parser.enabled", false)

  val ENGINE_CONN_SPRING_NAME = CommonVars("wds.linkis.engineconn.name", "linkis-cg-engineconn")

  val ENGINE_CONN_MANAGER_SPRING_NAME =
    CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-engineconnmanager")

  val ENGINE_CONN_PORT_RANGE = CommonVars("wds.linkis.engineconn.port.range", "-")

  val MANAGER_SERVICE_NAME =
    CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-linkismanager")

  val ENTRANCE_SERVICE_NAME = CommonVars("wds.linkis.entrance.name", "linkis-cg-entrance")

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.engine.default.limit", 5000)

  val SKIP_PYTHON_PARSER = CommonVars("linkis.code.parser.skip.python", true, "skip python parser")

  val RESULT_SET_STORE_PATH = CommonVars(
    "wds.linkis.resultSet.store.path",
    CommonVars[String]("wds.linkis.filesystem.hdfs.root.path", "hdfs:///tmp/linkis/").getValue
  )

  val ENGINE_CONN_YARN_APP_KILL_SCRIPTS_PATH = CommonVars(
    "wds.linkis.engine.yarn.app.kill.scripts.path",
    Configuration.getLinkisHome + "/sbin/kill-yarn-jobs.sh"
  )

  val ENGINECONN_ENVKEYS = CommonVars("wds.linkis.engineconn.env.keys", "").getValue

  val ERROR_CODE_DESC_LEN =
    CommonVars("linkis.error.code.desc.len", 512, "Error code description maximum length").getValue

  def getEngineEnvValue(envKey: String): String = {
    CommonVars(envKey, "").getValue
  }

}
