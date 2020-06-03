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
import com.webank.wedatasphere.linkis.resourcemanager.domain.{UserPreUsedResource, UserReleasedResource, UserUsedResource}
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState._

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class UserEvent extends NotifyRMEvent {
  val user: String

  override def hashCode(): Int = user.hashCode
}

class UserPreUsedEvent(override val eventScope: EventScope.EventScope,
                       override val user: String, val creator: String, val userPreUsedResource: UserPreUsedResource) extends UserEvent with ModuleInstanceEvent with TicketIdEvent {
  override val moduleName: String = userPreUsedResource.moduleInstance.getApplicationName
  override val moduleInstance: ServiceInstance = userPreUsedResource.moduleInstance
  override val ticketId: String = userPreUsedResource.ticketId

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}

class UserUsedEvent(override val eventScope: EventScope.EventScope,
                    override val user: String, val userUsedResource: UserUsedResource) extends UserEvent with ModuleInstanceEvent with TicketIdEvent {
  override val moduleName: String = userUsedResource.moduleInstance.getApplicationName
  override val moduleInstance: ServiceInstance = userUsedResource.moduleInstance
  override val ticketId: String = userUsedResource.ticketId

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}

class UserReleasedEvent(override val eventScope: EventScope.EventScope,
                        override val user: String, val userReleasedResource: UserReleasedResource) extends UserEvent with ModuleInstanceEvent with TicketIdEvent {
  override val moduleName: String = userReleasedResource.moduleInstance.getApplicationName
  override val moduleInstance: ServiceInstance = userReleasedResource.moduleInstance
  override val ticketId: String = userReleasedResource.ticketId

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}
}
