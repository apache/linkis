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

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.dao.{EmMetaDataDao, EmResourceMetaDataDao}
import com.webank.wedatasphere.linkis.resourcemanager.domain.{EmMetaData, EmResourceMetaData, ModuleInfo, ModuleResourceRecord}
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMErrorException
import com.webank.wedatasphere.linkis.resourcemanager.{Resource, ResourceRequestPolicy, ResourceSerializer}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions

/**
  * Created by shanhuang on 9/11/18.
  */
@Component
class ModuleResourceRecordService extends Logging {

  implicit val formats = DefaultFormats + ResourceSerializer
  @Autowired
  var emResourceMetaDataDao: EmResourceMetaDataDao = _
  @Autowired
  var emMetaDataDao: EmMetaDataDao = _

  def putModulePolicy(moduleName: String, policy: ResourceRequestPolicy.ResourceRequestPolicy): Unit = {
    var emMetaData = emMetaDataDao.getByEmName(moduleName)
    if (emMetaData == null) {
      emMetaData = new EmMetaData(moduleName, policy.toString)
      emMetaDataDao.insert(emMetaData)
    } else {
      emMetaData.setResourceRequestPolicy(policy.toString)
      emMetaDataDao.update(emMetaData)
    }
  }

  def getModulePolicy(moduleName: String): ResourceRequestPolicy.ResourceRequestPolicy = {
    val emMetaData = emMetaDataDao.getByEmName(moduleName)
    if (emMetaData == null) throw new RMErrorException(110005, s"Module(模块): $moduleName Not registered in the resource manager(并没有在资源管理器进行注册)") else ResourceRequestPolicy.withName(emMetaData.getResourceRequestPolicy)
  }

  def getModuleName: Array[String] = JavaConversions.asScalaBuffer(emMetaDataDao.getAll).toArray.map(_.getEmName)

  /**
    * Obtain resource usage records for the same type of module by module name
    * 通过模块名获得同一类模块的资源使用记录
    *
    * @param moduleName
    * @return
    */
  def getModuleResourceRecords(moduleName: String): Array[EmResourceMetaData] = {
    val records = getByEmName(moduleName)
    if (records.size() < 1) throw new RMErrorException(110005, s"Module(模块): $moduleName Not registered in the resource manager(并没有在资源管理器进行注册)")
    JavaConversions.asScalaBuffer(records).toArray
  }

  def getModuleResourceRecord(moduleInstance: ServiceInstance): EmResourceMetaData = {
    val m = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (m == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    m
  }

  def putModuleRegisterRecord(moduleInfo: ModuleInfo): Unit = synchronized {
    val existing = getByEmInstance(moduleInfo.moduleInstance.getApplicationName, moduleInfo.moduleInstance.getInstance)
    if (existing != null)
      throw new RMErrorException(110005, s"Module instance(模块实例): ${moduleInfo.moduleInstance} Already registered, if you need to re-register, please log out and then register(已经注册，如果需要重新注册请注销后再进行注册)")
    ModuleResourceRecord(moduleInfo, Resource.initResource(moduleInfo.resourceRequestPolicy),
      moduleInfo.totalResource, Resource.initResource(moduleInfo.resourceRequestPolicy))
    val newRecord = new EmResourceMetaData(
      moduleInfo.moduleInstance.getApplicationName,
      moduleInfo.moduleInstance.getInstance,
      serialize(moduleInfo.totalResource),
      serialize(moduleInfo.protectedResource),
      moduleInfo.resourceRequestPolicy.toString,
      serialize(Resource.initResource(moduleInfo.resourceRequestPolicy)),
      serialize(moduleInfo.totalResource),
      serialize(Resource.initResource(moduleInfo.resourceRequestPolicy)),
      System.currentTimeMillis()
    )
    insert(newRecord)
    info(s"Succeed to  register module ${moduleInfo.moduleInstance}")
  }

  def moduleLockedUserResource(moduleInstance: ServiceInstance, resource: Resource): Unit = synchronized {
    val existing = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (existing == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    existing.setLeftResource(serialize(deserialize(existing.getLeftResource) - resource))
    existing.setLockedResource(serialize(deserialize(existing.getLockedResource) + resource))
    update(existing)
  }

  def moduleUsedUserResource(moduleInstance: ServiceInstance, usedResource: Resource, lockedResource: Resource): Unit = synchronized {
    val existing = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (existing == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    existing.setUsedResource(serialize(deserialize(existing.getUsedResource) + usedResource))
    existing.setLeftResource(serialize(deserialize(existing.getLeftResource) + lockedResource - usedResource))
    existing.setLockedResource(serialize(deserialize(existing.getLockedResource) - lockedResource))
    update(existing)
  }

  def moduleReleasedUserResource(moduleInstance: ServiceInstance, usedResource: Resource): Unit = synchronized {
    val existing = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (existing == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    existing.setUsedResource(serialize(deserialize(existing.getUsedResource) - usedResource))
    existing.setLeftResource(serialize(deserialize(existing.getLeftResource) + usedResource))
    update(existing)
  }

  def moduleClearLockedResource(moduleInstance: ServiceInstance, resource: Resource): Unit = synchronized {
    val existing = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (existing == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    existing.setLeftResource(serialize(deserialize(existing.getLeftResource) + resource))
    existing.setLockedResource(serialize(deserialize(existing.getLockedResource) - resource))
    update(existing)
  }

  def moduleClearUsedResource(moduleInstance: ServiceInstance, resource: Resource): Unit = synchronized {
    val existing = getByEmInstance(moduleInstance.getApplicationName, moduleInstance.getInstance)
    if (existing == null)
      throw new RMErrorException(110005, s"Module instance(模块实例): $moduleInstance Not registered in the resource manager(并没有在资源管理器进行注册)")
    existing.setUsedResource(serialize(deserialize(existing.getUsedResource) - resource))
    existing.setLeftResource(serialize(deserialize(existing.getLeftResource) + resource))
  }

  def serialize(resource: Resource) = write(resource)

  def deserialize(plainData: String) = read[Resource](plainData)

  def getAll(): util.List[EmResourceMetaData] = emResourceMetaDataDao.getAll

  def getByEmName(emApplicationName: String): util.List[EmResourceMetaData] = emResourceMetaDataDao.getByEmName(emApplicationName)

  def getByEmInstance(emApplicationName: String, emInstance: String): EmResourceMetaData = emResourceMetaDataDao.getByEmInstance(emApplicationName, emInstance)

  def insert(emResourceMetaData: EmResourceMetaData): Unit = {
    emResourceMetaDataDao.insert(emResourceMetaData)
  }

  def update(emResourceMetaData: EmResourceMetaData): Unit = {
    emResourceMetaDataDao.update(emResourceMetaData)
  }

  def delete(id: Integer) = {
    emResourceMetaDataDao.deleteById(id)
  }

}
