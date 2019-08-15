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

package com.webank.wedatasphere.linkis.resourcemanager.schedule

import com.webank.wedatasphere.linkis.resourcemanager.event.metric.{MetricRMEvent, MetricRMEventExecutor, MetricRMEventListenerBus}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{NotifyRMEvent, NotifyRMEventExecutor, NotifyRMEventListenerBus}
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorManager, ExecutorState}
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent

import scala.concurrent.duration.Duration

/**
  * Created by shanhuang on 2018/9/27.
  */
class RMEventExecutorManager extends ExecutorManager {
  private var notifyRMEventListenerBus: NotifyRMEventListenerBus = _
  private var metricRMEventListenerBus: MetricRMEventListenerBus = _
  private var notifyExecutor: NotifyRMEventExecutor = _
  private var metricExecutor: MetricRMEventExecutor = _
  private val RM_NOTIFY_CONSTRUCTOR_LOCK = new Object()
  private val RM_METRIC_CONSTRUCTOR_LOCK = new Object()

  def setNotifyRMEventListenerBus(notifyRMEventListenerBus: NotifyRMEventListenerBus) = {
    this.notifyRMEventListenerBus = notifyRMEventListenerBus
  }

  def setMetricRMEventListenerBus(metricRMEventListenerBus: MetricRMEventListenerBus) = {
    this.metricRMEventListenerBus = metricRMEventListenerBus
  }

  override def setExecutorListener(executorListener: ExecutorListener) = {}

  override protected def createExecutor(event: SchedulerEvent) = {
    event match {
      case n: NotifyRMEvent => {
        if (notifyExecutor != null) {
          notifyExecutor
        } else {
          RM_NOTIFY_CONSTRUCTOR_LOCK.synchronized {
            if (notifyExecutor == null) {
              notifyExecutor = new NotifyRMEventExecutor(1)
              notifyExecutor.setNotifyRMEventListenerBus(this.notifyRMEventListenerBus)
              notifyExecutor.setState(ExecutorState.Idle)
            }
            notifyExecutor
          }
        }
      }
      case m: MetricRMEvent => {
        if (metricExecutor != null) {
          metricExecutor
        } else {
          RM_METRIC_CONSTRUCTOR_LOCK.synchronized {
            if (metricExecutor == null) {
              metricExecutor = new MetricRMEventExecutor(2)
              metricExecutor.setMetricRMEventListenerBus(this.metricRMEventListenerBus)
              metricExecutor.setState(ExecutorState.Idle)
            }
            metricExecutor
          }
        }
      }
    }
  }

  override def askExecutor(event: SchedulerEvent) = {
    val executor = createExecutor(event)
    if (executor.state == ExecutorState.Idle || executor.state == ExecutorState.Busy) {
      Some(executor)
    } else {
      throw new RMErrorException(11009,"Ask executor error")
    }
  }

  override def askExecutor(event: SchedulerEvent, wait: Duration) = null

  override def getById(id: Long) = null

  override def getByGroup(groupName: String) = null

  override protected def delete(executor: Executor) = {
    executor.close()
  }

  override def shutdown() = {
    notifyExecutor.close()
    metricExecutor.close()
    notifyExecutor = null
    metricExecutor = null
  }

}
