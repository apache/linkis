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
 
package org.apache.linkis.scheduler.queue.fifoqueue

import org.apache.linkis.scheduler.queue.{AbstractGroup, Group, GroupFactory, SchedulerEvent}

import scala.collection.mutable


class FIFOGroupFactory extends GroupFactory {

  private val groupMap = new mutable.HashMap[String, Group]()
  private val lock = new Array[Byte](0)

  //Obtained from the database(从数据库获取)
  private var defaultMaxRunningJobs: Int = 1
  private var defaultMaxAskExecutorTimes: Long = 30000l
  private var defaultInitCapacity: Int = 1000
  private var defaultMaxCapacity: Int = 5000

  def setDefaultMaxRunningJobs(defaultMaxRunningJobs: Int): Unit =
    this.defaultMaxRunningJobs = defaultMaxRunningJobs
  def getDefaultMaxRunningJobs: Int = defaultMaxRunningJobs

  def setDefaultMaxAskExecutorTimes(defaultMaxAskExecutorTimes: Long): Unit =
    this.defaultMaxAskExecutorTimes = defaultMaxAskExecutorTimes
  def getDefaultMaxAskExecutorTimes: Long = defaultMaxAskExecutorTimes

  protected def getInitCapacity(groupName: String): Int = defaultInitCapacity
  def setDefaultInitCapacity(defaultInitCapacity: Int): Unit =
    this.defaultInitCapacity = defaultInitCapacity

  protected def getMaxCapacity(groupName: String): Int = defaultMaxCapacity
  def setDefaultMaxCapacity(defaultMaxCapacity: Int): Unit =
    this.defaultMaxCapacity = defaultMaxCapacity

  override def getOrCreateGroup(event: SchedulerEvent): Group = {
    val groupName = getGroupNameByEvent(event)
    if(groupMap.contains(groupName)) return groupMap(groupName)
    lock.synchronized {
      groupMap.getOrElseUpdate(groupName, {
        val group = createGroup(groupName)
        group.setMaxRunningJobs(defaultMaxRunningJobs)
        group.setMaxAskExecutorTimes(defaultMaxAskExecutorTimes)
        group
      })
    }
  }

  override def getGroup(groupName: String): Group = groupMap(groupName)

  protected def createGroup(groupName: String): AbstractGroup =
    new FIFOGroup(groupName, getInitCapacity(groupName), getMaxCapacity(groupName))

  protected def getGroupNameByEvent(event: SchedulerEvent): String = FIFOGroupFactory.FIFO_GROUP_NAME

}
object FIFOGroupFactory {
  val FIFO_GROUP_NAME = "FIFO-Group"
}