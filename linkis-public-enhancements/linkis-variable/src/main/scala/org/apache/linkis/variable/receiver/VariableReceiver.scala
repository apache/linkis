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

package org.apache.linkis.variable.receiver

import org.apache.linkis.protocol.variable.{RequestQueryAppVariable, RequestQueryGlobalVariable}
import org.apache.linkis.rpc.{Receiver, Sender}
import org.apache.linkis.variable.service.VariableService

import scala.concurrent.duration.Duration

class VariableReceiver extends Receiver {

  private var variableService: VariableService = _

  def this(variableService: VariableService) = {
    this()
    this.variableService = variableService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case RequestQueryGlobalVariable(userName: String) =>
      variableService.queryGolbalVariable(userName)
    case e: RequestQueryAppVariable =>
      variableService.queryAppVariable(e.userName, e.creator, e.appName)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
