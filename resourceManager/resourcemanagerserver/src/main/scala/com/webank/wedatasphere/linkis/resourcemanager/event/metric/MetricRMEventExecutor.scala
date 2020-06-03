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

package com.webank.wedatasphere.linkis.resourcemanager.event.metric

import com.webank.wedatasphere.linkis.common.listener.ListenerBus
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent
import com.webank.wedatasphere.linkis.resourcemanager.schedule.RMEventExecuteRequest
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState._
import com.webank.wedatasphere.linkis.scheduler.executer.{AbstractExecutor, ExecuteRequest, SuccessExecuteResponse}


/**
  * Created by shanhuang on 9/11/18.
  */

class MetricRMEventListenerBus extends ListenerBus[MetricRMEventListener, RMEvent] {
  override protected def doPostEvent(listener: MetricRMEventListener, event: RMEvent) = event match {
    case event: MetricRMEvent =>
      listener.onMetricRMEvent(event)
  }
}

class MetricRMEventExecutor(id: Int) extends AbstractExecutor(id) {

  private var metricRMEventListenerBus: MetricRMEventListenerBus = _

  def setMetricRMEventListenerBus(metricRMEventListenerBus: MetricRMEventListenerBus) =
    this.metricRMEventListenerBus = metricRMEventListenerBus

  def setState(state: ExecutorState) = {
    this.transition(state)
  }

  override def execute(executeRequest: ExecuteRequest) = {
    ensureIdle {
      executeRequest match {
        case eventRequest: RMEventExecuteRequest =>
          metricRMEventListenerBus.postToAll(eventRequest.event)
          SuccessExecuteResponse()
      }
    }
  }


  override def getExecutorInfo = null

  override protected def callback() = {

  }

  override def close(): Unit = {}
}
