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

package org.apache.linkis.engineconn.acessible.executor.hook

import org.apache.linkis.manager.common.protocol.engine.{
  EngineOperateRequest,
  EngineOperateResponse
}

import scala.collection.mutable.ArrayBuffer

trait OperationHook {
  def getName(): String

  def doPreOperation(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit

  def doPostOperation(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit

}

object OperationHook {
  private var operationHooks: ArrayBuffer[OperationHook] = new ArrayBuffer[OperationHook]()

  def registerOperationHook(operationHook: OperationHook): Unit = {
    operationHooks.append(operationHook)
  }

  def getOperationHooks(): Array[OperationHook] = operationHooks.toArray
}
