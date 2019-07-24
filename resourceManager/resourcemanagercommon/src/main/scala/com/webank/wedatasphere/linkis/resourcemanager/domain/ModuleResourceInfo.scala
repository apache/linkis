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

package com.webank.wedatasphere.linkis.resourcemanager.domain

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.{Resource, ResourceSerializer}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}

/**
  * Created by shanhuang on 9/11/18.
  */
case class ModuleResourceInfo(moduleInstance: ServiceInstance, totalResource: Resource, usedResource: Resource)

object ModuleResourceInfoSerializer extends CustomSerializer[ModuleResourceInfo](implicit formats => ( {
  case JObject(List(("ModuleResourceInfo", JObject(List(("moduleInstance", moduleInstance), ("totalResource", totalResource), ("usedResource", usedResource)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    ModuleResourceInfo(moduleInstance.extract[ServiceInstance], totalResource.extract[Resource], usedResource.extract[Resource])
}, {
  case m: ModuleResourceInfo =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    ("ModuleResourceInfo", new JObject(List(("moduleInstance", Extraction.decompose(m.moduleInstance)), ("totalResource", Extraction.decompose(m.totalResource)), ("usedResource", Extraction.decompose(m.usedResource)))))
}))

case class ModuleResourceRecord(moduleInfo: ModuleInfo, moduleUsedResources: Resource, moduleLeftResource: Resource, moduleLockedResource: Resource, registerTime: Long = System.currentTimeMillis())
