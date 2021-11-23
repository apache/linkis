/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.acessible.executor.entity

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.acessible.executor.listener.event.{ExecutorCompletedEvent, ExecutorStatusChangedEvent}
import org.apache.linkis.engineconn.executor.entity.SensibleExecutor
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.scheduler.exception.SchedulerErrorException

abstract class AccessibleExecutor extends SensibleExecutor {

    private var isExecutorClosed = false

    def isIdle: Boolean = NodeStatus.isIdle(getStatus) || NodeStatus.Starting == getStatus

    def isBusy: Boolean = NodeStatus.isLocked(getStatus)

    def whenBusy[A](f: => A): Unit = whenStatus(NodeStatus.Busy, f)

    def whenIdle[A](f: => A): Unit = whenStatus(NodeStatus.Idle, f)

    def whenStatus[A](_status: NodeStatus, f: => A): Unit = if (getStatus == _status) f

    def ensureBusy[A](f: => A): A = {
        updateLastActivityTime()
        if (isBusy) synchronized {
            if (isBusy) return f
        }
        throw new SchedulerErrorException(20001, "%s is in status %s." format(toString, getStatus))
    }

    def ensureIdle[A](f: => A): A = ensureIdle(f, true)

    def ensureIdle[A](f: => A, transitionState: Boolean): A = {
        if (isIdle) synchronized {
            if (isIdle) {
                if (transitionState) transition(NodeStatus.Busy)
                return Utils.tryFinally(f) {
                    if (transitionState) transition(NodeStatus.Idle)
                    callback()
                }
            }
        }
        throw new SchedulerErrorException(20001, "%s is in status %s." format(toString, getStatus))
    }


    def ensureAvailable[A](f: => A): A = {
        if (NodeStatus.isAvailable(getStatus)) synchronized {
            if (NodeStatus.isAvailable(getStatus)) return Utils.tryFinally(f)(callback())
        }
        throw new SchedulerErrorException(20001, "%s is in status %s." format(toString, getStatus))
    }

    def whenAvailable[A](f: => A): A = {
        if (NodeStatus.isAvailable(getStatus)) return Utils.tryFinally(f)(callback())
        throw new SchedulerErrorException(20001, "%s is in status %s." format(toString, getStatus))
    }

    protected def callback(): Unit

    def supportCallBackLogs(): Boolean

    protected override def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit = {
        ExecutorListenerBusContext.getExecutorListenerBusContext()
          .getEngineConnAsyncListenerBus.post(ExecutorStatusChangedEvent(this, fromStatus, toStatus))
        toStatus match {
            case NodeStatus.Failed | NodeStatus.Success =>
                ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus.post(ExecutorCompletedEvent(this, ""))
            case _ =>
        }
    }

    override def close(): Unit = {
        isExecutorClosed = true
        super.close()
    }

    override def isClosed: Boolean = isExecutorClosed
}
