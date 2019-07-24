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

package com.webank.wedatasphere.linkis.resourcemanager.listener

import com.webank.wedatasphere.linkis.common.listener.SingleThreadListenerBus
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.RMEventListener
import com.webank.wedatasphere.linkis.resourcemanager.event.RMEvent
import com.webank.wedatasphere.linkis.resourcemanager.event.metric.{MetricRMEvent, MetricRMEventListener}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{NotifyRMEvent, NotifyRMEventListener}

/**
  * Created by shanhuang on 9/11/18.
  */
private[wedatasphere] class RMListenerBus[L <: RMEventListener, E <: RMEvent]
  extends SingleThreadListenerBus[L, E](1000, "RM-Metric-Thread") {
  override protected def doPostEvent(listener: L, event: E): Unit = listener match {
    case l: NotifyRMEventListener => event match {
      case e: NotifyRMEvent => l.onNotifyRMEvent(e)
      case _ =>
    }
    case m: MetricRMEventListener => event match {
      case e: MetricRMEvent => m.onMetricRMEvent(e)
      case _ =>
    }
  }
}

object RMListenerBus extends Logging {
  private val rmListenerBus = new RMListenerBus[RMEventListener, RMEvent]

  def getRMListenerBusInstance = rmListenerBus

  def init(): Unit = {
    info("add  Listener to RMListenerBus...")
    rmListenerBus.addListener(new ModuleListener)
    rmListenerBus.addListener(new UserListener)
    rmListenerBus.start()
    info("Started RMListenerBus.")
  }
}
