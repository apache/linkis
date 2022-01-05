/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.engineplugin.common.resource

import org.apache.linkis.manager.common.entity.resource.{NodeResource, Resource}
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException


trait AbstractEngineResourceFactory extends EngineResourceFactory {

  protected def getRequestResource(properties: java.util.Map[String, String]): Resource

  protected def getMinRequestResource(engineResourceRequest: EngineResourceRequest): Resource = getRequestResource(engineResourceRequest.properties)

  protected def getMaxRequestResource(engineResourceRequest: EngineResourceRequest): Resource = getRequestResource(engineResourceRequest.properties)

  override def createEngineResource(engineResourceRequest: EngineResourceRequest): NodeResource = {
    val user = engineResourceRequest.user
    val engineResource = new UserNodeResource
    val minResource = getMinRequestResource(engineResourceRequest)
    val maxResource = getMaxRequestResource(engineResourceRequest)
    if(minResource.getClass != maxResource.getClass) throw new EngineConnPluginErrorException(70103,
      s"The minResource ${minResource.getClass.getSimpleName} is not the same with the maxResource${maxResource.getClass.getSimpleName}.")
    engineResource.setUser(user)
    engineResource.setMinResource(minResource)
    engineResource.setResourceType(ResourceUtils.getResourceTypeByResource(minResource))
    engineResource.setMaxResource(maxResource)
    engineResource
  }
}
