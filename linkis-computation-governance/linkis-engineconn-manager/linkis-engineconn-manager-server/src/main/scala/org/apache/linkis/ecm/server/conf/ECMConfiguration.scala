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

package org.apache.linkis.ecm.server.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}
import org.apache.linkis.common.utils.ByteTimeUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.common.conf.RMConfiguration

import java.io.File
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

object ECMConfiguration {

  // listenerbus
  val ECM_ASYNC_BUS_CAPACITY: Int = CommonVars("wds.linkis.ecm.async.bus.capacity", 500).getValue

  val ECM_ASYNC_BUS_NAME: String =
    CommonVars("wds.linkis.ecm.async.bus.name", "em_async_bus").getValue

  val ECM_ASYNC_BUS_CONSUMER_SIZE: Int =
    CommonVars("wds.linkis.ecm.async.bus.consumer.size", 30).getValue

  val ECM_ASYNC_BUS_THREAD_MAX_FREE_TIME: Long = CommonVars(
    "wds.linkis.ecm.async.bus.max.free.time",
    ByteTimeUtils.timeStringAsMs("2m")
  ).getValue

  val ECM_ASYNC_BUS_WAITTOEMPTY_TIME: Long =
    CommonVars("wds.linkis.ecm.async.bus.waittoempty.time", 5000L).getValue

  // resource
  val ECM_STIMATE_ACTUAL_MEMORY_ENABLE: Boolean =
    CommonVars[Boolean]("linkis.ecm.stimate.actual.memory.enable", false).getValue

  val ECM_MAX_MEMORY_AVAILABLE: Long =
    CommonVars[Long]("wds.linkis.ecm.memory.max", ByteTimeUtils.byteStringAsBytes("100g")).getValue

  val ECM_MAX_CORES_AVAILABLE: Int = CommonVars[Integer]("wds.linkis.ecm.cores.max", 100).getValue

  val ECM_MAX_CREATE_INSTANCES: Int =
    CommonVars[Integer]("wds.linkis.ecm.engineconn.instances.max", 50).getValue

  val ECM_PROTECTED_MEMORY: Long = ByteTimeUtils.byteStringAsBytes(
    CommonVars[String]("wds.linkis.ecm.protected.memory", "10g").getValue
  )

  val ECM_PROTECTED_CPU_LOAD: Double =
    CommonVars[Double]("wds.linkis.ecm.protected.cpu.load", 0.98d).getValue

  val ECM_PROTECTED_CORES: Int =
    CommonVars[Integer]("wds.linkis.ecm.protected.cores.max", 2).getValue

  val ECM_PROTECTED_INSTANCES: Int =
    CommonVars[Integer]("wds.linkis.ecm.protected.engine.instances", 2).getValue

  val ECM_PROTECTED_LOAD_ENABLED: Boolean =
    CommonVars[Boolean]("wds.linkis.ecm.protected.load.enabled", false).getValue

  val MANAGER_SERVICE_NAME: String = GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue

  val ENGINE_CONN_MANAGER_SPRING_NAME: String =
    GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue

  val ECM_HEALTH_REPORT_PERIOD: Long =
    CommonVars("wds.linkis.ecm.health.report.period", 10).getValue

  val ECM_HEALTH_REPORT_DELAY: Long =
    CommonVars("wds.linkis.ecm.health.report.delay", 10).getValue

  val ECM_HOME_DIR: String =
    CommonVars("wds.linkis.ecm.home.dir", this.getClass.getResource("/").getPath).getValue

  val ENGINECONN_ROOT_DIR: String =
    CommonVars("wds.linkis.engineconn.root.dir", s"${ECM_HOME_DIR}engineConnRootDir").getValue

  val ENGINECONN_PUBLIC_DIR: String = CommonVars(
    "wds.linkis.engineconn.public.dir",
    s"$ENGINECONN_ROOT_DIR${File.separator}engineConnPublickDir"
  ).getValue

  val ECM_LAUNCH_MAX_THREAD_SIZE: Int =
    CommonVars("wds.linkis.ecm.launch.max.thread.size", 100).getValue

  /**
   * engineconn创建时间，如果为0，则使用ms中默认的 engineconn created time
   */
  val ENGINECONN_CREATE_DURATION: Duration = Duration(
    CommonVars("wds.linkis.ecm.engineconn.create.duration", 1000 * 60 * 10).getValue,
    TimeUnit.MILLISECONDS
  )

  val WAIT_ENGINECONN_PID =
    CommonVars("wds.linkis.engineconn.wait.callback.pid", new TimeType("3s"))

  val ENGINE_START_ERROR_MSG_MAX_LEN =
    CommonVars("wds.linkis.ecm.engine.start.error.msg.max.len", 500)

  val ECM_PROCESS_SCRIPT_KILL: Boolean =
    CommonVars[Boolean]("wds.linkis.ecm.script.kill.engineconn", true).getValue

  val ECM_YARN_CLUSTER_NAME: String =
    CommonVars(
      "wds.linkis.ecm.yarn.cluster.name",
      RMConfiguration.DEFAULT_YARN_CLUSTER_NAME.getValue
    ).getValue

  val ECM_YARN_CLUSTER_TYPE: String =
    CommonVars(
      "wds.linkis.ecm.yarn.cluster.type",
      RMConfiguration.DEFAULT_YARN_TYPE.getValue
    ).getValue

}
