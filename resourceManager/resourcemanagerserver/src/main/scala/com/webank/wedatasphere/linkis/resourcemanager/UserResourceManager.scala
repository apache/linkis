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
import com.webank.wedatasphere.linkis.resourcemanager.event.notify._

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class UserResourceManager extends NotifyRMEventListener {

  def getUserStartedInstances(moduleName: String, user: String): Int

  def getUserLockedInstances(moduleName: String, user: String): Int

  //  def getStartedInstances(moduleName: String): Int
  //  def getLockedInstances(moduleName: String): Int

  def getModuleResourceUsed(moduleName: String, user: String): Resource

  def getModuleResourceLocked(moduleName: String, user: String): Resource

  def getModuleInstanceResourceUsed(moduleInstance: ServiceInstance, user: String): Resource

  def getModuleInstanceResourceLocked(moduleInstance: ServiceInstance, user: String): Resource

  def dealUserPreUsedEvent(event: UserPreUsedEvent): Unit

  def dealUserUsedEvent(event: UserUsedEvent): Unit

  def dealUserReleasedEvent(event: UserReleasedEvent): Unit

  def dealClearPrdUsedEvent(event: ClearPrdUsedEvent): Unit

  def dealClearUsedEvent(event: ClearUsedEvent): Unit
}


