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

package org.apache.linkis.entrance.scheduler

import org.apache.linkis.common.listener.ListenerEventBus
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import org.apache.linkis.scheduler.executer.ExecutorManager
import org.apache.linkis.scheduler.queue.{ConsumerManager, GroupFactory}

class EntranceSchedulerContext extends SchedulerContext {
  private var groupFactory: GroupFactory = _
  private var consumerManager: ConsumerManager = _
  private var executorManager: ExecutorManager = _

  private var offlineFlag: Boolean = false

  def setOfflineFlag(offlineFlag: Boolean): Unit = this.offlineFlag = offlineFlag
  def getOfflineFlag: Boolean = this.offlineFlag

  def this(
      groupFactory: GroupFactory,
      consumerManager: ConsumerManager,
      executorManager: ExecutorManager
  ) = {
    this()
    this.groupFactory = groupFactory
    this.consumerManager = consumerManager
    this.executorManager = executorManager
    this.consumerManager.setSchedulerContext(this)
  }

  override def getOrCreateGroupFactory: GroupFactory = groupFactory

  override def getOrCreateConsumerManager: ConsumerManager = consumerManager

  override def getOrCreateExecutorManager: ExecutorManager = executorManager

  override def getOrCreateSchedulerListenerBus
      : ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = null // TODO wait for

}
