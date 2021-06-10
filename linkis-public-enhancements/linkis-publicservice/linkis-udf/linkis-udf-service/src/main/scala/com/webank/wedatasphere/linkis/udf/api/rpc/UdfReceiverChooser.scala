/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.udf.api.rpc

import com.webank.wedatasphere.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
import com.webank.wedatasphere.linkis.udf.service.UDFTreeService
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class UdfReceiverChooser extends ReceiverChooser {
  @Autowired
  private var udfTreeService: UDFTreeService = _
  private var udfReceiver: Option[UdfReceiver] = None

  @PostConstruct
  def init(): Unit = udfReceiver = Some(new UdfReceiver(udfTreeService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: UdfProtocol => udfReceiver
    case _ => None
  }
}
