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

package org.apache.linkis.manager.am.pointer

import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.exception.AMErrorException
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceRequest
import org.apache.linkis.rpc.Sender

import org.springframework.stereotype.Component

@Component
class DefaultEngineConnPluginPointer extends EngineConnPluginPointer {

  private def getEngineConnPluginSender: Sender =
    Sender.getSender(AMConfiguration.ENGINECONN_SPRING_APPLICATION_NAME.getValue)

  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    getEngineConnPluginSender.ask(engineResourceRequest) match {
      case nodeResource: NodeResource =>
        nodeResource
      case ecpTaskException: Exception =>
        throw new AMErrorException(
          AMConstant.ENGINE_ERROR_CODE,
          ecpTaskException.getMessage,
          ecpTaskException
        )
      case _ =>
        throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, s"Failed to create engineResource")
    }
  }

}
