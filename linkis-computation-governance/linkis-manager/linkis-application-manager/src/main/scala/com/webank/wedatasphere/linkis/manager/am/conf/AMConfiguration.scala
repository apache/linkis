/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.conf

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}


object AMConfiguration {

  val GOVERNANCE_STATION_ADMIN = CommonVars("wds.linkis.governance.station.admin", "hadoop")

  val ENGINE_START_MAX_TIME = CommonVars("wds.linkis.manager.am.engine.start.max.time", new TimeType("10m"))

  val ENGINE_CONN_START_REST_MAX_WAIT_TIME = CommonVars("wds.linkis.manager.am.engine.rest.start.max.time", new TimeType("40s"))

  val ENGINE_REUSE_MAX_TIME = CommonVars("wds.linkis.manager.am.engine.reuse.max.time", new TimeType("5m"))

  val ENGINE_REUSE_COUNT_LIMIT = CommonVars("wds.linkis.manager.am.engine.reuse.count.limit", 10)

  val NODE_STATUS_HEARTBEAT_TIME = CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("3m"))


  val NODE_HEARTBEAT_MAX_UPDATE_TIME = CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("5m"))

  val DEFAULT_NODE_OWNER = CommonVars("wds.linkis.manager.am.default.node.owner", "hadoop")

  val STOP_ENGINE_WAIT = CommonVars("wds.linkis.manager.am.stop.engine.wait", new TimeType("5m"))

  val STOP_EM_WAIT = CommonVars("wds.linkis.manager.am.stop.em.wait", new TimeType("5m"))

  val EM_LABEL_INIT_WAIT = CommonVars("wds.linkis.manager.am.em.label.init.wait", new TimeType("5m"))

  val ENGINECONN_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.engineconn.application.name", "linkis-cg-engineplugin")

  val ENGINECONN_DEBUG_ENABLED = CommonVars("wds.linkis.engineconn.debug.mode.enable", false)

  val MULTI_USER_ENGINE_TYPES = CommonVars("wds.linkis.multi.user.engine.types", "jdbc,es,presto,io_file")

  val MULTI_USER_ENGINE_USER = CommonVars("wds.linkis.multi.user.engine.user", "{jdbc:\"hadoop\", es: \"hadoop\", presto:\"hadoop\",io_file:\"root\"}")

  val MONITOR_SWITCH_ON = CommonVars("wds.linkis.manager.am.monitor.switch.on", true)

  val ENGINE_LOCKER_MAX_TIME = CommonVars("wds.linkis.manager.am.engine.locker.max.time", 1000*60*5)
}
