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

package com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue

import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}

import scala.collection.mutable


class ParallelGroupFactory extends GroupFactory{
  private val groupMap = new mutable.HashMap[String, Group]()
  def getInitCapacity(groupName: String): Int= 100

  def getMaxCapacity(groupName: String): Int = 1000

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  override def getOrCreateGroup(groupName: String) = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (groupMap.get(groupName).isDefined) {
        groupMap.get(groupName).get
      }
      else {
        val group = new ParallelGroup(groupName, getInitCapacity(groupName), getMaxCapacity(groupName))
        groupMap.put(groupName, group)
        group
      }
    }
  }

  override def getGroupNameByEvent(event: SchedulerEvent) = {
    val belongList = groupMap.values.filter(x => x.belongTo(event)).map(x => x.getGroupName).toList
    if(belongList.size > 0){
      belongList(0)
    }else{
      "NULL"
    }
  }

}
