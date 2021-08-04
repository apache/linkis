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

package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorException
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.EngineResourceRequest
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.stereotype.Component


@Component
class DefaultEngineConnPluginPointer extends EngineConnPluginPointer {

  private def getEngineConnPluginSender: Sender = Sender.getSender(AMConfiguration.ENGINECONN_SPRING_APPLICATION_NAME.getValue)

  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    getEngineConnPluginSender.ask(engineResourceRequest) match {
      case nodeResource: NodeResource =>
        nodeResource
      case _ =>
        throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, s"Failed to create engineResource")
    }
  }
}
