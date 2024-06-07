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

package org.apache.linkis.engineconn.core.hook

import org.apache.linkis.common.utils.Logging

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class ShutdownHook extends Logging {

  var exitCode = 0

  private val lock = new ReentrantLock()
  private val condition = lock.newCondition()

  // Guarded by "lock"
  private var error: Throwable = null

  // Guarded by "lock"
  private var stopped: Boolean = false

  private val tryStopTimes = new AtomicInteger(0)

  private val maxTimes = 10;

  def notifyError(e: Throwable): Unit = {
    lock.lock()
    try {
      setExitCode(1)
      error = e
      logger.error("process exit reason: ", e)
      condition.signalAll()
    } finally {
      lock.unlock()
    }
  }

  def notifyStop(): Unit = {
    lock.lock()
    val num = tryStopTimes.incrementAndGet()
    try {
      setExitCode(0)
      stopped = true
      condition.signalAll()
    } finally {
      lock.unlock()
      if (num >= maxTimes) {
        logger.error(s"try to stop with times:${num}, now do system exit!!!")
        System.exit(0)
      }
    }
  }

  /**
   * Return `true` if it's stopped; or throw the reported error if `notifyError` has been called; or
   * `false` if the waiting time detectably elapsed before return from the method.
   */
  def waitForStopOrError(timeout: Long = -1): Boolean = {
    lock.lock()
    try {
      if (timeout < 0) {
        while (!stopped && error == null) {
          condition.await()
        }
      } else {
        var nanos = TimeUnit.MILLISECONDS.toNanos(timeout)
        while (!stopped && error == null && nanos > 0) {
          nanos = condition.awaitNanos(nanos)
        }
      }
      // If already had error, then throw it
      if (error != null) throw error
      // already stopped or timeout
      stopped
    } finally {
      lock.unlock()
    }
  }

  def setExitCode(exitCode: Int): Unit = this.exitCode = exitCode

  def getExitCode(): Int = exitCode

}

object ShutdownHook {

  private val shutdownHook = new ShutdownHook

  def getShutdownHook: ShutdownHook = shutdownHook

}
