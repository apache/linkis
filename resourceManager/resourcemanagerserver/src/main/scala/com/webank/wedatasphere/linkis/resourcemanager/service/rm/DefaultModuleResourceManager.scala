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

package com.webank.wedatasphere.linkis.resourcemanager.service.rm

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.resourcemanager.domain.{EmResourceMetaData, ModuleResourceInfo}
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.{ModuleRegisterEvent, ModuleUnregisterEvent, NotifyRMEvent}
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMErrorException
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, UserResourceRecordService}
import com.webank.wedatasphere.linkis.resourcemanager.{ModuleResourceManager, Resource}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/9/14.
  */
@Component
class DefaultModuleResourceManager extends ModuleResourceManager with Logging {

  @Autowired
  var moduleResourceRecordService: ModuleResourceRecordService = _
  @Autowired
  var userResourceRecordService: UserResourceRecordService = _

  override def getModuleResourceInfo(moduleInstance: ServiceInstance): Array[ModuleResourceInfo] = {
    if (moduleInstance.getInstance == null) {
      moduleResourceRecordService.getModuleResourceRecords(moduleInstance.getApplicationName).map { m =>
        ModuleResourceInfo(
          ServiceInstance(m.getEmApplicationName, m.getEmInstance),
          moduleResourceRecordService.deserialize(m.getTotalResource),
          moduleResourceRecordService.deserialize(m.getUsedResource)
        )

      }
    } else {
      val m = moduleResourceRecordService.getModuleResourceRecord(moduleInstance)
      Array(ModuleResourceInfo(
        ServiceInstance(m.getEmApplicationName, m.getEmInstance),
        moduleResourceRecordService.deserialize(m.getTotalResource),
        moduleResourceRecordService.deserialize(m.getUsedResource)
      ))
    }
  }

  override def getModuleResources(moduleName: String): Array[Resource] = {
    moduleResourceRecordService.getModuleResourceRecords(moduleName).map(_.getTotalResource).map(moduleResourceRecordService.deserialize)
  }

  override def getModuleTotalResources(moduleName: String): Resource = {
    val records = moduleResourceRecordService.getModuleResourceRecords(moduleName)
    if (records == null || records.length < 1) {
      null
    } else {
      records.map(_.getTotalResource).map(moduleResourceRecordService.deserialize).reduce((r1, r2) => r1 + r2)
    }
  }


  override def getModuleUsedResources(moduleName: String): Resource = {
    val records = moduleResourceRecordService.getModuleResourceRecords(moduleName)
    if (records == null || records.length < 1) {
      null
    } else {
      records.map(_.getUsedResource).map(moduleResourceRecordService.deserialize).reduce((r1, r2) => r1 + r2)
    }
  }

  override def getModuleLockedResources(moduleName: String): Resource = {
    val records = moduleResourceRecordService.getModuleResourceRecords(moduleName)
    if (records == null || records.length < 1) {
      null
    } else {
      records.map(_.getLockedResource).map(moduleResourceRecordService.deserialize).reduce((r1, r2) => r1 + r2)
    }
  }

  override def getModuleLeftResources(moduleName: String): Resource = {
    val records = moduleResourceRecordService.getModuleResourceRecords(moduleName)
    if (records == null || records.length < 1) {
      null
    } else {
      records.map(_.getLeftResource).map(moduleResourceRecordService.deserialize).reduce((r1, r2) => r1 + r2)
    }
  }

  override def getInstanceResource(moduleInstance: ServiceInstance): Resource = {
    moduleResourceRecordService.deserialize(moduleResourceRecordService.getModuleResourceRecord(moduleInstance).getTotalResource)
  }

  override def getInstanceUsedResource(moduleInstance: ServiceInstance): Resource = {
    moduleResourceRecordService.deserialize(moduleResourceRecordService.getModuleResourceRecord(moduleInstance).getUsedResource)
  }

  override def getInstanceLockedResource(moduleInstance: ServiceInstance): Resource = {
    moduleResourceRecordService.deserialize(moduleResourceRecordService.getModuleResourceRecord(moduleInstance).getLockedResource)
  }

  override def getInstanceLeftResource(moduleInstance: ServiceInstance): Resource = {
    moduleResourceRecordService.deserialize(moduleResourceRecordService.getModuleResourceRecord(moduleInstance).getLeftResource)
  }


  override def onNotifyRMEvent(event: NotifyRMEvent): Unit = event match {
    case moduleRegisterEvent: ModuleRegisterEvent => dealModuleRegisterEvent(moduleRegisterEvent)
    case moduleUnregisterEvent: ModuleUnregisterEvent => dealModuleUnregisterEvent(moduleUnregisterEvent)
    case _ =>
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}

  override def dealModuleRegisterEvent(event: ModuleRegisterEvent): Unit = synchronized {
    //RMListenerBus.getRMListenerBusInstance.post(event)
    info(s"Start processing  registration event of module：${event.moduleInfo}")
    val moduleInfo = event.moduleInfo
    moduleResourceRecordService.putModulePolicy(moduleInfo.moduleInstance.getApplicationName, moduleInfo.resourceRequestPolicy)
    moduleResourceRecordService.putModuleRegisterRecord(moduleInfo)
    info(s"End processing registration events ${event.moduleInfo.moduleInstance} success")
  }

  /**
    *
    * @param event
    */
  override def dealModuleUnregisterEvent(event: ModuleUnregisterEvent): Unit = synchronized {
    //RMListenerBus.getRMListenerBusInstance.post(event)
    info(s"Start processing  logout event of module：${event.moduleInstance}")
    val moduleInstance = event.moduleInstance
    Utils.tryQuietly(userResourceRecordService.clearModuleResourceRecord(moduleInstance))
    val record = moduleResourceRecordService.getModuleResourceRecord(moduleInstance)
    if (record == null) throw new RMErrorException(110005, s"Failed to process logout event of module : $moduleInstance Not registered")
    moduleResourceRecordService.delete(record.getId)
    info(s"End processing  logout event of module：${event.moduleInstance} success")
  }

  override def getModuleNames(): Array[String] = {
    moduleResourceRecordService.getModuleName
  }

  override def getModuleInstancesByName(moduleName: String): Array[EmResourceMetaData] = {
    moduleResourceRecordService.getModuleResourceRecords(moduleName)
  }
}
