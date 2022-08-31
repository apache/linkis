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

package org.apache.linkis.scheduler.queue.fifoqueue

import org.apache.linkis.common.listener.ListenerEventBus
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import org.apache.linkis.scheduler.executer.ExecutorManager
import org.apache.linkis.scheduler.queue.{ConsumerManager, GroupFactory}

class FIFOSchedulerContextImpl(val maxParallelismUsers: Int) extends SchedulerContext with Logging {
  private var consumerManager: ConsumerManager = _
  private var groupFactory: GroupFactory = _
  private var executorManager: ExecutorManager = _

  private var listenerEventBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] =
    _

  private val lock = new Object()

  override def getOrCreateGroupFactory: GroupFactory = {
    if (groupFactory != null) return groupFactory
    lock.synchronized {
      if (groupFactory == null) {
        groupFactory = createGroupFactory()
      }
    }
    groupFactory
  }

  def setGroupFactory(groupFactory: GroupFactory): Unit = this.groupFactory = groupFactory

  protected def createGroupFactory(): GroupFactory = new FIFOGroupFactory

  override def getOrCreateConsumerManager: ConsumerManager = {
    if (consumerManager != null) return consumerManager
    lock.synchronized {
      if (consumerManager == null) {
        consumerManager = createConsumerManager()
        consumerManager.setSchedulerContext(this)
      }
    }
    consumerManager
  }

  def setConsumerManager(consumerManager: ConsumerManager): Unit = {
    this.consumerManager = consumerManager
    this.consumerManager.setSchedulerContext(this)
  }

  protected def createConsumerManager(): ConsumerManager = new FIFOConsumerManager

  def setExecutorManager(executorManager: ExecutorManager): Unit = this.executorManager =
    executorManager

  override def getOrCreateExecutorManager: ExecutorManager = executorManager

  override def getOrCreateSchedulerListenerBus
      : ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = listenerEventBus

  def setSchedulerListenerBus(
      listenerEventBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent]
  ): Unit =
    this.listenerEventBus = listenerEventBus

}
