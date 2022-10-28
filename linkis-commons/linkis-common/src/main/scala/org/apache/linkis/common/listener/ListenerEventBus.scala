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

package org.apache.linkis.common.listener

import org.apache.linkis.common.utils.{Logging, Utils}

import org.apache.commons.lang3.time.DateFormatUtils

import java.time.Duration
import java.util.concurrent.{ArrayBlockingQueue, CopyOnWriteArrayList, Future, TimeoutException}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import scala.util.control.NonFatal

trait ListenerBus[L <: EventListener, E <: Event] extends Logging {
  val self = this

  private val listeners = new CopyOnWriteArrayList[L]

  /**
   * Add a listener to listen events. This method is thread-safe and can be called in any thread.
   */
  final def addListener(listener: L): Unit = {
    listeners.add(listener)
    logger.info(toString + " add a new listener => " + listener.getClass)
  }

  /**
   * Remove a listener and it won't receive any events. This method is thread-safe and can be called
   * in any thread.
   */
  final def removeListener(listener: L): Unit = {
    listeners.remove(listener)
  }

  /**
   * Post the event to all registered listeners. The `postToAll` caller should guarantee calling
   * `postToAll` in the same thread for all events.
   */
  final def postToAll(event: E): Unit = {
    // JavaConverters can create a JIterableWrapper if we use asScala.
    // However, this method will be called frequently. To avoid the wrapper cost, here we use
    // Java Iterator directly.
    val iter = listeners.iterator
    while (iter.hasNext) {
      val listener = iter.next()
      Utils.tryCatch {
        doPostEvent(listener, event)
      } {
        case NonFatal(e) =>
          Utils.tryAndError(listener.onEventError(event, e))
        case t: Throwable => throw t
      }
    }
  }

  /**
   * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
   * thread for all listeners.
   */
  protected def doPostEvent(listener: L, event: E): Unit

}

abstract class ListenerEventBus[L <: EventListener, E <: Event](
    val eventQueueCapacity: Int,
    name: String
)(
    listenerConsumerThreadSize: Int = 5,
    listenerThreadMaxFreeTime: Long = Duration.ofMinutes(2).toMillis
) extends ListenerBus[L, E]
    with Logging {

  private lazy val eventQueue = new ArrayBlockingQueue[E](eventQueueCapacity)

  protected val executorService =
    Utils.newCachedThreadPool(listenerConsumerThreadSize + 2, name + "-Consumer-ThreadPool", true)

  private val eventDealThreads =
    Array.tabulate(listenerConsumerThreadSize)(new ListenerEventThread(_))

  private val started = new AtomicBoolean(false)
  private val stopped = new AtomicBoolean(false)

  private var listenerThread: Future[_] = _

  /**
   * Start sending events to attached listeners.
   *
   * This first sends out all buffered events posted before this listener bus has started, then
   * listens for any additional events asynchronously while the listener bus is still running. This
   * should only be called once.
   */
  def start(): Unit = {
    if (started.compareAndSet(false, true)) {
      listenerThread = executorService.submit(new Runnable {
        override def run(): Unit =
          while (!stopped.get) {
            val event = Utils.tryCatch(eventQueue.take()) { case t: InterruptedException =>
              logger.info(s"stopped $name thread.", t)
              return
            }
            while (!eventDealThreads.exists(_.putEvent(event)) && !stopped.get)
              Utils.tryAndError(Thread.sleep(1))
          }
      })
    } else {
      throw new IllegalStateException(s"$name already started!")
    }
  }

  protected val dropEvent: DropEvent = new IgnoreDropEvent

  def post(event: E): Unit = {
    if (stopped.get || executorService.isTerminated || (listenerThread.isDone && started.get())) {
      dropEvent.onBusStopped(event)
    } else if (!eventQueue.offer(event)) {
      dropEvent.onDropEvent(event)
    }
  }

  /**
   * For testing only. Wait until there are no more events in the queue, or until the specified time
   * has elapsed. Throw `TimeoutException` if the specified time elapsed before the queue emptied.
   * Exposed for testing.
   */
  @throws(classOf[TimeoutException])
  def waitUntilEmpty(timeoutMillis: Long): Unit = {
    val finishTime = System.currentTimeMillis + timeoutMillis
    while (!queueIsEmpty) {
      if (System.currentTimeMillis > finishTime) {
        throw new TimeoutException(
          s"The event queue is not empty after $timeoutMillis milliseconds"
        )
      }
      /* Sleep rather than using wait/notify, because this is used only for testing and
       * wait/notify add overhead in the general case. */
      Thread.sleep(10)
    }
  }

  /**
   * For testing only. Return whether the listener daemon thread is still alive. Exposed for
   * testing.
   */
  def listenerThreadIsAlive: Boolean = !listenerThread.isDone

  /**
   * Return whether the event queue is empty.
   *
   * The use of synchronized here guarantees that all events that once belonged to this queue have
   * already been processed by all attached listeners, if this returns true.
   */
  private def queueIsEmpty: Boolean = synchronized {
    eventQueue.isEmpty && !eventDealThreads.exists(_.isRunning)
  }

  /**
   * Stop the listener bus. It will wait until the queued events have been processed, but drop the
   * new events after stopping.
   */
  def stop(): Unit = {
    if (!started.get()) {
      throw new IllegalStateException(s"Attempted to stop $name that has not yet started!")
    }
    if (stopped.compareAndSet(false, true)) {
      // Call eventLock.release() so that listenerThread will poll `null` from `eventQueue` and know
      // `stop` is called.
      logger.info(s"try to stop $name thread.")
      //      eventLock.release()
      listenerThread.cancel(true)
      eventDealThreads.foreach(_.shutdown())
    } else {
      // Keep quiet
    }
  }

  override val toString: String = name + "-ListenerBus"

  trait DropEvent {
    def onDropEvent(event: E): Unit
    def onBusStopped(event: E): Unit
  }

  class IgnoreDropEvent extends DropEvent {
    private val droppedEventsCounter = new AtomicLong(0L)
    @volatile private var lastReportTimestamp = 0L
    private val logDroppedEvent = new AtomicBoolean(false)
    private val logStoppedEvent = new AtomicBoolean(false)

    executorService.submit(new Runnable {

      override def run(): Unit = while (true) {
        val droppedEvents = droppedEventsCounter.get
        if (droppedEvents > 0) {
          // Don't log too frequently
          if (System.currentTimeMillis() - lastReportTimestamp >= 60 * 1000) {
            // There may be multiple threads trying to decrease droppedEventsCounter.
            // Use "compareAndSet" to make sure only one thread can win.
            // And if another thread is increasing droppedEventsCounter, "compareAndSet" will fail and
            // then that thread will update it.
            if (droppedEventsCounter.compareAndSet(droppedEvents, 0)) {
              val prevLastReportTimestamp = lastReportTimestamp
              lastReportTimestamp = System.currentTimeMillis()
              logger.warn(
                s"Dropped $droppedEvents ListenerEvents since " +
                  DateFormatUtils.format(prevLastReportTimestamp, "yyyy-MM-dd HH:mm:ss")
              )
            }
          }
        }
        Utils.tryQuietly(Thread.sleep(600000))
      }

    })

    /**
     * If the event queue exceeds its capacity, the new events will be dropped. The subclasses will
     * be notified with the dropped events.
     *
     * Note: `onDropEvent` can be called in any thread.
     */
    def onDropEvent(event: E): Unit = {
      droppedEventsCounter.incrementAndGet()
      if (logDroppedEvent.compareAndSet(false, true)) {
        // Only log the following message once to avoid duplicated annoying logs.
        logger.error(
          "Dropping ListenerEvent because no remaining room in event queue. " +
            "This likely means one of the Listeners is too slow and cannot keep up with " +
            "the rate at which tasks are being started by the scheduler."
        )
      }
    }

    override def onBusStopped(event: E): Unit = {
      droppedEventsCounter.incrementAndGet()
      if (logStoppedEvent.compareAndSet(false, true)) {
        logger.error(s"$name has already stopped! Dropping event $event.")
      }
    }

  }

  protected class ListenerEventThread(index: Int) extends Runnable {
    private var future: Option[Future[_]] = None
    private var continue = true
    private var event: Option[E] = None
    private var lastEventDealTime = 0L

    def releaseFreeThread(): Unit = if (
        listenerThreadMaxFreeTime > 0 && future.isDefined && event.isEmpty && lastEventDealTime > 0 &&
        System.currentTimeMillis() - lastEventDealTime >= listenerThreadMaxFreeTime
    ) {
      synchronized {
        if (lastEventDealTime == 0 && future.isEmpty) return
        lastEventDealTime = 0L
        continue = false
        future.foreach(_.cancel(true))
        future = None
      }
    }

    def isRunning: Boolean = event.isDefined

    def putEvent(event: E): Boolean = if (this.event.isDefined) {
      false
    } else {
      synchronized {
        if (this.event.isDefined) false
        else {
          lastEventDealTime = System.currentTimeMillis()
          this.event = Some(event)
          if (future.isEmpty) future = Some(executorService.submit(this))
          else notify()
          true
        }
      }
    }

    override def run(): Unit = {
      val threadName = Thread.currentThread().getName
      val currentThreadName = s"$name-Thread-$index"
      Thread.currentThread().setName(currentThreadName)
      logger.info(s"$currentThreadName begin.")
      def threadRelease(): Unit = {
        logger.info(s"$currentThreadName released.")
        Thread.currentThread().setName(threadName)
      }
      while (continue) {
        synchronized {
          while (event.isEmpty)
            Utils.tryQuietly(
              wait(),
              _ => {
                threadRelease()
                return
              }
            )
        }
        Utils.tryFinally(event.foreach(postToAll))(synchronized {
          lastEventDealTime = System.currentTimeMillis()
          event = None
        })
      }
      threadRelease()
    }

    def shutdown(): Unit = {
      continue = false
      future.foreach(_.cancel(true))
    }

  }

}
