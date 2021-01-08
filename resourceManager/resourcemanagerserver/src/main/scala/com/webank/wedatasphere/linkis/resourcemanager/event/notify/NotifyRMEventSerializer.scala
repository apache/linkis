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

package com.webank.wedatasphere.linkis.resourcemanager.event.notify

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.domain._
import com.webank.wedatasphere.linkis.resourcemanager.event.EventScope
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}

/**
  * Created by shanhuang on 9/11/18.
  */
object NotifyRMEventSerializer extends CustomSerializer[NotifyRMEvent](implicit formats => ( {
  case JObject(List(("DefaultNotifyRMEvent", JObject(List(("user", user), ("moduleName", moduleName), ("eventScope", eventScope)))))) =>
    new DefaultNotifyRMEvent(user.extract[String], moduleName.extract[String], EventScope.withName(eventScope.extract[String]))
  case JObject(List(("ModuleRegisterEvent", JObject(List(("eventScope", eventScope), ("moduleInfo", moduleInfo)))))) =>
    implicit val formats = DefaultFormats + ModuleInfoSerializer
    new ModuleRegisterEvent(EventScope.withName(eventScope.extract[String]), moduleInfo.extract[ModuleInfo])
  case JObject(List(("ModuleUnregisterEvent", JObject(List(("eventScope", eventScope), ("moduleInstance", moduleInstance)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    new ModuleUnregisterEvent(EventScope.withName(eventScope.extract[String]), moduleInstance.extract[ServiceInstance])
  case JObject(List(("ModuleLock", JObject(List(("eventScope", eventScope), ("moduleInstance", moduleInstance)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    new ModuleLock(EventScope.withName(eventScope.extract[String]), moduleInstance.extract[ServiceInstance])
  case JObject(List(("UserLock", JObject(List(("eventScope", eventScope), ("user", user), ("moduleInstance", moduleInstance)))))) =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    new UserLock(EventScope.withName(eventScope.extract[String]), user.extract[String], moduleInstance.extract[ServiceInstance])
  case JObject(List(("UserPreUsedEvent", JObject(List(("eventScope", eventScope), ("user", user), ("creator", creator), ("userPreUsedResource", userPreUsedResource)))))) =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    new UserPreUsedEvent(EventScope.withName(eventScope.extract[String]), user.extract[String], creator.extract[String], userPreUsedResource.extract[UserResourceInfo].asInstanceOf[UserPreUsedResource])
  case JObject(List(("UserUsedEvent", JObject(List(("eventScope", eventScope), ("user", user), ("userUsedResource", userUsedResource)))))) =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    new UserUsedEvent(EventScope.withName(eventScope.extract[String]), user.extract[String], userUsedResource.extract[UserResourceInfo].asInstanceOf[UserUsedResource])
  case JObject(List(("UserReleasedEvent", JObject(List(("eventScope", eventScope), ("user", user), ("userReleasedResource", userReleasedResource)))))) =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    new UserReleasedEvent(EventScope.withName(eventScope.extract[String]), user.extract[String], userReleasedResource.extract[UserResourceInfo].asInstanceOf[UserReleasedResource])
  case JObject(List(("ClearPrdUsedEvent", JObject(List(("eventScope", eventScope), ("user", user), ("userPreUsedResource", userPreUsedResource)))))) =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    new ClearPrdUsedEvent(EventScope.withName(eventScope.extract[String]), user.extract[String], userPreUsedResource.extract[UserResourceInfo].asInstanceOf[UserPreUsedResource])
  case JObject(List(("ClearUsedEvent", JObject(List(("eventScope", eventScope), ("user", user), ("userUsedResource", userUsedResource)))))) =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    new ClearUsedEvent(EventScope.withName(eventScope.extract[String]), user.extract[String], userUsedResource.extract[UserResourceInfo].asInstanceOf[UserUsedResource])
  case JObject(List(("WaitUsedEvent", JObject(List(("clearEvent", clearEvent), ("timeOut", timeOut)))))) =>
    new WaitUsedEvent(clearEvent.extract[NotifyRMEvent].asInstanceOf[ClearEvent], timeOut.extract[Long])
  case JObject(List(("WaitReleasedEvent", JObject(List(("clearEvent", clearEvent), ("timeOut", timeOut)))))) =>
    new WaitReleasedEvent(clearEvent.extract[NotifyRMEvent].asInstanceOf[ClearEvent], timeOut.extract[Long])
}, {
  case d: DefaultNotifyRMEvent =>
    ("DefaultNotifyRMEvent", ("user", d.user) ~ ("moduleName", d.moduleName) ~ ("eventScope", d.eventScope.toString))
  case d: ModuleRegisterEvent =>
    implicit val formats = DefaultFormats + ModuleInfoSerializer
    ("ModuleRegisterEvent", ("eventScope", d.eventScope.toString) ~ ("moduleInfo", Extraction.decompose(d.moduleInfo)))
  case d: ModuleUnregisterEvent =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    ("ModuleUnregisterEvent", ("eventScope", d.eventScope.toString) ~ ("moduleInstance", Extraction.decompose(d.moduleInstance)))
  case d: ModuleLock =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    ("ModuleLock", ("eventScope", d.eventScope.toString) ~ ("moduleInstance", Extraction.decompose(d.moduleInstance)))
  case d: UserLock =>
    implicit val formats = DefaultFormats + ModuleInstanceSerializer
    ("UserLock", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("moduleInstance", Extraction.decompose(d.moduleInstance)))
  case d: UserPreUsedEvent =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    ("UserPreUsedEvent", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("creator", d.creator) ~ ("userPreUsedResource", Extraction.decompose(d.userPreUsedResource)))
  case d: UserUsedEvent =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    ("UserUsedEvent", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("userUsedResource", Extraction.decompose(d.userUsedResource)))
  case d: UserReleasedEvent =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    ("UserReleasedEvent", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("userReleasedResource", Extraction.decompose(d.userReleasedResource)))
  case d: ClearPrdUsedEvent =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    ("ClearPrdUsedEvent", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("userPreUsedResource", Extraction.decompose(d.userPreUsedResource)))
  case d: ClearUsedEvent =>
    implicit val formats = DefaultFormats + UserResourceInfoSerializer
    ("ClearUsedEvent", ("eventScope", d.eventScope.toString) ~ ("user", d.user) ~ ("userUsedResource", Extraction.decompose(d.userUsedResource)))
  case d: WaitUsedEvent =>
    ("WaitUsedEvent", ("clearEvent", Extraction.decompose(d.clearEvent)) ~ ("timeOut", d.timeOut))
  case d: WaitReleasedEvent =>
    ("WaitReleasedEvent", ("clearEvent", Extraction.decompose(d.clearEvent)) ~ ("timeOut", d.timeOut))
})
)
