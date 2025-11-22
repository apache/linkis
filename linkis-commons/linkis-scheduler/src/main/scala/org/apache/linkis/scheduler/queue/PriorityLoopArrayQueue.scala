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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.scheduler.conf.SchedulerConfiguration

import java.util
import java.util.Comparator
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 优先级队列元素
 * @param element
 *   实际元素
 * @param priority
 *   优先级
 * @param index
 *   唯一索引
 */
case class PriorityQueueElement(element: Any, priority: Int, index: Int)

/**
 * 固定大小集合，元素满后会移除最先插入集合的元素
 * @param maxSize
 *   集合大小
 * @tparam K
 * @tparam V
 */
class FixedSizeCollection[K, V](val maxSize: Int) extends util.LinkedHashMap[K, V] {
  // 当集合大小超过最大值时，返回true，自动删除最老的元素
  protected override def removeEldestEntry(eldest: util.Map.Entry[K, V]): Boolean = size > maxSize
}

/**
 * 优先级队列，优先级相同时先进先出
 * @param group
 */
class PriorityLoopArrayQueue(var group: Group) extends ConsumeQueue with Logging {

  private val maxCapacity = group.getMaximumCapacity

  /** 优先级队列 */
  private val priorityEventQueue = new PriorityBlockingQueue[PriorityQueueElement](
    group.getMaximumCapacity,
    new Comparator[PriorityQueueElement] {

      override def compare(o1: PriorityQueueElement, o2: PriorityQueueElement): Int =
        if (o1.priority != o2.priority) o2.priority - o1.priority
        else o1.index - o2.index

    }
  )

  /** 累加器 1.越先进队列值越小，优先级相同时控制先进先出 2.队列元素唯一索引，不会重复 */
  private val index = new AtomicInteger

  /** 记录队列中当前所有元素索引，元素存入优先级队列时添加，从优先级队列移除时删除 */
  private val indexMap = new util.HashMap[Int, SchedulerEvent]()

  /** 记录已经消费的元素，会有固定缓存大小，默认1000，元素从优先级队列移除时添加 */
  private val fixedSizeCollection =
    new FixedSizeCollection[Integer, SchedulerEvent](
      SchedulerConfiguration.MAX_PRIORITY_QUEUE_CACHE_SIZE
    )

  private val rwLock = new ReentrantReadWriteLock

  protected[this] var realSize = size
  override def isEmpty: Boolean = size <= 0
  override def isFull: Boolean = size >= maxCapacity
  def size: Int = priorityEventQueue.size

  /**
   * 将元素添加进队列
   * @param element
   * @return
   */
  private def addToPriorityQueue(element: PriorityQueueElement): Boolean = {
    priorityEventQueue.offer(element)
    rwLock.writeLock.lock
    Utils.tryFinally(indexMap.put(element.index, element.element.asInstanceOf[SchedulerEvent]))(
      rwLock.writeLock.unlock()
    )
    true
  }

  /**
   * 从队列中获取并移除元素
   * @return
   */
  private def getAndRemoveTop: SchedulerEvent = {
    val top: PriorityQueueElement = priorityEventQueue.take()
    rwLock.writeLock.lock
    Utils.tryFinally {
      indexMap.remove(top.index)
      fixedSizeCollection.put(top.index, top.element.asInstanceOf[SchedulerEvent])
    }(rwLock.writeLock.unlock())
    top.element.asInstanceOf[SchedulerEvent]
  }

  override def remove(event: SchedulerEvent): Unit = {
    get(event).foreach(x => x.cancel())
  }

  override def getWaitingEvents: Array[SchedulerEvent] = {
    toIndexedSeq
      .filter(x =>
        x.getState.equals(SchedulerEventState.Inited) || x.getState
          .equals(SchedulerEventState.Scheduled)
      )
      .toArray
  }

  override def clearAll(): Unit = priorityEventQueue synchronized {
    realSize = 0
    index.set(0)
    priorityEventQueue.clear()
    fixedSizeCollection.clear()
    indexMap.clear()
  }

  override def get(event: SchedulerEvent): Option[SchedulerEvent] = {
    val eventSeq = toIndexedSeq.filter(x => x.getId.equals(event.getId)).seq
    if (eventSeq.size > 0) Some(eventSeq(0)) else None
  }

  /**
   * 根据索引获取队列元素
   * @param index
   * @return
   */
  override def get(index: Int): Option[SchedulerEvent] = {
    if (!indexMap.containsKey(index) && !fixedSizeCollection.containsKey(index)) {
      throw new IllegalArgumentException(
        "The index " + index + " has already been deleted, now index must be better than " + index
      )
    }
    rwLock.readLock().lock()
    Utils.tryFinally {
      if (fixedSizeCollection.get(index) != null) Option(fixedSizeCollection.get(index))
      else Option(indexMap.get(index))
    }(rwLock.readLock().unlock())
  }

  override def getGroup: Group = group

  override def setGroup(group: Group): Unit = {
    this.group = group
  }

  def toIndexedSeq: IndexedSeq[SchedulerEvent] = if (size == 0) {
    IndexedSeq.empty[SchedulerEvent]
  } else {
    priorityEventQueue
      .toArray()
      .map(_.asInstanceOf[PriorityQueueElement].element.asInstanceOf[SchedulerEvent])
      .toIndexedSeq
  }

  def add(event: SchedulerEvent): Int = {
    // 每次添加的时候需要给计数器+1，优先级相同时，控制先进先出
    event.setIndex(index.addAndGet(1))
    addToPriorityQueue(PriorityQueueElement(event, event.getPriority, event.getIndex))
    event.getIndex
  }

  override def waitingSize: Int = size

  /**
   * Add one, if the queue is full, it will block until the queue is
   * available（添加一个，如果队列满了，将会一直阻塞，直到队列可用）
   *
   * @return
   *   Return index subscript（返回index下标）
   */
  override def put(event: SchedulerEvent): Int = {
    add(event)
  }

  /**
   * Add one, return None if the queue is full（添加一个，如果队列满了，返回None）
   *
   * @return
   */
  override def offer(event: SchedulerEvent): Option[Int] = {
    if (isFull) None else Some(add(event))
  }

  /**
   * Get the latest SchedulerEvent of a group, if it does not exist, it will block
   * [<br>（获取某个group最新的SchedulerEvent，如果不存在，就一直阻塞<br>） This method will move the pointer（该方法会移动指针）
   *
   * @return
   */
  override def take(): SchedulerEvent = {
    getAndRemoveTop
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
    if (waitingSize == 0) {
      Thread.sleep(mills)
    }
    if (waitingSize == 0) None else Option(getAndRemoveTop)
  }

  /**
   * Get the latest SchedulerEvent of a group and move the pointer to the next one. If not, return
   * directly to None 获取某个group最新的SchedulerEvent，并移动指针到下一个。如果没有，直接返回None
   *
   * @return
   */
  override def poll(): Option[SchedulerEvent] = {
    if (waitingSize == 0) None
    else Option(getAndRemoveTop)
  }

  /**
   * Only get the latest SchedulerEvent of a group, and do not move the pointer. If not, return
   * directly to None 只获取某个group最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   *
   * @return
   */
  override def peek(): Option[SchedulerEvent] = {
    val ele: PriorityQueueElement = priorityEventQueue.peek()
    if (ele == null) None else Option(ele.element.asInstanceOf[SchedulerEvent])
  }

  /**
   * Get the latest SchedulerEvent whose group satisfies the condition and does not move the
   * pointer. If not, return directly to None 获取某个group满足条件的最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   * @param op
   *   满足的条件
   * @return
   */
  override def peek(op: (SchedulerEvent) => Boolean): Option[SchedulerEvent] = {
    val ele: PriorityQueueElement = priorityEventQueue.peek()
    if (ele == null) return None
    val event: Option[SchedulerEvent] = Option(
      priorityEventQueue.peek().element.asInstanceOf[SchedulerEvent]
    )
    if (op(event.get)) event else None
  }

}
