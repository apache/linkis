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
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy.ResourceRequestPolicy
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMWarnException
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, UserMetaData, UserResourceRecordService}
import com.webank.wedatasphere.linkis.resourcemanager.utils.YarnUtil
import org.json4s.DefaultFormats

/**
  * Created by shanhuang on 9/11/18.
  */
abstract class RequestResourceService(userMetaData: UserMetaData, userResourceRecordService: UserResourceRecordService, moduleResourceRecordService: ModuleResourceRecordService, userResourceManager: UserResourceManager, moduleResourceManager: ModuleResourceManager) extends Logging {

  val requestPolicy: ResourceRequestPolicy

  def canRequest(moduleInstance: ServiceInstance, user: String, creator: String, requestResource: Resource): Boolean = {
    // Global instance limit check
    val userAvailableInstances = userMetaData.getUserGlobalInstanceLimit(user)
    val userExistingInstances = userResourceRecordService.getUserResourceRecordByUser(user).length
    info(s"user ${user} available instances: ${userAvailableInstances}, started instances: ${userExistingInstances}")
    if (userAvailableInstances <= userExistingInstances) {
      info(s"user ${user} can start ${userAvailableInstances} instances, but already have ${userExistingInstances} started.")
      throw new RMWarnException(111005, s"The user ${user} has started ${userExistingInstances} engines, and the number of global compute engine instances is limited to ${userAvailableInstances}, which failed to start.(用户 ${user} 已启动 ${userExistingInstances} 个引擎，而全局计算引擎实例数限制为 ${userAvailableInstances} 个，启动失败。)")
    }

    val moduleResourceRecord = moduleResourceRecordService.getModuleResourceRecord(moduleInstance)
    val moduleLeftResource = moduleResourceRecordService.deserialize(moduleResourceRecord.getLeftResource)
    val protectedResource = moduleResourceRecordService.deserialize(moduleResourceRecord.getProtectedResource)
    if ((moduleLeftResource - requestResource) < protectedResource) {
      info(s"moduleInstance:$moduleInstance left resource: ${moduleLeftResource} -  requestResource：$requestResource < protectedResource:${protectedResource}")
      throw new RMWarnException(111005, s"${generateNotEnoughMessage(requestResource, moduleLeftResource)}")
    }
    val (moduleAvailableResource, creatorAvailableResource) = userMetaData.getUserAvailableResource(moduleInstance.getApplicationName, user, creator)

    val (moduleUsedResource, creatorUsedResource) = userResourceRecordService.getModuleAndCreatorResource(moduleInstance.getApplicationName, user, creator, requestResource)

    if (moduleAvailableResource.resource >= moduleUsedResource) if (creatorAvailableResource.resource >= creatorUsedResource)
      true
    else {
      info(s"creator:$creator for $user had used module resource:$creatorUsedResource > creatorAvailableResource:${creatorAvailableResource.resource} ")
      throw new RMWarnException(111007, s"${generateNotEnoughMessage(creatorUsedResource, creatorAvailableResource.resource)}")
    } else {
      info(s"$user had used module resource:$moduleUsedResource > moduleAvailableResource: $moduleAvailableResource")
      throw new RMWarnException(111005, s"${generateNotEnoughMessage(moduleUsedResource, moduleAvailableResource.resource)}")
    }
  }

  def generateNotEnoughMessage(requestResource: Resource, availableResource: Resource): String = requestResource match {
    case m: MemoryResource =>
      s"Insufficient remote server memory resources(远程服务器内存资源不足)。"
    case c: CPUResource =>
      s"Insufficient remote server CPU resources(远程服务器CPU资源不足)。"
    case i: InstanceResource =>
      s"Insufficient remote server resources(远程服务器资源不足)。"
    case l: LoadResource =>
      val loadAvailable = availableResource.asInstanceOf[LoadResource]
      if (l.cores > loadAvailable.cores) s"Insufficient remote server CPU resources(远程服务器CPU资源不足)。" else s"Insufficient remote server memory resources(远程服务器内存资源不足)。"
    case li: LoadInstanceResource =>
      val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
      if (li.cores > loadInstanceAvailable.cores) s"Insufficient remote server CPU resources(远程服务器CPU资源不足)。" else if (li.memory > loadInstanceAvailable.memory) s"Insufficient remote server memory resources(远程服务器内存资源不足)。" else s"Insufficient remote server resources(远程服务器资源不足)。"
    case yarn: YarnResource =>
      val yarnAvailable = availableResource.asInstanceOf[YarnResource]
      if (yarn.queueCores > yarnAvailable.queueCores) s"The queue CPU resources are insufficient. It is recommended to reduce the number of actuators.(队列CPU资源不足，建议调小执行器个数。)" else if (yarn.queueMemory > yarnAvailable.queueMemory) s"The queue memory resources are insufficient. It is recommended to reduce the processor memory.(队列内存资源不足，建议调小执行器内存。)" else s"The number of queue instances exceeds the limit.(队列实例数超过限制。)"
    case dy: DriverAndYarnResource =>
      val dyAvailable = availableResource.asInstanceOf[DriverAndYarnResource]
      if (dy.loadInstanceResource > dyAvailable.loadInstanceResource) s"When requesting server resources(请求服务器资源时)，${generateNotEnoughMessage(dy.loadInstanceResource, dyAvailable.loadInstanceResource)}" else s"When requesting queue resources(请求队列资源时)，${generateNotEnoughMessage(dy.yarnResource, dyAvailable.yarnResource)}"
    case s: SpecialResource => throw new RMWarnException(111003, "not supported resource type " + s.getClass)
    case r: Resource => throw new RMWarnException(111003, "not supported resource type " + r.getClass)
  }
}


class SelfDefinedRequestResourceService(userMetaData: UserMetaData, userResourceRecordService: UserResourceRecordService, moduleResourceRecordService: ModuleResourceRecordService, userResourceManager: UserResourceManager, moduleResourceManager: ModuleResourceManager) extends RequestResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager) {
  override val requestPolicy: ResourceRequestPolicy = ResourceRequestPolicy.Special

  override def canRequest(moduleInstance: ServiceInstance, user: String, creator: String, requestResource: Resource): Boolean = {
    //TODO Use feign
    return false
  }

}

import com.webank.wedatasphere.linkis.resourcemanager.ResourceRequestPolicy._

class DefaultReqResourceService(userMetaData: UserMetaData, userResourceRecordService: UserResourceRecordService, moduleResourceRecordService: ModuleResourceRecordService, val userResourceManager: UserResourceManager,
                                val moduleResourceManager: ModuleResourceManager) extends RequestResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager) {

  implicit val formats = DefaultFormats + ResourceSerializer


  override val requestPolicy: ResourceRequestPolicy = Default

  override def canRequest(moduleInstance: ServiceInstance, user: String, creator: String, requestResource: Resource): Boolean = {
    super.canRequest(moduleInstance, user, creator, requestResource)
  }
}

class YarnReqResourceService(userMetaData: UserMetaData, userResourceRecordService: UserResourceRecordService, moduleResourceRecordService: ModuleResourceRecordService, val userResourceManager: UserResourceManager,
                             val moduleResourceManager: ModuleResourceManager) extends RequestResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager) {

  override val requestPolicy: ResourceRequestPolicy = Yarn

  override def canRequest(moduleInstance: ServiceInstance, user: String, creator: String, requestResource: Resource): Boolean = {
    if (!super.canRequest(moduleInstance, user, creator, requestResource)) return false
    val yarnResource = requestResource.asInstanceOf[YarnResource]
    val (maxCapacity, usedCapacity) = YarnUtil.getQueueInfo(yarnResource.queueName)
    info(s"This queue:${yarnResource.queueName} used resource:$usedCapacity and max resource：$maxCapacity")
    val queueLeftResource = maxCapacity - moduleResourceManager.getInstanceLockedResource(moduleInstance) - usedCapacity
    if (queueLeftResource < yarnResource) {
      info(s"User: $user request queue (${yarnResource.queueName}) resource $yarnResource is greater than queue (${yarnResource.queueName}) remaining resources $queueLeftResource(用户:$user 请求的队列（${yarnResource.queueName}）资源$yarnResource 大于队列（${yarnResource.queueName}）剩余资源$queueLeftResource) ")
      throw new RMWarnException(111007, s"${generateNotEnoughMessage(yarnResource, queueLeftResource)}")
    }
    else
      true
  }
}

class DriverAndYarnReqResourceService(userMetaData: UserMetaData, userResourceRecordService: UserResourceRecordService, moduleResourceRecordService: ModuleResourceRecordService, val userResourceManager: UserResourceManager,
                                      val moduleResourceManager: ModuleResourceManager) extends RequestResourceService(userMetaData, userResourceRecordService, moduleResourceRecordService, userResourceManager, moduleResourceManager) {

  implicit val formats = DefaultFormats + ResourceSerializer


  override val requestPolicy: ResourceRequestPolicy = DriverAndYarn

  override def canRequest(moduleInstance: ServiceInstance, user: String, creator: String, requestResource: Resource): Boolean = {
    if (!super.canRequest(moduleInstance, user, creator, requestResource)) return false
    val driverAndYarnResource = requestResource.asInstanceOf[DriverAndYarnResource]
    val yarnResource = driverAndYarnResource.yarnResource
    val (maxCapacity, usedCapacity) = YarnUtil.getQueueInfo(yarnResource.queueName)
    info(s"This queue:${yarnResource.queueName} used resource:$usedCapacity and max resource：$maxCapacity")
    val queueLeftResource = maxCapacity - usedCapacity //Add a collection of queue resource usage records(新增一个queue资源使用记录的集合)
    info(s"queue: ${yarnResource.queueName} left $queueLeftResource this request：$yarnResource ")
    if (queueLeftResource < yarnResource) {
      info(s"User: $user request queue (${yarnResource.queueName}) resource $yarnResource is greater than queue (${yarnResource.queueName}) remaining resources $queueLeftResource(用户:$user 请求的队列（${yarnResource.queueName}）资源$yarnResource 大于队列（${yarnResource.queueName}）剩余资源$queueLeftResource)")
      throw new RMWarnException(111007, s"${generateNotEnoughMessage(yarnResource, queueLeftResource)}")
    }
    else
      true
  }
}

