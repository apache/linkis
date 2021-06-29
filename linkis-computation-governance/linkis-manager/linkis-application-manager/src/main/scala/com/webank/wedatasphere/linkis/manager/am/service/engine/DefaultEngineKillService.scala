/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineInfoClearRequest}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.stereotype.Service


@Service
class DefaultEngineKillService extends AbstractEngineService with EngineKillService with Logging {

  @Receiver
  override def killEngine(engineInfoClearRequest: EngineInfoClearRequest): Unit = {
    info(s"Start to kill engine invoke enginePointer ${engineInfoClearRequest.getEngineNode.getServiceInstance}")
    getEMService().stopEngine(engineInfoClearRequest.getEngineNode, engineInfoClearRequest.getEngineNode.getEMNode)
    info(s"Finished to kill engine invoke enginePointer ${engineInfoClearRequest.getEngineNode.getServiceInstance}")
  }

  @Receiver
  override def dealEngineRelease(engineConnReleaseRequest: EngineConnReleaseRequest): Unit = {
    info(s"Start to kill engine , with msg : ${engineConnReleaseRequest.getMsg}")
    if (null == engineConnReleaseRequest.getServiceInstance) {
      warn(s"Invalid empty serviceInstance, will not kill engine.")
      return
    }
    val engineNode = getEngineNodeManager.getEngineNode(engineConnReleaseRequest.getServiceInstance)
    if (null != engineNode) {
      getEMService().stopEngine(engineNode, engineNode.getEMNode)
      info(s"Finished to kill engine.")
    } else {
      warn(s"Cannot find valid engineNode from serviceInstance : ${engineConnReleaseRequest.getServiceInstance.toString}")
    }
  }
}
