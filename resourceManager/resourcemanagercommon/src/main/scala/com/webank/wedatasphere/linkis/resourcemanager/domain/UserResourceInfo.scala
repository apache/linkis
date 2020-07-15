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

trait UserResourceInfo

case class UserPreUsedResource(ticketId: String, moduleInstance: ServiceInstance, resource: Resource) extends UserResourceInfo

case class UserUsedResource(ticketId: String, moduleInstance: ServiceInstance, resource: Resource, engineInstance: ServiceInstance = null) extends UserResourceInfo

case class UserReleasedResource(ticketId: String, moduleInstance: ServiceInstance) extends UserResourceInfo


object UserResourceInfoSerializer extends CustomSerializer[UserResourceInfo](implicit formats => ( {
  case JObject(List(("UserPreUsedResource", JObject(List(("ticketId", ticketId), ("moduleInstance", moduleInstance), ("resource", resource)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    new UserPreUsedResource(ticketId.extract[String], moduleInstance.extract[ServiceInstance], resource.extract[Resource])
  case JObject(List(("UserUsedResource", JObject(List(("ticketId", ticketId), ("moduleInstance", moduleInstance), ("resource", resource), ("engineInstance", engineInstance)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    new UserUsedResource(ticketId.extract[String], moduleInstance.extract[ServiceInstance], resource.extract[Resource], engineInstance.extract[ServiceInstance])
  case JObject(List(("UserReleasedResource", JObject(List(("ticketId", ticketId), ("moduleInstance", moduleInstance)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    new UserReleasedResource(ticketId.extract[String], moduleInstance.extract[ServiceInstance])
}, {
  case d: UserPreUsedResource =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    ("UserPreUsedResource", new JObject(List(("ticketId", Extraction.decompose(d.ticketId)), ("moduleInstance", Extraction.decompose(d.moduleInstance)), ("resource", Extraction.decompose(d.resource)))))
  case d: UserUsedResource =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    ("UserUsedResource", new JObject(List(("ticketId", Extraction.decompose(d.ticketId)), ("moduleInstance", Extraction.decompose(d.moduleInstance)), ("resource", Extraction.decompose(d.resource)), ("engineInstance", Extraction.decompose(d.engineInstance)))))
  case d: UserReleasedResource =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer + ResourceSerializer
    ("UserReleasedResource", new JObject(List(("ticketId", Extraction.decompose(d.ticketId)), ("moduleInstance", Extraction.decompose(d.moduleInstance)))))
})
)