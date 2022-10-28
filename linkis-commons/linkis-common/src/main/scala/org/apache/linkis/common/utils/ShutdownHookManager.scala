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

package org.apache.linkis.common.utils

import java.util.PriorityQueue

import org.slf4j.{Logger, LoggerFactory}

private[linkis] object ShutdownHookManager {

  val DEFAULT_SHUTDOWN_ORDER: Int = Int.MaxValue

  private lazy val shutdownHooks = {
    val manager = new LinkisShutdownHookManager()
    manager.install()
    manager
  }

  /**
   * This detects whether the JVM is shutting down by Runtime#addShutdownHook throwing an
   * IllegalStateException.
   */
  def inShutdown(): Boolean = {
    try {
      val hook = new Thread {
        override def run(): Unit = {}
      }
      // scalastyle:off runtimeaddshutdownhook
      Runtime.getRuntime.addShutdownHook(hook)
      // scalastyle:on runtimeaddshutdownhook
      Runtime.getRuntime.removeShutdownHook(hook)
    } catch {
      case _: IllegalStateException => return true
    }
    false
  }

  /**
   * Adds a shutdown hook with default order.
   *
   * @param hook
   *   The code to run during shutdown.
   * @return
   *   A handle that can be used to unregister the shutdown hook.
   */
  def addShutdownHook(hook: => Unit): LinkisShutdownHook = {
    addShutdownHook(DEFAULT_SHUTDOWN_ORDER)(hook)
  }

  /**
   * Adds a shutdown hook with the given order. Hooks with lower order values run first.
   *
   * @param hook
   *   The code to run during shutdown.
   * @return
   *   A handle that can be used to unregister the shutdown hook.
   */
  def addShutdownHook(order: Int)(hook: => Unit): LinkisShutdownHook = {
    shutdownHooks.add(order, hook)
  }

  /**
   * Remove a previously installed shutdown hook.
   *
   * @param ref
   *   A handle returned by `addShutdownHook`.
   * @return
   *   Whether the hook was removed.
   */
  def removeShutdownHook(ref: LinkisShutdownHook): Boolean = {
    shutdownHooks.remove(ref)
  }

}

private[utils] class LinkisShutdownHookManager {

  private val hooks = new PriorityQueue[LinkisShutdownHook]()
  @volatile private var shuttingDown = false

  implicit val logger: Logger = LoggerFactory.getLogger(classOf[LinkisShutdownHookManager])

  def install(): Unit = {
    val hookTask = new Runnable() {
      override def run(): Unit = runAll()
    }
    // scalastyle:off runtimeaddshutdownhook
    Runtime.getRuntime.addShutdownHook(new Thread(hookTask))
    // scalastyle:on runtimeaddshutdownhook
  }

  def runAll(): Unit = {
    shuttingDown = true
    var nextHook: LinkisShutdownHook = null
    while ({ nextHook = hooks.synchronized { hooks.poll() }; nextHook != null }) {
      Utils.tryAndWarn(nextHook.run())
    }
  }

  def add(order: Int, hook: => Unit): LinkisShutdownHook = {
    hooks.synchronized {
      if (shuttingDown) {
        throw new IllegalStateException("Shutdown hooks cannot be modified during shutdown.")
      }
      val hookRef = new LinkisShutdownHook(order, hook)
      hooks.add(hookRef)
      hookRef
    }
  }

  def remove(ref: LinkisShutdownHook): Boolean = {
    hooks.synchronized { hooks.remove(ref) }
  }

}

private[linkis] class LinkisShutdownHook(private val priority: Int, hook: => Unit)
    extends Comparable[LinkisShutdownHook] {

  override def compareTo(other: LinkisShutdownHook): Int = priority.compareTo(other.priority)

  def run(): Unit = hook

}
