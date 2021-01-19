/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.resourcemanager.utils

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}


/**
  * Created by shanhuang on 9/11/18.
  */
object RMConfiguration {

  val RM_MODEL_APPLICATION_NAME = CommonVars("wds.linkis.rm.model.application.name", "ResourceManager")
  //Engine type(引擎类型)：subProcess,subThread,noResource  Here the configuration name and configuration change, temporarily the same(这里配置名和配置代改，暂相同)
  val EVENT_LISTENER_BUS_CLAZZ = CommonVars("wds.linkis.rm.event.listener.bus.clazz", "com.webank.wedatasphere.linkis.resourcemanager.event.notify.NotifyRMEventListenerBus")
  val MODULE_RM_CLAZZ = CommonVars("wds.linkis.rm.module.rm.clazz", "com.webank.wedatasphere.linkis.resourcemanager.service.rm.DefaultModuleResourceManager")
  val USER_RM_CLAZZ = CommonVars("wds.linkis.rm.user.module.rm.clazz", "com.webank.wedatasphere.linkis.resourcemanager.service.rm.DefaultUserResourceManager")

  val NOTIFY_ENGINE_CLAZZ = CommonVars("wds.linkis.rm.event.listener.bus.clazz", "com.webank.wedatasphere.linkis.resourcemanager.notify")
  val SCHEDULER_RM_CLAZZ = CommonVars("wds.linkis.rm.event.listener.bus.clazz", "com.webank.wedatasphere.linkis")

  val RM_WAIT_EVENT_TIME_OUT = CommonVars("wds.linkis.rm.wait.event.time.out", 1000 * 60 * 10L)

  val RM_REGISTER_INTERVAL_TIME = CommonVars("wds.linkis.rm.register.interval.time", 1000 * 60 * 2L)

  //Resource parameter(资源参数)
  val USER_AVAILABLE_CPU = CommonVars("wds.linkis.client.cores.max", 20)
  val USER_AVAILABLE_MEMORY = CommonVars("wds.linkis.client.memory.max", new ByteType("60g"))
  val USER_AVAILABLE_INSTANCE = CommonVars("wds.linkis.instance", 3)

  val USER_AVAILABLE_YARN_INSTANCE_CPU = CommonVars("wds.linkis.yarnqueue.cores.max", 50)
  val USER_AVAILABLE_YARN_INSTANCE_MEMORY = CommonVars("wds.linkis.yarnqueue.memory.max", new ByteType("100g"))
  val USER_AVAILABLE_YARN_INSTANCE = CommonVars("wds.linkis.yarn.instance.max", 3)
  val USER_AVAILABLE_YARN_QUEUE_NAME = CommonVars("wds.linkis.yarnqueue", "ide")
  val USER_AVAILABLE_YARN_QUEUE_ACLS_AUTO = CommonVars("wds.linkis.yarnqueue.acls.auto", false)

  val USER_MODULE_WAIT_USED = CommonVars("wds.linkis.rm.user.module.wait.used", 60 * 10L)
  val USER_MODULE_WAIT_RELEASE = CommonVars("wds.linkis.rm.user.module.wait.used", -1L)


  //Module
  val MODULE_ARRAY_BUFFER = CommonVars("wds.linkis.rm.module.init.array", 1000)
  val USER_ARRAY_BUFFER = CommonVars("wds.linkis.rm.user.init.array", 2000)

  //zk
  val ZOOKEEPER_HOST = CommonVars("com.webank.wedatasphere.linkis.resourcemanager.zookeeper.host", "")
  val ZOOKEEPER_TIMEOUT = CommonVars("com.webank.wedatasphere.linkis.resourcemanager.zookeeper.timeout", 1000 * 60 * 60)

  //
  val RM_COMPLETED_SCAN_INTERVAL = CommonVars("wds.linkis.rm.module.completed.scan.interval", 10000L)
  val RM_ENGINE_SCAN_INTERVAL = CommonVars("wds.linkis.rm.engine.scan.interval", 120000L)
  val RM_ENGINE_RELEASE_THRESHOLD = CommonVars("wds.linkis.rm.engine.release.threshold", 120000L)

  //configuration
  val CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.rm.conf.application.name", "cloud-publicservice")

  //presto
  val USER_AVAILABLE_PRESTO_MEMORY = CommonVars[ByteType]("wds.linkis.presto.max.memory", new ByteType("100GB"))
  val USER_AVAILABLE_PRESTO_INSTANCES = CommonVars[Int]("wds.linkis.presto.max.instances", 20)
}
