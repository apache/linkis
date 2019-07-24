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

package com.webank.wedatasphere.linkis.rpc

import com.webank.wedatasphere.linkis.entranceclient
import com.webank.wedatasphere.linkis.entranceclient.EngineApplicationNameFactory

/**
  * Created by johnnwang on 2019/1/22.
  */
class ClientReceiverChooser extends ReceiverChooser with entranceclient.EngineManagerApplicationNameFactory with EngineApplicationNameFactory {
  private var receiver: Option[Receiver] = None
  def setReceiver(receiver: Receiver): Unit = {
    this.receiver = Option(receiver)
    RPCSpringBeanCache.registerReceiverChooser(this)
  }
  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = receiver.find{ _ =>
    val applicationName = event.serviceInstance.getApplicationName
    applicationName == getEngineApplicationName || applicationName == getEngineManagerApplicationName
  }
}
