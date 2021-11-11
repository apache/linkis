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

import java.util.concurrent.TimeUnit

import org.apache.linkis.scheduler.queue.{AbstractGroup, SchedulerEvent}

import scala.concurrent.duration.Duration

class FIFOGroup(groupName: String, initCapacity: Int, maxCapacity: Int) extends AbstractGroup{

  private var maxAskInterval = 30000l
  private var minAskInterval = 10000l

  def getMaxAskInterval = maxAskInterval
  def setMaxAskInterval(maxAskInterval: Long) = this.maxAskInterval = maxAskInterval
  def getMinAskInterval = minAskInterval
  def setMinAskInterval(minAskInterval: Long) = this.minAskInterval = minAskInterval

  def getMaxAskExecutorDuration = if(getMaxAskExecutorTimes <= 0) Duration.Inf else Duration(getMaxAskExecutorTimes, TimeUnit.MILLISECONDS)
  def getAskExecutorInterval = if(getMaxAskExecutorTimes <= 0) Duration(maxAskInterval, TimeUnit.MILLISECONDS)
  else if(getMaxAskExecutorTimes > maxAskInterval) Duration(math.min(math.max(getMaxAskExecutorTimes / 10, minAskInterval), maxAskInterval), TimeUnit.MILLISECONDS)
  else if(getMaxAskExecutorTimes > minAskInterval) Duration(minAskInterval, TimeUnit.MILLISECONDS)
  else Duration(getMaxAskExecutorTimes, TimeUnit.MILLISECONDS)

  override def getGroupName = groupName

  /**
    * The percentage of waiting Jobs in the entire ConsumeQueue(等待的Job占整个ConsumeQueue的百分比)
    *
    * @return
    */
  override def getInitCapacity = initCapacity

  /**
    * The waiting Job accounts for the largest percentage of the entire ConsumeQueue(等待的Job占整个ConsumeQueue的最大百分比)
    *
    * @return
    */
override def getMaximumCapacity = maxCapacity
override def belongTo(event: SchedulerEvent) = true
}
