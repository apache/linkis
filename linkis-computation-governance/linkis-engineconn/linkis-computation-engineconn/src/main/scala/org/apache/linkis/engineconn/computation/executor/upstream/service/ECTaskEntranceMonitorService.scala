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

package org.apache.linkis.engineconn.computation.executor.upstream.service

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.upstream.ECTaskEntranceMonitor
import org.apache.linkis.engineconn.computation.executor.upstream.event.TaskStatusChangedForUpstreamMonitorEvent
import org.apache.linkis.engineconn.computation.executor.upstream.listener.TaskStatusChangedForUpstreamMonitorListener
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconn.executor.listener.event.EngineConnSyncEvent
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class ECTaskEntranceMonitorService
    extends TaskStatusChangedForUpstreamMonitorListener
    with Logging {

  private val eCTaskEntranceMonitor = new ECTaskEntranceMonitor

  private val syncListenerBus =
    ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnSyncListenerBus

  @PostConstruct
  def init(): Unit = {
    val shouldStartMonitor = ComputationExecutorConf.UPSTREAM_MONITOR_ECTASK_SHOULD_START
    if (shouldStartMonitor) {
      syncListenerBus.addListener(this)
      eCTaskEntranceMonitor.start
    }
  }

  override def onEvent(event: EngineConnSyncEvent): Unit = event match {
    case taskStatusChangedForUpstreamMonitorEvent: TaskStatusChangedForUpstreamMonitorEvent =>
      onTaskStatusChanged(taskStatusChangedForUpstreamMonitorEvent)
    case _ => logger.info("ignored EngineConnSyncEvent " + event.getClass.getCanonicalName)
  }

  override def onTaskStatusChanged(event: TaskStatusChangedForUpstreamMonitorEvent): Unit = {
    val fromStatus = event.fromStatus
    val toStatus = event.toStatus
    if (
        (fromStatus == ExecutionNodeStatus.Inited || fromStatus == ExecutionNodeStatus.Scheduled) &&
        (toStatus == ExecutionNodeStatus.Running)
    ) {
      logger.info("registering new task: " + event.taskId)
      eCTaskEntranceMonitor.register(event.task, event.executor)
    } else if (
        fromStatus == ExecutionNodeStatus.Running &&
        (toStatus == ExecutionNodeStatus.Succeed || toStatus == ExecutionNodeStatus.Failed || toStatus == ExecutionNodeStatus.Cancelled || toStatus == ExecutionNodeStatus.Timeout)
    ) {
      logger.info("unRegistering task: " + event.taskId)
      eCTaskEntranceMonitor.unregister(event.task.getTaskId)
    }
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}

  def shutdown(): Unit = {
    eCTaskEntranceMonitor.shutdown
  }

}
