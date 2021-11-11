/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.utils

import sun.misc.{Signal, SignalHandler}

import scala.collection.mutable.ArrayBuffer


object ShutdownUtils {

  private val shutdownRunners = ArrayBuffer[ShutdownRunner]()

  def addShutdownHook(runnable: Runnable): Unit = addShutdownHook(Int.MaxValue, runnable)

  def addShutdownHook(order: Int, runnable: Runnable): Unit =
    shutdownRunners synchronized shutdownRunners += new DefaultShutdownRunner(order, runnable)

  def addShutdownHook(hook: => Unit): Unit = addShutdownHook(Int.MaxValue, hook)

  def addShutdownHook(order: Int, hook: => Unit): Unit =
    shutdownRunners synchronized shutdownRunners += new FunctionShutdownRunner(order, hook)

  def addShutdownHook(shutdownRunner: ShutdownRunner): Unit =
    shutdownRunners synchronized shutdownRunners += shutdownRunner
   private val signals = Array("TERM", "HUP", "INT").map(signal => Utils.tryQuietly(new Signal(signal))).filter(_ != null)
  private val signalHandler = new SignalHandler {
    override def handle(signal: Signal): Unit = {
      val hooks = shutdownRunners.sortBy(_.order).toArray.map{
        case m: DefaultShutdownRunner =>
          Utils.defaultScheduler.execute(m)
          m
        case m =>
          val runnable = new DefaultShutdownRunner(m.order, m)
          Utils.defaultScheduler.execute(runnable)
          runnable
      }
      val startTime = System.currentTimeMillis
      ShutdownUtils synchronized {
        while(System.currentTimeMillis - startTime < 30000 && hooks.exists(!_.isCompleted))
          ShutdownUtils.wait(3000)
      }
      System.exit(0)
    }
  }
  signals.foreach(Signal.handle(_, signalHandler))
}
trait ShutdownRunner extends Runnable {
  val order: Int
}
class DefaultShutdownRunner(override val order: Int,
                            runnable: Runnable) extends ShutdownRunner {
  private var completed = false
  override def run(): Unit = Utils.tryFinally(runnable.run()){
    completed = true
    ShutdownUtils synchronized ShutdownUtils.notify()
  }
  def isCompleted = completed
}
class FunctionShutdownRunner(override val order: Int,
                             hook: => Unit) extends ShutdownRunner {
  override def run(): Unit = hook
}