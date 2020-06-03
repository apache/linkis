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

package com.webank.wedatasphere.linkis.resourcemanager.service

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.resourcemanager._
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.NotifyRMEventListenerBus
import com.webank.wedatasphere.linkis.resourcemanager.schedule.{EventSchedulerContextImpl, EventSchedulerImpl, RMEventExecutorManager}
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, ResourceLockService}
import com.webank.wedatasphere.linkis.resourcemanager.utils.{RMConfiguration, RMUtils}
import com.webank.wedatasphere.linkis.scheduler.Scheduler


/**
  * Created by shanhuang on 9/11/18.
  */
class DefaultRMContext extends RMContext with Logging {

  private val moduleResourceManager = RMUtils.getClassInstance[ModuleResourceManager](RMConfiguration.MODULE_RM_CLAZZ.getValue)

  private val userResourceManager = RMUtils.getClassInstance[UserResourceManager](RMConfiguration.USER_RM_CLAZZ.getValue)

  private val notifyRMEventListenerBus = RMUtils.getClassInstance[NotifyRMEventListenerBus](RMConfiguration.EVENT_LISTENER_BUS_CLAZZ.getValue)

  private val scheduler = new EventSchedulerImpl(new EventSchedulerContextImpl(1000))

  private val resourceLockService = new ResourceLockService
  // TODO
  private val moduleResourceRecordService = new ModuleResourceRecordService

  //private val rmListenerBus: RMListenerBus[TListener, Event] = RMListenerBus.getRMListenerBusInstance

  //  private val requestResourceService = Array(new DefaultReqResourceService(userResourceManager, moduleResourceManager),
  //    new YarnReqResourceService(userResourceManager, moduleResourceManager),
  //    new DriverAndYarnReqResourceService(userResourceManager, moduleResourceManager))

  //初始化：
  init()

  def init(): Unit = {
    notifyRMEventListenerBus.addListener(moduleResourceManager)
    notifyRMEventListenerBus.addListener(userResourceManager)
    //add rm listener
    //RMListenerBus.init()
    scheduler.init()
    scheduler.getSchedulerContext.getOrCreateExecutorManager.asInstanceOf[RMEventExecutorManager].setNotifyRMEventListenerBus(notifyRMEventListenerBus)
    shutdown()
  }

  def shutdown(): Unit = {
    Utils.addShutdownHook(scheduler.shutdown)
  }

  //  override def getResourceLockService: ResourceLockService = resourceLockService
  //
  //  override def getModuleResourceManager: ModuleResourceManager = moduleResourceManager
  //
  //  override def getUserResourceManager: UserResourceManager = userResourceManager

  //  override def getRequestResourceServices: Array[RequestResourceService] = requestResourceService

  override def getScheduler: Scheduler = scheduler

  //override def getRMListenerBus: RMListenerBus[TListener, Event] = rmListenerBus

  //  override def getModuleResourceRecordService: ModuleResourceRecordService = moduleResourceRecordService
}

