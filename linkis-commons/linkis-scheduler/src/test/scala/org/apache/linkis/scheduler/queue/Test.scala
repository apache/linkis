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

import java.util
import java.util.{PriorityQueue, Queue}

case class PriorityFIFOQueue() {
  case class QueueItem(item: Queue[String], priority: Int)

  import java.util.Comparator

  val cNode: Comparator[QueueItem] = new Comparator[QueueItem]() {
    override def compare(o1: QueueItem, o2: QueueItem): Int = o2.priority - o1.priority
  }

  private val queue = new PriorityQueue[QueueItem](cNode)
  private var _size = 0
  private var _count: Long = 0L

  def size: Int = _size

  def isEmpty: Boolean = _size == 0

  def enqueue(item: String, priority: Int): Unit = {
    val deque = new util.ArrayDeque[String]()
    deque.add(item)
    queue.add(QueueItem(deque, priority))
  }

}
