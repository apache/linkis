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

package org.apache.linkis.manager.rm.service.impl

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.service.engine.EngineStopService
import org.apache.linkis.manager.am.vo.CanCreateECRes
import org.apache.linkis.manager.common.conf.RMConfiguration
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{AMEMNode, AMEngineNode, EngineNode, InfoRMNode}
import org.apache.linkis.manager.common.entity.persistence.{
  PersistenceLabel,
  PersistenceLock,
  PersistenceNodeMetrics,
  PersistenceResource
}
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary
import org.apache.linkis.manager.common.exception.{RMErrorException, RMWarnException}
import org.apache.linkis.manager.common.protocol.engine.{EngineAskRequest, EngineCreateRequest}
import org.apache.linkis.manager.common.utils.{ManagerUtils, ResourceUtils}
import org.apache.linkis.manager.label.LabelManagerUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.persistence.{
  LabelManagerPersistence,
  NodeManagerPersistence,
  NodeMetricManagerPersistence,
  ResourceManagerPersistence
}
import org.apache.linkis.manager.rm.{
  AvailableResource,
  NotEnoughResource,
  ResourceInfo,
  ResultResource
}
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.entity.{LabelResourceMapping, ResourceOperationType}
import org.apache.linkis.manager.rm.entity.ResourceOperationType.{LOCK, RELEASE, USED}
import org.apache.linkis.manager.rm.exception.{RMErrorCode, RMLockFailedRetryException}
import org.apache.linkis.manager.rm.external.service.ExternalResourceService
import org.apache.linkis.manager.rm.service.{
  LabelResourceService,
  RequestResourceService,
  ResourceLockService,
  ResourceManager
}
import org.apache.linkis.manager.rm.utils.RMUtils

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.MessageFormat
import java.util
import java.util.{Date, List, UUID}
import java.util.concurrent.{LinkedBlockingDeque, LinkedBlockingQueue, TimeUnit}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import com.google.common.collect.Lists

@Component
class DefaultResourceManager extends ResourceManager with Logging with InitializingBean {

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  @Autowired
  private var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var resourceLockService: ResourceLockService = _

  @Autowired
  private var labelResourceService: LabelResourceService = _

  @Autowired
  private var externalResourceService: ExternalResourceService = _

  @Autowired
  private var resourceLogService: ResourceLogService = _

  @Autowired
  private var labelManagerPersistence: LabelManagerPersistence = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var engineStopService: EngineStopService = _

  private var requestResourceServices: Array[RequestResourceService] = _

  // 不设置容量大小，因为EC的个数有限
  private val waitForDealResourceLabels = new LinkedBlockingDeque[LabelResourceMapping]()

  override def afterPropertiesSet(): Unit = {
    requestResourceServices = Array(
      new DefaultReqResourceService(labelResourceService),
      new DriverAndYarnReqResourceService(labelResourceService, externalResourceService)
    )
    // submit force release timeout lock job
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          logger.info("Start force release timeout locks")
          Utils.tryAndWarn(
            resourceLockService
              .clearTimeoutLock(RMConfiguration.LOCK_RELEASE_TIMEOUT.getValue.toLong)
          )
        }
      },
      RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue.toLong,
      RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
    // submit force dealing locked failed resourceLabel resources
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          logger.debug(
            s"Start force dealing locked failed resourceLabel resources. waitForDealResourceLabels size ${waitForDealResourceLabels.size()}"
          )
          val labelResourceMapping = waitForDealResourceLabels.poll(100, TimeUnit.MILLISECONDS)
          if (labelResourceMapping != null) {
            resourceDeal(labelResourceMapping)
          }
        }
      },
      RMConfiguration.LOCK_FAILED_LABEL_RESOURCE_DEAL_INTERVAL.getValue.toLong,
      RMConfiguration.LOCK_FAILED_LABEL_RESOURCE_DEAL_INTERVAL.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
  }

  /**
   * The registration method is mainly used to notify all RM nodes (including the node)
   * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
   */
  override def register(serviceInstance: ServiceInstance, resource: NodeResource): Unit = {
    logger.info(s"Start processing registration of ServiceInstance: ${serviceInstance}")
    val eMInstanceLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName)
    eMInstanceLabel.setInstance(serviceInstance.getInstance)

    val emResource = labelResourceService.getLabelResource(eMInstanceLabel)
    var registerResourceFlag = true
    if (emResource != null) {
      registerResourceFlag = false
      logger.warn(s"${serviceInstance} has been registered, resource is ${emResource}.")
      val leftResource = emResource.getLeftResource
      if (leftResource != null && Resource.getZeroResource(leftResource).moreThan(leftResource)) {
        logger.warn(
          s"${serviceInstance} has been registered, but left Resource <0 need to register resource."
        )
        registerResourceFlag = true
      }
      val usedResource = emResource.getLockedResource.add(emResource.getUsedResource)
      if (usedResource.moreThan(emResource.getMaxResource)) {
        logger.warn(
          s"${serviceInstance} has been registered, but usedResource > MaxResource  need to register resource."
        )
        registerResourceFlag = true
      }

      if (!(resource.getMaxResource.equalsTo(emResource.getMaxResource))) {
        logger.warn(
          s"${serviceInstance} has been registered, but inconsistent newly registered resources  need to register resource."
        )
        registerResourceFlag = true
      }
    }

    if (!registerResourceFlag) {
      logger.warn(s"${serviceInstance} has been registered, skip register resource.")
      return
    }
    val lock = tryLockOneLabel(eMInstanceLabel, -1, Utils.getJvmUser)
    try {
      labelResourceService.removeResourceByLabel(eMInstanceLabel)
      labelResourceService.setLabelResource(
        eMInstanceLabel,
        resource,
        eMInstanceLabel.getStringValue
      )
    } catch {
      case exception: Exception =>
        resourceLogService.failed(
          ChangeType.ECM_INIT,
          resource.getMaxResource,
          null,
          eMInstanceLabel,
          exception
        )
        throw exception
      case _ =>
    } finally {
      resourceLockService.unLock(lock)
    }
  }

  /**
   * The registration method is mainly used to notify all RM nodes , and the instance is offline.
   * 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
   */
  override def unregister(serviceInstance: ServiceInstance): Unit = {

    val eMInstanceLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName)
    eMInstanceLabel.setInstance(serviceInstance.getInstance)
    val lock = tryLockOneLabel(eMInstanceLabel, -1, Utils.getJvmUser)
    try {
      labelResourceService.removeResourceByLabel(eMInstanceLabel)
    } catch {
      case exception: Exception =>
        resourceLogService.failed(
          ChangeType.ECM_CLEAR,
          Resource.initResource(ResourceType.LoadInstance),
          null,
          eMInstanceLabel,
          exception
        )
      case _ =>
    } finally {
      resourceLockService.unLock(lock)
      logger.info(s"Finished to clear ecm resource:${serviceInstance}")
    }
    logger.info(s"Finished to clear ec for ecm ${serviceInstance}")
  }

  /**
   * Request resources, if not successful, return directly 请求资源，如果不成功，直接返回
   *
   * @param labels
   * @param resource
   * @return
   */
  override def requestResource(
      labels: util.List[Label[_]],
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): ResultResource = {
    requestResource(labels, resource, engineCreateRequest, -1)
  }

  /**
   * Request resources and wait for a certain amount of time until the requested resource is met
   * 请求资源，并等待一定的时间，直到满足请求的资源
   *
   * @param labels
   * @param resource
   * @param wait
   * @return
   */
  override def requestResource(
      labels: util.List[Label[_]],
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest,
      wait: Long
  ): ResultResource = {
    val labelContainer = labelResourceService.enrichLabels(labels)
    // check resource with lock
    val requestResourceService = getRequestResourceService(resource.getResourceType)
    resource.setLockedResource(resource.getMinResource)
    val resourceLabels = labelContainer.getResourceLabels.asScala
    val persistenceLocks = new ArrayBuffer[PersistenceLock]()
    val emInstanceLabel = labelContainer.getEMInstanceLabel
    val userCreatorEngineTypeLabel = labelContainer.getCombinedResourceLabel

    Utils.tryFinally {
      // check ecm resource if not enough return
      Utils.tryCatch {
        labelContainer.setCurrentLabel(emInstanceLabel)
        // set externalResourceService for get yarn resource
        requestResourceService.setExternalResourceService(externalResourceService)
        if (!requestResourceService.canRequest(labelContainer, resource, engineCreateRequest)) {
          return NotEnoughResource(s"Labels:${emInstanceLabel.getStringValue} not enough resource")
        }
      } {
        case exception: RMWarnException => return NotEnoughResource(exception.getMessage)
        case exception: Exception =>
          throw exception
      }
      // lock userCreatorEngineTypeLabel
      persistenceLocks.append(
        tryLockOneLabel(
          userCreatorEngineTypeLabel,
          wait,
          labelContainer.getUserCreatorLabel.getUser
        )
      )
      Utils.tryCatch {
        // set externalResourceService for get yarn resource
        requestResourceService.setExternalResourceService(externalResourceService)
        labelContainer.setCurrentLabel(userCreatorEngineTypeLabel)
        if (!requestResourceService.canRequest(labelContainer, resource, engineCreateRequest)) {
          return NotEnoughResource(
            s"Labels:${userCreatorEngineTypeLabel.getStringValue} not enough resource"
          )
        }
      } {
        case exception: RMWarnException => return NotEnoughResource(exception.getMessage)
        case exception: Exception =>
          throw exception
      }
      // lock ecmLabel
      persistenceLocks.append(
        tryLockOneLabel(emInstanceLabel, wait, labelContainer.getUserCreatorLabel.getUser)
      )
      resourceLabels.foreach { label =>
        labelContainer.setCurrentLabel(label)
        val labelResource = labelResourceService.getLabelResource(label)
        if (labelResource != null) {
          labelResource.setLeftResource(
            labelResource.getLeftResource.minus(resource.getLockedResource)
          )
          labelResource.setLockedResource(
            labelResource.getLockedResource.add(resource.getLockedResource)
          )
          labelResourceService.setLabelResource(
            label,
            labelResource,
            labelContainer.getCombinedResourceLabel.getStringValue
          )
          logger.info(s"ResourceChanged:${label.getStringValue} --> ${labelResource}")
          resourceCheck(label, labelResource)
        }
      }
    } {
      persistenceLocks.foreach(resourceLockService.unLock)
    }

    // add ec node
    val tickedId = RMUtils.getECTicketID
    val emNode = new AMEMNode
    emNode.setServiceInstance(labelContainer.getEMInstanceLabel.getServiceInstance)
    val engineNode = new AMEngineNode
    engineNode.setEMNode(emNode)
    engineNode.setServiceInstance(ServiceInstance(labelContainer.getEngineServiceName, tickedId))
    engineNode.setNodeResource(resource)
    engineNode.setTicketId(tickedId)
    nodeManagerPersistence.addEngineNode(engineNode)

    // add labels
    val engineInstanceLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
    engineInstanceLabel.setServiceName(labelContainer.getEngineServiceName)
    engineInstanceLabel.setInstance(tickedId)

    nodeLabelService.addLabelToNode(engineNode.getServiceInstance, engineInstanceLabel)

    // add ec resource
    labelResourceService.setEngineConnLabelResource(
      engineInstanceLabel,
      resource,
      labelContainer.getCombinedResourceLabel.getStringValue
    )
    // record engine locked resource
    labelContainer.getLabels.add(engineInstanceLabel)
    resourceLogService.recordUserResourceAction(
      labelContainer,
      tickedId,
      ChangeType.ENGINE_REQUEST,
      resource.getLockedResource
    )

    val persistenceLabel = labelFactory.convertLabel(engineInstanceLabel, classOf[PersistenceLabel])
    val persistenceEngineLabel = labelManagerPersistence.getLabelByKeyValue(
      persistenceLabel.getLabelKey,
      persistenceLabel.getStringValue
    )

    // fire timeout check scheduled job
    if (RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue > 0) {
      Utils.defaultScheduler.schedule(
        new UnlockTimeoutResourceRunnable(labels, persistenceEngineLabel, tickedId),
        RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue,
        TimeUnit.MILLISECONDS
      )
    }
    AvailableResource(tickedId)
  }

  def getRequestResourceService(resourceType: ResourceType): RequestResourceService = {
    val requestResourceService = requestResourceServices.find(_.resourceType == resourceType)
    requestResourceService.getOrElse(
      requestResourceServices.find(_.resourceType == ResourceType.Default).get
    )
  }

  /**
   * When the resource is instantiated, the total amount of resources actually occupied is returned.
   * 当资源被实例化后，返回实际占用的资源总量
   *
   * @param labels
   * @param usedResource
   */
  override def resourceUsed(labels: util.List[Label[_]], usedResource: NodeResource): Unit = {
    val labelContainer = labelResourceService.enrichLabels(labels)
    if (null == labelContainer.getEngineInstanceLabel) {
      throw new RMErrorException(
        RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode,
        "engine instance label is null"
      )
    }
    var lockedResource: NodeResource = null
    var persistenceResource: PersistenceResource = null
    try {
      persistenceResource =
        labelResourceService.getPersistenceResource(labelContainer.getEngineInstanceLabel)
      lockedResource = ResourceUtils.fromPersistenceResource(persistenceResource)
    } catch {
      case e: NullPointerException =>
        logger.error(
          s"EngineInstanceLabel [${labelContainer.getEngineInstanceLabel}] cause NullPointerException"
        )
        throw e
    }
    val nodeInstance =
      nodeManagerPersistence.getEngineNode(labelContainer.getEngineInstanceLabel.getServiceInstance)
    if (nodeInstance == null) {
      throw new RMErrorException(
        RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode,
        s"No serviceInstance found by engine ${labelContainer.getEngineInstanceLabel}, current label resource ${lockedResource}"
      )
    }
    if (
        lockedResource == null || !lockedResource.getLockedResource.moreThan(
          Resource.initResource(lockedResource.getResourceType)
        )
    ) {
      throw new RMErrorException(
        RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode,
        s"No locked resource found by engine ${labelContainer.getEngineInstanceLabel}, current label resource ${lockedResource}"
      )
    }
    logger.info(
      s"resourceUsed ready:${labelContainer.getEMInstanceLabel.getServiceInstance}, used resource ${lockedResource.getLockedResource}"
    )
    val addedResource =
      Resource.initResource(lockedResource.getResourceType).add(lockedResource.getLockedResource)

    val engineInstanceLabel: EngineInstanceLabel = labelContainer.getEngineInstanceLabel
    Utils.tryCatch {
      lockedResource.setUsedResource(lockedResource.getLockedResource)
      updateYarnApplicationID(usedResource, lockedResource)
      lockedResource.setLockedResource(Resource.getZeroResource(lockedResource.getLockedResource))
      labelResourceService.setLabelResource(
        engineInstanceLabel,
        lockedResource,
        labelContainer.getCombinedResourceLabel.getStringValue
      )
      resourceLogService.success(
        ChangeType.ENGINE_INIT,
        lockedResource.getLockedResource,
        engineInstanceLabel
      )
    } { case exception: Exception =>
      logger.error(
        s"${engineInstanceLabel.getStringValue} used resource failed!, resource: ${lockedResource}",
        exception
      )
    }
    val labelResourceSet = new mutable.HashSet[LabelResourceMapping]()
    Utils.tryCatch {
      labelContainer.getResourceLabels.asScala
        .filter(!_.isInstanceOf[EngineInstanceLabel])
        .foreach { label =>
          val persistenceLock = Utils.tryCatch {
            tryLockOneLabel(label, -1, labelContainer.getUserCreatorLabel.getUser)
          } { case t: Exception =>
            logger.warn(
              s"${engineInstanceLabel.getStringValue} used resource for resourceLabel $label failed, wait for dealing! Reason: ${t.getMessage}."
            )
            val labelResourceMapping = new LabelResourceMapping(
              label,
              addedResource,
              ResourceOperationType.USED,
              labelContainer.getUserCreatorLabel.getUser
            )
            waitForDealResourceLabels.offer(labelResourceMapping)
            null
          }
          if (persistenceLock != null) Utils.tryFinally {
            labelContainer.setCurrentLabel(label)
            val labelResource = labelResourceService.getLabelResource(label)
            if (labelResource != null) {
              labelResource.setLockedResource(labelResource.getLockedResource.minus(addedResource))
              if (null == labelResource.getUsedResource) {
                labelResource.setUsedResource(Resource.initResource(labelResource.getResourceType))
              }
              labelResource.setUsedResource(labelResource.getUsedResource.add(addedResource))
              labelResourceService.setLabelResource(
                label,
                labelResource,
                labelContainer.getCombinedResourceLabel.getStringValue
              )
              labelResourceSet
                .add(
                  new LabelResourceMapping(
                    label,
                    addedResource,
                    ResourceOperationType.USED,
                    labelContainer.getUserCreatorLabel.getUser
                  )
                )
              resourceCheck(label, labelResource)
            }
          } {
            resourceLockService.unLock(persistenceLock)
          }
          if (label.getClass.isAssignableFrom(labelContainer.getCombinedResourceLabel.getClass)) {
            resourceLogService.recordUserResourceAction(
              labelContainer,
              persistenceResource.getTicketId,
              ChangeType.ENGINE_INIT,
              addedResource,
              NodeStatus.Running
            )
          }
        }
    } { case exception: Exception =>
      logger.error(
        s"${labelContainer.getEngineInstanceLabel.getStringValue} used resource failed!, resource: ${lockedResource}",
        exception
      )
    }
  }

  def resourceCheck(label: Label[_], labelResource: NodeResource): Unit = {
    if (labelResource != null && label != null) {
      val resourceInit = Resource.initResource(labelResource.getResourceType)
      if (
          !labelResource.getLockedResource.notLess(resourceInit) ||
          !labelResource.getUsedResource.notLess(resourceInit) ||
          !labelResource.getLeftResource.notLess(resourceInit)
      ) {
        logger.info(
          s"found error resource! resource label:${label.getStringValue}, resource:${labelResource}, please check!"
        )
      }
    }
  }

  private def updateYarnApplicationID(
      nodeResource: NodeResource,
      lockedResource: NodeResource
  ): Unit = {
    lockedResource.getUsedResource match {
      case driverAndYarnResource: DriverAndYarnResource =>
        nodeResource.getUsedResource match {
          case resource: DriverAndYarnResource =>
            val newYarnResource = resource.getYarnResource
            val applicationId: String = if (null != newYarnResource) {
              newYarnResource.getApplicationId
            } else {
              null
            }
            val oriYarnResource = driverAndYarnResource.getYarnResource
            val tmpUsedResource = new DriverAndYarnResource(
              driverAndYarnResource.getLoadInstanceResource,
              new YarnResource(
                oriYarnResource.getQueueMemory,
                oriYarnResource.getQueueCores,
                oriYarnResource.getQueueInstances,
                oriYarnResource.getQueueName,
                applicationId
              )
            )
            lockedResource.setUsedResource(tmpUsedResource)
          case _ =>
        }
      case yarnResource: YarnResource =>
        nodeResource.getUsedResource match {
          case resource: YarnResource =>
            val tmpYarnResource = new YarnResource(
              yarnResource.getQueueMemory,
              yarnResource.getQueueCores,
              yarnResource.getQueueInstances,
              yarnResource.getQueueName,
              resource.getApplicationId
            )
            lockedResource.setUsedResource(tmpYarnResource)
          case _ =>
        }
      case _ =>
    }
  }

  private def resourceDeal(labelResourceMapping: LabelResourceMapping): Unit = {
    logger.info(s"try to deal labelResource $labelResourceMapping.")
    val persistenceLock = Utils.tryCatch {
      tryLockOneLabel(labelResourceMapping.getLabel(), -1, labelResourceMapping.getUser)
    } { case t: Exception =>
      logger.error(s"Failed to deal labelResource $labelResourceMapping, wait for retry.", t)
      waitForDealResourceLabels.addFirst(labelResourceMapping)
      return
    }
    Utils.tryCatch {
      val resource = labelResourceService.getLabelResource(labelResourceMapping.getLabel())
      labelResourceMapping.getResourceOperationType match {
        case RELEASE =>
          resource.setLeftResource(resource.getLeftResource.add(labelResourceMapping.getResource()))
          resource.setLockedResource(
            resource.getLockedResource.minus(labelResourceMapping.getResource())
          )
        case USED =>
          resource.setLockedResource(
            resource.getLeftResource.minus(labelResourceMapping.getResource())
          )
          resource.setUsedResource(
            resource.getLockedResource.add(labelResourceMapping.getResource())
          )
        case _ =>
      }
      labelResourceService.setLabelResource(
        labelResourceMapping.getLabel(),
        resource,
        labelResourceMapping.getResourceOperationType.toString
      )
      logger.info(s"succeed to deal labelResource $labelResourceMapping.")
    } { case e: Exception =>
      logger.error(s"Failed to deal labelResource $labelResourceMapping, wait for retry.", e)
      waitForDealResourceLabels.addFirst(labelResourceMapping)
    }
    resourceLockService.unLock(persistenceLock)
  }

  override def tryLockOneLabel(
      label: Label[_],
      timeOut: Long = -1,
      user: String
  ): PersistenceLock = {
    val persistenceLock = new PersistenceLock
    persistenceLock.setLockObject(label.getStringValue)
    persistenceLock.setCreateTime(new Date)
    persistenceLock.setCreator(user)
    persistenceLock.setUpdateTime(new Date)
    persistenceLock.setUpdator(user)
    val locked = resourceLockService.tryLock(persistenceLock, timeOut)
    if (!locked) {
      throw new RMLockFailedRetryException(
        RMErrorCode.LOCK_LABEL_FAILED.getErrorCode,
        MessageFormat.format(
          RMErrorCode.LOCK_LABEL_FAILED.getErrorDesc,
          label.getStringValue,
          timeOut.toString
        )
      )
    }
    persistenceLock
  }

  /**
   * Method called when the resource usage is released 当资源使用完成释放后，调用的方法
   *
   * @param ecNode
   */
  override def resourceReleased(ecNode: EngineNode): Unit = {
    val labelContainer = labelResourceService.enrichLabels(ecNode.getLabels)
    if (null == labelContainer.getEngineInstanceLabel) {
      throw new RMErrorException(
        RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode,
        "engine instance label is null"
      )
    }
    val instanceLock = tryLockOneLabel(
      labelContainer.getEngineInstanceLabel,
      RMUtils.RM_RESOURCE_LOCK_WAIT_TIME.getValue,
      labelContainer.getUserCreatorLabel.getUser
    )
    Utils.tryFinally {
      val persistenceResource: PersistenceResource =
        labelResourceService.getPersistenceResource(labelContainer.getEngineInstanceLabel)
      val usedResource = ResourceUtils.fromPersistenceResource(persistenceResource)
      if (usedResource == null) {
        throw new RMErrorException(
          RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode,
          s"No used resource found by engine ${labelContainer.getEngineInstanceLabel}"
        )
      }
      logger.info(
        s"resourceRelease ready:${labelContainer.getEngineInstanceLabel.getServiceInstance},current node resource${usedResource}"
      )

      val status = if (null == ecNode.getNodeStatus) {
        getNodeStatus(labelContainer.getEngineInstanceLabel)
      } else {
        ecNode.getNodeStatus
      }

      labelContainer.getResourceLabels.asScala
        .filter(!_.isInstanceOf[EngineInstanceLabel])
        .foreach { label =>
          Utils.tryCatch {
            val persistenceLock = Utils.tryCatch {
              tryLockOneLabel(
                label,
                RMUtils.RM_RESOURCE_LOCK_WAIT_TIME.getValue,
                labelContainer.getUserCreatorLabel.getUser
              )
            } { case t: Exception =>
              logger.warn(
                s"${labelContainer.getEngineInstanceLabel.getServiceInstance} release resource for resourceLabel $label failed, wait for dealing! Reason: ${t.getMessage}."
              )
              if (
                  null != usedResource.getUsedResource && usedResource.getUsedResource.moreThan(
                    Resource
                      .initResource(usedResource.getResourceType)
                  )
              ) {
                val labelResourceMapping = new LabelResourceMapping(
                  label,
                  usedResource.getUsedResource,
                  ResourceOperationType.RELEASE,
                  labelContainer.getUserCreatorLabel.getUser
                )
                waitForDealResourceLabels.offer(labelResourceMapping)
              }
              if (
                  null != usedResource.getLockedResource && usedResource.getLockedResource
                    .moreThan(
                      Resource
                        .initResource(usedResource.getResourceType)
                    )
              ) {
                val labelResourceMapping = new LabelResourceMapping(
                  label,
                  usedResource.getLockedResource,
                  ResourceOperationType.RELEASE,
                  labelContainer.getUserCreatorLabel.getUser
                )
                waitForDealResourceLabels.offer(labelResourceMapping)
              }
              null
            }
            if (persistenceLock != null) Utils.tryFinally {
              val labelResource = labelResourceService.getLabelResource(label)
              if (labelResource != null) {
                if (label.isInstanceOf[EMInstanceLabel]) timeCheck(labelResource, usedResource)
                if (
                    null != usedResource.getUsedResource && usedResource.getUsedResource.moreThan(
                      Resource
                        .initResource(usedResource.getResourceType)
                    )
                ) {
                  labelResource.setUsedResource(
                    labelResource.getUsedResource.minus(usedResource.getUsedResource)
                  )
                  labelResource.setLeftResource(
                    labelResource.getLeftResource.add(usedResource.getUsedResource)
                  )
                }
                if (
                    null != usedResource.getLockedResource && usedResource.getLockedResource
                      .moreThan(
                        Resource
                          .initResource(usedResource.getResourceType)
                      )
                ) {
                  labelResource.setLockedResource(
                    labelResource.getLockedResource.minus(usedResource.getLockedResource)
                  )
                  labelResource.setLeftResource(
                    labelResource.getLeftResource.add(usedResource.getLockedResource)
                  )
                }
                labelResourceService.setLabelResource(
                  label,
                  labelResource,
                  labelContainer.getCombinedResourceLabel.getStringValue
                )
                resourceCheck(label, labelResource)
              }
            } {
              resourceLockService.unLock(persistenceLock)
            }

            val releasedResource = if (usedResource.getUsedResource != null) {
              usedResource.getUsedResource
            } else {
              usedResource.getLockedResource
            }
            var heartbeatMsgMetrics = ""
            Utils.tryAndWarn {
              val oldMetrics = nodeMetricManagerPersistence.getNodeMetrics(ecNode)
              if (oldMetrics != null && StringUtils.isNotBlank(oldMetrics.getHeartBeatMsg)) {
                heartbeatMsgMetrics = oldMetrics.getHeartBeatMsg
              }
            }
            if (label.getClass.isAssignableFrom(labelContainer.getCombinedResourceLabel.getClass)) {
              resourceLogService.recordUserResourceAction(
                labelContainer,
                persistenceResource.getTicketId,
                ChangeType.ENGINE_CLEAR,
                releasedResource,
                status,
                heartbeatMsgMetrics
              )
            }
          } { case exception: Exception =>
            logger.error(
              s"Failed to release resource label ${labelContainer.getEngineInstanceLabel.getStringValue}",
              exception
            )
          }
        }
      val engineInstanceLabel = labelContainer.getEngineInstanceLabel
      Utils.tryCatch {
        labelResourceService.removeResourceByLabel(engineInstanceLabel)
        resourceLogService.success(
          ChangeType.ENGINE_CLEAR,
          usedResource.getUsedResource,
          engineInstanceLabel,
          null
        )
      } {
        case exception: Exception =>
          resourceLogService.failed(
            ChangeType.ENGINE_CLEAR,
            usedResource.getUsedResource,
            engineInstanceLabel,
            null,
            exception
          )
          throw exception
        case _ =>
      }
    } {
      logger.info(
        s"Finished release instance ${labelContainer.getEngineInstanceLabel.getServiceInstance} resource"
      )
      resourceLockService.unLock(instanceLock)
    }
  }

  def timeCheck(labelResource: NodeResource, usedResource: NodeResource): Unit = {
    if (labelResource.getCreateTime != null && usedResource.getCreateTime != null) {
      if (labelResource.getCreateTime.getTime > usedResource.getCreateTime.getTime) {
        throw new RMErrorException(
          ManagerCommonErrorCodeSummary.RESOURCE_LATER_CREATED.getErrorCode,
          s"no need to clear this labelResource, labelResource:${labelResource} created time is after than usedResource:${usedResource}" +
            s"无需清理该标签的资源,该标签资源的创建时间晚于已用资源的创建时间"
        )
      }
    }
  }

  private def getNodeStatus(engineInstanceLabel: EngineInstanceLabel): NodeStatus = {
    val node = new AMEngineNode()
    node.setServiceInstance(engineInstanceLabel.getServiceInstance)
    val metrics = nodeMetricManagerPersistence.getNodeMetrics(node)
    val status = if (null != metrics) {
      val timeStatus = NodeStatus.values()(metrics.getStatus)
      if (!NodeStatus.isCompleted(timeStatus)) {
        NodeStatus.Failed
      } else {
        timeStatus
      }
    } else {
      logger.warn("EC {} status unknown", engineInstanceLabel.getServiceInstance)
      NodeStatus.Failed
    }
    status
  }

  /**
   * If the IP and port are empty, return the resource status of all modules of a module   * Return
   * the use of this instance resource if there is an IP and port
   *
   * @param serviceInstances
   * @return
   */

  override def getResourceInfo(serviceInstances: Array[ServiceInstance]): ResourceInfo = {
    var resourceInfo: ResourceInfo = null
    val getBatchEnable = RMConfiguration.GET_RESOURCE_BY_LABEL_VALUE_ENABLED.getValue
    val startTime = System.currentTimeMillis
    if (getBatchEnable) {
      resourceInfo = getInstancesResourceBatch(serviceInstances)
    } else {
      // old
      resourceInfo = getInstancesResource(serviceInstances)
    }
    logger.info(
      s"getResourceInfo with serviceInstances size: ${serviceInstances.length} resource size: ${resourceInfo.resourceInfo
        .size()}, cost: ${(System.currentTimeMillis - startTime) / 1000.0} s"
    )
    resourceInfo
  }

  private def getInstancesResourceBatch(serviceInstances: Array[ServiceInstance]): ResourceInfo = {
    val resourceInfo = ResourceInfo(Lists.newArrayList())
    val labelValues: java.util.ArrayList[String] = new java.util.ArrayList[String]()
    for (serviceInstance <- serviceInstances) {
      val engineInstanceLabel: EngineInstanceLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EngineInstanceLabel])
      engineInstanceLabel.setServiceName(serviceInstance.getApplicationName)
      engineInstanceLabel.setInstance(serviceInstance.getInstance)
      val label = LabelManagerUtils.convertPersistenceLabel(engineInstanceLabel)
      val labelValue = label.getStringValue()
      labelValues.add(labelValue)
    }
    val persistenceResources: util.List[PersistenceResource] =
      resourceManagerPersistence.getResourceByLabelValues(labelValues)
    if (persistenceResources != null && persistenceResources.size() > 0) {
      if (persistenceResources.size() != serviceInstances.length) {
        logger.error(
          s"resource query failed. serviceInstances size: ${serviceInstances.length}, resource size: ${persistenceResources.size()}"
        )
        return resourceInfo
      }
      persistenceResources.asScala.foreach({ resource =>
        val rmNode = new InfoRMNode
        val persistenceResource = resource.asInstanceOf[PersistenceResource]
        val serviceInstanceArray: Array[ServiceInstance] =
          serviceInstances.filter { serviceInstance =>
            persistenceResource.getLabelValue.contains(serviceInstance.getInstance)
          }
        if (serviceInstanceArray == null || serviceInstanceArray.length != 1) {
          logger.error("logic error. please optimization.")
        }
        val serviceInstance = serviceInstanceArray(0)
        val aggregatedResource = ResourceUtils.fromPersistenceResource(persistenceResource)
        rmNode.setServiceInstance(serviceInstance)
        rmNode.setNodeResource(aggregatedResource)
        resourceInfo.resourceInfo.add(rmNode)
      })

    }
    resourceInfo
  }

  private def getInstancesResource(serviceInstances: Array[ServiceInstance]): ResourceInfo = {
    val resourceInfo = ResourceInfo(Lists.newArrayList())
    serviceInstances.foreach({ serviceInstance =>
      val rmNode = new InfoRMNode
      var aggregatedResource: NodeResource = null
      serviceInstance.getApplicationName match {
        case GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue =>
          val engineInstanceLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(
            classOf[EngineInstanceLabel]
          )
          engineInstanceLabel.setServiceName(serviceInstance.getApplicationName)
          engineInstanceLabel.setInstance(serviceInstance.getInstance)
          aggregatedResource = labelResourceService.getLabelResource(engineInstanceLabel)
          logger.info("getLabelResource engineconn, engineInstanceLabel {}", engineInstanceLabel)
        case GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue =>
          val emInstanceLabel =
            LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
          emInstanceLabel.setServiceName(serviceInstance.getApplicationName)
          emInstanceLabel.setInstance(serviceInstance.getInstance)
          aggregatedResource = labelResourceService.getLabelResource(emInstanceLabel)
          logger.info("getLabelResource ecm, emInstanceLabel {}", emInstanceLabel)
      }
      rmNode.setServiceInstance(serviceInstance)
      rmNode.setNodeResource(aggregatedResource)
      resourceInfo.resourceInfo.add(rmNode)
    })
    resourceInfo
  }

  override def resourceReport(labels: util.List[Label[_]], reportResource: NodeResource): Unit = {}

  class UnlockTimeoutResourceRunnable(
      labels: util.List[Label[_]],
      persistenceEngineLabel: PersistenceLabel,
      ticketId: String
  ) extends Runnable {

    override def run(): Unit = Utils.tryAndWarnMsg {
      logger.info(
        s"check locked resource of ${persistenceEngineLabel.getStringValue}, ticketId: $ticketId"
      )
      val persistResource = resourceManagerPersistence.getNodeResourceByTicketId(ticketId)
      if (null == persistResource) {
        logger.info(s" ticketId $ticketId relation resource not exists")
        return
      }
      val usedResource = ResourceUtils.fromPersistenceResource(persistResource)
      if (
          usedResource != null
          && usedResource.getLockedResource != null
          && usedResource.getLockedResource.moreThan(
            Resource.getZeroResource(usedResource.getLockedResource)
          )
      ) {
        val dbEngineInstanceLabel = labelManagerPersistence.getLabel(persistenceEngineLabel.getId)
        val currnentEngineInstanceLabel =
          if (null == dbEngineInstanceLabel) persistenceEngineLabel else dbEngineInstanceLabel
        if (
            LabelKeyConstant.ENGINE_INSTANCE_KEY.equalsIgnoreCase(
              currnentEngineInstanceLabel.getLabelKey
            )
        ) {
          ManagerUtils.persistenceLabelToRealLabel(currnentEngineInstanceLabel) match {
            case engineInstanceLabel: EngineInstanceLabel =>
              labels.add(engineInstanceLabel)
              logger.warn(
                s"serviceInstance ${engineInstanceLabel.getServiceInstance} lock resource timeout, clear resource"
              )
              val ecNode = new AMEngineNode()
              ecNode.setServiceInstance(engineInstanceLabel.getServiceInstance)
              ecNode.setNodeStatus(NodeStatus.Failed)
              ecNode.setLabels(labels)
              resourceReleased(ecNode)
            case _ =>
          }
        }

      }
      logger.info(s"Finished to check unlock resource of ${ticketId}")
    }(s"Failed to UnlockTimeoutResourceRunnable $ticketId")

  }

  /**
   * Can request resource
   *
   * @param labels
   * @param resource
   * @param engineCreateRequest
   * @return
   */
  override def canRequestResource(
      labels: util.List[Label[_]],
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): CanCreateECRes = {
    val labelContainer = labelResourceService.enrichLabels(labels)

    // check resource with lock
    val requestResourceService = getRequestResourceService(resource.getResourceType)
    if (
        labelContainer.getEMInstanceLabel == null || labelContainer.getCombinedResourceLabel == null
    ) {
      val canCreateECRes = new CanCreateECRes
      canCreateECRes.setCanCreateEC(false)
      canCreateECRes.setReason("EMLabel,userCreator and engineTypeLabel cannot null")
    }
    requestResourceService.canRequestResource(labelContainer, resource, engineCreateRequest)
  }

  override def resetResource(label: PersistenceLabel, resource: NodeResource): Unit = {
    labelResourceService.setLabelResource(label, resource, label.getStringValue)
  }

  override def unLock(persistenceLock: PersistenceLock): Unit = {
    resourceLockService.unLock(persistenceLock)
  }

}
