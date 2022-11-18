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

package org.apache.linkis.udf.api.rpc

import org.apache.linkis.rpc.{Receiver, ReceiverChooser, RPCMessageEvent}
import org.apache.linkis.udf.service.{UDFService, UDFTreeService}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class UdfReceiverChooser extends ReceiverChooser {

  @Autowired
  private var udfTreeService: UDFTreeService = _

  @Autowired
  private var udfService: UDFService = _

  private var udfReceiver: Option[UdfReceiver] = None

  @PostConstruct
  def init(): Unit = udfReceiver = Some(new UdfReceiver(udfTreeService, udfService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: UdfProtocol => udfReceiver
    case _ => None
  }

}
