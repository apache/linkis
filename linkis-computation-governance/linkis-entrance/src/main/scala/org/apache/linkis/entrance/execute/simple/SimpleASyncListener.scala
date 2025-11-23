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

package org.apache.linkis.entrance.execute.simple

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.EntranceServer
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.orchestrator.listener.OrchestratorAsyncEvent
import org.apache.linkis.orchestrator.listener.task.{
  TaskLogEvent,
  TaskLogListener,
  TaskProgressListener,
  TaskRunningInfoEvent
}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class SimpleASyncListener extends TaskLogListener with TaskProgressListener with Logging {

  @Autowired private var entranceServer: EntranceServer = _

  @PostConstruct
  def init(): Unit = {
    if (EntranceConfiguration.LINKIS_ENTRANCE_SKIP_ORCHESTRATOR) {
      SimpleExecuteBusContext
        .getOrchestratorListenerBusContext()
        .getOrchestratorAsyncListenerBus
        .addListener(this)
    }
  }

  override def onLogUpdate(taskLogEvent: TaskLogEvent): Unit = {}

  override def onProgressOn(taskProgressEvent: TaskRunningInfoEvent): Unit = {}

  override def onEvent(event: OrchestratorAsyncEvent): Unit = {}

  override def onEventError(event: Event, t: Throwable): Unit = {}
}
