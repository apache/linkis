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

package org.apache.linkis.engineconn.executor.entity

import org.apache.linkis.common.utils.Logging

trait Executor extends Logging {

  def getId: String

  def init(): Unit

  def tryReady(): Boolean

  def tryShutdown(): Boolean

  def tryFailed(): Boolean

  def trySucceed(): Boolean

  /**
   * 仅用于Kill Executor EngineConn kill 在AccessibleService
   */
  def close(): Unit = {
    logger.warn(s"Executor($getId) exit by close.")
  }

  def isClosed: Boolean

}

trait ConcurrentExecutor extends Executor {

  def getConcurrentLimit: Int

  def killAll(): Unit

  def hasTaskRunning(): Boolean

}
