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

package com.webank.wedatasphere.linkis.engine.execute.scheduler

import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration._
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}


/**
  * Created by enjoyyin on 2018/9/25.
  */
class EngineGroupFactory extends GroupFactory {
  private val group = new FIFOGroup(Sender.getThisServiceInstance.getApplicationName + "Engine", ENGINE_MAX_PARALLELISM.getValue, ENGINE_MAX_CONSUMER_QUEUE_SIZE.getValue)
  if(!ENGINE_SUPPORT_PARALLELISM.getValue) group.setMaxRunningJobs(1)
  else group.setMaxRunningJobs(ENGINE_MAX_PARALLELISM.getValue)

  def getGroup = group

  override def getOrCreateGroup(groupName: String): Group = group

  override def getGroupNameByEvent(event: SchedulerEvent): String = group.getGroupName
}
