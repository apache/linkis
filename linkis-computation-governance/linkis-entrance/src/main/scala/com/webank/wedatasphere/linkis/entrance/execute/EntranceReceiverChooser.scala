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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.{ENGINE_MANAGER_SPRING_APPLICATION_NAME, ENGINE_SPRING_APPLICATION_NAME}
import com.webank.wedatasphere.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}

/**
  * Created by enjoyyin on 2019/3/15.
  */
class EntranceReceiverChooser(entranceContext: EntranceContext) extends ReceiverChooser {
  private val receiver = new EntranceReceiver(entranceContext)
  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = {
    val applicationName = event.serviceInstance.getApplicationName
    if (applicationName == ENGINE_MANAGER_SPRING_APPLICATION_NAME.getValue || applicationName == ENGINE_SPRING_APPLICATION_NAME.getValue)
      Option(receiver)
    else None
  }
}