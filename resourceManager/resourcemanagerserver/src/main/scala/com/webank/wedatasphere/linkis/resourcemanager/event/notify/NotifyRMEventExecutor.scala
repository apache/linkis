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

import com.webank.wedatasphere.linkis.common.listener.ListenerBus
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent
import com.webank.wedatasphere.linkis.resourcemanager.schedule.RMEventExecuteRequest
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecutorState => _, _}

/**
  * Created by shanhuang on 9/11/18.
  */

class NotifyRMEventListenerBus extends ListenerBus[NotifyRMEventListener, RMEvent] with Logging {
  override protected def doPostEvent(listener: NotifyRMEventListener, event: RMEvent) = event match {
    case event: NotifyRMEvent =>
      info(s"receive a event, module：${event.moduleName} + scope: {${event.eventScope}} : event object:$event ListenerBus:$this")
      try
        listener.onNotifyRMEvent(event)
      catch {
        case t: Throwable =>
          error(s"Failed to process event:$event,", t)
          throw t
      }
  }
}

class NotifyRMEventExecutor(id: Int) extends AbstractExecutor(id) {

  private var notifyRMEventListenerBus: NotifyRMEventListenerBus = _

  def setNotifyRMEventListenerBus(notifyRMEventListenerBus: NotifyRMEventListenerBus) =
    this.notifyRMEventListenerBus = notifyRMEventListenerBus

  def setState(state: ExecutorState) = {
    this.transition(state)
  }

  override def execute(executeRequest: ExecuteRequest) = {
    ensureIdle {
      executeRequest match {
        case eventRequest: RMEventExecuteRequest =>
          notifyRMEventListenerBus.postToAll(eventRequest.event)
          SuccessExecuteResponse()
      }
    }
  }

  override def getExecutorInfo = null

  override protected def callback() = {

  }

  override def close(): Unit = {}
}
