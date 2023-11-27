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

  def get(index: Int): T = eventQueue synchronized {
    val _max = max
    if (index < realSize) {
      throw new IllegalArgumentException(
        "The index " + index + " has already been deleted, now index must be bigger than " + realSize
      )
    } else if (index > _max) {
      throw new IllegalArgumentException("The index " + index + " must be less than " + _max)
    }
    val _index = (flag + (index - realSize + maxCapacity - 1)) % maxCapacity
    eventQueue(_index).asInstanceOf[T]
  }

  def clear(): Unit = eventQueue synchronized {
    flag = 0
    tail = 0
    realSize = 0
    (0 until maxCapacity).foreach(eventQueue(_) = null)
  }

  def fakeClear(): Unit = eventQueue synchronized {
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

  private def filledSize = if (tail >= flag) tail - flag else tail + maxCapacity - flag

  def size: Int = filledSize

  def isFull: Boolean = filledSize == maxCapacity - 1

  def nonEmpty: Boolean = size > 0

  def toList: List[T] = toIndexedSeq.toList

  def toIndexedSeq: IndexedSeq[T] = if (filledSize == 0) IndexedSeq.empty[T]
  else eventQueue synchronized { (min to max).map(get) }

}

object LoopArray {
  def apply[T](maxCapacity: Int): LoopArray[T] = new LoopArray(maxCapacity)

  def apply[T](): LoopArray[T] = new LoopArray()
}
