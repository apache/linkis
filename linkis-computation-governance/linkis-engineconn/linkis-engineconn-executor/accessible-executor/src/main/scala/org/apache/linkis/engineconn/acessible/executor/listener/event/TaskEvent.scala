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

package org.apache.linkis.engineconn.acessible.executor.listener.event

import org.apache.linkis.engineconn.executor.listener.event.EngineConnSyncEvent
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.protocol.engine.JobProgressInfo

trait TaskEvent extends EngineConnSyncEvent {}

case class TaskProgressUpdateEvent(
    taskId: String,
    progress: Float,
    progressInfo: Array[JobProgressInfo]
) extends TaskEvent

case class TaskLogUpdateEvent(taskId: String, log: String) extends TaskEvent

case class TaskStatusChangedEvent(
    taskId: String,
    fromStatus: ExecutionNodeStatus,
    toStatus: ExecutionNodeStatus
) extends TaskEvent

case class TaskResultCreateEvent(taskId: String, resStr: String, alias: String) extends TaskEvent

case class TaskResultSizeCreatedEvent(taskId: String, resultSize: Int) extends TaskEvent

case class TaskResponseErrorEvent(taskId: String, errorMsg: String) extends TaskEvent
