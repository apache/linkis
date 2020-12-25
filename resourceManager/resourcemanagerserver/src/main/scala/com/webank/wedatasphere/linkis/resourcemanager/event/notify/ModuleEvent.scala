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

package com.webank.wedatasphere.linkis.resourcemanager.event.notify

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState._

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class ModuleEvent extends NotifyRMEvent

class ModuleRegisterEvent(override val eventScope: EventScope.EventScope, val moduleInfo: ModuleInfo) extends ModuleEvent with ModuleInstanceEvent {
  override val moduleName = moduleInfo.moduleInstance.getApplicationName
  override val moduleInstance: ServiceInstance = moduleInfo.moduleInstance

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}

  override def hashCode(): Int = moduleInfo.moduleInstance.hashCode()
}

class ModuleUnregisterEvent(override val eventScope: EventScope.EventScope, override val moduleInstance: ServiceInstance) extends ModuleEvent with ModuleInstanceEvent {
  override val moduleName = moduleInstance.getApplicationName

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}

  override def hashCode(): Int = moduleInstance.hashCode()
}

