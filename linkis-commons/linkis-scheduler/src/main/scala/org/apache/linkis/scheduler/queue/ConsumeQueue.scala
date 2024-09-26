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

abstract class ConsumeQueue {
  def remove(event: SchedulerEvent): Unit
  def getWaitingEvents: Array[SchedulerEvent]
  def size: Int

  def waitingSize: Int
  def isEmpty: Boolean
  def isFull: Boolean
  def clearAll(): Unit
  def get(event: SchedulerEvent): Option[SchedulerEvent]
  def get(index: Int): Option[SchedulerEvent]
  def getGroup: Group
  def setGroup(group: Group): Unit

  /**
   * Add one, if the queue is full, it will block until the queue is
   * available(添加一个，如果队列满了，将会一直阻塞，直到队列可用)
   * @return
   *   Return index subscript(返回index下标)
   */
  def put(event: SchedulerEvent): Int

  /**
   * Add one, return None if the queue is full(添加一个，如果队列满了，返回None)
   * @return
   */
  def offer(event: SchedulerEvent): Option[Int]

  /**
   * Get the latest SchedulerEvent of a group, if it does not exist, it will block
   * [<br>(获取某个group最新的SchedulerEvent，如果不存在，就一直阻塞<br>) This method will move the pointer(该方法会移动指针)
   * @return
   */
  def take(): SchedulerEvent

  /**
   * Get the latest SchedulerEvent of a group, if it does not exist, block the maximum waiting
   * time<br>(获取某个group最新的SchedulerEvent，如果不存在，就阻塞到最大等待时间<br>) This method will move the
   * pointer（该方法会移动指针）
   * @param mills
   *   Maximum waiting time（最大等待时间）
   * @return
   */
  def take(mills: Long): Option[SchedulerEvent]

  /**
   * Get the latest SchedulerEvent of a group and move the pointer to the next one. If not, return
   * directly to None 获取某个group最新的SchedulerEvent，并移动指针到下一个。如果没有，直接返回None
   * @return
   */
  def poll(): Option[SchedulerEvent]

  /**
   * Only get the latest SchedulerEvent of a group, and do not move the pointer. If not, return
   * directly to None 只获取某个group最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @return
   */
  def peek(): Option[SchedulerEvent]

  /**
   * Get the latest SchedulerEvent whose group satisfies the condition and does not move the
   * pointer. If not, return directly to None 获取某个group满足条件的最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @param op
   *   Satisfied condition（满足的条件）
   * @return
   */
  def peek(op: SchedulerEvent => Boolean): Option[SchedulerEvent]
}
