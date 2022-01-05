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
 
package org.apache.linkis.scheduler

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.queue.SchedulerEvent
import org.apache.commons.lang.StringUtils


abstract class AbstractScheduler extends Scheduler {
  override def init(): Unit = {}

  override def start(): Unit = {}

  private def getEventId(index: Int, groupName: String): String = groupName + "_" + index
  private def getIndexAndGroupName(eventId: String): (Int, String) = {
    if(StringUtils.isBlank(eventId) || !eventId.contains("_"))
      throw new SchedulerErrorException(12011, s"Unrecognized execId $eventId.（不能识别的execId $eventId.)")
    val index = eventId.lastIndexOf("_")
    if(index < 1) throw new SchedulerErrorException(12011, s"Unrecognized execId $eventId.（不能识别的execId $eventId.)")
    (eventId.substring(index + 1).toInt, eventId.substring(0, index))
  }
  override def submit(event: SchedulerEvent): Unit = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(event)
    val consumer = getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(group.getGroupName)
    val index = consumer.getConsumeQueue.offer(event)
    index.map(getEventId(_, group.getGroupName)).foreach(event.setId)
    if(index.isEmpty) throw  new SchedulerErrorException(12001,"The submission job failed and the queue is full!(提交作业失败，队列已满！)")
  }

  override def get(event: SchedulerEvent): Option[SchedulerEvent] = get(event.getId)

  override def get(eventId: String): Option[SchedulerEvent] = {
    val (index, groupName) = getIndexAndGroupName(eventId)
    val consumer = getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    consumer.getRunningEvents.find(_.getId == eventId).orElse(consumer.getConsumeQueue.get(index))
  }

  override def shutdown(): Unit = if(getSchedulerContext != null) {
    if(getSchedulerContext.getOrCreateConsumerManager != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateConsumerManager.shutdown())
    if(getSchedulerContext.getOrCreateExecutorManager != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateExecutorManager.shutdown())
    if(getSchedulerContext.getOrCreateSchedulerListenerBus != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateSchedulerListenerBus.stop())
  }
}
