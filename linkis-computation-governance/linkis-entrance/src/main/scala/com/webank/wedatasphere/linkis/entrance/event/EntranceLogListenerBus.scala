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

package com.webank.wedatasphere.linkis.entrance.event

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.listener.SingleThreadListenerBus
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration

/**
  * description: It is an implementation of SingleThreadListenerBus in Entrance Module
  */
class EntranceLogListenerBus[L <: EntranceLogListener, E <: EntranceLogEvent](eventQueueCapacity: Int, name: String)
  extends SingleThreadListenerBus[L, E](eventQueueCapacity, name) {

  def this(eventQueueCapacity: Int) = this(eventQueueCapacity, DataWorkCloudApplication.getApplicationName + "-EventListenerBus")
  def this() = this(EntranceConfiguration.ENTRANCE_LISTENER_BUS_EVENT_QUEUE_CAPACITY.getValue)

  override protected def doPostEvent(listener: L, event: E): Unit = listener.onEvent(event)
}