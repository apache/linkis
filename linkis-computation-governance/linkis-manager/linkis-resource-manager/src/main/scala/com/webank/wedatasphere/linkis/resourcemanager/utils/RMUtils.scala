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

package com.webank.wedatasphere.linkis.resourcemanager.utils

import com.webank.wedatasphere.linkis.common.conf.{CommonVars, TimeType}
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource
import com.webank.wedatasphere.linkis.manager.common.entity.resource._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}

object RMUtils extends Logging {

  implicit val formats = DefaultFormats + ResourceSerializer

  val MANAGER_KILL_ENGINE_EAIT = CommonVars("wds.linkis.manager.rm.kill.engine.wait", new TimeType("30s"))

  val RM_REQUEST_ENABLE = CommonVars("wds.linkis.manager.rm.request.enable", true)

  def deserializeResource(plainResource: String): Resource = {
    read[Resource](plainResource)
  }

  def serializeResource(resource: Resource): String = {
    write(resource)
  }

  def toPersistenceResource(nodeResource: NodeResource) : PersistenceResource = {
    val persistenceResource = new PersistenceResource
    if(nodeResource.getMaxResource != null) persistenceResource.setMaxResource(serializeResource(nodeResource.getMaxResource))
    if(nodeResource.getMinResource != null) persistenceResource.setMinResource(serializeResource(nodeResource.getMinResource))
    if(nodeResource.getLockedResource != null) persistenceResource.setLockedResource(serializeResource(nodeResource.getLockedResource))
    if(nodeResource.getExpectedResource != null) persistenceResource.setExpectedResource(serializeResource(nodeResource.getExpectedResource))
    if(nodeResource.getLeftResource != null) persistenceResource.setLeftResource(serializeResource(nodeResource.getLeftResource))
    persistenceResource.setResourceType(nodeResource.getResourceType.toString())
    persistenceResource
  }

  /*  def fromPersistenceResource(persistenceResource: PersistenceResource) : CommonNodeResource = {
      val nodeResource = new CommonNodeResource
      if(persistenceResource.getMaxResource != null) nodeResource.setMaxResource(deserializeResource(persistenceResource.getMaxResource))
      if(persistenceResource.getMinResource != null) nodeResource.setMinResource(deserializeResource(persistenceResource.getMinResource))
      if(persistenceResource.getLockedResource != null) nodeResource.setLockedResource(deserializeResource(persistenceResource.getLockedResource))
      if(persistenceResource.getExpectedResource != null) nodeResource.setMaxResource(deserializeResource(persistenceResource.getExpectedResource))
      if(persistenceResource.getLeftResource != null) nodeResource.setLeftResource(deserializeResource(persistenceResource.getLeftResource))
      nodeResource.setResourceType(ResourceType.valueOf(persistenceResource.getResourceType))
      nodeResource
    }*/

  def aggregateNodeResource(firstNodeResource: NodeResource, secondNodeResource: NodeResource) : CommonNodeResource = {
    if(firstNodeResource != null && secondNodeResource != null){
      val aggregatedNodeResource = new CommonNodeResource
      aggregatedNodeResource.setResourceType(firstNodeResource.getResourceType)
      aggregatedNodeResource.setMaxResource(aggregateResource(firstNodeResource.getMaxResource, secondNodeResource.getMaxResource))
      aggregatedNodeResource.setMinResource(aggregateResource(firstNodeResource.getMinResource, secondNodeResource.getMinResource))
      aggregatedNodeResource.setUsedResource(aggregateResource(firstNodeResource.getUsedResource, secondNodeResource.getUsedResource))
      aggregatedNodeResource.setLockedResource(aggregateResource(firstNodeResource.getLockedResource, secondNodeResource.getLockedResource))
      aggregatedNodeResource.setLeftResource(aggregateResource(firstNodeResource.getLeftResource, secondNodeResource.getLeftResource))
      return aggregatedNodeResource
    }
    if(firstNodeResource == null && secondNodeResource == null){
      return null
    }
    if(firstNodeResource == null) {
      return secondNodeResource.asInstanceOf[CommonNodeResource]
    } else {
      return firstNodeResource.asInstanceOf[CommonNodeResource]
    }
  }

  def aggregateResource(firstResource: Resource, secondResource: Resource) :  Resource = {
    (firstResource, secondResource) match {
      case (null, null) => null
      case (null, secondResource) => secondResource
      case (firstResource, null) => firstResource
      case (firstResource, secondResource) if firstResource.getClass.equals(secondResource.getClass) => firstResource.add(secondResource)
      case _ => null
    }
  }

}
