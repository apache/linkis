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

import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Comparator, TimeZone}

import com.google.common.collect.Lists
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.entity.resource._
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineStopRequest
import com.webank.wedatasphere.linkis.manager.common.serializer.NodeResourceSerializer
import com.webank.wedatasphere.linkis.manager.common.utils.ResourceUtils
import com.webank.wedatasphere.linkis.manager.label.builder.CombinedLabelBuilder
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.cluster.ClusterLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineInstanceLabel, EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.persistence.{NodeManagerPersistence, NodeMetricManagerPersistence, ResourceManagerPersistence}
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer
import com.webank.wedatasphere.linkis.resourcemanager.external.service.ExternalResourceService
import com.webank.wedatasphere.linkis.resourcemanager.external.yarn.{YarnAppInfo, YarnResourceIdentifier}
import com.webank.wedatasphere.linkis.resourcemanager.service.LabelResourceService
import com.webank.wedatasphere.linkis.resourcemanager.utils.{RMConfiguration, RMUtils, UserConfiguration}
import com.webank.wedatasphere.linkis.server.{BDPJettyServerHelper, Message}
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


@Path("/linkisManager/rm")
@Component
class RMMonitorRest extends Logging {

  implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
  val mapper = new ObjectMapper()
  val dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
  dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
  val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
  val combinedLabelBuilder = new CombinedLabelBuilder

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  @Autowired
  var externalResourceService: ExternalResourceService = _

  @Autowired
  var labelResourceService: LabelResourceService = _

  @Autowired
  private var messagePublisher: MessagePublisher = _

  def appendMessageData(message: Message, key: String, value: AnyRef) = message.data(key, mapper.readTree(write(value)))

  @POST
  @Path("applicationlist")
  def getApplicationList(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val userName = SecurityFilter.getLoginUsername(request)
    val userCreator = param.get("userCreator").asInstanceOf[String]
    val engineType = if(param.get("engineType") == null) null else param.get("engineType").asInstanceOf[String]
    val nodes = getEngineNodes(userName, true)
    val creatorToApplicationList = new mutable.HashMap[String, mutable.HashMap[String, Any]]
    nodes.foreach{ node =>
      val userCreatorLabel = node.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
      if(getUserCreator(userCreatorLabel).equals(userCreator)){
        if(engineType == null || getEngineType(engineTypeLabel).equals(engineType)){
          if(!creatorToApplicationList.contains(userCreatorLabel.getCreator)){
            val applicationList = new mutable.HashMap[String, Any]
            applicationList.put("engineInstances", new mutable.ArrayBuffer[Any])
            applicationList.put("usedResource", Resource.initResource(ResourceType.LoadInstance))
            applicationList.put("maxResource", Resource.initResource(ResourceType.LoadInstance))
            applicationList.put("minResource", Resource.initResource(ResourceType.LoadInstance))
            creatorToApplicationList.put(userCreatorLabel.getCreator, applicationList)
          }
          val applicationList = creatorToApplicationList.get(userCreatorLabel.getCreator).get
          applicationList.put("usedResource", applicationList.get("usedResource").get.asInstanceOf[Resource] + node.getNodeResource.getUsedResource)
          applicationList.put("maxResource", applicationList.get("maxResource").get.asInstanceOf[Resource] + node.getNodeResource.getMaxResource)
          applicationList.put("minResource", applicationList.get("minResource").get.asInstanceOf[Resource] + node.getNodeResource.getMinResource)
          val engineInstance = new mutable.HashMap[String, Any]
          engineInstance.put("creator", userCreatorLabel.getCreator)
          engineInstance.put("engineType", engineTypeLabel.getEngineType)
          engineInstance.put("instance", node.getServiceInstance.getInstance)
          engineInstance.put("label", engineTypeLabel.getStringValue)
          engineInstance.put("resource", node.getNodeResource)
          if(node.getNodeStatus == null){
            engineInstance.put("status", "Busy")
          } else {
            engineInstance.put("status", node.getNodeStatus.toString)
          }
          engineInstance.put("startTime", dateFormat.format(node.getStartTime))
          engineInstance.put("owner", node.getOwner)
          applicationList.get("engineInstances").get.asInstanceOf[mutable.ArrayBuffer[Any]].append(engineInstance)
        }
      }
    }
    val applications = creatorToApplicationList.map{ creatorEntry =>
      val application = new mutable.HashMap[String, Any]
      application.put("creator", creatorEntry._1)
      application.put("applicationList", creatorEntry._2)
      application
    }
    appendMessageData(message, "applications", applications)
    message
  }

  @POST
  @Path("userresources")
  def getUserResource(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val userName = SecurityFilter.getLoginUsername(request)
    var nodes = getEngineNodes(userName, true)
    if(nodes == null) nodes = new Array[EngineNode](1)
    val userCreatorEngineTypeResourceMap =new mutable.HashMap[String, mutable.HashMap[String, NodeResource]]
    nodes.foreach { node =>
      val userCreatorLabel = node.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
      val userCreator = getUserCreator(userCreatorLabel)
      if(!userCreatorEngineTypeResourceMap.contains(userCreator)){
        userCreatorEngineTypeResourceMap.put(userCreator, new mutable.HashMap[String, NodeResource])
      }
      val engineTypeResourceMap = userCreatorEngineTypeResourceMap.get(userCreator).get
      val engineType = getEngineType(engineTypeLabel)
      if(!engineTypeResourceMap.contains(engineType)){
        val nodeResource = CommonNodeResource.initNodeResource(ResourceType.LoadInstance)
        engineTypeResourceMap.put(engineType, nodeResource)
      }
      val resource = engineTypeResourceMap.get(engineType).get
      resource.setUsedResource(node.getNodeResource.getUsedResource + resource.getUsedResource)
      //combined label
      val combinedLabel = combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel));
      var labelResource = labelResourceService.getLabelResource(combinedLabel)
      if(labelResource == null){
        resource.setLeftResource(node.getNodeResource.getMaxResource - resource.getUsedResource)
      }else {
        resource.setLeftResource(labelResource.getLeftResource)
      }
    }
    val userCreatorEngineTypeResources = userCreatorEngineTypeResourceMap.map{ userCreatorEntry =>
      val userCreatorEngineTypeResource = new mutable.HashMap[String, Any]
      userCreatorEngineTypeResource.put("userCreator", userCreatorEntry._1)
      val engineTypeResources = userCreatorEntry._2.map{ engineTypeEntry =>
        val engineTypeResource = new mutable.HashMap[String, Any]
        engineTypeResource.put("engineType", engineTypeEntry._1)
        engineTypeResource.put("resource", engineTypeEntry._2)
        engineTypeResource
      }
      userCreatorEngineTypeResource.put("engineTypes", engineTypeResources)
      userCreatorEngineTypeResource
    }
    appendMessageData(message, "userResources", userCreatorEngineTypeResources)
    message
  }

  private def getUserCreator(userCreatorLabel: UserCreatorLabel): String = {
    "(" + userCreatorLabel.getUser + "," + userCreatorLabel.getCreator + ")"
  }

  private def getEngineType(engineTypeLabel: EngineTypeLabel): String = {
    "(" + engineTypeLabel.getEngineType + "," + engineTypeLabel.getVersion + ")"
  }

  @POST
  @Path("engines")
  def getEngines(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val userName = SecurityFilter.getLoginUsername(request)
    val nodes = getEngineNodes(userName, true)
    if(nodes == null || nodes.isEmpty) return message
    val engines = ArrayBuffer[mutable.HashMap[String, Any]]()
    nodes.foreach{ node =>
      val userCreatorLabel = node.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
      val record = new mutable.HashMap[String, Any]
      record.put("applicationName", node.getServiceInstance.getApplicationName)
      record.put("engineInstance", node.getServiceInstance.getInstance)
      record.put("moduleName", node.getEMNode.getServiceInstance.getApplicationName)
      record.put("engineManagerInstance", node.getEMNode.getServiceInstance.getInstance)
      record.put("creator", userCreatorLabel.getCreator)
      record.put("engineType", engineTypeLabel.getEngineType)
      if(node.getNodeResource != null){
        if (node.getNodeResource.getLockedResource != null) record.put("preUsedResource", node.getNodeResource.getLockedResource)
        if (node.getNodeResource.getUsedResource != null) record.put("usedResource", node.getNodeResource.getUsedResource)
      }
      if(node.getNodeStatus == null){
        record.put("engineStatus", "Busy")
      } else {
        record.put("engineStatus", node.getNodeStatus.toString)
      }
      engines.append(record)
    }
    appendMessageData(message, "engines", engines)
  }

  /**
   * 仅用于向下兼容老接口
   * @param request
   * @param param
   * @return
   */
  @POST
  @Path("enginekill")
  def killEngine(@Context request: HttpServletRequest, param: util.ArrayList[util.Map[String, AnyRef]]): Response = {
    val userName = SecurityFilter.getLoginUsername(request)
    for (engineParam <- param) {
      val moduleName = engineParam.get("applicationName").asInstanceOf[String]
      val engineInstance = engineParam.get("engineInstance").asInstanceOf[String]
      val stopEngineRequest = new EngineStopRequest(ServiceInstance(moduleName, engineInstance), userName)
      val job = messagePublisher.publish(stopEngineRequest)
      Utils.tryAndWarn(job.get(RMUtils.MANAGER_KILL_ENGINE_EAIT.getValue.toLong, TimeUnit.MILLISECONDS))
      info(s"Finished to kill engine ")
    }
    Message.ok("成功提交kill引擎请求。")
  }


  @POST
  @Path("queueresources")
  def getQueueResource(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok("")
    val yarnIdentifier = new YarnResourceIdentifier(param.get("queuename").asInstanceOf[String])
    val clusterLabel = labelFactory.createLabel(classOf[ClusterLabel])
    clusterLabel.setClusterName(param.get("clustername").asInstanceOf[String])
    clusterLabel.setClusterType(param.get("clustertype").asInstanceOf[String])
    val labelContainer = new RMLabelContainer(Lists.newArrayList(clusterLabel))
    val providedYarnResource = externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
    var usedMemoryPercentage, usedCPUPercentage = 0.0
    (providedYarnResource.getMaxResource.asInstanceOf[YarnResource],
      providedYarnResource.getUsedResource.asInstanceOf[YarnResource]) match {
      case (maxResource, usedResource) =>
        val queueInfo = new mutable.HashMap[String, Any]()
        queueInfo.put("queuename", maxResource)
        queueInfo.put("maxResources", Map("memory" -> maxResource.queueMemory, "cores" -> maxResource.queueCores))
        queueInfo.put("usedResources", Map("memory" -> usedResource.queueMemory, "cores" -> usedResource.queueCores))
        usedMemoryPercentage = usedResource.queueMemory.asInstanceOf[Double] / maxResource.queueMemory.asInstanceOf[Double]
        usedCPUPercentage = usedResource.queueCores.asInstanceOf[Double] / maxResource.queueCores.asInstanceOf[Double]
        queueInfo.put("usedPercentage", Map("memory" -> usedMemoryPercentage, "cores" -> usedCPUPercentage))
        appendMessageData(message, "queueInfo", queueInfo)
      case _ => Message.error("获取队列资源失败")
    }

    val userResourceRecords = new ArrayBuffer[mutable.HashMap[String, Any]]()
    val yarnAppsInfo = externalResourceService.getAppInfo(ResourceType.Yarn, labelContainer, yarnIdentifier)
    yarnAppsInfo.groupBy(_.asInstanceOf[YarnAppInfo].user).foreach { userAppInfo =>
      val nodes = getEngineNodes(userAppInfo._1, true)
      var busyResource = Resource.initResource(ResourceType.Yarn).asInstanceOf[YarnResource]
      var idleResource = Resource.initResource(ResourceType.Yarn).asInstanceOf[YarnResource]
      val appIdToEngineNode = new mutable.HashMap[String, EngineNode]()
      nodes.foreach { node =>
        if (node.getNodeResource != null && node.getNodeResource.getUsedResource != null) node.getNodeResource.getUsedResource match {
          case driverYarn: DriverAndYarnResource if driverYarn.yarnResource.queueName.equals(yarnIdentifier.getQueueName) =>
            appIdToEngineNode.put(driverYarn.yarnResource.applicationId, node)
          case yarn: YarnResource if yarn.queueName.equals(yarnIdentifier.getQueueName) =>
            appIdToEngineNode.put(yarn.applicationId, node)
          case _ =>
        }
      }
      userAppInfo._2.foreach { appInfo =>
        appIdToEngineNode.get(appInfo.asInstanceOf[YarnAppInfo].id) match {
          case Some(node) =>
            if(NodeStatus.Busy == node.getNodeStatus){
              busyResource = busyResource.add(appInfo.asInstanceOf[YarnAppInfo].usedResource)
            } else {
              idleResource = idleResource.add(appInfo.asInstanceOf[YarnAppInfo].usedResource)
            }
          case None =>
            busyResource = busyResource.add(appInfo.asInstanceOf[YarnAppInfo].usedResource)
        }
      }

      val totalResource = busyResource.add(idleResource)
      if (totalResource > Resource.getZeroResource(totalResource)) {
        val userResource = new mutable.HashMap[String, Any]()
        userResource.put("username", userAppInfo._1)
        val queueResource = providedYarnResource.getMaxResource.asInstanceOf[YarnResource]
        if (usedMemoryPercentage > usedCPUPercentage) {
          userResource.put("busyPercentage", busyResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory.asInstanceOf[Double])
          userResource.put("idlePercentage", idleResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory.asInstanceOf[Double])
          userResource.put("totalPercentage", totalResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory.asInstanceOf[Double])
        } else {
          userResource.put("busyPercentage", busyResource.queueCores.asInstanceOf[Double] / queueResource.queueCores.asInstanceOf[Double])
          userResource.put("idlePercentage", idleResource.queueCores.asInstanceOf[Double] / queueResource.queueCores.asInstanceOf[Double])
          userResource.put("totalPercentage", totalResource.queueCores.asInstanceOf[Double] / queueResource.queueCores.asInstanceOf[Double])
        }
        userResourceRecords.add(userResource)
      }
    }
    //排序
    userResourceRecords.sort(new Comparator[mutable.Map[String, Any]]() {
      override def compare(o1: mutable.Map[String, Any], o2: mutable.Map[String, Any]): Int = if (o1.get("totalPercentage").getOrElse(0.0).asInstanceOf[Double] > o2.get("totalPercentage").getOrElse(0.0).asInstanceOf[Double]) -1 else 1
    })
    appendMessageData(message, "userResources", userResourceRecords)
  }

  private def getEngineNodes(user: String, withResource: Boolean = false): Array[EngineNode] ={
    val nodes = nodeManagerPersistence.getNodes(user).map(_.getServiceInstance).map(nodeManagerPersistence.getEngineNode).filter(_ != null)
    val metrics = nodeMetricManagerPersistence.getNodeMetrics(nodes).map(m => (m.getServiceInstance.toString,m)).toMap
    val configurationMap = new mutable.HashMap[String, Resource]
    nodes.map{ node =>
      node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance))
      if(!node.getLabels.exists(_.isInstanceOf[UserCreatorLabel])){
        return null
      }
      metrics.get(node.getServiceInstance.toString).foreach(metricsConverter.fillMetricsToNode(node,_))
      if(withResource){
        val userCreatorLabel = node.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
        val engineTypeLabel = node.getLabels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
        val engineInstanceLabel = node.getLabels.find(_.isInstanceOf[EngineInstanceLabel]).get.asInstanceOf[EngineInstanceLabel]
        engineInstanceLabel.setServiceName(node.getServiceInstance.getApplicationName)
        engineInstanceLabel.setInstance(node.getServiceInstance.getInstance)
        val nodeResource = labelResourceService.getLabelResource(engineInstanceLabel)
        val configurationKey = getUserCreator(userCreatorLabel) + getEngineType(engineTypeLabel)
        val configuredResource = configurationMap.get(configurationKey) match {
          case Some(resource) => resource
          case None =>
            if(nodeResource != null){
              val resource = UserConfiguration.getUserConfiguredResource(nodeResource.getResourceType, userCreatorLabel, engineTypeLabel)
              configurationMap.put(configurationKey, resource)
              resource
            }else null
        }
        if(nodeResource != null){
          nodeResource.setMaxResource(configuredResource)
          if(null == nodeResource.getUsedResource) nodeResource.setUsedResource(nodeResource.getLockedResource)
          if(null == nodeResource.getMinResource) nodeResource.setMinResource(Resource.initResource(nodeResource.getResourceType))
          node.setNodeResource(ResourceUtils.convertTo(nodeResource, ResourceType.LoadInstance))
        }
      }
      node
    }.filter(_ != null).toArray
  }

  @POST
  @Path("queues")
  def getQueues(@Context request: HttpServletRequest, param: util.Map[String, AnyRef]): Response = {
    val message = Message.ok()
    val userName = SecurityFilter.getLoginUsername(request)
    val clusters = new mutable.ArrayBuffer[Any]()
    val clusterInfo = new mutable.HashMap[String, Any]()
    val queues = new mutable.LinkedHashSet[String]()
    val userConfiguration = UserConfiguration.getGlobalConfig(userName)
    val clusterName = RMConfiguration.USER_AVAILABLE_CLUSTER_NAME.getValue(userConfiguration)
    clusterInfo.put("clustername", clusterName)
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration))
    queues.add(RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue)
    clusterInfo.put("queues", queues)
    clusters.append(clusterInfo)
    appendMessageData(message, "queues", clusters)
  }

}
