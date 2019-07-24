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

package com.webank.wedatasphere.linkis.enginemanager

import com.webank.wedatasphere.linkis.enginemanager.impl.{UserEngineResource, UserTimeoutEngineResource}
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, TimeoutRequestEngine}
import com.webank.wedatasphere.linkis.resourcemanager.Resource
import org.apache.commons.lang.StringUtils

/**
  * Created by johnnwang on 2018/10/11.
  */
trait AbstractEngineResourceFactory extends EngineResourceFactory {

  protected def getRequestResource(properties: java.util.Map[String, String]): Resource

  override def createEngineResource(request: RequestEngine): EngineResource = {
    val user = if(StringUtils.isEmpty(request.user)) request.creator
      else request.user
    val engineResource = request match {
      case timeoutRequest: TimeoutRequestEngine =>
        val engineResource = new UserTimeoutEngineResource
        engineResource.setTimeout(timeoutRequest.timeout)
        engineResource
      case _ => new UserEngineResource
    }
    engineResource.setCreator(request.creator)
    engineResource.setUser(user)
    engineResource.setResource(getRequestResource(request.properties))
    engineResource
  }
}
