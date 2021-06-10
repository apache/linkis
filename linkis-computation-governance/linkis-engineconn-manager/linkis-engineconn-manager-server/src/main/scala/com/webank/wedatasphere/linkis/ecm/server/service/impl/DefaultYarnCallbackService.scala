/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.ecm.server.service.impl

import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.listener.{YarnAppIdCallbackEvent, YarnInfoCallbackEvent}
import com.webank.wedatasphere.linkis.ecm.server.service.YarnCallbackService
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.protocol.callback.{YarnAPPIdCallbackProtocol, YarnInfoCallbackProtocol}


class DefaultYarnCallbackService extends YarnCallbackService {

  @Receiver
  override def dealApplicationId(protocol: YarnAPPIdCallbackProtocol): Unit = {
    LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(YarnAppIdCallbackEvent(protocol))
  }

  @Receiver
  override def dealApplicationURI(protocol: YarnInfoCallbackProtocol): Unit = {
    LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(YarnInfoCallbackEvent(protocol))
  }
}
