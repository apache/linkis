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

package org.apache.linkis.monitor.utils.alert

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}

import java.util.concurrent.{Future, LinkedBlockingQueue}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

abstract class PooledAlertSender extends AlertSender with Logging {
  private val THREAD_POOL_SIZE = CommonVars[Int]("linkis.alert.pool.size", 5).getValue

  private val alertDescQ: LinkedBlockingQueue[AlertDesc] =
    new LinkedBlockingQueue[AlertDesc](1000)

  protected implicit val executors =
    Utils.newCachedExecutionContext(THREAD_POOL_SIZE, "alert-pool-thread-", false)

  private val stopped: AtomicBoolean = new AtomicBoolean(false)
  private val runningNumber: AtomicInteger = new AtomicInteger(0)
  private var future: Future[_] = _

  /**
   * add an alertDesc to queue
   *
   * @param alertDesc
   *   should encapsulates every information an alert platform needs for sending an alarm
   */
  def addAlertToPool(alertDesc: AlertDesc): Unit = {
    alertDescQ.add(alertDesc)
  }

  /**
   * describes actual actions for sending an alert
   *
   * @return
   *   true if it is a success
   */
  override def doSendAlert(alertDesc: AlertDesc): Boolean

  def start(): Unit = {
    future = Utils.defaultScheduler.submit(new Runnable() {
      override def run() {
        logger.info("Pooled alert thread started!")
        while (!stopped.get) {
          executors synchronized {
            while (!stopped.get && runningNumber.get >= THREAD_POOL_SIZE) {
              logger.info("Pooled alert thread is full, start waiting")
              executors.wait()
            }
          }
          logger.info("Pooled alert thread continue processing")

          if (stopped.get && alertDescQ.size() == 0) return
          val alertDesc = Utils.tryQuietly(alertDescQ.take)
          if (alertDesc == null) return
          executors.submit(new Runnable {
            override def run() {
              runningNumber.addAndGet(1)
              Utils.tryAndWarn {
                logger.info("sending alert , information: " + alertDesc)
                val ok = doSendAlert(alertDesc)
                if (!ok) {
                  warn("Failed to send alert: " + alertDesc)
                } else {
                  logger.info("successfully send alert: " + alertDesc)
                }
                runningNumber.decrementAndGet
                executors synchronized executors.notify
              }
            }
          })
        }
      }
    })
  }

  def shutdown(waitComplete: Boolean = true, timeoutMs: Long = -1): Unit = {
    logger.info("stopping the Pooled alert thread...")
    if (waitComplete) {
      val startTime = System.currentTimeMillis()
      while (
          (alertDescQ.size() > 0 || runningNumber
            .get() > 0) && (timeoutMs == -1 || System.currentTimeMillis() - startTime > timeoutMs)
      ) {
        Utils.tryQuietly(Thread.sleep(5 * 1000L))
      }
    }
    executors.shutdown
    stopped.set(true)
    future.cancel(true)
    logger.info("Pooled alert thread is stopped")
  }

}
