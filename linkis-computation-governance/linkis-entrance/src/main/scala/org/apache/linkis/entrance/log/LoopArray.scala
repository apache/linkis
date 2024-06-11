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

package org.apache.linkis.entrance.log

class LoopArray[T](maxCapacity: Int) {

  private val eventQueue: Array[Any] = new Array[Any](maxCapacity)

  def this() = this(32)

  // realSize 游标之前的数据 已经被重写覆盖了
  // The data before realSize cursor has been overwritten by rewriting
  protected[this] var realSize = 0

  // the loop begin indx
  private var front = 0

  // the loop last index
  // 尾部 下一个存储的游标
  private var tail = 0

  private var clearEleNums = 0

  def add(event: T): T = {
    var t = null.asInstanceOf[T]
    eventQueue synchronized {
      val nextIndex = (tail + 1) % maxCapacity
      // 首尾相遇 第一次循环队列满了，后续所有add动作 nextIndex和front都是相等的 front指针不断往前循环移动
      // When the first and last ends meet, the first circular queue is full, and all subsequent add actions nextIndex and front are equal.
      // The front pointer continues to move forward in a circular motion.
      if (nextIndex == front) {
        front = (front + 1) % maxCapacity
        realSize += 1
      }
      t = eventQueue(tail).asInstanceOf[T]
      eventQueue(tail) = event
      tail = nextIndex
    }
    t
  }

  def get(index: Int): T = eventQueue synchronized {
    val _max = max
    if (index < realSize) {
      throw new IllegalArgumentException(
        "The index " + index + " has already been deleted, now index must be bigger than " + realSize
      )
    } else if (index > _max) {
      throw new IllegalArgumentException("The index " + index + " must be less than " + _max)
    }
    val _index = (front + (index - realSize + maxCapacity - 1)) % maxCapacity
    eventQueue(_index).asInstanceOf[T]
  }

  def clear(): Unit = eventQueue synchronized {
    front = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  def fakeClear(): Unit = eventQueue synchronized {
    clearEleNums = clearEleNums + size
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  def min: Int = realSize

  def max: Int = {
    var _size = filledSize
    if (_size == 0) {
      _size = 1
    }
    realSize + _size
  }

  def fakeClearEleNums: Int = clearEleNums

  private def filledSize = {
    if (tail == front && tail == 0) {
      0
    } else if (tail > front) {
      tail - front
    } else {
      tail + maxCapacity - front
    }
  }

  def size: Int = filledSize

  def isFull: Boolean = filledSize == maxCapacity - 1

  // If it is not empty, it means that the loop queue is full this round.
  // 不为空 说明本轮 循环队列满了
  def isNextOneEmpty(): Boolean = {

    eventQueue(tail).asInstanceOf[T] == null

  }

  def isEmpty: Boolean = size == 0

  def toList: List[T] = toIndexedSeq.toList

  def toIndexedSeq: IndexedSeq[T] = if (filledSize == 0) IndexedSeq.empty[T]
  else eventQueue synchronized { (min to max).map(get) }

}

object LoopArray {
  def apply[T](maxCapacity: Int): LoopArray[T] = new LoopArray(maxCapacity)

  def apply[T](): LoopArray[T] = new LoopArray()
}
