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

package org.apache.linkis.manager.am.service.engine

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.engineplugin.server.service.EngineConnResourceFactoryService
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.conf.GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.job.{JobReqQuery, JobReqUpdate}
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.am.conf.{AMConfiguration, EngineConnConfigurationService}
import org.apache.linkis.manager.am.exception.AMErrorException
import org.apache.linkis.manager.am.label.{EngineReuseLabelChooser, LabelChecker}
import org.apache.linkis.manager.am.selector.{ECAvailableRule, NodeSelector}
import org.apache.linkis.manager.am.utils.AMUtils
import org.apache.linkis.manager.am.vo.CanCreateECRes
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{EMNode, EngineNode}
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.common.protocol.engine.{EngineCreateRequest, EngineStopRequest}
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.engineplugin.common.launch.entity.{
  EngineConnBuildRequestImpl,
  EngineConnCreationDescImpl
}
import org.apache.linkis.manager.engineplugin.common.resource.TimeoutEngineResourceRequest
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.entity.{EngineNodeLabel, Label}
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel}
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import org.apache.linkis.manager.label.service.{NodeLabelService, UserLabelService}
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence
import org.apache.linkis.manager.rm.{AvailableResource, NotEnoughResource}
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.manager.service.common.label.LabelFilter
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util
import java.util.concurrent.{TimeoutException, TimeUnit}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

@Service
class DefaultEngineCreateService
    extends AbstractEngineService
    with EngineCreateService
    with Logging {

  @Autowired
  private var nodeSelector: NodeSelector = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var labelCheckerList: util.List[LabelChecker] = _

  @Autowired
  private var labelFilter: LabelFilter = _

  @Autowired
  private var userLabelService: UserLabelService = _

  @Autowired
  private var engineConnConfigurationService: EngineConnConfigurationService = _

  @Autowired
  private var engineConnResourceFactoryService: EngineConnResourceFactoryService = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var engineReuseLabelChoosers: util.List[EngineReuseLabelChooser] = _

  @Autowired
  private var engineStopService: EngineStopService = _

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  def buildLabel(labels: util.Map[String, AnyRef], user: String): util.List[Label[_]] = {
    // 1. Check if Label is valid
    var labelList: util.List[Label[_]] = LabelUtils.distinctLabel(
      labelBuilderFactory.getLabels(labels),
      userLabelService.getUserLabels(user)
    )

    // label chooser
    if (null != engineReuseLabelChoosers) {
      engineReuseLabelChoosers.asScala.foreach { chooser =>
        labelList = chooser.chooseLabels(labelList)
      }
    }

    for (labelChecker <- labelCheckerList.asScala) {
      if (!labelChecker.checkEngineLabel(labelList)) {
        throw new AMErrorException(
          AMConstant.EM_ERROR_CODE,
          "Need to specify engineType and userCreator label"
        )
      }
    }
    labelList
  }

  private def selectECM(
      engineCreateRequest: EngineCreateRequest,
      labelList: util.List[Label[_]]
  ): EMNode = {
    val emLabelList = new util.ArrayList[Label[_]](labelList)
    val emInstanceLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    emInstanceLabel.setAlias(ENGINE_CONN_MANAGER_SPRING_NAME.getValue)
    emLabelList.add(emInstanceLabel)

    // 2. Get all available ECMs by labels
    val emScoreNodeList =
      getEMService().getEMNodes(emLabelList.asScala.filter(!_.isInstanceOf[EngineTypeLabel]).asJava)
    if (null == emScoreNodeList || emScoreNodeList.isEmpty) {
      throw new LinkisRetryException(
        AMConstant.EM_ERROR_CODE,
        s" The ecm of labels ${engineCreateRequest.getLabels} not found"
      )
    }

    // 3. Get the ECM with the lowest load by selection algorithm
    logger.info(s"Suitable ems size is ${emScoreNodeList.length}")
    val choseNode = nodeSelector.choseNode(emScoreNodeList.toArray)
    if (null == choseNode || choseNode.isEmpty) {
      throw new LinkisRetryException(
        AMConstant.EM_ERROR_CODE,
        s" There are corresponding ECM tenant labels ${engineCreateRequest.getLabels}, but none of them are healthy"
      )
    }
    choseNode.get.asInstanceOf[EMNode]
  }

  @Receiver
  @throws[LinkisRetryException]
  override def createEngine(
      engineCreateRequest: EngineCreateRequest,
      sender: Sender
  ): EngineNode = {
    val startTime = System.currentTimeMillis
    val taskId = JobUtils.getJobIdFromStringMap(engineCreateRequest.getProperties)
    logger.info(s"Task: $taskId start to create Engine for request: $engineCreateRequest.")

    val timeout =
      if (engineCreateRequest.getTimeout <= 0) {
        AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong
      } else engineCreateRequest.getTimeout

    // 1 build label
    val labelList = buildLabel(engineCreateRequest.getLabels, engineCreateRequest.getUser)

    // 2 select suite ecm
    val emNode = selectECM(engineCreateRequest, labelList)
    // 3. generate Resource
    if (engineCreateRequest.getProperties == null) {
      engineCreateRequest.setProperties(new util.HashMap[String, String]())
    }

    val resource =
      generateResource(
        engineCreateRequest.getProperties,
        engineCreateRequest.getUser,
        labelFilter.choseEngineLabel(labelList),
        timeout
      )
    // 4. request resource
    val resourceTicketId = resourceManager.requestResource(
      LabelUtils.distinctLabel(labelList, emNode.getLabels),
      resource,
      engineCreateRequest,
      timeout
    ) match {
      case AvailableResource(ticketId) =>
        ticketId
      case NotEnoughResource(reason) =>
        logger.warn(s"not enough resource: $reason")
        throw new LinkisRetryException(AMConstant.EM_ERROR_CODE, s"not enough resource: : $reason")
    }

    // 5. build engineConn request
    val engineBuildRequest = EngineConnBuildRequestImpl(
      resourceTicketId,
      labelFilter.choseEngineLabel(labelList),
      resource,
      EngineConnCreationDescImpl(
        engineCreateRequest.getCreateService,
        engineCreateRequest.getDescription,
        engineCreateRequest.getProperties
      )
    )

    // 6. Call ECM to send engine start request
    // AM will update the serviceInstance table
    // It is necessary to replace the ticketID and update the Label of EngineConn
    // It is necessary to modify the id in EngineInstanceLabel to Instance information
    val oldServiceInstance = new ServiceInstance
    oldServiceInstance.setApplicationName(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    oldServiceInstance.setInstance(resourceTicketId)

    val engineNode = Utils.tryCatch(getEMService().createEngine(engineBuildRequest, emNode)) {
      case t: Throwable =>
        logger.info(s"Failed to create ec($resourceTicketId) ask ecm ${emNode.getServiceInstance}")
        val failedEcNode = getEngineNodeManager.getEngineNode(oldServiceInstance)
        if (null == failedEcNode) {
          logger.info(s" engineConn does not exist in db: $oldServiceInstance ")
        } else {
          failedEcNode.setLabels(nodeLabelService.getNodeLabels(oldServiceInstance))
          failedEcNode.getLabels.addAll(
            LabelUtils.distinctLabel(labelFilter.choseEngineLabel(labelList), emNode.getLabels)
          )
          failedEcNode.setNodeStatus(NodeStatus.Failed)
          engineStopService.engineConnInfoClear(failedEcNode)
        }
        throw t
    }

    logger.info(
      s"Task: $taskId finished to create  engineConn $engineNode. ticketId is $resourceTicketId"
    )
    engineNode.setTicketId(resourceTicketId)

    val params: String = BDPJettyServerHelper.gson.toJson(engineCreateRequest.getProperties)
    logger.info(s"Task: $taskId finished to create  engineConn with params: $params")
    engineNode.setParams(params)

    // 7.Update persistent information: including inserting engine/metrics
    Utils.tryCatch(getEngineNodeManager.updateEngineNode(oldServiceInstance, engineNode)) { t =>
      logger.warn(s"Failed to update engineNode $engineNode", t)
      t match {
        case linkisRetryException: LinkisRetryException =>
          logger.warn(
            s"node $oldServiceInstance update failed,caused by retry Exception, do not to stop ec"
          )
        case _ =>
          val stopEngineRequest =
            new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
          engineStopService.asyncStopEngine(stopEngineRequest)
      }
      val failedEcNode = getEngineNodeManager.getEngineNode(oldServiceInstance)
      if (null == failedEcNode) {
        logger.info(s" engineConn does not exist in db: $oldServiceInstance ")
      } else {
        failedEcNode.setLabels(nodeLabelService.getNodeLabels(oldServiceInstance))
        failedEcNode.getLabels.addAll(
          LabelUtils.distinctLabel(labelFilter.choseEngineLabel(labelList), emNode.getLabels)
        )
        failedEcNode.setNodeStatus(NodeStatus.Failed)
        engineStopService.engineConnInfoClear(failedEcNode)
      }
      throw new LinkisRetryException(
        AMConstant.EM_ERROR_CODE,
        s"Failed to update engineNode: ${t.getMessage}"
      )
    }
    if (Configuration.METRICS_INCREMENTAL_UPDATE_ENABLE.getValue) {
      val emInstance = engineNode.getServiceInstance.getInstance
      val ecmInstance = engineNode.getEMNode.getServiceInstance.getInstance
      // 8. Update job history metrics after successful engine creation - 异步执行
      AMUtils.updateMetricsAsync(
        taskId,
        resourceTicketId,
        emInstance,
        ecmInstance,
        null,
        isReuse = false
      )
    }
    // 9. Add the Label of EngineConn, and add the Alias of engineConn
    val engineConnAliasLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    labelList.add(engineConnAliasLabel)
    nodeLabelService.addLabelsToNode(
      engineNode.getServiceInstance,
      labelFilter.choseEngineLabel(
        LabelUtils.distinctLabel(labelList, fromEMGetEngineLabels(emNode.getLabels))
      )
    )
    if (System.currentTimeMillis - startTime >= timeout && engineCreateRequest.isIgnoreTimeout) {
      logger.info(
        s"Return a EngineConn $engineNode for request: $engineCreateRequest since the creator set ignoreTimeout=true and maxStartTime is reached."
      )
      return engineNode
    }
    val leftWaitTime = timeout - (System.currentTimeMillis - startTime)
    if (ECAvailableRule.getInstance.isNeedAvailable(labelList)) {
      ensureECAvailable(engineNode, resourceTicketId, leftWaitTime)
      logger.info(
        s"Task: $taskId finished to create Engine for request: $engineCreateRequest and get engineNode $engineNode. time taken ${System
          .currentTimeMillis() - startTime}ms"
      )
    } else {
      logger.info(
        s"Task: $taskId finished to create Engine for request: $engineCreateRequest and get engineNode $engineNode.And did not judge the availability,time taken ${System
          .currentTimeMillis() - startTime}ms"
      )
    }
    engineNode
  }

  def canCreateEC(engineCreateRequest: EngineCreateRequest): CanCreateECRes = {
    // 1 build label
    val labelList = buildLabel(engineCreateRequest.getLabels, engineCreateRequest.getUser)

    // 2 select suite ecm
    val emNode = selectECM(engineCreateRequest, labelList)
    // 3. generate Resource
    if (engineCreateRequest.getProperties == null) {
      engineCreateRequest.setProperties(new util.HashMap[String, String]())
    }
    val resource = generateResource(
      engineCreateRequest.getProperties,
      engineCreateRequest.getUser,
      labelFilter.choseEngineLabel(labelList),
      AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong
    )

    // 4. check resource
    resourceManager.canRequestResource(
      LabelUtils.distinctLabel(labelList, emNode.getLabels),
      resource,
      engineCreateRequest
    )

  }

  /**
   * Read the management console configuration and the parameters passed in by the user to combine
   * request resources
   * @param engineCreateRequest
   * @param labelList
   * @param timeout
   * @return
   */
  def generateResource(
      props: util.Map[String, String],
      user: String,
      labelList: util.List[Label[_]],
      timeout: Long
  ): NodeResource = {
    val configProp = engineConnConfigurationService.getConsoleConfiguration(labelList)
    if (null != configProp && configProp.asScala.nonEmpty) {
      configProp.asScala.foreach(keyValue => {
        if (!props.containsKey(keyValue._1)) {
          props.put(keyValue._1, keyValue._2)
        }
      })
    }

    val crossQueue = props.get(AMConfiguration.CROSS_QUEUE)
    if (StringUtils.isNotBlank(crossQueue)) {
      val queueName = props.getOrDefault(AMConfiguration.YARN_QUEUE_NAME_CONFIG_KEY, "default")
      props.put(AMConfiguration.YARN_QUEUE_NAME_CONFIG_KEY, crossQueue)
      logger.info(
        s"Switch queues according to queueRule with crossQueue : $queueName to $crossQueue"
      )
    }

    val timeoutEngineResourceRequest = TimeoutEngineResourceRequest(timeout, user, labelList, props)
    engineConnResourceFactoryService.createEngineResource(timeoutEngineResourceRequest)
  }

  private def fromEMGetEngineLabels(emLabels: util.List[Label[_]]): util.List[Label[_]] = {
    emLabels.asScala.filter { label =>
      label.isInstanceOf[EngineNodeLabel] && !label.isInstanceOf[EngineTypeLabel]
    }.asJava
  }

  private def ensuresIdle(engineNode: EngineNode, resourceTicketId: String): Boolean = {

    val engineNodeInfo =
      Utils.tryAndWarnMsg(if (engineNode.getMark == AMConstant.CLUSTER_PROCESS_MARK) {
        getEngineNodeManager.getEngineNodeInfoByTicketId(resourceTicketId)
      } else {
        getEngineNodeManager.getEngineNodeInfoByDB(engineNode)
      })("Failed to from db get engine node info")

    if (null == engineNodeInfo) return false

    if (engineNodeInfo.getServiceInstance != null) {
      engineNode.setServiceInstance(engineNodeInfo.getServiceInstance)
    }
    if (NodeStatus.isCompleted(engineNodeInfo.getNodeStatus)) {
      val metrics = nodeMetricManagerPersistence.getNodeMetrics(engineNodeInfo)
      val msg = if (metrics != null) metrics.getHeartBeatMsg else null
      val (reason, canRetry) = getStartErrorInfo(msg)
      if (canRetry.isDefined) {
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"${engineNode.getServiceInstance} ticketID:$resourceTicketId Failed to initialize engine, reason: ${reason}"
        )
      }
      throw new AMErrorException(
        AMConstant.EM_ERROR_CODE,
        s"${engineNode.getServiceInstance} ticketID:$resourceTicketId Failed to initialize engine, reason: ${reason}"
      )
    }
    NodeStatus.isAvailable(engineNodeInfo.getNodeStatus)
  }

  private def getStartErrorInfo(msg: String): (String, Option[Boolean]) = {

    if (StringUtils.isNotBlank(msg)) {
      val jsonNode = BDPJettyServerHelper.jacksonJson.readTree(msg)
      if (jsonNode != null && jsonNode.has(AMConstant.START_REASON)) {
        val startReason = jsonNode.get(AMConstant.START_REASON).asText()
        if (jsonNode.has(AMConstant.EC_CAN_RETRY)) {
          return (startReason, Some(true))
        } else {
          return (startReason, None)
        }
      }
    }
    (null, None)
  }

  /**
   * Need to ensure that the newly created amount ec is available before returning
   * @param engineNode
   * @param resourceTicketId
   * @param timeout
   * @return
   */
  def ensureECAvailable(
      engineNode: EngineNode,
      resourceTicketId: String,
      timeout: Long
  ): EngineNode = {
    Utils.tryCatch {
      logger.info(
        s"Start to wait engineConn($engineNode) to be available, but only ${ByteTimeUtils.msDurationToString(timeout)} left."
      )
      Utils.waitUntil(
        () => ensuresIdle(engineNode, resourceTicketId),
        Duration(timeout, TimeUnit.MILLISECONDS)
      )
    } {
      case _: TimeoutException =>
        logger.info(
          s"Waiting for engineNode:$engineNode($resourceTicketId) initialization TimeoutException , now stop it."
        )
        val stopEngineRequest =
          new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
        engineStopService.asyncStopEngine(stopEngineRequest)
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Waiting for engineNode:$engineNode($resourceTicketId) initialization TimeoutException, already waiting $timeout ms"
        )
      case t: Throwable =>
        logger.info(
          s"Waiting for $engineNode($resourceTicketId) initialization failure , now stop it."
        )
        val stopEngineRequest =
          new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
        engineStopService.asyncStopEngine(stopEngineRequest)
        throw t
    }
    engineNode
  }

}
