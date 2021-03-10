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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.resource

import com.webank.wedatasphere.linkis.manager.common.entity.resource.{NodeResource, Resource}
import com.webank.wedatasphere.linkis.manager.common.utils.ResourceUtils


trait AbstractEngineResourceFactory extends EngineResourceFactory {

  protected def getRequestResource(properties: java.util.Map[String, String]): Resource

  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    val user = engineResourceRequest.user
    val engineResource = new UserNodeResource
    val resource = getRequestResource(engineResourceRequest.properties)
    engineResource.setUser(user)
    engineResource.setMinResource(resource)
    engineResource.setResourceType(ResourceUtils.getResourceTypeByResource(resource))
    engineResource.setMaxResource(engineResource.getMinResource)
    engineResource
  }
}
