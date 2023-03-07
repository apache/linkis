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

import org.apache.linkis.common.conf.{CommonVars, Configuration, TimeType}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.manager.common.entity.enumeration.MaintainType

object AMConfiguration {

  val ECM_ADMIN_OPERATIONS = CommonVars("wds.linkis.governance.admin.operations", "")

  val ENGINE_START_MAX_TIME =
    CommonVars("wds.linkis.manager.am.engine.start.max.time", new TimeType("11m"))

  val ENGINE_CONN_START_REST_MAX_WAIT_TIME =
    CommonVars("wds.linkis.manager.am.engine.rest.start.max.time", new TimeType("40s"))

  val ENGINE_REUSE_MAX_TIME =
    CommonVars("wds.linkis.manager.am.engine.reuse.max.time", new TimeType("5m"))

  val ENGINE_REUSE_COUNT_LIMIT = CommonVars("wds.linkis.manager.am.engine.reuse.count.limit", 2)

  val NODE_STATUS_HEARTBEAT_TIME =
    CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("3m"))

  val NODE_HEARTBEAT_MAX_UPDATE_TIME =
    CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("5m"))

  val DEFAULT_NODE_OWNER = CommonVars("wds.linkis.manager.am.default.node.owner", "hadoop")

  val STOP_ENGINE_WAIT = CommonVars("wds.linkis.manager.am.stop.engine.wait", new TimeType("5m"))

  val STOP_EM_WAIT = CommonVars("wds.linkis.manager.am.stop.em.wait", new TimeType("5m"))

  val EM_LABEL_INIT_WAIT =
    CommonVars("wds.linkis.manager.am.em.label.init.wait", new TimeType("5m"))

  val EM_NEW_WAIT_MILLS = CommonVars("wds.linkis.manager.am.em.new.wait.mills", 1000 * 60L)

  val ENGINECONN_DEBUG_ENABLED = CommonVars("wds.linkis.engineconn.debug.mode.enable", false)

  val MULTI_USER_ENGINE_TYPES = CommonVars(
    "wds.linkis.multi.user.engine.types",
    "jdbc,es,presto,io_file,appconn,openlookeng,trino"
  )

  val ALLOW_BATCH_KILL_ENGINE_TYPES =
    CommonVars("wds.linkis.allow.batch.kill.engine.types", "spark,hive,python")

  val MULTI_USER_ENGINE_USER =
    CommonVars("wds.linkis.multi.user.engine.user", getDefaultMultiEngineUser)

  val ENGINE_LOCKER_MAX_TIME =
    CommonVars("wds.linkis.manager.am.engine.locker.max.time", 1000 * 60 * 5)

  val AM_CAN_RETRY_LOGS =
    CommonVars("wds.linkis.manager.am.can.retry.logs", "already in use;Cannot allocate memory")

  val ASK_ENGINE_ASYNC_MAX_THREAD_SIZE: Int =
    CommonVars("wds.linkis.ecm.launch.max.thread.size", 200).getValue

  val ASYNC_STOP_ENGINE_MAX_THREAD_SIZE: Int =
    CommonVars("wds.linkis.async.stop.engine.size", 20).getValue

  val EC_MAINTAIN_TIME_STR =
    CommonVars("wds.linkis.ec.maintain.time.key", MaintainType.Default.toString)

  val EC_MAINTAIN_WORK_START_TIME =
    CommonVars("wds.linkis.ec.maintain.time.work.start.time", 8).getValue

  val EC_MAINTAIN_WORK_END_TIME =
    CommonVars("wds.linkis.ec.maintain.time.work.end.time", 19).getValue

  val NODE_SELECT_HOTSPOT_EXCLUSION_RULE =
    CommonVars("linkis.node.select.hotspot.exclusion.rule.enable", true).getValue

  private def getDefaultMultiEngineUser(): String = {
    val jvmUser = Utils.getJvmUser
    s""" {jdbc:"$jvmUser", es: "$jvmUser", presto:"$jvmUser", appconn:"$jvmUser", openlookeng:"$jvmUser", trino:"$jvmUser", io_file:"root"}"""
  }

  def isMultiUserEngine(engineType: String): Boolean = {
    val multiUserEngine = AMConfiguration.MULTI_USER_ENGINE_TYPES.getValue.split(",")
    val findResult = multiUserEngine.find(_.equalsIgnoreCase(engineType))
    if (findResult.isDefined) {
      true
    } else {
      false
    }
  }

  def isAllowKilledEngineType(engineType: String): Boolean = {
    val allowBatchKillEngine = AMConfiguration.ALLOW_BATCH_KILL_ENGINE_TYPES.getValue.split(",")
    val findResult = allowBatchKillEngine.find(_.equalsIgnoreCase(engineType))
    if (findResult.isDefined) {
      true
    } else {
      false
    }
  }

}
