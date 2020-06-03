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

package com.webank.wedatasphere.linkis.resourcemanager.service.metadata

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.dao.UserResourceMetaDataDao
import com.webank.wedatasphere.linkis.resourcemanager.domain.UserResourceMetaData
import com.webank.wedatasphere.linkis.resourcemanager.event.notify.UserPreUsedEvent
import com.webank.wedatasphere.linkis.resourcemanager.exception.{RMErrorException, RMWarnException}
import com.webank.wedatasphere.linkis.resourcemanager.{Resource, ResourceSerializer}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions

/**
  * Created by shanhuang on 9/11/18.
  */
@Component
class UserResourceRecordService extends Logging {

  implicit val formats = DefaultFormats + ResourceSerializer

  @Autowired
  var userResourceMetaDataDao: UserResourceMetaDataDao = _

  def clearModuleResourceRecord(moduleInstance: ServiceInstance): Unit = synchronized {
    val start = System.currentTimeMillis()
    info(s"Start clear ModuleResourceRecord  time:$start")
    userResourceMetaDataDao.deleteByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    info(s"End clear ModuleResourceRecord  take time:${System.currentTimeMillis() - start}")
  }

  def getUserResourceRecordByUser(user: String): Array[UserResourceMetaData] = JavaConversions.asScalaBuffer(userResourceMetaDataDao.getByUser(user)).toArray

  def putUserModulePreUsed(event: UserPreUsedEvent): Unit = synchronized {
    val userPreUsedResource = event.userPreUsedResource
    val existing = userResourceMetaDataDao.getByTicketId(event.userPreUsedResource.ticketId)
    if (existing == null) {
      val newRecord = new UserResourceMetaData(
        event.user,
        userPreUsedResource.ticketId,
        event.creator,
        userPreUsedResource.moduleInstance.getApplicationName,
        userPreUsedResource.moduleInstance.getInstance,
        null,
        null,
        serialize(userPreUsedResource.resource),
        null,
        null,
        System.currentTimeMillis,
        null
      )
      userResourceMetaDataDao.insert(newRecord)
      info(s"user add new ResourceRecords resource value:${userPreUsedResource.resource}")
    } else {
      existing.setUserLockedResource(serialize(userPreUsedResource.resource))
      userResourceMetaDataDao.update(existing)
      info(s"user add new ResourceRecords resource value:${userPreUsedResource.resource}")
    }
  }


  def getUserModuleRecord(user: String, ticketId: String): UserResourceMetaData = {
    val existing = userResourceMetaDataDao.getByTicketId(ticketId)
    if (existing == null) throw new RMErrorException(110004, s"user：${user} ResourceRecords  ticketId:$ticketId lose，please Re-request")
    if (!existing.getUser.equals(user)) throw new RMErrorException(110004, s"user：${user} doesn't own this ticketId，please Re-request")
    existing
  }

  def removeUser(user: String): Unit = {
    val existing = userResourceMetaDataDao.getByUser(user)
    if (existing == null || existing.isEmpty) throw new RMWarnException(111006, s"Failed to remove user user: $user as no userResourceRecord found")
    userResourceMetaDataDao.deleteByUser(user)
  }

  def removeUserTicketId(ticketId: String, userResourceRecord: UserResourceMetaData): Unit = {
    info(s"Clear user ${userResourceRecord.getUser} ticketId：$ticketId info")
    val existing = userResourceMetaDataDao.getByTicketId(ticketId)
    if (existing != null) userResourceMetaDataDao.deleteById(existing.getId)
  }

  def getModuleAndCreatorResource(moduleName: String, user: String, creator: String, requestResource: Resource): (Resource, Resource) = {
    val userRecords = JavaConversions.asScalaBuffer(userResourceMetaDataDao.getByUser(user))
    var moduleResource = Resource.getZeroResource(requestResource)
    var creatorResource = Resource.getZeroResource(requestResource)

    if (userRecords != null && !userRecords.isEmpty) userRecords.foreach { resourceRecord =>
      if (resourceRecord.getEmApplicationName == moduleName) {
        info(s"moduleName:$moduleName used record:$resourceRecord")
        if (resourceRecord.getUserUsedResource != null) {
          moduleResource = moduleResource + deserialize(resourceRecord.getUserUsedResource)
          if (creator.equals(resourceRecord.getCreator)) creatorResource = creatorResource + deserialize(resourceRecord.getUserUsedResource)
        } else if (resourceRecord.getUserLockedResource != null) {
          moduleResource = moduleResource + deserialize(resourceRecord.getUserLockedResource)
          if (creator.equals(resourceRecord.getCreator)) creatorResource = creatorResource + deserialize(resourceRecord.getUserLockedResource)
        }
      }
    }
    info(s"Get user:$user on module used $moduleResource,and creator used:$creator, $creatorResource")
    (moduleResource + requestResource, creatorResource + requestResource)
  }

  def update(userResourceMetaData: UserResourceMetaData): Unit = {
    userResourceMetaDataDao.update(userResourceMetaData)
  }

  def getAll(): Array[UserResourceMetaData] = JavaConversions.asScalaBuffer(userResourceMetaDataDao.getAll).toArray

  def serialize(resource: Resource) = write(resource)

  def deserialize(plainData: String) = read[Resource](plainData)
}
