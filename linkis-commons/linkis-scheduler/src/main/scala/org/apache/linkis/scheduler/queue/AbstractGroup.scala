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

import org.apache.linkis.scheduler.queue.GroupStatus.GroupStatus

abstract class AbstractGroup extends Group {

  private var _status: GroupStatus = _
  private var maxRunningJobs: Int = _
  private var maxAllowRunningJobs: Int = 0
  private var maxAskExecutorTimes: Long = 0L

  def setMaxRunningJobs(maxRunningJobs: Int): Unit = this.maxRunningJobs = maxRunningJobs
  def getMaxRunningJobs: Int = maxRunningJobs

  def setMaxAllowRunningJobs(maxAllowRunningJobs: Int): Unit = this.maxAllowRunningJobs =
    maxAllowRunningJobs

  def getMaxAllowRunningJobs: Int =
    if (maxAllowRunningJobs <= 0) maxRunningJobs else Math.min(maxAllowRunningJobs, maxRunningJobs)

  def setMaxAskExecutorTimes(maxAskExecutorTimes: Long): Unit = this.maxAskExecutorTimes =
    maxAskExecutorTimes

  def getMaxAskExecutorTimes: Long = maxAskExecutorTimes

  override def getStatus: GroupStatus = _status
  def setStatus(status: GroupStatus): Unit = this._status = status
}
