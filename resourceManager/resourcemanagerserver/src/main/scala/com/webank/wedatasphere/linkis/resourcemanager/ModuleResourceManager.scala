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

package com.webank.wedatasphere.linkis.resourcemanager

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.resourcemanager.domain.{EmResourceMetaData, ModuleResourceInfo}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{ModuleRegisterEvent, ModuleUnregisterEvent, NotifyRMEventListener}

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class ModuleResourceManager extends NotifyRMEventListener {

  def getModuleResourceInfo(moduleInstance: ServiceInstance): Array[ModuleResourceInfo]

  def getModuleResources(moduleName: String): Array[Resource]

  def getModuleTotalResources(moduleName: String): Resource

  def getModuleUsedResources(moduleName: String): Resource

  def getModuleLockedResources(moduleName: String): Resource

  def getModuleLeftResources(moduleName: String): Resource

  def getInstanceResource(moduleInstance: ServiceInstance): Resource

  def getInstanceUsedResource(moduleInstance: ServiceInstance): Resource

  def getInstanceLockedResource(moduleInstance: ServiceInstance): Resource

  def getInstanceLeftResource(moduleInstance: ServiceInstance): Resource

  def getModuleNames(): Array[String]

  def getModuleInstancesByName(moduleName: String): Array[EmResourceMetaData]

  def dealModuleRegisterEvent(event: ModuleRegisterEvent): Unit

  def dealModuleUnregisterEvent(event: ModuleUnregisterEvent): Unit
}