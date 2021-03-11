/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.governance.common.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object GovernanceCommonConf {

  val CONF_FILTER_RM = "wds.linkis.rm"

  val SPARK_ENGINE_VERSION = CommonVars("wds.linkis.spark.engine.version", "2.4.3")

  val HIVE_ENGINE_VERSION = CommonVars("wds.linkis.hive.engine.version", "1.2.1")

  val PYTHON_ENGINE_VERSION = CommonVars("wds.linkis.python.engine.version", "python2")

  val ENGINE_CONN_SPRING_NAME = CommonVars("wds.linkis.engineconn.name", "EngineConn")

  val ENGINE_CONN_MANAGER_SPRING_NAME = CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-engineconnmanager")

  val MANAGER_SPRING_NAME = CommonVars("wds.linkis.engineconn.manager.name", "linkis-cg-linkismanager")

  val ENTRANCE_SPRING_NAME = CommonVars("wds.linkis.entrance.name", "linkis-cg-entrance")


}
