/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.am.service.engine

import org.apache.commons.lang.exception.ExceptionUtils

import java.util
import java.util.concurrent.{TimeUnit, TimeoutException}
import org.apache.linkis.common.exception.LinkisRetryException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.label.EngineReuseLabelChooser
import org.apache.linkis.manager.am.selector.NodeSelector
import org.apache.linkis.manager.am.utils.AMUtils
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.engine.{EngineReuseRequest, EngineStopRequest}
import org.apache.linkis.manager.common.utils.ManagerUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.{EngineNodeLabel, Label}
import org.apache.linkis.manager.label.entity.engine.ReuseExclusionLabel
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import org.apache.linkis.manager.label.service.{NodeLabelService, UserLabelService}
import org.apache.linkis.manager.label.utils.LabelUtils
import org.apache.linkis.message.annotation.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

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

  @Receiver
  @throws[LinkisRetryException]
  override def reuseEngine(engineReuseRequest: EngineReuseRequest): EngineNode = {
    info(s"Start to reuse Engine for request: $engineReuseRequest")
    //TODO Label Factory And builder
    val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
    //Label:传入的Label和用户默认的Label 去重
    var labelList: util.List[Label[_]] = LabelUtils.distinctLabel(labelBuilderFactory.getLabels(engineReuseRequest.getLabels),
      userLabelService.getUserLabels(engineReuseRequest.getUser))

    val exclusionInstances: Array[String] = labelList.find(_.isInstanceOf[ReuseExclusionLabel]) match {
      case Some(l) =>
        l.asInstanceOf[ReuseExclusionLabel].getInstances
      case None =>
        Array.empty[String]
    }

    labelList = labelList.filter(_.isInstanceOf[EngineNodeLabel])

    val engineConnAliasLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    labelList.add(engineConnAliasLabel)

    //label chooser
    if (null != engineReuseLabelChoosers) {
      engineReuseLabelChoosers.foreach { chooser =>
        labelList = chooser.chooseLabels(labelList)
      }
    }

    val instances = nodeLabelService.getScoredNodeMapsByLabels(labelList)

    if (null != instances && null != exclusionInstances && exclusionInstances.nonEmpty) {
      val instancesKeys = instances.keys.toArray
      instancesKeys.filter{ instance =>
        exclusionInstances.exists(_.equalsIgnoreCase(instance.getServiceInstance.getInstance))
      }.foreach{ instance =>
        logger.info(s"will  be not reuse ${instance.getServiceInstance}, cause use exclusion label")
        instances.remove(instance)
     }
    }
    if (null == instances || instances.isEmpty) {
      throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"No engine can be reused, cause from db is null")
    }
    var engineScoreList = getEngineNodeManager.getEngineNodes(instances.map(_._1).toSeq.toArray)

    var engine: EngineNode = null
    var count = 1
    val timeout = if (engineReuseRequest.getTimeOut <= 0) AMConfiguration.ENGINE_REUSE_MAX_TIME.getValue.toLong else engineReuseRequest.getTimeOut
    val reuseLimit = if (engineReuseRequest.getReuseCount <= 0) AMConfiguration.ENGINE_REUSE_COUNT_LIMIT.getValue else engineReuseRequest.getReuseCount

    def selectEngineToReuse: Boolean = {
      if (count > reuseLimit) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Engine reuse exceeds limit: $reuseLimit")
      }
      //3. 执行Select 判断label分数、判断是否可用、判断负载
      val choseNode = nodeSelector.choseNode(engineScoreList.toArray)
      //4. 获取Select后排在第一的engine，修改EngineNode的Label为新标签，并调用EngineNodeManager的reuse请求
      if (choseNode.isEmpty) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, "No engine can be reused")
      }

      //5. 调用EngineNodeManager 进行reuse 如果reuse失败，则去掉该engine进行重新reuse走3和4
      val engineNode = choseNode.get.asInstanceOf[EngineNode]
      logger.info(s"prepare to reuse engineNode: ${engineNode.getServiceInstance}")
      engine = Utils.tryCatch(getEngineNodeManager.reuseEngine(engineNode)) { t: Throwable =>
          error(s"Failed to reuse engine ${engineNode.getServiceInstance}", t)
          if (ExceptionUtils.getRootCause(t).isInstanceOf[TimeoutException]) {
            error(s"Failed to reuse ${engineNode.getServiceInstance}, now to stop this")
            val stopEngineRequest = new EngineStopRequest(engineNode.getServiceInstance, ManagerUtils.getAdminUser)
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
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Waiting for Engine initialization failure, already waiting $timeout ms")
      case t: Throwable =>
        info(s"Failed to reuse engineConn time taken ${System.currentTimeMillis() - startTime}")
        throw t
    }
    info(s"Finished to reuse Engine for request: $engineReuseRequest get EngineNode $engine, time taken ${System.currentTimeMillis() - startTime}")
    val engineServiceLabelList = instances.filter(kv => kv._1.getServiceInstance.equals(engine.getServiceInstance))
    if (null != engineServiceLabelList && engineServiceLabelList.nonEmpty) {
      engine.setLabels(engineServiceLabelList.head._2)
    } else {
      error("Get choosen engineNode : " + AMUtils.GSON.toJson(engine) + " from engineLabelMap : " + AMUtils.GSON.toJson(instances))
    }
    engine
  }




}
