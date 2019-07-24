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

package com.webank.wedatasphere.linkis.resourcemanager.event

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope.EventScope
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent

/**
  * Created by shanhuang on 9/11/18.
  */
trait RMEvent extends SchedulerEvent with Event {
  val moduleName: String

  override def pause(): Unit = {}
}

trait Tokenized {
  val token: Int
}

trait Scoped {
  val eventScope: EventScope
}

object EventScope extends Enumeration {
  type EventScope = Value
  val Instance, Service, User, Other = Value
}

