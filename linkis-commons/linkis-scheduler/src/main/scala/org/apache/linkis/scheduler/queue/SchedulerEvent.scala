/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.scheduler.queue

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.scheduler.errorcode.LinkisSchedulerErrorCodeSummary._
import org.apache.linkis.scheduler.exception.SchedulerErrorException
import org.apache.linkis.scheduler.queue.SchedulerEventState._

import java.text.MessageFormat

trait SchedulerEvent extends Logging {

  private[queue] var id: String = _
  private var state: SchedulerEventState = Inited
  val createTime = System.currentTimeMillis
  protected var scheduledTime: Long = 0L
  protected var startTime: Long = 0L
  protected var endTime: Long = 0L
  protected var priority: Int = 100
  protected var index: Int = 0

  def getEndTime: Long = endTime
  def getStartTime: Long = startTime
  def getPriority: Int = priority
  def getIndex: Int = index

  /*
   * To be compatible with old versions.
   * It's not recommonded to use scheduledTime, which was only several mills at most time.
   */
  @deprecated
  def getScheduledTime: Long = scheduledTime

  def getId: String = id

  def setId(id: String): Unit = {
    this.id = id
    this synchronized notify()
  }

  def setPriority(priority: Int): Unit = {
    this.priority = priority
  }

  def setIndex(index: Int): Unit = {
    this.index = index
  }

  def turnToScheduled(): Boolean = if (!isWaiting) {
    false
  } else {
    this synchronized {
      if (!isWaiting) false
      else {
        scheduledTime = System.currentTimeMillis
        while (id == null) wait(100)
        transition(Scheduled)
        true
      }
    }
  }

  def pause(): Unit
  def resume(): Unit

  def cancel(): Unit = transition(Cancelled)

  def isWaiting: Boolean = state == Inited

  def isScheduled: Boolean = state == Scheduled

  def isRunning: Boolean = state == Running

  def isCompleted: Boolean = SchedulerEventState.isCompleted(state)

  def isSucceed: Boolean = SchedulerEventState.isSucceed(state)

  def isWaitForRetry: Boolean = state == WaitForRetry

  def getState: SchedulerEventState = state

  def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit

  def beforeStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {}

  protected def transition(state: SchedulerEventState): Unit = synchronized {
    if (state.id < this.state.id && state != WaitForRetry) {
      throw new SchedulerErrorException(
        TASK_STATUS_FLIP_ERROR.getErrorCode,
        MessageFormat.format(TASK_STATUS_FLIP_ERROR.getErrorDesc, this.state, state)
      )
    }
    logger.info(s"$toString change status ${this.state} => $state.")
    val oldState = this.state
    this.state = state
    afterStateChanged(oldState, state)
  }

}
