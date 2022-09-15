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

package org.apache.linkis.common.collection

class LoopArray[T](maxCapacity: Int) {

  private val eventQueue: Array[Any] = new Array[Any](maxCapacity)

  def this() = this(32)

  protected[this] var realSize = 0
  private var flag = 0
  private var tail = 0

  def add(event: T): T = {
    var t = null.asInstanceOf[T]
    eventQueue synchronized {
      val index = (tail + 1) % maxCapacity
      if (index == flag) {
        flag = (flag + 1) % maxCapacity
        realSize += 1
      }
      t = eventQueue(tail).asInstanceOf[T]
      eventQueue(tail) = event
      tail = index
    }
    t
  }

  @throws(classOf[IllegalArgumentException])
  def get(index: Int): T = eventQueue synchronized {
    val curMax = max
    if (index < realSize) {
      throw new IllegalArgumentException(
        "The index " + index + " has already been deleted, now index must be better than " + realSize
      )
    } else if (index > curMax) {
      throw new IllegalArgumentException("The index " + index + " must be less than " + curMax)
    }

    eventQueue(index % maxCapacity).asInstanceOf[T]
  }

  def clear(): Unit = eventQueue synchronized {
    flag = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  def min: Int = realSize

  def max: Int = {
    var _size = filledSize
    if (_size == 0) {
      _size = 1
    }
    realSize + _size - 1
  }

  private def filledSize: Int = if (tail >= flag) tail - flag else tail + maxCapacity - flag

  def size: Int = filledSize

  def isFull: Boolean = filledSize == maxCapacity - 1

  def nonEmpty: Boolean = size > 0

  def toList: List[T] = toIndexedSeq.toList

  def toIndexedSeq: IndexedSeq[T] = if (filledSize == 0) IndexedSeq.empty[T]
  else eventQueue synchronized { (min to max).map(get) }

}

class BlockingLoopArray[T](maxCapacity: Int = 32) extends LoopArray[T](maxCapacity) {

  private val writeLock = new Array[Byte](0)
  private val readLock = new Array[Byte](0)

  private var takeIndex = 0

  override def add(event: T): T = throw new IllegalAccessException("not supported method!")

  /**
   * Add one, if the queue is full, it will block until the queue is
   * available（添加一个，如果队列满了，将会一直阻塞，直到队列可用）
   * @param event
   * @return
   *   Always return true（总是返回true）
   */
  def put(event: T): Boolean = {
    writeLock synchronized {
      while (isFull) writeLock.wait(1000)
      super.add(event)
    }
    readLock synchronized { readLock.notify() }
    true
  }

  /**
   * Add one, return FALSE if the queue is full（添加一个，如果队列满了，返回FALSE)
   * @param event
   * @return
   */
  def offer(event: T): Boolean = if (isFull) false
  else {
    writeLock synchronized {
      if (isFull) return false
      else super.add(event)
    }
    readLock synchronized { readLock.notify() }
    true
  }

  /**
   * Get the latest one, if not, it will block until all new ones are
   * added(获取最新的一个，如果没有，将会一直阻塞，直到有的新的添加进来)
   * @return
   */
  def take(): T = {
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
    t
  }

  /**
   * Get the latest one, if not, it will return None.(获取最新的一个，如果没有，将会返回None.) Note: This method does
   * not move the pointer(注意：该方法不会移动指针)
   * @return
   */
  def peek(): Option[T] = readLock synchronized {
    if (waitingSize == 0 || takeIndex > max) None
    else if (takeIndex < min) Some(get(min))
    else Option(get(takeIndex))
  }

  /**
   * Get the latest one, if not, it will return None.(获取最新的一个，如果没有，将会返回None.) Note: This method will
   * move the pointer(注意：该方法会移动指针)
   * @return
   */
  def poll(): Option[T] = {
    if (waitingSize == 0) return None
    val event = readLock synchronized {
      val _min = min
      val _max = max
      if (takeIndex < _min) takeIndex = _min
      else if (takeIndex > _max) return None
      val t = get(takeIndex)
      takeIndex += 1
      Option(t)
    }
    writeLock synchronized { writeLock.notify() }
    event
  }

  override def isFull: Boolean = super.isFull && takeIndex == realSize

  def waitingSize: Int = if (takeIndex <= realSize) super.size
  else {
    val length = super.size - takeIndex + realSize
    if (length < 0) 0 else length
  }

  override def clear(): Unit = readLock synchronized {
    takeIndex = 0
    super.clear()
  }

  override def toIndexedSeq: IndexedSeq[T] = if (waitingSize == 0) IndexedSeq.empty[T]
  else readLock synchronized { (takeIndex to max).map(get) }

}

object LoopArray {

  def apply[T](maxCapacity: Int): LoopArray[T] = new LoopArray[T](maxCapacity)

  def apply[T](): LoopArray[T] = new LoopArray[T]()
}
