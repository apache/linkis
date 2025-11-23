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

trait Group {

  def getGroupName: String

  /**
   * The percentage of waiting Jobs in the entire ConsumeQueue（等待的Job占整个ConsumeQueue的百分比）
   * @return
   */
  def getInitCapacity: Int

  /**
   * The waiting Job accounts for the largest percentage of the entire
   * ConsumeQueue（等待的Job占整个ConsumeQueue的最大百分比）
   * @return
   */
  def getMaximumCapacity: Int

  def getStatus: GroupStatus.GroupStatus

  def belongTo(event: SchedulerEvent): Boolean

}

object GroupStatus extends Enumeration {
  type GroupStatus = Value
  val USING, UNUSED = Value
}
