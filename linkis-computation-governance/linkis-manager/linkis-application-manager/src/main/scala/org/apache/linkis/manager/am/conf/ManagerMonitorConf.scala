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

package org.apache.linkis.manager.am.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

object ManagerMonitorConf {

  val NODE_MAX_CREATE_TIME =
    CommonVars("wds.linkis.manager.am.node.create.time", new TimeType("12m"))

  val NODE_HEARTBEAT_MAX_UPDATE_TIME =
    CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("12m"))

  val ENGINE_KILL_TIMEOUT =
    CommonVars("wds.linkis.manager.am.engine.kill.timeout", new TimeType("2m"))

  val EM_KILL_TIMEOUT = CommonVars("wds.linkis.manager.am.em.kill.timeout", new TimeType("2m"))

  val MANAGER_MONITOR_ASYNC_POLL_SIZE =
    CommonVars("wds.linkis.manager.monitor.async.poll.size", 5)

  val MONITOR_SWITCH_ON = CommonVars("wds.linkis.manager.am.monitor.switch.on", true)

  val ECM_HEARTBEAT_MAX_UPDATE_TIME =
    CommonVars("wds.linkis.manager.am.ecm.heartbeat", new TimeType("5m"))

}
