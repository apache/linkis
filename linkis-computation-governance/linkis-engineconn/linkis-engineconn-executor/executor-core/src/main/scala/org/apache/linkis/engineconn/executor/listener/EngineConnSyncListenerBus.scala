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

package org.apache.linkis.engineconn.executor.listener

import org.apache.linkis.common.listener.ListenerBus
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.executor.listener.event.EngineConnSyncEvent

class EngineConnSyncListenerBus
    extends ListenerBus[EngineConnSyncListener, EngineConnSyncEvent]
    with Logging {

  /**
   * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
   * thread for all listeners.
   */
  override protected def doPostEvent(
      listener: EngineConnSyncListener,
      event: EngineConnSyncEvent
  ): Unit = {
    logger.debug(s"$listener start to deal event $event")
    listener.onEvent(event)
    logger.debug(s"$listener Finished  to deal event $event")
  }

}

object EngineConnSyncListenerBus {

  def NAME: String = "EngineServerSyncListenerBus"

  lazy val getInstance = new EngineConnSyncListenerBus()

}
