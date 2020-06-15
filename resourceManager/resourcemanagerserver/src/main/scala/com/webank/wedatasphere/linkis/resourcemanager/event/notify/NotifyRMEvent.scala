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
import com.webank.wedatasphere.linkis.resourcemanager.event._
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState


/**
  * Created by shanhuang on 9/11/18.
  */
trait NotifyRMEvent extends RMEvent with Scoped {
  def merge(old: NotifyRMEvent): NotifyRMEvent = {
    //Merge this event with old event, return self by default
    this
  }

  override def resume(): Unit = {}
}

trait ModuleInstanceEvent {
  val moduleInstance: ServiceInstance
}

trait TicketIdEvent {
  val ticketId: String
}

trait NotifyLockRMEvent extends NotifyRMEvent

class DefaultNotifyRMEvent(val user: String, override val moduleName: String, override val eventScope: EventScope.EventScope) extends NotifyRMEvent {
  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}

class ModuleLock(override val eventScope: EventScope.EventScope, override val moduleInstance: ServiceInstance) extends NotifyLockRMEvent with ModuleInstanceEvent {
  override val moduleName = moduleInstance.getApplicationName

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}

class UserLock(override val eventScope: EventScope.EventScope, val user: String, override val moduleInstance: ServiceInstance) extends NotifyLockRMEvent with ModuleInstanceEvent {
  override val moduleName = moduleInstance.getApplicationName

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}
