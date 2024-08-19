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

class LoopArrayQueue(var group: Group) extends ConsumeQueue with Logging {
  private val eventQueue: Array[Any] = new Array[Any](group.getMaximumCapacity)
  private val maxCapacity: Int = group.getMaximumCapacity

  private val writeLock = new Array[Byte](0)
  private val readLock = new Array[Byte](0)

  private var flag = 0
  private var tail = 0
  private var takeIndex = 0

  protected[this] var realSize = 0

  private def filledSize: Int = if (tail >= flag) tail - flag else tail + maxCapacity - flag

  override def remove(event: SchedulerEvent): Unit = {
    get(event).foreach(x => x.cancel())
  }

  override def getWaitingEvents: Array[SchedulerEvent] = {
    eventQueue synchronized {
      toIndexedSeq
        .filter(x =>
          x.getState.equals(SchedulerEventState.Inited) || x.getState
            .equals(SchedulerEventState.Scheduled)
        )
        .toArray
    }
  }

  override def size: Int = filledSize

  override def isEmpty: Boolean = size == 0

  override def isFull: Boolean = filledSize == maxCapacity - 1 && takeIndex == realSize

  override def clearAll(): Unit = eventQueue synchronized {
    flag = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  override def get(event: SchedulerEvent): Option[SchedulerEvent] = {
    eventQueue synchronized {
      val eventSeq = toIndexedSeq.filter(x => x.getId.equals(event.getId)).seq
      if (eventSeq.size > 0) Some(eventSeq(0)) else None
    }
  }

  override def get(index: Int): Option[SchedulerEvent] = {
    var event: SchedulerEvent = null
    eventQueue synchronized {
      val _max = max
      if (index < realSize) {
        throw new IllegalArgumentException(
          "The index " + index + " has already been deleted, now index must be better than " + realSize
        )
      } else if (index > _max) {
        throw new IllegalArgumentException("The index " + index + " must be less than " + _max)
      }
      val _index = (flag + (index - realSize)) % maxCapacity
      event = eventQueue(_index).asInstanceOf[SchedulerEvent]
    }
    Option(event)
  }

  override def getGroup: Group = group

  override def setGroup(group: Group): Unit = {
    this.group = group
  }

  def toIndexedSeq: IndexedSeq[SchedulerEvent] = if (filledSize == 0) {
    IndexedSeq.empty[SchedulerEvent]
  } else eventQueue synchronized { (min to max).map(x => get(x).get).filter(x => x != None) }

  def add(event: SchedulerEvent): Int = {
    eventQueue synchronized {
      val index = (tail + 1) % maxCapacity
      if (index == flag) {
        flag = (flag + 1) % maxCapacity
        realSize += 1
      }
      eventQueue(tail) = event
      tail = index
    }
    max
  }

  override def waitingSize: Int = if (takeIndex <= realSize) size
  else {
    val length = size - takeIndex + realSize
    if (length < 0) 0 else length
  }

  def min: Int = realSize

  def max: Int = {
    var _size = filledSize
    if (_size == 0) {
      _size = 1
    }
    realSize + _size - 1
  }

  /**
   * Add one, if the queue is full, it will block until the queue is
   * available（添加一个，如果队列满了，将会一直阻塞，直到队列可用）
   *
   * @return
   *   Return index subscript（返回index下标）
   */
  override def put(event: SchedulerEvent): Int = {
    var index = -1
    writeLock synchronized {
      while (isFull) writeLock.wait(1000)
      index = add(event)
    }
    readLock synchronized { readLock.notify() }
    index
  }

  /**
   * Add one, return None if the queue is full（添加一个，如果队列满了，返回None）
   *
   * @return
   */
  override def offer(event: SchedulerEvent): Option[Int] = {
    var index = -1
    writeLock synchronized {
      if (isFull) return None
      else {
        index = add(event)
      }
    }
    readLock synchronized { readLock.notify() }
    Some(index)
  }

  /**
   * Get the latest SchedulerEvent of a group, if it does not exist, it will block
   * [<br>（获取某个group最新的SchedulerEvent，如果不存在，就一直阻塞<br>） This method will move the pointer（该方法会移动指针）
   *
   * @return
   */
  override def take(): SchedulerEvent = {
    val t = readLock synchronized {
      while (waitingSize == 0 || takeIndex > max) {
        readLock.wait(1000)
      }
      if (takeIndex < min) takeIndex = min
      val t = get(takeIndex)
      takeIndex += 1
      t
    }
    writeLock synchronized { writeLock.notify() }
    t.get
  }

  /**
   * Get the latest SchedulerEvent of a group, if it does not exist, block the maximum waiting
   * time<br>（获取某个group最新的SchedulerEvent，如果不存在，就阻塞到最大等待时间<br>） This method will move the
   * pointer（该方法会移动指针）
   * @param mills
   *   Maximum waiting time（最大等待时间）
   * @return
   */
  override def take(mills: Long): Option[SchedulerEvent] = {
    val t = readLock synchronized {
      if (waitingSize == 0 || takeIndex > max) readLock.wait(mills)
      if (waitingSize == 0 || takeIndex > max) return None
      if (takeIndex < min) takeIndex = min
      val t = get(takeIndex)
      takeIndex += 1
      t
    }
    writeLock synchronized { writeLock.notify() }
    t
  }

  /**
   * Get the latest SchedulerEvent of a group and move the pointer to the next one. If not, return
   * directly to None 获取某个group最新的SchedulerEvent，并移动指针到下一个。如果没有，直接返回None
   *
   * @return
   */
  override def poll(): Option[SchedulerEvent] = {
    val event = readLock synchronized {
      val _min = min
      val _max = max
      if (takeIndex < _min) takeIndex = _min
      else if (takeIndex > _max) {
        logger.info(s"none, notice...max: ${_max}, takeIndex: $takeIndex, realSize: $realSize.")
        return None
      }
      val t = get(takeIndex)
      if (t == null) {
        logger.info("null, notice...")
      }
      takeIndex += 1
      t
    }
    writeLock synchronized { writeLock.notify() }
    event
  }

  /**
   * Only get the latest SchedulerEvent of a group, and do not move the pointer. If not, return
   * directly to None 只获取某个group最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   *
   * @return
   */
  override def peek(): Option[SchedulerEvent] = readLock synchronized {
    if (waitingSize == 0 || takeIndex > max) None
    else if (takeIndex < min) get(min)
    else get(takeIndex)
  }

  /**
   * Get the latest SchedulerEvent whose group satisfies the condition and does not move the
   * pointer. If not, return directly to None 获取某个group满足条件的最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @param op
   *   满足的条件
   * @return
   */
  override def peek(op: (SchedulerEvent) => Boolean): Option[SchedulerEvent] = {
    if (waitingSize == 0 || takeIndex > max) None
    else if (takeIndex < min) {
      val event = get(min)
      if (op(event.get)) event else None
    } else {
      val event = get(takeIndex)
      if (op(event.get)) event else None
    }
  }

}
