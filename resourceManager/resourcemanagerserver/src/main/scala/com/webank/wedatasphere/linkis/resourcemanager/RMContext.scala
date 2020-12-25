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

import com.webank.wedatasphere.linkis.resourcemanager.service.DefaultRMContext
import com.webank.wedatasphere.linkis.scheduler.Scheduler

/**
  * Created by shanhuang on 9/11/18.
  */
trait RMContext {

  def getScheduler: Scheduler

  //  def getResourceLockService: ResourceLockService
  //  def getModuleResourceRecordService: ModuleResourceRecordService
  //  def getModuleResourceManager: ModuleResourceManager
  //  def getUserResourceManager: UserResourceManager
  //  def getRequestResourceServices: Array[RequestResourceService]
  //  def getRMListenerBus: RMListenerBus[TListener, Event]
}

object RMContext {
  val rmContext = new DefaultRMContext()

  def getOrCreateRMContext(): RMContext = rmContext
}