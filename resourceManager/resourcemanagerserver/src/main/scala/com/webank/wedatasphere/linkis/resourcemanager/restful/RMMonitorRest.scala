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

package com.webank.wedatasphere.linkis.resourcemanager.restful

import java.util
import java.util.Comparator

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.protocol.config.RequestQueryAppConfigWithGlobal
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngineStatus, RequestUserEngineKill, ResponseEngineStatus, ResponseUserEngineKill}
import com.webank.wedatasphere.linkis.protocol.utils.ProtocolUtils
import com.webank.wedatasphere.linkis.resourcemanager.domain.{ModuleInstanceSerializer, UserResourceMetaData}
import com.webank.wedatasphere.linkis.resourcemanager.service.metadata.{ModuleResourceRecordService, UserConfiguration, UserResourceRecordService}
import com.webank.wedatasphere.linkis.resourcemanager.utils.{RMConfiguration, YarnUtil}
import com.webank.wedatasphere.linkis.resourcemanager.{ResourceSerializer, _}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState
import com.webank.wedatasphere.linkis.server.Message
import com.webank.wedatasphere.linkis.server.security.SecurityFilter
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{POST, Path}
import org.codehaus.jackson.map.ObjectMapper
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
  * Created by shanhuang on 9/11/18.
  */
@Path("/resourcemanager")
@Component
class RMMonitorRest {

  @Autowired
  var moduleResourceManager: ModuleResourceManager = _
  @Autowired
  var userResourceManager: UserResourceManager = _
  @Autowired
  var moduleResourceRecordService: ModuleResourceRecordService = _
  @Autowired
  var userResourceRecordService: UserResourceRecordService = _

  implicit val formats = DefaultFormats + ResourceSerializer + ModuleInstanceSerializer
  val mapper = new ObjectMapper()

  def appendMessageData(message: Message, key: String, value: AnyRef) = message.data(key, mapper.readTree(write(value)))


  @POST
  @Path("moduleresources")
  def getModulesResource(@Context request: HttpServletRequest): Response = {
    val message = Message.ok("")
    val moduleRecords = moduleResourceRecordService.getAll
    for (moduleRecord <- moduleRecords) {
      val resourceMap = new mutable.HashMap[String, String]()
      resourceMap.put("totalResource", moduleResourceRecordService.deserialize(moduleRecord.getTotalResource).toJson)
      resourceMap.put("usedResource", moduleResourceRecordService.deserialize(moduleRecord.getUsedResource).toJson)
      appendMessageData(message, moduleRecord.getEmApplicationName, resourceMap)
    }
    message
  }

  @POST
  @Path("userresources")
  def getUserResource(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val userName = SecurityFilter.getLoginUsername(request)
    val modules = moduleResourceRecordService.getAll.map(_.getEmApplicationName).toSet
    modules.foreach { moduleName =>
      val resourceMap = new mutable.HashMap[String, String]()
      resourceMap.put("usedResource", userResourceManager.getModuleResourceUsed(moduleName, userName).toJson)
      resourceMap.put("lockedResource", userResourceManager.getModuleResourceLocked(moduleName, userName).toJson)
      appendMessageData(message, moduleName, resourceMap)
    }
    message
  }

  @POST
  @Path("engines")
  def getEngines(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val userName = SecurityFilter.getLoginUsername(request)
    val userResourceRecords = userResourceRecordService.getUserResourceRecordByUser(userName)
    if (userResourceRecords == null || userResourceRecords.isEmpty) return message
    val engines = ArrayBuffer[mutable.HashMap[String, Any]]()
    userResourceRecords.foreach { userResourceRecord =>
      val ticketId = userResourceRecord.getTicketId

      val record = new mutable.HashMap[String, Any]()
      record.put("ticketId", ticketId)
      record.put("applicationName", userResourceRecord.getEngineApplicationName)
      record.put("engineInstance", userResourceRecord.getEngineInstance)
      record.put("moduleName", userResourceRecord.getEmApplicationName)
      record.put("engineManagerInstance", userResourceRecord.getEmInstance)
      record.put("creator", userResourceRecord.getCreator)
      if (userResourceRecord.getUsedTime != null) record.put("usedTime", userResourceRecord.getUsedTime)
      if (userResourceRecord.getUserLockedResource != null) record.put("preUsedResource", userResourceRecordService.deserialize(userResourceRecord.getUserLockedResource).toJson)
      if (userResourceRecord.getUserUsedResource != null) record.put("usedResource", userResourceRecordService.deserialize(userResourceRecord.getUserUsedResource).toJson)
      if (userResourceRecord.getEngineInstance != null) try {
        val statusInfo = Sender.getSender(ServiceInstance(userResourceRecord.getEngineApplicationName, userResourceRecord.getEngineInstance))
          .ask(RequestEngineStatus(RequestEngineStatus.Status_BasicInfo)).asInstanceOf[ResponseEngineStatus]
        record.put("statusInfo", statusInfo)
        record.put("engineStatus", ExecutorState(statusInfo.state).toString)
      } catch {
        case _: Throwable =>
          record.put("engineStatus", "ShuttingDown")
          record.put("errorMessage", "Cannot connect to this engine, but its resource is still not released!")
      } else {
        record.put("applicationName", ProtocolUtils.getAppName(userResourceRecord.getEmApplicationName).getOrElse("") + "Engine")
        record.put("engineStatus", ExecutorState.Starting.toString)
      }
      engines.append(record)
    }
    appendMessageData(message, "engines", engines)
  }

  @POST
  @Path("enginekill")
  def killEngine(@Context request: HttpServletRequest, param: util.ArrayList[util.Map[String, AnyRef]]): Response = {
    val userName = SecurityFilter.getLoginUsername(request)
    for (engineParam <- param) {
      val ticketId = engineParam.get("ticketId").asInstanceOf[String]
      val moduleName = engineParam.get("moduleName").asInstanceOf[String]
      val emInstance = engineParam.get("engineManagerInstance").asInstanceOf[String]
      val creator = engineParam.get("creator").asInstanceOf[String]

      val requestKillEngine = RequestUserEngineKill(ticketId, creator, userName, Map.empty)
      val responseKillEngine = Sender.getSender(ServiceInstance(moduleName, emInstance)).ask(requestKillEngine).asInstanceOf[ResponseUserEngineKill]
    }
    Message.ok("成功提交kill引擎请求。")
  }

  @POST
  @Path("queueresources")
  def getQueueResource(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val queuename = param.get("queuename").asInstanceOf[String]
    val queueResource = YarnUtil.getQueueInfo(queuename)
    var usedMemoryPercentage, usedCPUPercentage = 0.0
    queueResource match {
      case (maxResource, usedResource) =>
        val queueInfo = new mutable.HashMap[String, Any]()
        queueInfo.put("queuename", maxResource.queueName)
        queueInfo.put("maxResources", Map("memory" -> maxResource.queueMemory, "cores" -> maxResource.queueCores))
        queueInfo.put("usedResources", Map("memory" -> usedResource.queueMemory, "cores" -> usedResource.queueCores))
        usedMemoryPercentage = usedResource.queueMemory.asInstanceOf[Double] / maxResource.queueMemory.asInstanceOf[Double]
        usedCPUPercentage = usedResource.queueCores.asInstanceOf[Double] / maxResource.queueCores.asInstanceOf[Double]
        queueInfo.put("usedPercentage", Map("memory" -> usedMemoryPercentage, "cores" -> usedCPUPercentage))
        appendMessageData(message, "queueInfo", queueInfo)
      case _ => Message.error("获取队列资源失败")
    }

    val userResources = new ArrayBuffer[mutable.HashMap[String, Any]]()
    val yarnAppsInfo = YarnUtil.getApplicationsInfo(queuename)
    val userToResourceRecords = userResourceRecordService.getAll().groupBy(_.getUser)
    yarnAppsInfo.groupBy(_.user).foreach { userAppInfo =>
      var busyResource = Resource.initResource(ResourceRequestPolicy.Yarn).asInstanceOf[YarnResource]
      var idleResource = Resource.initResource(ResourceRequestPolicy.Yarn).asInstanceOf[YarnResource]
      val appIdToResourceRecords = new mutable.HashMap[String, UserResourceMetaData]()
      userToResourceRecords.get(userAppInfo._1).getOrElse(new Array[UserResourceMetaData](0)).foreach { resourceRecord =>
        if (resourceRecord.getUserUsedResource != null) userResourceRecordService.deserialize(resourceRecord.getUserUsedResource) match {
          case driverYarn: DriverAndYarnResource if driverYarn.yarnResource.queueName.equals(queuename) =>
            appIdToResourceRecords.put(driverYarn.yarnResource.applicationId, resourceRecord)
          case yarn: YarnResource if yarn.queueName.equals(queuename) =>
            appIdToResourceRecords.put(yarn.applicationId, resourceRecord)
          case _ =>
        }
      }
      userAppInfo._2.foreach { appInfo =>
        appIdToResourceRecords.get(appInfo.id) match {
          case Some(resourceRecord) =>
            if (ExecutorState.Busy == askEngineStatus(resourceRecord.getEngineApplicationName, resourceRecord.getEngineInstance)) busyResource = busyResource.add(appInfo.usedResource) else idleResource = idleResource.add(appInfo.usedResource)
          case None =>
            //if(appInfo.applicationType == "MAPREDUCE")
            busyResource = busyResource.add(appInfo.usedResource)
        }
      }

      val totalResource = busyResource.add(idleResource)
      if (totalResource > Resource.getZeroResource(totalResource)) {
        val userResource = new mutable.HashMap[String, Any]()
        userResource.put("username", userAppInfo._1)
        if (usedMemoryPercentage > usedCPUPercentage) {
          userResource.put("busyPercentage", busyResource.queueMemory.asInstanceOf[Double] / queueResource._1.queueMemory.asInstanceOf[Double])
          userResource.put("idlePercentage", idleResource.queueMemory.asInstanceOf[Double] / queueResource._1.queueMemory.asInstanceOf[Double])
          userResource.put("totalPercentage", totalResource.queueMemory.asInstanceOf[Double] / queueResource._1.queueMemory.asInstanceOf[Double])
        } else {
          userResource.put("busyPercentage", busyResource.queueCores.asInstanceOf[Double] / queueResource._1.queueCores.asInstanceOf[Double])
          userResource.put("idlePercentage", idleResource.queueCores.asInstanceOf[Double] / queueResource._1.queueCores.asInstanceOf[Double])
          userResource.put("totalPercentage", totalResource.queueCores.asInstanceOf[Double] / queueResource._1.queueCores.asInstanceOf[Double])
        }
        userResources.add(userResource)
      }
    }
    //排序
    userResources.sort(new Comparator[mutable.Map[String, Any]]() {
      override def compare(o1: mutable.Map[String, Any], o2: mutable.Map[String, Any]): Int = if (o1.get("totalPercentage").getOrElse(0.0).asInstanceOf[Double] > o2.get("totalPercentage").getOrElse(0.0).asInstanceOf[Double]) -1 else 1
    })
    appendMessageData(message, "userResources", userResources)
  }

  private def askEngineStatus(engineApplicationName: String, engineInstance: String) = {
    val statusInfo = Sender.getSender(ServiceInstance(engineApplicationName, engineInstance))
      .ask(RequestEngineStatus(RequestEngineStatus.Status_BasicInfo)).asInstanceOf[ResponseEngineStatus]
    ExecutorState(statusInfo.state)
  }

  @POST
  @Path("queues")
  def getQueues(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok()
    val userName = SecurityFilter.getLoginUsername(request)
    val queues = new mutable.LinkedHashSet[String]()
    val userConfiguration = UserConfiguration.getCacheMap(RequestQueryAppConfigWithGlobal(userName, null, null, true))
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration))
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue)
    appendMessageData(message, "queues", queues)
  }

}
