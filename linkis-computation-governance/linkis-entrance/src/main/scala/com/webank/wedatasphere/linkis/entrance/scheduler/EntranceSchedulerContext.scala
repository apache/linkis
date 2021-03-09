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

package com.webank.wedatasphere.linkis.entrance.scheduler

import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager
import com.webank.wedatasphere.linkis.scheduler.queue.{ConsumerManager, GroupFactory}

/**
  * Created by enjoyyin on 2019/1/22.
  */
class EntranceSchedulerContext extends SchedulerContext {
  private var groupFactory: GroupFactory = _
  private var consumerManager: ConsumerManager = _
  private var executorManager: ExecutorManager = _

  def this(groupFactory: GroupFactory, consumerManager: ConsumerManager, executorManager: ExecutorManager) = {
    this()
    this.groupFactory = groupFactory
    this.consumerManager = consumerManager
    this.executorManager = executorManager
    this.consumerManager.setSchedulerContext(this)
  }

  override def getOrCreateGroupFactory: GroupFactory = groupFactory

  override def getOrCreateConsumerManager: ConsumerManager = consumerManager

  override def getOrCreateExecutorManager: ExecutorManager = executorManager

  override def getOrCreateSchedulerListenerBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = null  //TODO wait for
}
