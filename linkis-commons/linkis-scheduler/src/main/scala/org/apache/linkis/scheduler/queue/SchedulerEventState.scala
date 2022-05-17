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


object SchedulerEventState extends Enumeration {
  type SchedulerEventState = Value

  val Inited = Value("Inited")
  val WaitForRetry = Value("WaitForRetry")
  val Scheduled = Value("Scheduled")
  val Running = Value("Running")
  val Succeed = Value("Succeed")
  val Failed = Value("Failed")
  val Cancelled = Value("Cancelled")
  val Timeout = Value("Timeout")


  def isRunning(jobState: SchedulerEventState) = jobState == Running

  def isScheduled(jobState: SchedulerEventState) = jobState != Inited

  def isCompleted(jobState: SchedulerEventState) = jobState match {
    case Inited | Scheduled | Running | WaitForRetry => false
    case _ => true
  }

  def isSucceed(jobState: SchedulerEventState) = jobState == Succeed

  def isCompletedByStr(jobState: String): Boolean = jobState match {
      case "Inited" => false
      case "WaitForRetry" => false
      case "Scheduled" => false
      case "Running" => false
      case "Succeed" => true
      case "Failed" => true
      case "Cancelled" => true
      case "Timeout" => true
      case _ => true
  }
}