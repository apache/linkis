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
 
package org.apache.linkis.scheduler.executer

import java.io.Closeable

import org.apache.linkis.protocol.engine.EngineState


trait Executor extends Closeable {

  def getId: Long
  def execute(executeRequest: ExecuteRequest): ExecuteResponse

  def state: ExecutorState.ExecutorState

  def getExecutorInfo: ExecutorInfo
}
object ExecutorState {
  type ExecutorState = EngineState
  val Starting = EngineState.Starting
  val Idle = EngineState.Idle
  val Busy = EngineState.Busy
  val ShuttingDown = EngineState.ShuttingDown
  val Error = EngineState.Error
  val Dead = EngineState.Dead
  val Success = EngineState.Success

  def apply(x: Int): ExecutorState = EngineState.values()(x)

  def isCompleted(state: ExecutorState) = EngineState.isCompleted(state.asInstanceOf[EngineState])

  def isAvailable(state: ExecutorState) = EngineState.isAvailable(state.asInstanceOf[EngineState])
}