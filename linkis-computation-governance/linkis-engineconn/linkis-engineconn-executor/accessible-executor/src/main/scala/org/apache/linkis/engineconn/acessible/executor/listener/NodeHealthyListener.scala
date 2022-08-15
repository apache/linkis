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

package org.apache.linkis.engineconn.acessible.executor.listener

import org.apache.linkis.common.listener.Event
import org.apache.linkis.engineconn.acessible.executor.listener.event.NodeHealthyUpdateEvent
import org.apache.linkis.engineconn.executor.listener.EngineConnAsyncListener
import org.apache.linkis.engineconn.executor.listener.event.EngineConnAsyncEvent

trait NodeHealthyListener extends EngineConnAsyncListener {

  def onNodeHealthyUpdate(nodeHealthyUpdateEvent: NodeHealthyUpdateEvent): Unit

  override def onEvent(event: EngineConnAsyncEvent): Unit = event match {

    case nodeHealthyUpdateEvent: NodeHealthyUpdateEvent =>
      onNodeHealthyUpdate(nodeHealthyUpdateEvent)
    case _ =>
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}

}
