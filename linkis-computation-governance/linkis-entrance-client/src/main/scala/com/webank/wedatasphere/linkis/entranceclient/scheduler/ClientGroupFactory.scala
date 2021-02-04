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

package com.webank.wedatasphere.linkis.entranceclient.scheduler

import com.webank.wedatasphere.linkis.entranceclient
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}

/**
  * Created by johnnwang on 2018/10/30.
  */
class ClientGroupFactory extends GroupFactory {

  private var group: ParallelGroup = _

  private var maxCapacity: Int = 1000
  private var maxRunningJobs: Int = 30
  private var maxAskExecutorTimes: Long = 5 * 60 * 1000

  def setQueueSize(maxCapacity: Int) = this.maxCapacity = maxCapacity
  def setMaxRunningJobs(maxRunningJobs: Int) = this.maxRunningJobs = maxRunningJobs
  def setMaxAskExecutorTimes(maxAskExecutorTimes: Long) = this.maxAskExecutorTimes = maxAskExecutorTimes

  override def getOrCreateGroup(groupName: String): Group = {
    if(group == null) synchronized {
      if(group == null) {
        group = new ParallelGroup(entranceclient.scheduler.ClientGroupFactory.GROUP_NAME, 100, maxCapacity)
        group.setMaxRunningJobs(maxRunningJobs)
        group.setMaxAskExecutorTimes(maxAskExecutorTimes)
      }
    }
    group
  }

  override def getGroupNameByEvent(event: SchedulerEvent): String = entranceclient.scheduler.ClientGroupFactory.GROUP_NAME
}

object ClientGroupFactory {
  val GROUP_NAME = "Client"
}