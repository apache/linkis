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
import com.webank.wedatasphere.linkis.resourcemanager.event.notify._
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, UserResourceRecordService}
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMUtils
import com.webank.wedatasphere.linkis.resourcemanager.{Resource, UserResourceManager}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/9/15.
  */
@Component
class DefaultUserResourceManager extends UserResourceManager with Logging {

  @Autowired
  var moduleResourceRecordService: ModuleResourceRecordService = _
  @Autowired
  var userResourceRecordService: UserResourceRecordService = _

  override def getUserStartedInstances(moduleName: String, user: String): Int = {
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    if (userResourceRecords == null || userResourceRecords.isEmpty) return 0
    userResourceRecords.count { record =>
      record.getEmApplicationName == moduleName && record.getUserLockedResource == null && record.getUserUsedResource != null
    }
  }

  override def getUserLockedInstances(moduleName: String, user: String): Int = {
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    if (userResourceRecords == null || userResourceRecords.isEmpty) return 0
    userResourceRecords.count { record =>
      record.getEmApplicationName == moduleName && record.getUserUsedResource == null && record.getUserLockedResource != null
    }
  }


  //  override def getStartedInstances(moduleName: String): Int = {
  //    val moduleResourceRecords = userResourceRecordService.
  //    if(moduleResourceRecords.isEmpty){
  //      0
  //    } else {
  //      moduleResourceRecords.count(_.get)
  //    }
  //
  //  }
  //
  //  override def getLockedInstances(moduleName: String): Int = {
  //    val moduleInstanceRecord =  moduleInstanceMap.get(moduleName).orNull
  //    if(moduleInstanceRecord == null){
  //      0
  //    } else {
  //      moduleInstanceRecord.locked
  //    }
  //  }

  override def getModuleResourceUsed(moduleName: String, user: String): Resource = {
    //TODO DriveryarnSpecial judgment(特殊判断)
    val policy = moduleResourceRecordService.getModulePolicy(moduleName)
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    var resource = Resource.initResource(policy)
    if (userResourceRecords != null && userResourceRecords.nonEmpty) {
      userResourceRecords.foreach { resourceRecord =>
        if (resourceRecord.getEmApplicationName == moduleName) {
          if (resourceRecord.getUserUsedResource != null) {
            resource = resource + userResourceRecordService.deserialize(resourceRecord.getUserUsedResource)
          }
        }
      }
    }
    resource
  }


  override def getModuleResourceLocked(moduleName: String, user: String): Resource = {
    val policy = moduleResourceRecordService.getModulePolicy(moduleName)
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    var resource = Resource.initResource(policy)
    if (userResourceRecords != null && userResourceRecords.nonEmpty) {
      userResourceRecords.foreach { resourceRecord =>
        if (resourceRecord.getEmApplicationName == moduleName) {
          if (resourceRecord.getUserLockedResource != null) {
            resource = resource + userResourceRecordService.deserialize(resourceRecord.getUserLockedResource)
          }
        }
      }
    }
    resource
  }

  override def getModuleInstanceResourceUsed(moduleInstance: ServiceInstance, user: String): Resource = {
    val policy = moduleResourceRecordService.getModulePolicy(moduleInstance.getApplicationName)
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    var resource = Resource.initResource(policy)
    if (userResourceRecords != null && userResourceRecords.nonEmpty) {
      userResourceRecords.foreach { resourceRecord =>
        if (resourceRecord.getEmApplicationName == moduleInstance.getApplicationName && resourceRecord.getEmInstance == moduleInstance.getInstance) {
          if (resourceRecord.getUserUsedResource != null) {
            resource = resource + userResourceRecordService.deserialize(resourceRecord.getUserUsedResource)
          }
        }
      }
    }
    resource
  }

  override def getModuleInstanceResourceLocked(moduleInstance: ServiceInstance, user: String): Resource = {
    val policy = moduleResourceRecordService.getModulePolicy(moduleInstance.getApplicationName)
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(user)
    var resource = Resource.initResource(policy)
    if (userResourceRecords != null && userResourceRecords.nonEmpty) {
      userResourceRecords.foreach { resourceRecord =>
        if (resourceRecord.getEmApplicationName == moduleInstance.getApplicationName && resourceRecord.getEmInstance == moduleInstance.getInstance) {
          if (resourceRecord.getUserLockedResource != null) {
            resource = resource + userResourceRecordService.deserialize(resourceRecord.getUserLockedResource)
          }
        }
      }
    }
    resource
  }

  override def onNotifyRMEvent(event: NotifyRMEvent): Unit = event match {
    case userPreUsedEvent: UserPreUsedEvent => dealUserPreUsedEvent(userPreUsedEvent)
    case userUsedEvent: UserUsedEvent => dealUserUsedEvent(userUsedEvent)
    case userReleasedEvent: UserReleasedEvent => dealUserReleasedEvent(userReleasedEvent)
    case clearPrdUsedEvent: ClearPrdUsedEvent => dealClearPrdUsedEvent(clearPrdUsedEvent)
    case clearUsedEvent: ClearUsedEvent => dealClearUsedEvent(clearUsedEvent)
    case _ =>
  }

  def dealUserPreUsedEvent(event: UserPreUsedEvent): Unit = RMUtils.buildLock(event.user) synchronized {
    info(s"${event.user} from module：${event.userPreUsedResource.moduleInstance} request preUseResource：${event.userPreUsedResource}")
    val preUsedResource = event.userPreUsedResource
    Utils.tryAndError {
      moduleResourceRecordService.moduleLockedUserResource(preUsedResource.moduleInstance, preUsedResource.resource)
      userResourceRecordService.putUserModulePreUsed(event)
      //      val moduleInstanceRecord = moduleInstanceMap.getOrElse(event.moduleName, ModuleInstanceRecord(event.moduleName, 0, 0))
      //      moduleInstanceMap(event.moduleName) = ModuleInstanceRecord(moduleInstanceRecord.moduleName, moduleInstanceRecord.start, moduleInstanceRecord.locked + 1)
    }
  }

  def dealUserUsedEvent(event: UserUsedEvent): Unit = RMUtils.buildLock(event.user) synchronized {

    info(s"${event.user} committed module：${event.userUsedResource.moduleInstance} usedResource：${event.userUsedResource}")

    val usedResource = event.userUsedResource
    val userResourceRecord = userResourceRecordService.getUserModuleRecord(event.user, usedResource.ticketId)

    moduleResourceRecordService.moduleUsedUserResource(usedResource.moduleInstance, usedResource.resource, userResourceRecordService.deserialize(userResourceRecord.getUserLockedResource))
    userResourceRecord.setEngineApplicationName(usedResource.engineInstance.getApplicationName)
    userResourceRecord.setEngineInstance(usedResource.engineInstance.getInstance)
    userResourceRecord.setUserLockedResource(null)
    userResourceRecord.setUserUsedResource(userResourceRecordService.serialize(usedResource.resource))
    userResourceRecord.setUsedTime(System.currentTimeMillis())
    userResourceRecordService.update(userResourceRecord)

    //    val moduleInstanceRecord = moduleInstanceMap.getOrElse(event.moduleName, ModuleInstanceRecord(event.moduleName, 0, 0))
    //    moduleInstanceMap(event.moduleName) = ModuleInstanceRecord(moduleInstanceRecord.moduleName, moduleInstanceRecord.start + 1, moduleInstanceRecord.locked - 1)
    //notify bus
    //RMListenerBus.getRMListenerBusInstance.post(new UserSessionStartEvent(event.user,userResourceRecord.getCreator, usedResource))
    info("Thread " + Thread.currentThread() + "finished dealUserUsedEvent")
  }

  def dealUserReleasedEvent(event: UserReleasedEvent): Unit = RMUtils.buildLock(event.user) synchronized {
    info(s"${event.user} from module：${event.userReleasedResource.moduleInstance} released resource：${event.userReleasedResource}")
    val releasedResource = event.userReleasedResource
    //RMListenerBus.getRMListenerBusInstance.post(new UserSessionEndEvent(event.user, releasedResource))
    val userResourceRecord = userResourceRecordService.getUserModuleRecord(event.user, releasedResource.ticketId)
    if (null == userResourceRecord.getUserUsedResource && userResourceRecord.getUserLockedResource != null) {
      moduleResourceRecordService.moduleClearLockedResource(releasedResource.moduleInstance, userResourceRecordService.deserialize(userResourceRecord.getUserLockedResource))
    } else {
      moduleResourceRecordService.moduleReleasedUserResource(releasedResource.moduleInstance, userResourceRecordService.deserialize(userResourceRecord.getUserUsedResource))
    }
    userResourceRecordService.removeUserTicketId(releasedResource.ticketId, userResourceRecord)

    //    val moduleInstanceRecord = moduleInstanceMap.getOrElse(event.moduleName, ModuleInstanceRecord(event.moduleName, 0, 0))
    //    moduleInstanceMap(event.moduleName) = ModuleInstanceRecord(moduleInstanceRecord.moduleName, moduleInstanceRecord.start - 1, moduleInstanceRecord.locked)

  }

  def dealClearPrdUsedEvent(event: ClearPrdUsedEvent): Unit = RMUtils.buildLock(event.user) synchronized {
    info(s"Clear:${event.user} in module :${event.userPreUsedResource.moduleInstance} preUseResource")
    val preUsedResource = event.userPreUsedResource
    val userResourceRecord = userResourceRecordService.getUserModuleRecord(event.user, preUsedResource.ticketId)

    moduleResourceRecordService.moduleClearLockedResource(preUsedResource.moduleInstance, preUsedResource.resource)
    userResourceRecordService.removeUserTicketId(preUsedResource.ticketId, userResourceRecord)

    //    val moduleInstanceRecord = moduleInstanceMap.getOrElse(event.moduleName, ModuleInstanceRecord(event.moduleName, 0, 0))
    //    moduleInstanceMap(event.moduleName) = ModuleInstanceRecord(moduleInstanceRecord.moduleName, moduleInstanceRecord.start, moduleInstanceRecord.locked - 1)
  }

  def dealClearUsedEvent(event: ClearUsedEvent): Unit = RMUtils.buildLock(event.user) synchronized {
    info(s"Clear:${event.user}in module:${event.userUsedResource.moduleInstance}usedResource")
    val usedResource = event.userUsedResource
    //RMListenerBus.getRMListenerBusInstance.post(new UserSessionEndEvent(event.user, new UserReleasedResource(usedResource.ticketId,usedResource.moduleInstance)))
    val userResourceRecord = userResourceRecordService.getUserModuleRecord(event.user, usedResource.ticketId)

    moduleResourceRecordService.moduleClearLockedResource(usedResource.moduleInstance, usedResource.resource)
    userResourceRecordService.removeUserTicketId(usedResource.ticketId, userResourceRecord)

    //    val moduleInstanceRecord = moduleInstanceMap.getOrElse(event.moduleName, ModuleInstanceRecord(event.moduleName, 0, 0))
    //    moduleInstanceMap(event.moduleName) = ModuleInstanceRecord(moduleInstanceRecord.moduleName, moduleInstanceRecord.start - 1, moduleInstanceRecord.locked)
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}


}
