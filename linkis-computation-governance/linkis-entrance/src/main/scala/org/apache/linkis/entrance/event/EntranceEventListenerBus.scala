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

package org.apache.linkis.entrance.event

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.listener.SingleThreadListenerBus
import org.apache.linkis.entrance.conf.EntranceConfiguration

/**
 * description: It is an implementation of SingleThreadListenerBus in Entrance Module
 */
class EntranceEventListenerBus[L <: EntranceEventListener, E <: EntranceEvent](
    eventQueueCapacity: Int,
    name: String
) extends SingleThreadListenerBus[L, E](eventQueueCapacity, name) {

  def this(eventQueueCapacity: Int) =
    this(eventQueueCapacity, DataWorkCloudApplication.getApplicationName + "-EventListenerBus")

  def this() = this(EntranceConfiguration.ENTRANCE_LISTENER_BUS_EVENT_QUEUE_CAPACITY.getValue)

  override protected def doPostEvent(listener: L, event: E): Unit = listener.onEvent(event)
}
