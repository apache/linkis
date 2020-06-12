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

import com.webank.wedatasphere.linkis.scheduler.queue.ConsumerManager
import com.webank.wedatasphere.linkis.scheduler.{AbstractScheduler, SchedulerContext}

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class EventScheduler(val schedulerContext: SchedulerContext) extends AbstractScheduler {
  def getGroupFactory(): EventGroupFactory

  def getConsumerManager(): ConsumerManager


}

class EventSchedulerImpl(schedulerContext: SchedulerContext) extends EventScheduler(schedulerContext) {
  private var consumerManager: EventConsumerManager = _
  private var groupFactory: EventGroupFactory = _

  override def getGroupFactory() = groupFactory

  override def getConsumerManager() = consumerManager

  override def init() = {
    consumerManager = schedulerContext.getOrCreateConsumerManager.asInstanceOf[EventConsumerManager]
    groupFactory = schedulerContext.getOrCreateGroupFactory.asInstanceOf[EventGroupFactory]
    "OK"
  }

  override def start() = {}

  override def getName = "EventParallelScheduler"

  /*override def submit(event: SchedulerEvent) = {
    val groupName = groupFactory.getGroupNameByEvent(event)
    val consumer = consumerManager.getOrCreateConsumer(groupName)
    event.setId(groupName)
    val res = consumer.getConsumeQueue.offer(event)
    if (!res.isDefined) throw new Exception("提交作业失败，队列已满！")
    else event.setId(groupName + "_" + res)
  }*/

  override def shutdown() = {
    consumerManager.shutdown()
  }

  override def getSchedulerContext = schedulerContext
}
