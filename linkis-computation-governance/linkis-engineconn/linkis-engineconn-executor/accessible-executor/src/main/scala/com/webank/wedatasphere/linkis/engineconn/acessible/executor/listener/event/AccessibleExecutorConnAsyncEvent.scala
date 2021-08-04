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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event

import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.engineconn.executor.listener.event.EngineConnAsyncEvent
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeHealthyInfo

trait AccessibleExecutorConnAsyncEvent extends EngineConnAsyncEvent {

}

case class ExecutorCreateEvent(executor: Executor) extends AccessibleExecutorConnAsyncEvent

case class ExecutorCompletedEvent(executor: Executor, message: String) extends AccessibleExecutorConnAsyncEvent

case class ExecutorStatusChangedEvent(executor: Executor, fromStatus: NodeStatus, toStatus: NodeStatus) extends AccessibleExecutorConnAsyncEvent

case class ExecutorLockEvent(executor: Executor, lock: String) extends AccessibleExecutorConnAsyncEvent

case class ExecutorUnLockEvent(executor: Executor, lock: String) extends AccessibleExecutorConnAsyncEvent

case class NodeHealthyUpdateEvent(nodeHealthyInfo: NodeHealthyInfo) extends AccessibleExecutorConnAsyncEvent

