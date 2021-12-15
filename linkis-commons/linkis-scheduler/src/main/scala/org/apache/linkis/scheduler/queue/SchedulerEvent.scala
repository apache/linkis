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
 
package org.apache.linkis.scheduler.queue

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.queue.SchedulerEventState._


trait SchedulerEvent extends Logging {

  private[queue] var id: String = _
  private var state: SchedulerEventState = Inited
  val createTime = System.currentTimeMillis
  protected var scheduledTime: Long = 0l
  protected var startTime: Long = 0l
  protected var endTime: Long = 0l

  def getEndTime = endTime
  def getStartTime = startTime
  def getScheduledTime = scheduledTime

  def getId = id

  def setId(id: String)={
    this.id = id
    this synchronized notify()
  }

  def turnToScheduled(): Boolean = if(!isWaiting) false else this synchronized {
    if(!isWaiting) false else {
      scheduledTime = System.currentTimeMillis
      while(id == null) wait(100)
      transition(Scheduled)
      true
    }
  }

  def pause(): Unit
  def resume(): Unit

  def cancel() = transition(Cancelled)

  def isWaiting = state == Inited

  def isScheduled = state == Scheduled

  def isRunning = state == Running

  def isCompleted = SchedulerEventState.isCompleted(state)

  def isSucceed: Boolean = SchedulerEventState.isSucceed(state)

  def isWaitForRetry: Boolean = state == WaitForRetry

  def getState = state

  def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit

  def beforeStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}

  protected def transition(state: SchedulerEventState): Unit = synchronized {
    if (state.id < this.state.id && state != WaitForRetry)
      throw new SchedulerErrorException(12000, s"Task status flip error! Cause: Failed to flip from ${this.state} to $state.（任务状态翻转出错！原因：不允许从${this.state} 翻转为$state.）") //抛异常
    info(s"$toString change status ${this.state} => $state.")
    val oldState = this.state
    this.state = state
    afterStateChanged(oldState, state)
  }
}
