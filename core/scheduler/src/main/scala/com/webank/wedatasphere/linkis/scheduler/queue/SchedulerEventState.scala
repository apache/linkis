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

package com.webank.wedatasphere.linkis.scheduler.queue

/**
  * Created by enjoyyin on 2018/9/4.
  */
object SchedulerEventState extends Enumeration {
  type SchedulerEventState = Value
  val Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout = Value

  def isRunning(jobState: SchedulerEventState) = jobState == Running

  def isScheduled(jobState: SchedulerEventState) = jobState != Inited

  def isCompleted(jobState: SchedulerEventState) = jobState match {
    case Inited | Scheduled | Running | WaitForRetry => false
    case _ => true
  }

  def isSucceed(jobState: SchedulerEventState) = jobState == Succeed

}