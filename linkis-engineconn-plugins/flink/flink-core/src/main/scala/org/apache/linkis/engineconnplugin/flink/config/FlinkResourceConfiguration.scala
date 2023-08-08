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

package org.apache.linkis.engineconnplugin.flink.config

import org.apache.linkis.common.conf.CommonVars

object FlinkResourceConfiguration {

  // Unit: M(单位为M)
  val LINKIS_FLINK_CLIENT_MEMORY = CommonVars[Int]("flink.client.memory", 1024)

  // Fixed to 1（固定为1） CommonVars[Int]("wds.linkis.driver.cores", 1)
  val LINKIS_FLINK_CLIENT_CORES = 1

  // Unit: M(单位为M)
  val LINKIS_FLINK_JOB_MANAGER_MEMORY = CommonVars[Int]("flink.jobmanager.memory", 1024)

  // Unit: M(单位为M)
  val LINKIS_FLINK_TASK_MANAGER_MEMORY = CommonVars[Int]("flink.taskmanager.memory", 4096)
  val LINKIS_FLINK_TASK_SLOTS = CommonVars[Int]("flink.taskmanager.numberOfTaskSlots", 2)
  val LINKIS_FLINK_TASK_MANAGER_CPU_CORES = CommonVars[Int]("flink.taskmanager.cpu.cores", 2)
  val LINKIS_FLINK_CONTAINERS = CommonVars[Int]("flink.container.num", 2)
  val LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")

  val FLINK_APP_DEFAULT_PARALLELISM = CommonVars("wds.linkis.engineconn.flink.app.parallelism", 4)

}
