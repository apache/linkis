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

package org.apache.linkis.manager.am.service.em

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.converter.MetricsConverter
import org.apache.linkis.manager.am.manager.EMNodeManager
import org.apache.linkis.manager.am.pointer.NodePointerBuilder
import org.apache.linkis.manager.am.service.engine.EngineInfoService
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo
import org.apache.linkis.manager.common.entity.node.{AMEMNode, EMNode}
import org.apache.linkis.manager.common.entity.resource.{NodeResource, Resource, ResourceType}
import org.apache.linkis.manager.common.protocol.em.GetEMInfoRequest
import org.apache.linkis.manager.common.protocol.node.NodeHealthyRequest
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.manager.persistence.{
  LabelManagerPersistence,
  NodeMetricManagerPersistence,
  ResourceManagerPersistence
}
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.server.toScalaBuffer

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.text.MessageFormat

import scala.collection.JavaConverters._

@Service
class DefaultEMInfoService extends EMInfoService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var nodePointerBuilder: NodePointerBuilder = _

  @Autowired
  private var defaultMetricsConverter: MetricsConverter = _

  @Autowired
  private var engineInfoService: EngineInfoService = _

  @Autowired
  private var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  private var labelManagerPersistence: LabelManagerPersistence = _

  @Receiver
  override def getEM(getEMInfoRequest: GetEMInfoRequest): EMNode = {
    val node = emNodeManager.getEM(getEMInfoRequest.getEm)
    if (null == node) new AMEMNode() else node
  }

  /**
   * 通过Label去拿，AliasServiceInstanceLabel 指定type为EM
   *
   * @return
   */
  override def getAllEM(): Array[EMNode] = {
    val label = new AliasServiceInstanceLabel
    label.setAlias(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue)
    val instances = nodeLabelService.getNodesByLabel(label)
    val resourceInfo = resourceManager.getResourceInfo(instances.asScala.toArray).resourceInfo
    val resourceInfoMap = resourceInfo.asScala.map(r => (r.getServiceInstance.toString, r)).toMap
    instances.asScala
      .map(emNodeManager.getEM)
      .filter(_ != null)
      .map { node =>
        node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance))
        resourceInfoMap
          .get(node.getServiceInstance.toString)
          .map(_.getNodeResource)
          .foreach(node.setNodeResource)
        node
      }
      .toArray[EMNode]
  }

  override def getEM(serviceInstance: ServiceInstance): EMNode = {
    if (serviceInstance != null) {
      emNodeManager.getEM(serviceInstance)
    } else {
      null
    }
  }

  override def updateEMInfo(
      serviceInstance: ServiceInstance,
      nodeHealthyInfo: NodeHealthyInfo
  ): Unit = {
    val node = emNodeManager.getEM(serviceInstance)
    if (null != node) {
      val metrics = nodeMetricManagerPersistence.getNodeMetrics(node)
      if (null != metrics && null != nodeHealthyInfo) {
        val oldHealthyInfo = defaultMetricsConverter.parseHealthyInfo(metrics)
        if (!nodeHealthyInfo.getNodeHealthy.equals(oldHealthyInfo.getNodeHealthy)) {
          metrics.setHealthy(defaultMetricsConverter.convertHealthyInfo(nodeHealthyInfo))
          nodeMetricManagerPersistence.addOrupdateNodeMetrics(metrics)
          val nodeHealthyRequest: NodeHealthyRequest = new NodeHealthyRequest
          nodeHealthyRequest.setNodeHealthy(nodeHealthyInfo.getNodeHealthy)
          nodePointerBuilder.buildEMNodePointer(node).updateNodeHealthyRequest(nodeHealthyRequest)
          logger.info(
            s"success to update healthy metric of instance: ${serviceInstance.getInstance},${metrics.getHealthy}"
          )
        }
      }
    }
  }

  override def resetResource(serviceInstance: String, username: String): Unit = {
    // ECM开关
    if (AMConfiguration.AM_ECM_RESET_RESOURCE && StringUtils.isNotBlank(serviceInstance)) {
      val filteredECMs = if (serviceInstance.equals("*")) {
        getAllEM()
      } else {
        getAllEM().filter(_.getServiceInstance.getInstance.equals(serviceInstance))
      }
      // 遍历处理ECM
      filteredECMs.foreach { ecmInstance =>
        // lock ECMInstance && set unhealthy
        logger.info(
          MessageFormat.format(
            s"ECM:{0} will be marked as unhealthy and locked",
            ecmInstance.getServiceInstance.getInstance
          )
        )
        val eMInstanceLabel = ecmInstance.getLabels.filter(_.isInstanceOf[EMInstanceLabel]).head
        val lock =
          resourceManager.tryLockOneLabel(eMInstanceLabel, -1, Utils.getJvmUser)
        engineInfoService
          .updateEngineHealthyStatus(ecmInstance.getServiceInstance, NodeHealthy.UnHealthy)
        Utils.tryFinally {
          // 获取ecm下所有node
          val nodeResource =
            engineInfoService.listEMEngines(ecmInstance).asScala.map(_.getNodeResource).toArray
          // 收集所有node所使用的资源（汇总、已使用、上锁）
          val (realSumResource, useResource, lockResource) =
            collectResource(nodeResource, ResourceType.LoadInstance)
          // 收集ECM资源
          val ecmNodeResource = ecmInstance.getNodeResource
          // 资源对比，资源重置
          if (
              (!(useResource.equalsTo(ecmNodeResource.getUsedResource)) || (!(lockResource
                .equalsTo(ecmNodeResource.getLockedResource))))
          ) {
            logger.info(
              MessageFormat.format(
                "ECM:{0} resources will be reset, Record Resources:{1} ,Real Resources:{2}",
                ecmInstance.getServiceInstance.getInstance,
                ecmNodeResource.getUsedResource.add(ecmNodeResource.getLockedResource),
                realSumResource
              )
            )
            ecmNodeResource.setLockedResource(lockResource)
            ecmNodeResource.setLeftResource(ecmNodeResource.getMaxResource.minus(realSumResource))
            ecmNodeResource.setUsedResource(useResource)
            val persistence = ResourceUtils.toPersistenceResource(ecmNodeResource)
            val resourceLabel = labelManagerPersistence.getLabelByResource(persistence)
            resourceManager.resetResource(resourceLabel.head, ecmNodeResource)
          }
        } {
          logger.info(
            MessageFormat.format(
              s"ECM:{0} will be marked as healthy and the lock will be released",
              ecmInstance.getServiceInstance.getInstance
            )
          )
          resourceManager.unLock(lock)
          engineInfoService
            .updateEngineHealthyStatus(ecmInstance.getServiceInstance, NodeHealthy.Healthy)
        }
      }
    }

    // 用户资源重置
    if (AMConfiguration.AM_USER_RESET_RESOURCE && StringUtils.isNotBlank(username)) {
      // 获取用户的标签
      val user = if (username.equals("*")) {
        ""
      } else {
        username
      }
      val labelValuePattern =
        MessageFormat.format("%{0}%,%{1}%,%{2}%,%", "", user, "")
      val userLabels = labelManagerPersistence.getLabelByPattern(
        labelValuePattern,
        "combined_userCreator_engineType",
        null,
        null
      )
      // 获取与这些标签关联的资源
      val userLabelResources = resourceManagerPersistence.getResourceByLabels(userLabels).asScala
      // 遍历用户标签资源
      userLabelResources.foreach { userLabelResource =>
        val labelUser = LabelUtil.getFromLabelStr(userLabelResource.getCreator, "user")
        val resourceLabel = labelManagerPersistence.getLabelByResource(userLabelResource)
        resourceLabel.head.setStringValue(userLabelResource.getCreator)
        // lock userCreatorEngineTypeLabel
        val lock = resourceManager.tryLockOneLabel(resourceLabel.head, -1, labelUser)
        Utils.tryFinally {
          val userPersistenceResource = ResourceUtils.fromPersistenceResource(userLabelResource)
          val userResourceType = ResourceType.valueOf(userLabelResource.getResourceType)
          val userEngineNodes = nodeLabelService.getEngineNodesWithResourceByUser(labelUser, true)
          val userEngineNodeFilter = userEngineNodes
            .filter { node =>
              val userCreatorLabelStr =
                LabelUtil.getUserCreatorLabel(node.getLabels).getStringValue
              val engineTypeLabelStr = LabelUtil.getEngineTypeLabel(node.getLabels).getStringValue
              userLabelResource.getCreator.equalsIgnoreCase(
                s"${userCreatorLabelStr},${engineTypeLabelStr}"
              )
            }
            .map(_.getNodeResource)
          // 收集所有node所使用的资源（汇总、已使用、上锁）
          val (sumResource, uedResource, lockResource) =
            collectResource(userEngineNodeFilter, userResourceType)
          if (
              (!(uedResource.equalsTo(userPersistenceResource.getUsedResource)) || (!(lockResource
                .equalsTo(userPersistenceResource.getLockedResource))))
          ) {
            logger.info(
              MessageFormat.format(
                "LabelUser:{0} resources will be reset, Record Resources:{1} ,Real Resources:{2}",
                labelUser,
                userPersistenceResource.getUsedResource
                  .add(userPersistenceResource.getLockedResource),
                sumResource
              )
            )
            userPersistenceResource.setLeftResource(
              userPersistenceResource.getMaxResource.minus(sumResource)
            )
            userPersistenceResource.setUsedResource(uedResource)
            userPersistenceResource.setLockedResource(lockResource)
            resourceManager.resetResource(resourceLabel.head, userPersistenceResource)
          }
        } {
          resourceManager.unLock(lock)
        }
      }
    }
  }

  /**
   * *
   *
   * @param resourceArray
   * @param resourceType
   * @return
   *   汇总资源，已使用资源，上锁资源
   */
  private def collectResource(
      resourceArray: Array[NodeResource],
      resourceType: ResourceType
  ): (Resource, Resource, Resource) = {
    if (resourceArray.isEmpty) {
      return (
        Resource.initResource(resourceType),
        Resource.initResource(resourceType),
        Resource.initResource(resourceType)
      )
    } else {
      resourceArray.foldLeft(
        (
          Resource.initResource(resourceType),
          Resource.initResource(resourceType),
          Resource.initResource(resourceType)
        )
      ) { case ((accSum, accUed, accLock), nodeResource) =>
        if (null == nodeResource.getUsedResource) {
          nodeResource.setUsedResource(Resource.initResource(resourceType))
        }
        if (null == nodeResource.getLockedResource) {
          nodeResource.setLockedResource(Resource.initResource(resourceType))
        }
        (
          accSum.add(nodeResource.getUsedResource.add(nodeResource.getLockedResource)),
          accUed.add(nodeResource.getUsedResource),
          accLock.add(nodeResource.getLockedResource)
        )
      }
    }
  }

}
