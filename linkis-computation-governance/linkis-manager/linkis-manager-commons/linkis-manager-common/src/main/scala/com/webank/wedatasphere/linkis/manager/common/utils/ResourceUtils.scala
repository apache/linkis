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

package com.webank.wedatasphere.linkis.manager.common.utils

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource
import com.webank.wedatasphere.linkis.manager.common.entity.resource._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}


object ResourceUtils {


  implicit val formats = DefaultFormats + ResourceSerializer

  def deserializeResource(plainResource: String): Resource = {
    read[Resource](plainResource)
  }

  def serializeResource(resource: Resource): String = {
    write(resource)
  }

  def toPersistenceResource(nodeResource: NodeResource): PersistenceResource = {
    val persistenceResource = new PersistenceResource
    if (nodeResource.getMaxResource != null) persistenceResource.setMaxResource(serializeResource(nodeResource.getMaxResource))
    if (nodeResource.getMinResource != null) persistenceResource.setMinResource(serializeResource(nodeResource.getMinResource))
    if (nodeResource.getLockedResource != null) persistenceResource.setLockedResource(serializeResource(nodeResource.getLockedResource))
    if (nodeResource.getExpectedResource != null) persistenceResource.setExpectedResource(serializeResource(nodeResource.getExpectedResource))
    if (nodeResource.getLeftResource != null) persistenceResource.setLeftResource(serializeResource(nodeResource.getLeftResource))
    if (nodeResource.getUsedResource != null) persistenceResource.setUsedResource(serializeResource(nodeResource.getUsedResource))
    persistenceResource.setResourceType(nodeResource.getResourceType.toString())
    persistenceResource
  }

  def fromPersistenceResource(persistenceResource: PersistenceResource): CommonNodeResource = {
    if(persistenceResource == null) return null
    val nodeResource = new CommonNodeResource
    if (persistenceResource.getMaxResource != null) nodeResource.setMaxResource(deserializeResource(persistenceResource.getMaxResource))
    if (persistenceResource.getMinResource != null) nodeResource.setMinResource(deserializeResource(persistenceResource.getMinResource))
    if (persistenceResource.getLockedResource != null) nodeResource.setLockedResource(deserializeResource(persistenceResource.getLockedResource))
    if (persistenceResource.getExpectedResource != null) nodeResource.setExpectedResource(deserializeResource(persistenceResource.getExpectedResource))
    if (persistenceResource.getLeftResource != null) nodeResource.setLeftResource(deserializeResource(persistenceResource.getLeftResource))
    if (persistenceResource.getUsedResource != null) nodeResource.setUsedResource(deserializeResource(persistenceResource.getUsedResource))
    if (persistenceResource.getCreateTime != null) nodeResource.setCreateTime(persistenceResource.getCreateTime)
    if (persistenceResource.getUpdateTime != null) nodeResource.setUpdateTime(persistenceResource.getUpdateTime)
    nodeResource.setResourceType(ResourceType.valueOf(persistenceResource.getResourceType))
    nodeResource
  }

  def getResourceTypeByResource(resource: Resource): ResourceType = resource match {
    case cpuResource: CPUResource => ResourceType.CPU
    case loadResource: LoadResource => ResourceType.Load
    case instanceResource: InstanceResource => ResourceType.Instance
    case loadInstanceResource: LoadInstanceResource => ResourceType.LoadInstance
    case yarnResource: YarnResource => ResourceType.Yarn
    case driverAndYarnResource: DriverAndYarnResource => ResourceType.DriverAndYarn
    case specialResource: SpecialResource => ResourceType.Special
    case _ => ResourceType.LoadInstance
  }

  def convertTo(nodeResource: NodeResource, resourceType: ResourceType) : NodeResource = {
    if(nodeResource.getResourceType.equals(resourceType)) return nodeResource
    if(resourceType.equals(ResourceType.LoadInstance)){
      if(nodeResource.getResourceType.equals(ResourceType.DriverAndYarn)){
        nodeResource.setResourceType(resourceType)
        if(nodeResource.getMaxResource != null) nodeResource.setMaxResource(nodeResource.getMaxResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        if(nodeResource.getMinResource != null) nodeResource.setMinResource(nodeResource.getMinResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        if(nodeResource.getUsedResource != null) nodeResource.setUsedResource(nodeResource.getUsedResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        if(nodeResource.getLockedResource != null) nodeResource.setLockedResource(nodeResource.getLockedResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        if(nodeResource.getExpectedResource != null) nodeResource.setExpectedResource(nodeResource.getExpectedResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        if(nodeResource.getLeftResource != null && nodeResource.getLeftResource.isInstanceOf[DriverAndYarnResource]){
          nodeResource.setLeftResource(nodeResource.getLeftResource.asInstanceOf[DriverAndYarnResource].loadInstanceResource)
        }
        return nodeResource
      }
    }
    return nodeResource
  }


}
