/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.resourcemanager.utils

import org.apache.linkis.common.conf.{ByteType, CommonVars, TimeType}



object RMConfiguration {

  val RM_APPLICATION_NAME = CommonVars("wds.linkis.rm.application.name", "ResourceManager")

  val RM_WAIT_EVENT_TIME_OUT = CommonVars("wds.linkis.rm.wait.event.time.out", 1000 * 60 * 10L)

  val RM_REGISTER_INTERVAL_TIME = CommonVars("wds.linkis.rm.register.interval.time", 1000 * 60 * 2L)

  val NODE_HEARTBEAT_INTERVAL = CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("3m"))
  val NODE_HEARTBEAT_MAX_UPDATE_TIME = CommonVars("wds.linkis.manager.am.node.heartbeat", new TimeType("5m"))
  val LOCK_RELEASE_TIMEOUT = CommonVars("wds.linkis.manager.rm.lock.release.timeout", new TimeType("5m"))
  val LOCK_RELEASE_CHECK_INTERVAL = CommonVars("wds.linkis.manager.rm.lock.release.check.interval", new TimeType("5m"))

  //Resource parameter(资源参数)
  val USER_AVAILABLE_CPU = CommonVars("wds.linkis.rm.client.core.max", 10)
  val USER_AVAILABLE_MEMORY = CommonVars("wds.linkis.rm.client.memory.max", new ByteType("20g"))
  val USER_AVAILABLE_INSTANCE = CommonVars("wds.linkis.rm.instance", 10)

  val USER_AVAILABLE_YARN_INSTANCE_CPU = CommonVars("wds.linkis.rm.yarnqueue.cores.max", 150)
  val USER_AVAILABLE_YARN_INSTANCE_MEMORY = CommonVars("wds.linkis.rm.yarnqueue.memory.max", new ByteType("450g"))
  val USER_AVAILABLE_YARN_INSTANCE = CommonVars("wds.linkis.rm.yarnqueue.instance.max", 30)
  val USER_AVAILABLE_YARN_QUEUE_NAME = CommonVars("wds.linkis.rm.yarnqueue", "default")
  val USER_AVAILABLE_CLUSTER_NAME = CommonVars("wds.linkis.rm.cluster", "sit")

  val USER_MODULE_WAIT_USED = CommonVars("wds.linkis.rm.user.module.wait.used", 60 * 10L)
  val USER_MODULE_WAIT_RELEASE = CommonVars("wds.linkis.rm.user.module.wait.used", -1L)

  //
  val RM_COMPLETED_SCAN_INTERVAL = CommonVars("wds.linkis.rm.module.completed.scan.interval", 10000L)
  val RM_ENGINE_SCAN_INTERVAL = CommonVars("wds.linkis.rm.engine.scan.interval", 120000L)
  val RM_ENGINE_RELEASE_THRESHOLD = CommonVars("wds.linkis.rm.engine.release.threshold", 120000L)

  //configuration

  val ALERT_SUB_SYSTEM_ID = CommonVars("wds.linkis.rm.alert.system.id", "5136")
  val ALERT_DEFAULT_UM = CommonVars("wds.linkis.rm.alert.default.um", "hadoop")
  val ALERT_IMS_URL = CommonVars("wds.linkis.rm.alert.ims.url", "127.0.0.1")
  val ALERT_DUPLICATION_INTERVAL = CommonVars("wds.linkis.rm.alert.duplication.interval", 1200L)
  val ALERT_CONTACT_GROUP = CommonVars("wds.linkis.rm.alert.contact.group", "q01/hadoop,q02/hadoop")
  val ALERT_DEFAULT_CONTACT = CommonVars("wds.linkis.rm.alert.default.contact", "hadoop")
  val ALERT_ENABLED = CommonVars("wds.linkis.rm.alert.enabled", false)

  //publics service
  val HIVE_ENGINE_MAINTAIN_TIME_STR = CommonVars("wds.linkis.hive.maintain.time.key", "wds.linkis.hive.maintain.time")

  val DEFAULT_YARN_CLUSTER_NAME = CommonVars("wds.linkis.rm.default.yarn.cluster.name", "sit")
  val DEFAULT_YARN_TYPE = CommonVars("wds.linkis.rm.default.yarn.cluster.type", "Yarn")
  val EXTERNAL_RETRY_NUM = CommonVars("wds.linkis.rm.external.retry.num", 3)

}
