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

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.listener.EngineConnPidCallbackEvent
import com.webank.wedatasphere.linkis.ecm.server.service.EngineConnPidCallbackService
import com.webank.wedatasphere.linkis.governance.common.protocol.task.ResponseEngineConnPid
import com.webank.wedatasphere.linkis.message.annotation.Receiver


class DefaultEngineConnPidCallbackService extends EngineConnPidCallbackService with Logging {

  @Receiver
  override def dealPid(protocol: ResponseEngineConnPid): Unit = {
    //1.设置pid
    //2.设置serviceInstance
    //3.状态为running
    LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnPidCallbackEvent(protocol))
  }


}
