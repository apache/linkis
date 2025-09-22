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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{CodeAndRunTypeUtils, Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.label.EngineReuseLabelChooser
import org.apache.linkis.manager.am.selector.NodeSelector
import org.apache.linkis.manager.am.service.ECResourceInfoService
import org.apache.linkis.manager.am.utils.AMUtils
import org.apache.linkis.manager.common.conf.RMConfiguration
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{EngineNode, ScoreServiceInstance}
import org.apache.linkis.manager.common.protocol.engine.{EngineReuseRequest, EngineStopRequest}
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf.{
  PYTHON_VERSION_KEY,
  SPARK_PYTHON_VERSION_KEY
}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.{EngineNodeLabel, Label}
import org.apache.linkis.manager.label.entity.engine.{
  EngineTypeLabel,
  ReuseExclusionLabel,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import org.apache.linkis.manager.label.service.{NodeLabelService, UserLabelService}
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.manager.persistence.NodeManagerPersistence
import org.apache.linkis.manager.service.common.label.LabelFilter
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util
import java.util.concurrent.{TimeoutException, TimeUnit}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

import com.google.common.cache.{Cache, CacheBuilder}

@Service
class DefaultEngineReuseService extends AbstractEngineService with EngineReuseService with Logging {

  @Autowired
  private var nodeSelector: NodeSelector = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var userLabelService: UserLabelService = _

  @Autowired
  private var engineReuseLabelChoosers: util.List[EngineReuseLabelChooser] = _

  @Autowired
  private var engineStopService: EngineStopService = _

  @Autowired
  private var engineCreateService: DefaultEngineCreateService = _

  @Autowired
  private var labelFilter: LabelFilter = _

  @Autowired
  private var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  private var ecResourceInfoService: ECResourceInfoService = _

  private val instanceCache: Cache[String, util.Map[ScoreServiceInstance, util.List[Label[_]]]] =
    CacheBuilder
      .newBuilder()
      .maximumSize(AMConfiguration.ENGINE_REUSE_CACHE_MAX_SIZE.getValue)
      .expireAfterWrite(
        AMConfiguration.ENGINE_REUSE_CACHE_EXPIRE_TIME.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
      .build()

  private val engineNodesCache: Cache[String, Array[EngineNode]] =
    CacheBuilder
      .newBuilder()
      .maximumSize(AMConfiguration.ENGINE_REUSE_CACHE_MAX_SIZE.getValue)
      .expireAfterWrite(
        AMConfiguration.ENGINE_REUSE_CACHE_EXPIRE_TIME.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
      .build()

  /**
   *   1. Obtain the EC corresponding to all labels 2. Judging reuse exclusion tags and fixed engine
   *      labels 3. Select the EC with the lowest load available 4. Lock the corresponding EC
   * @param engineReuseRequest
   * @param sender
   * @throws
   * @return
   */
  @Receiver
  @throws[LinkisRetryException]
  override def reuseEngine(engineReuseRequest: EngineReuseRequest, sender: Sender): EngineNode = {
    val taskId = JobUtils.getJobIdFromStringMap(engineReuseRequest.getProperties)
    logger.info(s"Task $taskId Start to reuse Engine for request: $engineReuseRequest")
    val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
    val labels: util.List[Label[_]] = labelBuilderFactory.getLabels(engineReuseRequest.getLabels)
    val labelList = LabelUtils
      .distinctLabel(labels, userLabelService.getUserLabels(engineReuseRequest.getUser))
      .asScala
    logger.info(s"Task ${taskId} labelList size: ${labelList.size}");
    val exclusionInstances: Array[String] =
      labelList.find(_.isInstanceOf[ReuseExclusionLabel]) match {
        case Some(l) =>
          l.asInstanceOf[ReuseExclusionLabel].getInstances
        case None =>
          Array.empty[String]
      }

    if (
        exclusionInstances.length == 1 && exclusionInstances(
          0
        ) == GovernanceCommonConf.WILDCARD_CONSTANT
    ) {
      logger.info(
        s"Task $taskId exists ReuseExclusionLabel and the configuration does not choose to reuse EC"
      )
      return null
    }

    var filterLabelList = labelList.filter(_.isInstanceOf[EngineNodeLabel]).asJava
    logger.info(s"Task ${taskId} filterLabelList size: ${filterLabelList.size}");

    val engineConnAliasLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    filterLabelList.add(engineConnAliasLabel)

    // label chooser
    if (null != engineReuseLabelChoosers) {
      engineReuseLabelChoosers.asScala.foreach { chooser =>
        filterLabelList = chooser.chooseLabels(filterLabelList)
      }
    }

    val userCreatorLabel: UserCreatorLabel = LabelUtil.getUserCreatorLabel(labels)
    val engineTypeLabel: EngineTypeLabel = LabelUtil.getEngineTypeLabel(labels)
    val cacheKey: String = userCreatorLabel.getStringValue + "_" + engineTypeLabel.getEngineType

    val cacheEnable: Boolean = AMConfiguration.ENGINE_REUSE_CACHE_SUPPORT_ENGINES.getValue.contains(
      engineTypeLabel.getEngineType
    ) && AMConfiguration.ENGINE_REUSE_ENABLE_CACHE.getValue

    val shuffEnable: Boolean = AMConfiguration.ENGINE_REUSE_SHUFF_SUPPORT_ENGINES.getValue.contains(
      engineTypeLabel.getEngineType
    ) && RMConfiguration.LABEL_SERVICE_INSTANCE_SHUFF_SWITCH.getValue

    val instances = if (cacheEnable) {
      var localInstances: util.Map[ScoreServiceInstance, util.List[Label[_]]] =
        instanceCache.getIfPresent(cacheKey)
      if (localInstances == null) this synchronized {
        localInstances = instanceCache.getIfPresent(cacheKey)
        if (localInstances == null) {
          localInstances =
            nodeLabelService.getScoredNodeMapsByLabelsReuse(filterLabelList, shuffEnable)
          instanceCache.put(cacheKey, localInstances)
        }
      }
      localInstances
    } else nodeLabelService.getScoredNodeMapsByLabelsReuse(filterLabelList, shuffEnable)

    if (null != instances && null != exclusionInstances && exclusionInstances.nonEmpty) {
      val instancesKeys = instances.asScala.keys.toArray
      instancesKeys
        .filter { instance =>
          exclusionInstances.exists(_.equalsIgnoreCase(instance.getServiceInstance.getInstance))
        }
        .foreach { instance =>
          logger.info(
            s"will  be not reuse ${instance.getServiceInstance}, cause use exclusion label"
          )
          instances.remove(instance)
        }
    }
    if (null == instances || instances.isEmpty) {
      throw new LinkisRetryException(
        AMConstant.ENGINE_ERROR_CODE,
        s"No engine can be reused, cause from db is null"
      )
    }

    var engineScoreList = if (cacheEnable) {
      var localEngineList: Array[EngineNode] = engineNodesCache.getIfPresent(cacheKey)
      if (localEngineList == null) this synchronized {
        localEngineList = engineNodesCache.getIfPresent(cacheKey)
        if (localEngineList == null) {
          localEngineList =
            getEngineNodeManager.getEngineNodes(instances.asScala.keys.toSeq.toArray)
          engineNodesCache.put(cacheKey, localEngineList)
        }
      }
      localEngineList
    } else getEngineNodeManager.getEngineNodes(instances.asScala.keys.toSeq.toArray)
    logger.info(s"Task ${taskId} engineScoreList size: ${engineScoreList.length}")

    // reuse EC according to template name
    val confTemplateNameKey = "ec.resource.name"
    val templateName: String =
      getValueByKeyFromProps(confTemplateNameKey, engineReuseRequest.getProperties)
    if (
        StringUtils.isNotBlank(templateName) && AMConfiguration.EC_REUSE_WITH_TEMPLATE_RULE_ENABLE
    ) {
      engineScoreList = engineScoreList
        .filter(engine => engine.getNodeStatus == NodeStatus.Unlock)
        .filter(engine => {
          val oldTemplateName: String =
            getValueByKeyFromProps(confTemplateNameKey, parseParamsToMap(engine.getParams))
          templateName.equalsIgnoreCase(oldTemplateName)
        })
      logger.info(s"${engineScoreList.length} engine by templateName can be reused.")
    }

    // 获取需要的资源
    if (AMConfiguration.EC_REUSE_WITH_RESOURCE_RULE_ENABLE) {
      logger.info(
        s"Task ${taskId} start to filter resources, the engine size: ${engineScoreList.length}"
      );
      val labels: util.List[Label[_]] =
        engineCreateService.buildLabel(engineReuseRequest.getLabels, engineReuseRequest.getUser)
      if (engineReuseRequest.getProperties == null) {
        engineReuseRequest.setProperties(new util.HashMap[String, String]())
      }

      val engineType: String = LabelUtil.getEngineType(labels)
      if (
          StringUtils.isNotBlank(engineType) && AMConfiguration.EC_REUSE_WITH_RESOURCE_WITH_ECS
            .contains(engineType.toLowerCase())
      ) {
        val resource = engineCreateService.generateResource(
          engineReuseRequest.getProperties,
          engineReuseRequest.getUser,
          labelFilter.choseEngineLabel(labels),
          AMConfiguration.ENGINE_START_MAX_TIME.getValue.toLong
        )
        val pythonVersion: String = getPythonVersion(engineReuseRequest.getProperties)

        // 只对python相关的引擎做python版本匹配
        val codeType = LabelUtil.getCodeType(labels)
        val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
        val pythonFlag: Boolean = languageType == CodeAndRunTypeUtils.LANGUAGE_TYPE_PYTHON

        // 过滤掉资源不满足的引擎
        engineScoreList = engineScoreList
          .filter(engine => engine.getNodeStatus == NodeStatus.Unlock)
          .filter(engine => {
            val enginePythonVersion: String = getPythonVersion(parseParamsToMap(engine.getParams))
            var pythonVersionMatch: Boolean = true
            if (
                StringUtils.isNotBlank(pythonVersion) && StringUtils
                  .isNotBlank(enginePythonVersion) && pythonFlag
            ) {
              pythonVersionMatch = pythonVersion.equalsIgnoreCase(enginePythonVersion)
            }
            if (!pythonVersionMatch) {
              logger.info(
                s"will be not reuse ${engine.getServiceInstance}, cause engine python version: $enginePythonVersion , param python version $pythonVersion is not match"
              )
            }
            if (engine.getNodeResource.getUsedResource != null) {
              // 引擎资源只有满足需要的资源才复用
              pythonVersionMatch && engine.getNodeResource.getUsedResource
                .notLess(resource.getMaxResource)
            } else {
              // 引擎正在启动中，比较锁住的资源，最终是否复用沿用之前复用逻辑
              pythonVersionMatch && engine.getNodeResource.getLockedResource
                .notLess(resource.getMaxResource)
            }
          })
      }

      logger.info(
        s"Task ${taskId} end filter resources, the engine size: ${engineScoreList.length}"
      );
      if (engineScoreList.isEmpty) {
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"No engine can be reused, cause all engine resources are not sufficient."
        )
      }
    }

    var engine: EngineNode = null
    var count = 1
    val timeout =
      if (engineReuseRequest.getTimeOut <= 0) {
        AMConfiguration.ENGINE_REUSE_MAX_TIME.getValue.toLong
      } else engineReuseRequest.getTimeOut
    val reuseLimit: Int =
      if (engineReuseRequest.getReuseCount <= 0) AMConfiguration.ENGINE_REUSE_COUNT_LIMIT
      else engineReuseRequest.getReuseCount

    def selectEngineToReuse: Boolean = {
      if (count > reuseLimit) {
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Engine reuse exceeds limit: $reuseLimit"
        )
      }
      val choseNode = nodeSelector.choseNode(engineScoreList.toArray)
      if (choseNode.isEmpty) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, "No engine can be reused")
      }
      val engineNode = choseNode.get.asInstanceOf[EngineNode]
      logger.info(s"prepare to reuse engineNode: ${engineNode.getServiceInstance}")
      engine = Utils.tryCatch(getEngineNodeManager.reuseEngine(engineNode)) { t: Throwable =>
        logger.info(s"Failed to reuse engine ${engineNode.getServiceInstance}", t)
        if (ExceptionUtils.getRootCause(t).isInstanceOf[TimeoutException]) {
          logger.info(s"Failed to reuse ${engineNode.getServiceInstance}, now to stop this")
          val stopEngineRequest =
            new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
          engineStopService.asyncStopEngine(stopEngineRequest)
        }
        null
      }
      if (null == engine) {
        count = count + 1
        engineScoreList = engineScoreList.filter(!_.equals(choseNode.get))
      }
      null != engine
    }

    val startTime = System.currentTimeMillis()
    try {
      Utils.waitUntil(() => selectEngineToReuse, Duration(timeout, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException =>
        throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          s"Waiting for Engine initialization failure, already waiting $timeout ms"
        )
      case t: Throwable =>
        logger.info(
          s"Failed to reuse engineConn time taken ${System.currentTimeMillis() - startTime}"
        )
        throw t
    }
    logger.info(
      s"Finished to reuse Engine for request: $engineReuseRequest get EngineNode $engine, time taken ${System
        .currentTimeMillis() - startTime}"
    )
    val engineServiceLabelList =
      instances.asScala.filter(kv => kv._1.getServiceInstance.equals(engine.getServiceInstance))
    if (null != engineServiceLabelList && engineServiceLabelList.nonEmpty) {
      engine.setLabels(engineServiceLabelList.head._2)
    } else {
      logger.info(
        "Get choosen engineNode : " + AMUtils.GSON
          .toJson(engine) + " from engineLabelMap : " + AMUtils.GSON.toJson(instances)
      )
    }
    if (Configuration.METRICS_INCREMENTAL_UPDATE_ENABLE.getValue) {
      val engineNode =
        ecResourceInfoService.getECResourceInfoRecordByInstance(
          engine.getServiceInstance.getInstance
        )
      // 异步更新 metrics
      AMUtils.updateMetricsAsync(
        taskId,
        engineNode.getTicketId,
        engineNode.getServiceInstance,
        engineNode.getEcmInstance,
        engineNode.getLogDirSuffix,
        isReuse = true
      )
    }
    engine
  }

  private def parseParamsToMap(params: String) = {
    if (StringUtils.isNotBlank(params)) {
      AMUtils.GSON.fromJson(params, classOf[util.Map[String, String]])
    } else {
      null
    }
  }

  private def getValueByKeyFromProps(key: String, paramsMap: util.Map[String, String]) = {
    if (paramsMap != null) {
      paramsMap.getOrDefault(key, "")
    } else {
      ""
    }
  }

  private def getPythonVersion(prop: util.Map[String, String]): String = {
    var pythonVersion: String = getValueByKeyFromProps(PYTHON_VERSION_KEY, prop)
    if (StringUtils.isBlank(pythonVersion)) {
      pythonVersion = getValueByKeyFromProps(SPARK_PYTHON_VERSION_KEY, prop)
    }
    pythonVersion
  }

}
