/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.rm.restful

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.conf.RMConfiguration
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.RMErrorException
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel
import org.apache.linkis.manager.label.entity.engine.{
  EngineInstanceLabel,
  EngineTypeLabel,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.persistence.{
  LabelManagerPersistence,
  NodeManagerPersistence,
  NodeMetricManagerPersistence,
  ResourceManagerPersistence
}
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.external.service.ExternalResourceService
import org.apache.linkis.manager.rm.external.yarn.{YarnAppInfo, YarnResourceIdentifier}
import org.apache.linkis.manager.rm.restful.vo.{UserCreatorEngineType, UserResourceVo}
import org.apache.linkis.manager.rm.service.{LabelResourceService, ResourceManager}
import org.apache.linkis.manager.rm.service.impl.UserResourceService
import org.apache.linkis.manager.rm.utils.{RMUtils, UserConfiguration}
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.server.{toScalaBuffer, BDPJettyServerHelper, Message}
import org.apache.linkis.server.security.SecurityFilter
import org.apache.linkis.server.utils.ModuleUserUtils

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

import javax.servlet.http.HttpServletRequest

import java.text.{MessageFormat, SimpleDateFormat}
import java.util
import java.util.{Comparator, List, TimeZone}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.pagehelper.page.PageMethod
import com.google.common.collect.Lists
import io.swagger.annotations.{Api, ApiOperation}

@RestController
@Api(tags = Array("resource management"))
@RequestMapping(path = Array("/linkisManager/rm"))
class RMMonitorRest extends Logging {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  private val dateFormatLocal = new ThreadLocal[SimpleDateFormat]() {
    override protected def initialValue = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
  }

  dateFormatLocal.get().setTimeZone(TimeZone.getTimeZone("GMT"))
  val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
  val combinedLabelBuilder = new CombinedLabelBuilder
  val gson = BDPJettyServerHelper.gson

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

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
  var resourceManager: ResourceManager = _

  @Autowired
  var userResourceService: UserResourceService = _

  var COMBINED_USERCREATOR_ENGINETYPE: String = _

  def appendMessageData(message: Message, key: String, value: AnyRef): Message = {
    val result = mapper.writeValueAsString(value)
    logger.info(s"appendMessageData result: $result")
    message.data(key, mapper.readTree(result))
  }

  @ApiOperation(value = "getApplicationList", notes = "get applicationList")
  @RequestMapping(path = Array("applicationlist"), method = Array(RequestMethod.POST))
  def getApplicationList(
      request: HttpServletRequest,
      @RequestBody param: util.Map[String, AnyRef]
  ): Message = {
    val message = Message.ok("")
    val userName = ModuleUserUtils.getOperationUser(request, "applicationlist")
    val userCreator =
      if (param.get("userCreator") == null) null
      else param.get("userCreator").asInstanceOf[String]
    val engineType =
      if (param.get("engineType") == null) null else param.get("engineType").asInstanceOf[String]
    val nodes = getEngineNodes(userName, true)

    val creatorToApplicationList = getCreatorToApplicationList(userCreator, engineType, nodes)

    val applications = getApplications(creatorToApplicationList)
    appendMessageData(message, "applications", applications)
    message
  }

  @ApiOperation(value = "resetUserResource", notes = "reset user resource")
  @RequestMapping(path = Array("resetResource"), method = Array(RequestMethod.DELETE))
  def resetUserResource(
      request: HttpServletRequest,
      @RequestParam(value = "resourceId", required = false) resourceId: Integer
  ): Message = {
    val queryUser = SecurityFilter.getLoginUser(request)
    if (Configuration.isNotAdmin(queryUser.get)) {
      throw new RMErrorException(ONLY_ADMIN_RESET.getErrorCode, ONLY_ADMIN_RESET.getErrorDesc)
    }

    if (resourceId == null || resourceId <= 0) {
      userResourceService.resetAllUserResource(COMBINED_USERCREATOR_ENGINETYPE)
    } else {
      userResourceService.resetUserResource(resourceId)
    }
    Message.ok("success")
  }

  @ApiOperation(value = "listAllEngineType", notes = "list all engineType")
  @RequestMapping(path = Array("engineType"), method = Array(RequestMethod.GET))
  def listAllEngineType(request: HttpServletRequest): Message = {
    val engineTypeString = RMUtils.ENGINE_TYPE.getValue
    val engineTypeList = engineTypeString.split(",")
    Message.ok.data("engineType", engineTypeList)
  }

  @ApiOperation(value = "getAllUserResource", notes = "get all user resource")
  @RequestMapping(path = Array("allUserResource"), method = Array(RequestMethod.GET))
  def getAllUserResource(
      request: HttpServletRequest,
      @RequestParam(value = "username", required = false) username: String,
      @RequestParam(value = "creator", required = false) creator: String,
      @RequestParam(value = "engineType", required = false) engineType: String,
      @RequestParam(value = "page", required = false) page: Int,
      @RequestParam(value = "size", required = false) size: Int
  ): Message = {
    val queryUser = SecurityFilter.getLoginUser(request)
    if (Configuration.isNotAdmin(queryUser.get)) {
      throw new RMErrorException(ONLY_ADMIN_READ.getErrorCode, ONLY_ADMIN_READ.getErrorDesc)
    }
    // 1. Construct a string for SQL LIKE query, query the label_value of the label table
    val searchUsername = if (StringUtils.isEmpty(username)) "" else username
    val searchCreator = if (StringUtils.isEmpty(creator)) "" else creator
    val searchEngineType = if (StringUtils.isEmpty(engineType)) "" else engineType
    // label value in db as :{"creator":"nodeexecution","user":"hadoop","engineType":"appconn","version":"1"}
    val labelValuePattern =
      MessageFormat.format("%{0}%,%{1}%,%{2}%,%", searchCreator, searchUsername, searchEngineType)

    if (COMBINED_USERCREATOR_ENGINETYPE == null) {
      val userCreatorLabel = labelFactory.createLabel(classOf[UserCreatorLabel])
      val engineTypeLabel = labelFactory.createLabel(classOf[EngineTypeLabel])
      val combinedLabel =
        combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel))
      COMBINED_USERCREATOR_ENGINETYPE = combinedLabel.getLabelKey
    }
    // 2. The resource label of all users, including the associated resourceId
    val resultPage = PageMethod.startPage(page, size)
    val userLabels = labelManagerPersistence.getLabelByPattern(
      labelValuePattern,
      COMBINED_USERCREATOR_ENGINETYPE,
      page,
      size
    )
    // 3. All user resources, including resourceId
    val resources = resourceManagerPersistence.getResourceByLabels(userLabels)
    val userResources = new util.ArrayList[UserResourceVo]()
    // 4. Store users and resources in Vo
    resources.asScala.foreach(resource => {
      val userResource = ResourceUtils.fromPersistenceResourceAndUser(resource)
      val userLabel = userLabels.asScala.find(_.getResourceId.equals(resource.getId)).orNull
      if (userLabel != null) {
        val userCreatorEngineType =
          gson.fromJson(userLabel.getStringValue, classOf[UserCreatorEngineType])
        if (userCreatorEngineType != null) {
          userResource.setUsername(userCreatorEngineType.getUser)
          userResource.setCreator(userCreatorEngineType.getCreator)
          userResource.setEngineType(userCreatorEngineType.getEngineType)
          userResource.setVersion(userCreatorEngineType.getVersion)
        }
      }
      userResources.add(RMUtils.toUserResourceVo(userResource))
    })
    Message.ok().data("resources", userResources).data("total", resultPage.getTotal)
  }

  @ApiOperation(value = "getUserResource", notes = "get user resource")
  @RequestMapping(path = Array("userresources"), method = Array(RequestMethod.POST))
  def getUserResource(
      request: HttpServletRequest,
      @RequestBody(required = false) param: util.Map[String, AnyRef]
  ): Message = {
    val message = Message.ok("")
    val userName = ModuleUserUtils.getOperationUser(request, "get userresources")
    var nodes = getEngineNodes(userName, true)
    if (nodes == null) {
      nodes = new Array[EngineNode](0)
    } else {
      nodes = nodes.filter(node => {
        node.getNodeResource != null &&
        !node.getLabels.isEmpty &&
        node.getLabels.asScala.find(_.isInstanceOf[UserCreatorLabel]).get != null &&
        node.getLabels.asScala.find(_.isInstanceOf[EngineTypeLabel]).get != null
      })
    }

    val userCreatorEngineTypeResourceMap =
      getUserCreatorEngineTypeResourceMap(nodes)

    val userCreatorEngineTypeResources = getUserResources(userCreatorEngineTypeResourceMap)

    appendMessageData(message, "userResources", userCreatorEngineTypeResources)
    message
  }

  @ApiOperation(value = "getEngines", notes = "get engines")
  @RequestMapping(path = Array("engines"), method = Array(RequestMethod.POST))
  def getEngines(
      request: HttpServletRequest,
      @RequestBody(required = false) param: util.Map[String, AnyRef]
  ): Message = {
    val message = Message.ok("")
    val userName = ModuleUserUtils.getOperationUser(request, "get engines")
    val nodes = getEngineNodes(userName, true)
    if (nodes == null || nodes.isEmpty) return message
    val engines = ArrayBuffer[mutable.HashMap[String, Any]]()
    nodes.foreach { node =>
      val userCreatorLabel = node.getLabels.asScala
        .find(_.isInstanceOf[UserCreatorLabel])
        .get
        .asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.asScala
        .find(_.isInstanceOf[EngineTypeLabel])
        .get
        .asInstanceOf[EngineTypeLabel]
      val record = new mutable.HashMap[String, Any]
      if (node.getServiceInstance != null) {
        record.put("applicationName", node.getServiceInstance.getApplicationName)
        record.put("engineInstance", node.getServiceInstance.getInstance)
      }

      record.put("creator", userCreatorLabel.getCreator)
      record.put("engineType", engineTypeLabel.getEngineType)
      if (node.getNodeResource != null) {
        if (node.getNodeResource.getLockedResource != null) {
          record.put("preUsedResource", node.getNodeResource.getLockedResource)
        }
        if (node.getNodeResource.getUsedResource != null) {
          record.put("usedResource", node.getNodeResource.getUsedResource)
        }
      }
      if (node.getNodeStatus == null) {
        record.put("engineStatus", "Busy")
      } else {
        record.put("engineStatus", node.getNodeStatus.toString)
      }
      engines.append(record)
    }
    appendMessageData(message, "engines", engines)
  }

  @ApiOperation(value = "getQueueResource", notes = "get queue resource")
  @RequestMapping(path = Array("queueresources"), method = Array(RequestMethod.POST))
  def getQueueResource(
      request: HttpServletRequest,
      @RequestBody param: util.Map[String, AnyRef]
  ): Message = {
    ModuleUserUtils.getOperationUser(request, "getQueueResource")
    val message = Message.ok("")
    val yarnIdentifier = new YarnResourceIdentifier(param.get("queuename").asInstanceOf[String])
    val clusterLabel = labelFactory.createLabel(classOf[ClusterLabel])
    clusterLabel.setClusterName(param.get("clustername").asInstanceOf[String])
    clusterLabel.setClusterType(param.get("clustertype").asInstanceOf[String])
    val labelContainer = new RMLabelContainer(Lists.newArrayList(clusterLabel))
    val providedYarnResource =
      externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
    var usedMemoryPercentage, usedCPUPercentage = 0.0
    (
      providedYarnResource.getMaxResource.asInstanceOf[YarnResource],
      providedYarnResource.getUsedResource.asInstanceOf[YarnResource]
    ) match {
      case (maxResource, usedResource) =>
        val queueInfo = new mutable.HashMap[String, Any]()
        queueInfo.put("queuename", maxResource)
        queueInfo.put(
          "maxResources",
          Map("memory" -> maxResource.queueMemory, "cores" -> maxResource.queueCores)
        )
        queueInfo.put(
          "usedResources",
          Map("memory" -> usedResource.queueMemory, "cores" -> usedResource.queueCores)
        )
        usedMemoryPercentage = usedResource.queueMemory
          .asInstanceOf[Double] / maxResource.queueMemory.asInstanceOf[Double]
        usedCPUPercentage = usedResource.queueCores.asInstanceOf[Double] / maxResource.queueCores
          .asInstanceOf[Double]
        queueInfo.put(
          "usedPercentage",
          Map("memory" -> usedMemoryPercentage, "cores" -> usedCPUPercentage)
        )
        queueInfo.put("maxApps", providedYarnResource.getMaxApps)
        queueInfo.put("numActiveApps", providedYarnResource.getNumActiveApps)
        queueInfo.put("numPendingApps", providedYarnResource.getNumPendingApps)
        appendMessageData(message, "queueInfo", queueInfo)
      case _ => Message.error("Failed to get queue resource")
    }

    val userResourceRecords = new ArrayBuffer[mutable.HashMap[String, Any]]()
    val yarnAppsInfo =
      externalResourceService.getAppInfo(ResourceType.Yarn, labelContainer, yarnIdentifier)
    val userList =
      yarnAppsInfo.asScala.groupBy(_.asInstanceOf[YarnAppInfo].user).keys.toList.asJava
    Utils.tryCatch {
      val nodesList = getEngineNodesByUserList(userList, true)
      yarnAppsInfo.asScala.groupBy(_.asInstanceOf[YarnAppInfo].user).foreach { userAppInfo =>
        var busyResource = Resource.initResource(ResourceType.Yarn).asInstanceOf[YarnResource]
        var idleResource = Resource.initResource(ResourceType.Yarn).asInstanceOf[YarnResource]
        val appIdToEngineNode = new mutable.HashMap[String, EngineNode]()
        val nodesplus = nodesList.get(userAppInfo._1)
        if (nodesplus.isDefined) {
          nodesplus.get.foreach(node => {
            if (node.getNodeResource != null && node.getNodeResource.getUsedResource != null) {
              node.getNodeResource.getUsedResource match {
                case driverYarn: DriverAndYarnResource
                    if driverYarn.yarnResource.queueName.equals(yarnIdentifier.getQueueName) =>
                  appIdToEngineNode.put(driverYarn.yarnResource.applicationId, node)
                case yarn: YarnResource if yarn.queueName.equals(yarnIdentifier.getQueueName) =>
                  appIdToEngineNode.put(yarn.applicationId, node)
                case _ =>
              }
            }
          })
        }
        userAppInfo._2.foreach { appInfo =>
          appIdToEngineNode.get(appInfo.asInstanceOf[YarnAppInfo].id) match {
            case Some(node) =>
              if (NodeStatus.Busy == node.getNodeStatus) {
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
            userResource.put(
              "busyPercentage",
              busyResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory
                .asInstanceOf[Double]
            )
            userResource.put(
              "idlePercentage",
              idleResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory
                .asInstanceOf[Double]
            )
            userResource.put(
              "totalPercentage",
              totalResource.queueMemory.asInstanceOf[Double] / queueResource.queueMemory
                .asInstanceOf[Double]
            )
          } else {
            userResource.put(
              "busyPercentage",
              busyResource.queueCores.asInstanceOf[Double] / queueResource.queueCores
                .asInstanceOf[Double]
            )
            userResource.put(
              "idlePercentage",
              idleResource.queueCores.asInstanceOf[Double] / queueResource.queueCores
                .asInstanceOf[Double]
            )
            userResource.put(
              "totalPercentage",
              totalResource.queueCores.asInstanceOf[Double] / queueResource.queueCores
                .asInstanceOf[Double]
            )
          }
          userResourceRecords.asJava.add(userResource)
        }
      }
    } {
      case exception: Exception =>
        logger.error(s"queresource search failed!", exception)
      case _ =>
    }

    userResourceRecords.asJava.sort(new Comparator[mutable.Map[String, Any]]() {
      override def compare(o1: mutable.Map[String, Any], o2: mutable.Map[String, Any]): Int = if (
          o1.getOrElse("totalPercentage", 0.0)
            .asInstanceOf[Double] > o2.getOrElse("totalPercentage", 0.0).asInstanceOf[Double]
      ) {
        -1
      } else 1
    })
    appendMessageData(message, "userResources", userResourceRecords)
  }

  @ApiOperation(value = "getQueues", notes = "get queues")
  @RequestMapping(path = Array("queues"), method = Array(RequestMethod.POST))
  def getQueues(
      request: HttpServletRequest,
      @RequestBody(required = false) param: util.Map[String, AnyRef]
  ): Message = {
    val message = Message.ok()
    val userName = ModuleUserUtils.getOperationUser(request, "get queues")
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

  private def getUserCreator(userCreatorLabel: UserCreatorLabel): String = {
    "(" + userCreatorLabel.getUser + "," + userCreatorLabel.getCreator + ")"
  }

  private def getEngineType(engineTypeLabel: EngineTypeLabel): String = {
    "(" + engineTypeLabel.getEngineType + "," + engineTypeLabel.getVersion + ")"
  }

  private def getEngineNodes(user: String, withResource: Boolean = false): Array[EngineNode] = {
    val serviceInstancelist = nodeManagerPersistence
      .getNodes(user)
      .map(_.getServiceInstance)
      .asJava
    val nodes = nodeManagerPersistence.getEngineNodeByServiceInstance(serviceInstancelist)
    val metrics = nodeMetricManagerPersistence
      .getNodeMetrics(nodes)
      .asScala
      .map(m => (m.getServiceInstance.toString, m))
      .toMap
    val configurationMap = new mutable.HashMap[String, Resource]
    val labelsMap =
      nodeLabelService.getNodeLabelsByInstanceList(nodes.map(_.getServiceInstance).asJava)
    nodes.asScala
      .map { node =>
//        node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance))
        node.setLabels(labelsMap.get(node.getServiceInstance.toString))
        if (!node.getLabels.asScala.exists(_.isInstanceOf[UserCreatorLabel])) {
          null
        } else {
          metrics
            .get(node.getServiceInstance.toString)
            .foreach(metricsConverter.fillMetricsToNode(node, _))
          if (withResource) {
            val userCreatorLabelOption =
              node.getLabels.asScala.find(_.isInstanceOf[UserCreatorLabel])
            val engineTypeLabelOption =
              node.getLabels.asScala.find(_.isInstanceOf[EngineTypeLabel])
            val engineInstanceOption =
              node.getLabels.asScala.find(_.isInstanceOf[EngineInstanceLabel])
            if (
                userCreatorLabelOption.isDefined && engineTypeLabelOption.isDefined && engineInstanceOption.isDefined
            ) {
              val userCreatorLabel = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
              val engineTypeLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
              val engineInstanceLabel = engineInstanceOption.get.asInstanceOf[EngineInstanceLabel]
              engineInstanceLabel.setServiceName(node.getServiceInstance.getApplicationName)
              engineInstanceLabel.setInstance(node.getServiceInstance.getInstance)
              val nodeResource = labelResourceService.getLabelResource(engineInstanceLabel)
              val configurationKey =
                getUserCreator(userCreatorLabel) + getEngineType(engineTypeLabel)
              val configuredResource = configurationMap.get(configurationKey) match {
                case Some(resource) => resource
                case None =>
                  if (nodeResource != null) {
                    val resource = UserConfiguration.getUserConfiguredResource(
                      nodeResource.getResourceType,
                      userCreatorLabel,
                      engineTypeLabel
                    )
                    configurationMap.put(configurationKey, resource)
                    resource
                  } else null
              }
              if (nodeResource != null) {
                nodeResource.setMaxResource(configuredResource)
                if (null == nodeResource.getUsedResource) {
                  nodeResource.setUsedResource(nodeResource.getLockedResource)
                }
                if (null == nodeResource.getMinResource) {
                  nodeResource.setMinResource(Resource.initResource(nodeResource.getResourceType))
                }
                node.setNodeResource(nodeResource)
              }
            }
          }
          node
        }
      }
      .filter(_ != null)
      .toArray
  }

  private def getEngineNodesByUserList(
      userList: List[String],
      withResource: Boolean = false
  ): Map[String, Array[EngineNode]] = {
    val serviceInstance =
      nodeManagerPersistence.getNodesByOwnerList(userList).map(_.getServiceInstance).asJava
    val engineNodesList = nodeManagerPersistence.getEngineNodeByServiceInstance(serviceInstance)
    val metrics = nodeMetricManagerPersistence
      .getNodeMetrics(engineNodesList)
      .asScala
      .map(m => (m.getServiceInstance.toString, m))
      .toMap
    val labelsMap =
      nodeLabelService.getNodeLabelsByInstanceList(engineNodesList.map(_.getServiceInstance).asJava)
    engineNodesList
      .map(nodeInfo => {
        nodeInfo.setLabels(labelsMap.get(nodeInfo.getServiceInstance.toString))
        if (nodeInfo.getLabels.exists(_.isInstanceOf[UserCreatorLabel])) {
          metrics
            .get(nodeInfo.getServiceInstance.toString)
            .foreach(metricsConverter.fillMetricsToNode(nodeInfo, _))
          if (withResource) {
            val engineInstanceOption = nodeInfo.getLabels.find(_.isInstanceOf[EngineInstanceLabel])
            if (engineInstanceOption.isDefined) {
              val engineInstanceLabel = engineInstanceOption.get.asInstanceOf[EngineInstanceLabel]
              engineInstanceLabel.setServiceName(nodeInfo.getServiceInstance.getApplicationName)
              engineInstanceLabel.setInstance(nodeInfo.getServiceInstance.getInstance)
              val nodeResource = labelResourceService.getLabelResource(engineInstanceLabel)
              if (nodeResource != null) {
                if (null == nodeResource.getUsedResource) {
                  nodeResource.setUsedResource(nodeResource.getLockedResource)
                }
                nodeInfo.setNodeResource(nodeResource)
              }
            }
          }
        }
        nodeInfo
      })
      .filter(_ != null)
      .toArray
      .groupBy(_.getOwner)
  }

  private def getUserResources(
      userCreatorEngineTypeResourceMap: mutable.HashMap[
        String,
        mutable.HashMap[String, NodeResource]
      ]
  ) = {

    val userCreatorEngineTypeResources = userCreatorEngineTypeResourceMap.map { userCreatorEntry =>
      val userCreatorEngineTypeResource = new mutable.HashMap[String, Any]
      userCreatorEngineTypeResource.put("userCreator", userCreatorEntry._1)
      var totalUsedMemory: Long = 0L
      var totalUsedCores: Int = 0
      var totalUsedInstances = 0
      var totalLockedMemory: Long = 0L
      var totalLockedCores: Int = 0
      var totalLockedInstances: Int = 0
      var totalMaxMemory: Long = 0L
      var totalMaxCores: Int = 0
      var totalMaxInstances: Int = 0
      val engineTypeResources = userCreatorEntry._2.map { engineTypeEntry =>
        val engineTypeResource = new mutable.HashMap[String, Any]
        engineTypeResource.put("engineType", engineTypeEntry._1)
        val engineResource = engineTypeEntry._2
        val usedResource = engineResource.getUsedResource.asInstanceOf[LoadInstanceResource]
        val lockedResource = engineResource.getLockedResource.asInstanceOf[LoadInstanceResource]
        val maxResource = engineResource.getMaxResource.asInstanceOf[LoadInstanceResource]
        val usedMemory = usedResource.memory
        val usedCores = usedResource.cores
        val usedInstances = usedResource.instances
        totalUsedMemory += usedMemory
        totalUsedCores += usedCores
        totalUsedInstances += usedInstances
        val lockedMemory = lockedResource.memory
        val lockedCores = lockedResource.cores
        val lockedInstances = lockedResource.instances
        totalLockedMemory += lockedMemory
        totalLockedCores += lockedCores
        totalLockedInstances += lockedInstances
        val maxMemory = maxResource.memory
        val maxCores = maxResource.cores
        val maxInstances = maxResource.instances
        totalMaxMemory += maxMemory
        totalMaxCores += maxCores
        totalMaxInstances += maxInstances

        val memoryPercent =
          if (maxMemory > 0) (usedMemory + lockedMemory) / maxMemory.toDouble else 0
        val coresPercent =
          if (maxCores > 0) (usedCores + lockedCores) / maxCores.toDouble else 0
        val instancePercent =
          if (maxInstances > 0) (usedInstances + lockedInstances) / maxInstances.toDouble else 0
        val maxPercent = Math.max(Math.max(memoryPercent, coresPercent), instancePercent)
        engineTypeResource.put("percent", maxPercent.formatted("%.2f"))
        engineTypeResource
      }
      val totalMemoryPercent =
        if (totalMaxMemory > 0) (totalUsedMemory + totalLockedMemory) / totalMaxMemory.toDouble
        else 0
      val totalCoresPercent =
        if (totalMaxCores > 0) (totalUsedCores + totalLockedCores) / totalMaxCores.toDouble
        else 0
      val totalInstancePercent =
        if (totalMaxInstances > 0) {
          (totalUsedInstances + totalLockedInstances) / totalMaxInstances.toDouble
        } else 0
      val totalPercent =
        Math.max(Math.max(totalMemoryPercent, totalCoresPercent), totalInstancePercent)
      userCreatorEngineTypeResource.put("engineTypes", engineTypeResources)
      userCreatorEngineTypeResource.put("percent", totalPercent.formatted("%.2f"))
      userCreatorEngineTypeResource
    }
    userCreatorEngineTypeResources
  }

  private def getUserCreatorEngineTypeResourceMap(nodes: Array[EngineNode]) = {
    val userCreatorEngineTypeResourceMap =
      new mutable.HashMap[String, mutable.HashMap[String, NodeResource]]

    for (node <- nodes) {
      val userCreatorLabel = node.getLabels.asScala
        .find(_.isInstanceOf[UserCreatorLabel])
        .get
        .asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.asScala
        .find(_.isInstanceOf[EngineTypeLabel])
        .get
        .asInstanceOf[EngineTypeLabel]
      val userCreator = getUserCreator(userCreatorLabel)

      if (!userCreatorEngineTypeResourceMap.contains(userCreator)) {
        userCreatorEngineTypeResourceMap.put(userCreator, new mutable.HashMap[String, NodeResource])
      }
      val engineTypeResourceMap = userCreatorEngineTypeResourceMap.get(userCreator).get
      val engineType = getEngineType(engineTypeLabel)
      if (!engineTypeResourceMap.contains(engineType)) {
        val nodeResource = CommonNodeResource.initNodeResource(ResourceType.LoadInstance)
        engineTypeResourceMap.put(engineType, nodeResource)
      }
      val resource = engineTypeResourceMap.get(engineType).get
      resource.setUsedResource(node.getNodeResource.getUsedResource + resource.getUsedResource)
      // combined label
      val combinedLabel =
        combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel));
      var labelResource = labelResourceService.getLabelResource(combinedLabel)
      if (labelResource == null) {
        resource.setLeftResource(node.getNodeResource.getMaxResource - resource.getUsedResource)
      } else {
        labelResource = ResourceUtils.convertTo(labelResource, ResourceType.LoadInstance)
        resource.setUsedResource(labelResource.getUsedResource)
        resource.setLockedResource(labelResource.getLockedResource)
        resource.setLeftResource(labelResource.getLeftResource)
        resource.setMaxResource(labelResource.getMaxResource)
      }
      resource.getLeftResource match {
        case dResource: DriverAndYarnResource =>
          resource.setLeftResource(dResource.loadInstanceResource)
        case _ =>
      }
    }

    userCreatorEngineTypeResourceMap
  }

  private def getCreatorToApplicationList(
      userCreator: String,
      engineType: String,
      nodes: Array[EngineNode]
  ) = {
    val creatorToApplicationList = new util.HashMap[String, util.HashMap[String, Any]]
    nodes.foreach { node =>
      val userCreatorLabel = node.getLabels.asScala
        .find(_.isInstanceOf[UserCreatorLabel])
        .get
        .asInstanceOf[UserCreatorLabel]
      val engineTypeLabel = node.getLabels.asScala
        .find(_.isInstanceOf[EngineTypeLabel])
        .get
        .asInstanceOf[EngineTypeLabel]
      if (getUserCreator(userCreatorLabel).equals(userCreator)) {
        if (engineType == null || getEngineType(engineTypeLabel).equals(engineType)) {
          if (!creatorToApplicationList.containsKey(userCreatorLabel.getCreator)) {
            val applicationList = new util.HashMap[String, Any]
            applicationList.put("engineInstances", new util.ArrayList[Any])
            applicationList.put("usedResource", Resource.initResource(ResourceType.LoadInstance))
            applicationList.put("maxResource", Resource.initResource(ResourceType.LoadInstance))
            applicationList.put("minResource", Resource.initResource(ResourceType.LoadInstance))
            applicationList.put("lockedResource", Resource.initResource(ResourceType.LoadInstance))
            creatorToApplicationList.put(userCreatorLabel.getCreator, applicationList)
          }
          val applicationList = creatorToApplicationList.get(userCreatorLabel.getCreator)
          applicationList.put(
            "usedResource",
            (if (applicationList.get("usedResource") == null) {
               Resource.initResource(ResourceType.LoadInstance)
             } else {
               applicationList
                 .get("usedResource")
                 .asInstanceOf[Resource]
             }) + node.getNodeResource.getUsedResource
          )
          applicationList.put(
            "maxResource",
            (if (applicationList.get("maxResource") == null) {
               Resource.initResource(ResourceType.LoadInstance)
             } else {
               applicationList
                 .get("maxResource")
                 .asInstanceOf[Resource]
             }) + node.getNodeResource.getMaxResource
          )
          applicationList.put(
            "minResource",
            (if (applicationList.get("minResource") == null) {
               Resource.initResource(ResourceType.LoadInstance)
             } else {
               applicationList
                 .get("minResource")
                 .asInstanceOf[Resource]
             }) + node.getNodeResource.getMinResource
          )
          applicationList.put(
            "lockedResource",
            (if (applicationList.get("lockedResource") == null) {
               Resource.initResource(ResourceType.LoadInstance)
             } else {
               applicationList
                 .get("lockedResource")
                 .asInstanceOf[Resource]
             }) + node.getNodeResource.getLockedResource
          )
          val engineInstance = new mutable.HashMap[String, Any]
          engineInstance.put("creator", userCreatorLabel.getCreator)
          engineInstance.put("engineType", engineTypeLabel.getEngineType)
          engineInstance.put("instance", node.getServiceInstance.getInstance)
          engineInstance.put("label", engineTypeLabel.getStringValue)
          node.setNodeResource(
            ResourceUtils.convertTo(node.getNodeResource, ResourceType.LoadInstance)
          )
          engineInstance.put("resource", node.getNodeResource)
          if (node.getNodeStatus == null) {
            engineInstance.put("status", "Busy")
          } else {
            engineInstance.put("status", node.getNodeStatus.toString)
          }
          engineInstance.put(
            "st" +
              "artTime",
            dateFormatLocal.get().format(node.getStartTime)
          )
          engineInstance.put("owner", node.getOwner)
          applicationList
            .get("engineInstances")
            .asInstanceOf[util.ArrayList[Any]]
            .add(engineInstance)
        }
      }
    }
    creatorToApplicationList
  }

  private def getApplications(
      creatorToApplicationList: util.HashMap[String, util.HashMap[String, Any]]
  ) = {
    val applications = new util.ArrayList[util.HashMap[String, Any]]()
    val iterator = creatorToApplicationList.entrySet().iterator();
    while (iterator.hasNext) {
      val entry = iterator.next()
      val application = new util.HashMap[String, Any]
      application.put("creator", entry.getKey)
      application.put("applicationList", entry.getValue)
      applications.add(application)
    }
    applications
  }

}
