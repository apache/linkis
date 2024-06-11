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
import org.apache.linkis.scheduler.SchedulerContext

import java.util.concurrent.ExecutorService

abstract class Consumer(schedulerContext: SchedulerContext, executeService: ExecutorService)
    extends Runnable
    with Logging {

  var terminate = false

  def setConsumeQueue(consumeQueue: ConsumeQueue): Unit

  def getConsumeQueue: ConsumeQueue

  def getGroup: Group

  def setGroup(group: Group): Unit

  def getRunningEvents: Array[SchedulerEvent]

  def getMaxRunningEvents: Int

  def getRunningSize: Int

  def getWaitingSize: Int

  def start(): Unit

  def shutdown(): Unit = {
    terminate = true
    logger.info(s"$toString stopped!")
  }

  override def toString: String = getGroup.getGroupName + "Consumer"
}
