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

package org.apache.linkis.orchestrator.computation.operation.log

import org.apache.linkis.orchestrator.Orchestration

import java.io.Closeable

import scala.collection.mutable.ArrayBuffer

/**
 */
class LogProcessor(rootExecTaskId: String, orchestration: Orchestration, logOperation: LogOperation)
    extends Closeable {

  private val listener = new ArrayBuffer[LogEvent => Unit]()

  def registerLogNotify(notify: LogEvent => Unit): Unit = {
    listener += notify
  }

  def getLog(): String = {
    null
  }

  def writeLog(log: String): Unit = {
    val logEvent = LogEvent(orchestration, log)
    listener.foreach(notify => notify.apply(logEvent))
  }

  override def close(): Unit = {
    logOperation.removeLogProcessor(rootExecTaskId)
    listener.clear()
  }

}
