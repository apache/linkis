/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.{ExecutorLockEvent, ExecutorUnLockEvent}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.EngineConnAsyncListener
import com.webank.wedatasphere.linkis.engineconn.executor.listener.event.EngineConnAsyncEvent

trait ExecutorLockListener extends EngineConnAsyncListener {

  def onAddLock(addLockEvent: ExecutorLockEvent): Unit

  def onReleaseLock(releaseLockEvent: ExecutorUnLockEvent): Unit

  override def onEvent(event: EngineConnAsyncEvent): Unit = event match {
    case addLockEvent: ExecutorLockEvent => onAddLock(addLockEvent)
    case releaseLockEvent: ExecutorUnLockEvent => onReleaseLock(releaseLockEvent)
    case _ =>
  }

  override def onEventError(event: Event, t: Throwable): Unit = {

  }

}
