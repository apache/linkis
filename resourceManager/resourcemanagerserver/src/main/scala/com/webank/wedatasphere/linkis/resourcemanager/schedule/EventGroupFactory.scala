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

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.event.metric.MetricRMEvent
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, GroupFactory, SchedulerEvent}

import scala.collection.mutable

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class EventGroupFactory extends GroupFactory {

  def getGroupNameByModule(moduleInstance: ServiceInstance): String

  def getGroupNameByUser(user: String): String
}

class EventGroupFactoryImpl extends EventGroupFactory with Logging {
  private val groupMap = new mutable.HashMap[String, Group]()
  private val RM_CONTEXT_CONSTRUCTOR_LOCK = new Object()
  private val maxGroupNum = 100

  def getInitCapacity(groupName: String): Int = 100

  def getMaxCapacity(groupName: String): Int = 1000

  def getBKDRHash2(str: String): Int = {
    val seed: Int = 131
    var hash: Int = 0
    for (i <- 0 to str.length - 1) {
      hash = hash * seed + str.charAt(i)
      hash = hash & 0x7FFFFFFF
      info("current hash code result is " + hash.toString)
    }
    return hash
  }


  override def getGroupNameByModule(moduleInstance: ServiceInstance) = {
    //val inputStr = moduleInstance.ip+moduleInstance.port.toString+moduleInstance.moduleName
    //val hash = getBKDRHash2(inputStr)
    val hash = moduleInstance.hashCode()
    val groupName = hash % maxGroupNum
    groupName.toString
  }

  override def getGroupNameByUser(user: String) = {
    //val hash = getBKDRHash2(user)
    val hash = user.hashCode
    val groupName = hash % maxGroupNum
    groupName.toString
  }

  override def getOrCreateGroup(groupName: String) = {
    RM_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
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
    event match {
      case metricRMEvent: MetricRMEvent => {
        "METRIC"
      }
      case _ => {
        val hash = event.hashCode
        val groupName = hash % maxGroupNum
        groupName.toString
      }
    }
  }
}

