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

import org.apache.linkis.protocol.variable.VariableProtocol
import org.apache.linkis.rpc.{Receiver, ReceiverChooser, RPCMessageEvent}
import org.apache.linkis.variable.service.VariableService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class VariableReceiverChooser extends ReceiverChooser {

  @Autowired
  private var variableService: VariableService = _

  private var receiver: Option[VariableReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new VariableReceiver(variableService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: VariableProtocol => receiver
    case _ => None
  }

}
